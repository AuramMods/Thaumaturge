package com.leclowndu93150.thaumaturge.client.render;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.registry.TTAttributes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.netty.buffer.Unpooled;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

class LivingEntityScaleClientRuntimeTest {
    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/zombie/zombie.png");

    @Test
    void theNativeAttributePacketSetsTheScaleUsedForTheModelAndLayers() throws Exception {
        var minecraft = mock(Minecraft.class);
        when(minecraft.isSameThread()).thenReturn(true);
        var level = mock(ClientLevel.class);
        var target = mock(LivingEntity.class);
        when(target.getPose()).thenReturn(Pose.STANDING);
        when(target.getAttributes()).thenReturn(scaleAttributes());
        when(level.getEntity(42)).thenReturn(target);
        var listener = mock(ClientPacketListener.class, CALLS_REAL_METHODS);
        for (var entry : Map.of("minecraft", minecraft, "level", level).entrySet()) {
            var field = ClientPacketListener.class.getDeclaredField(entry.getKey());
            field.setAccessible(true);
            field.set(listener, entry.getValue());
        }

        var source = scaleAttributes().getInstance(TTAttributes.SCALE.get());
        var renderer = new ProbeRenderer();
        var pose = new PoseStack();
        pose.translate(7.0, 8.0, 9.0);
        MultiBufferSource buffers = type -> mock(VertexConsumer.class);
        try (var instance = mockStatic(Minecraft.class)) {
            instance.when(Minecraft::getInstance).thenReturn(minecraft);
            for (float scale : new float[] {2.5F, 0.4F, 1.0F}) {
                source.setBaseValue(scale);
                var buffer = new FriendlyByteBuf(Unpooled.buffer());
                try {
                    new ClientboundUpdateAttributesPacket(42, List.of(source)).write(buffer);
                    listener.handleUpdateAttributes(new ClientboundUpdateAttributesPacket(buffer));
                    assertFalse(buffer.isReadable());
                } finally {
                    buffer.release();
                }

                renderer.render(target, 0.0F, 0.0F, pose, buffers, 0);
                assertEquals(scale, renderer.getModel().drawnScale, 1.0e-6F);
                assertEquals(scale, renderer.layerScale, 1.0e-6F);
                assertEquals(1.0F, renderedScale(pose));
                assertEquals(7.0F, pose.last().pose().m30());
            }
        }
    }

    @Test
    void theNativeDispatcherSamplesAShadowThatFollowsTheScale() {
        var options = mock(Options.class);
        when(options.entityShadows()).thenReturn(OptionInstance.createBoolean("entityShadows", true));
        var dispatcher = new EntityRenderDispatcher(mock(Minecraft.class), null, null, null, null, options, null);
        dispatcher.renderers = Map.of(EntityType.ZOMBIE, new ProbeRenderer() {
            @Override
            public void render(LivingEntity entity, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {}
        });
        var camera = mock(Camera.class);
        when(camera.getPosition()).thenReturn(Vec3.ZERO);
        var world = mock(Level.class);
        dispatcher.prepare(world, camera, null);
        var sampled = new HashSet<BlockPos>();
        var chunk = mock(ChunkAccess.class);
        when(chunk.getBlockState(any(BlockPos.class))).thenReturn(Blocks.AIR.defaultBlockState());
        when(world.getChunk(any(BlockPos.class))).thenAnswer(call -> {
            BlockPos column = call.getArgument(0);
            sampled.add(column.immutable());
            return chunk;
        });

        var zombie = mock(LivingEntity.class);
        doReturn(EntityType.ZOMBIE).when(zombie).getType();
        var attributes = scaleAttributes();
        when(zombie.getAttributes()).thenReturn(attributes);
        var scale = attributes.getInstance(TTAttributes.SCALE.get());
        var pose = new PoseStack();
        MultiBufferSource buffers = type -> mock(VertexConsumer.class);

        dispatcher.render(zombie, 0.0, 0.0, 0.0, 0.0F, 0.0F, pose, buffers, 0);
        assertEquals(Set.of(-1, 0), shadowColumns(sampled));
        sampled.clear();

        scale.setBaseValue(3.0);
        dispatcher.render(zombie, 0.0, 0.0, 0.0, 0.0F, 0.0F, pose, buffers, 0);
        assertEquals(Set.of(-2, -1, 0, 1), shadowColumns(sampled));
    }

    private static Set<Integer> shadowColumns(Set<BlockPos> positions) {
        var xs = new HashSet<Integer>();
        positions.forEach(pos -> xs.add(pos.getX()));
        return xs;
    }

    private static AttributeMap scaleAttributes() {
        return new AttributeMap(new AttributeSupplier.Builder().add(TTAttributes.SCALE.get()).build());
    }

    private static float renderedScale(PoseStack pose) {
        return pose.last().pose().getScale(new Vector3f()).x;
    }

    private static class ProbeRenderer extends LivingEntityRenderer<LivingEntity, ProbeModel> {
        private float layerScale;

        ProbeRenderer() {
            super(mock(EntityRendererProvider.Context.class), new ProbeModel(), 0.5F);
            addLayer(new RenderLayer<>(this) {
                @Override
                public void render(PoseStack pose, MultiBufferSource buffers, int light, LivingEntity entity,
                        float limb, float swing, float partialTick, float age, float yaw, float pitch) {
                    layerScale = renderedScale(pose);
                }
            });
        }

        @Override public ResourceLocation getTextureLocation(LivingEntity entity) { return TEXTURE; }
        @Override protected boolean shouldShowName(LivingEntity entity) { return false; }
        @Override protected RenderType getRenderType(LivingEntity entity, boolean visible, boolean translucent, boolean glowing) {
            return RenderType.entityCutout(TEXTURE);
        }
    }

    private static final class ProbeModel extends EntityModel<LivingEntity> {
        private float drawnScale;

        @Override public void setupAnim(LivingEntity entity, float limb, float swing, float age, float yaw, float pitch) {}

        @Override
        public void renderToBuffer(PoseStack pose, VertexConsumer vertices, int light, int overlay,
                float red, float green, float blue, float alpha) {
            drawnScale = renderedScale(pose);
        }
    }
}
