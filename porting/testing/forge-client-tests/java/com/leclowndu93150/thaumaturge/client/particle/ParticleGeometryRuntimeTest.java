package com.leclowndu93150.thaumaturge.client.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.content.particle.BoreDebrisParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.FluxGooDropletParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.InfusionCrumbsParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.SlashParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TTParticles;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.nio.ByteOrder;
import java.util.Map;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.eventbus.api.BusBuilder;
import org.joml.Quaternionf;
import org.junit.jupiter.api.Test;

class ParticleGeometryRuntimeTest {
    @Test
    void forgeRegisterEventInstallsAProviderForEveryRegisteredParticle() throws Exception {
        try (var render = mockStatic(RenderSystem.class)) {
            render.when(RenderSystem::maxSupportedTextureSize).thenReturn(4096);
            var engine = new ParticleEngine(null, mock(TextureManager.class));
            var bus = BusBuilder.builder().build();
            bus.addListener(TTParticleProviders::onRegister);
            bus.post(new RegisterParticleProvidersEvent(engine));
            var field = ParticleEngine.class.getDeclaredField("providers");
            field.setAccessible(true);
            var providers = (Map<?, ?>) field.get(engine);
            for (var entry : TTParticles.PARTICLES.getEntries()) {
                assertTrue(providers.containsKey(entry.getId()), entry.getId().toString());
            }
        }
    }

    @Test
    void orientedSlashWritesTheSourceCornerOrderUvsAndColorToTheNativeBuffer() throws Exception {
        var sheet = mock(ParticleSheet.class);
        when(sheet.frames()).thenReturn(4);
        when(sheet.u0(0)).thenReturn(0.25F);
        when(sheet.u1(0)).thenReturn(0.5F);
        var constructor = SlashParticle.class.getDeclaredConstructor(ClientLevel.class, double.class, double.class,
                double.class, double.class, double.class, double.class, SlashParticleOptions.class, ParticleSheet.class);
        constructor.setAccessible(true);
        double worldX = 16_777_216.25;
        var slash = constructor.newInstance(mock(ClientLevel.class), worldX, 7.0, -3.0, 0.0, 0.0, 0.0,
                new SlashParticleOptions(20, 90.0F, 0.0F, 0.0F), sheet);
        slash.setColor(0.25F, 0.5F, 1.0F);
        var camera = mock(Camera.class);
        when(camera.getPosition()).thenReturn(new Vec3(16_777_216.0, 7.0, -3.0));
        when(camera.rotation()).thenReturn(new Quaternionf().rotationX(0.7F));
        var format = DefaultVertexFormat.PARTICLE;
        var builder = new BufferBuilder(64);
        builder.begin(VertexFormat.Mode.QUADS, format);
        slash.render(builder, camera, 0.5F);
        var rendered = builder.end();
        try {
            assertEquals(4, rendered.drawState().vertexCount());
            var bytes = rendered.vertexBuffer().order(ByteOrder.nativeOrder());
            float[][] expected = {
                {0.25F, -0.05F, 0.05F, 0.5F, 1.0F},
                {0.25F, 0.05F, 0.05F, 0.5F, 0.0F},
                {0.25F, 0.05F, -0.05F, 0.25F, 0.0F},
                {0.25F, -0.05F, -0.05F, 0.25F, 1.0F}
            };
            for (int i = 0; i < expected.length; i++) {
                int base = i * format.getVertexSize();
                for (int coordinate = 0; coordinate < 5; coordinate++) {
                    assertEquals(expected[i][coordinate], bytes.getFloat(base + 4 * coordinate), 1.0E-6F);
                }
                int color = base + format.getOffset(2);
                assertEquals(63, Byte.toUnsignedInt(bytes.get(color)));
                assertEquals(127, Byte.toUnsignedInt(bytes.get(color + 1)));
                assertEquals(255, Byte.toUnsignedInt(bytes.get(color + 2)));
                assertEquals(255, Byte.toUnsignedInt(bytes.get(color + 3)));
            }
        } finally {
            rendered.release();
        }
    }

    @Test
    void boreAndInfusionDebrisEachSampleAQuarterOfTheAtlasSprite() throws Exception {
        var level = mock(ClientLevel.class);
        var sprite = mock(TextureAtlasSprite.class);
        when(sprite.getU(anyDouble())).thenAnswer(call -> call.getArgument(0, Double.class).floatValue() / 16.0F);
        when(sprite.getV(anyDouble())).thenAnswer(call -> call.getArgument(0, Double.class).floatValue() / 16.0F);
        var bore = BoreDebrisParticle.class.getDeclaredConstructor(ClientLevel.class, double.class, double.class,
                double.class, BoreDebrisParticleOptions.class, TextureAtlasSprite.class);
        var crumbs = InfusionCrumbsParticle.class.getDeclaredConstructor(ClientLevel.class, double.class, double.class,
                double.class, InfusionCrumbsParticleOptions.class, TextureAtlasSprite.class);
        bore.setAccessible(true);
        crumbs.setAccessible(true);
        var boreEffect = bore.newInstance(level, 0.0, 0.0, 0.0,
                new BoreDebrisParticleOptions(Blocks.STONE.defaultBlockState(), 2, 0, 0, 0, 0, 0), sprite);
        var infusionEffect = crumbs.newInstance(level, 0.0, 0.0, 0.0,
                new InfusionCrumbsParticleOptions(new ItemStack(Items.GOLD_INGOT), 2, 0, 0, 0, 0, 0), sprite);
        for (var effect : new TTParticle[] {boreEffect, infusionEffect}) {
            assertEquals(0.25F, effect.getU0() - effect.getU1(), 1.0E-6F);
            assertEquals(0.25F, effect.getV1() - effect.getV0(), 1.0E-6F);
            assertTrue(effect.getU1() >= 0.0F && effect.getU0() <= 1.0F);
            assertTrue(effect.getV0() >= 0.0F && effect.getV1() <= 1.0F);
        }
    }

    @Test
    void fluxGooKeepsNativeItemSpriteAndSourceLaunchVelocity() throws Exception {
        var level = mock(ClientLevel.class);
        when(level.getRandom()).thenReturn(RandomSource.create(123L));
        var client = mock(Minecraft.class);
        var renderer = mock(ItemRenderer.class);
        var model = mock(BakedModel.class);
        when(client.getItemRenderer()).thenReturn(renderer);
        when(renderer.getModel(any(), same(level), isNull(), eq(0))).thenReturn(model);
        when(model.getOverrides()).thenReturn(ItemOverrides.EMPTY);
        when(model.getParticleIcon(ModelData.EMPTY)).thenReturn(mock(TextureAtlasSprite.class));
        try (var minecraft = mockStatic(Minecraft.class)) {
            minecraft.when(Minecraft::getInstance).thenReturn(client);
            Particle particle = new FluxGooDropletParticle.Provider().createParticle(
                    new FluxGooDropletParticleOptions(0xFF66AA33, 0.6F, 96),
                    level, 0.0, 0.0, 0.0, 0.4, -0.3, 0.6);
            assertSame(ParticleRenderType.TERRAIN_SHEET, particle.getRenderType());
            assertEquals(96, particle.getLifetime());
            assertEquals(0.4, velocity(particle, "xd"), 0.05);
            assertEquals(-0.3, velocity(particle, "yd"), 0.05);
            assertEquals(0.6, velocity(particle, "zd"), 0.05);
        }
    }

    private static double velocity(Particle particle, String name) throws ReflectiveOperationException {
        var field = Particle.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.getDouble(particle);
    }
}
