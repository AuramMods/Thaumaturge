package com.leclowndu93150.thaumaturge.registry;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ForgeRegistries;
import org.junit.jupiter.api.Test;

class FluidRegistrationRuntimeTest {
    @Test
    void nativeRegistryKeepsTheThreeFluidTypesAndTheirPhysics() {
        var registry = ForgeRegistries.FLUID_TYPES.get();
        var flux = TTFluidTypes.FLUX_GOO.get();
        var purifying = TTFluidTypes.PURIFYING.get();
        var death = TTFluidTypes.LIQUID_DEATH.get();
        assertSame(flux, registry.getValue(TTIds.rl("flux_goo")));
        assertSame(purifying, registry.getValue(TTIds.rl("purifying")));
        assertSame(death, registry.getValue(TTIds.rl("liquid_death")));
        assertEquals(6000, flux.getViscosity());
        assertEquals(8, flux.getDensity());
        assertEquals(0.0001D, flux.motionScale(mock(Entity.class)));
        assertSame(TTSounds.GORE.get(), flux.getSound(SoundActions.BUCKET_FILL));
        assertSame(TTSounds.GORE.get(), flux.getSound(SoundActions.BUCKET_EMPTY));
        assertEquals(5, purifying.getLightLevel());
    }

    @Test
    void nativeClientHookInstallsTheExistingTexturesAndTintsOnlyOnTheClient() {
        var flux = TTFluidTypes.FLUX_GOO.get();
        var purifying = TTFluidTypes.PURIFYING.get();
        var death = TTFluidTypes.LIQUID_DEATH.get();
        if (FMLEnvironment.dist.isDedicatedServer()) {
            assertNull(flux.getRenderPropertiesInternal());
            assertNull(purifying.getRenderPropertiesInternal());
            assertNull(death.getRenderPropertiesInternal());
            return;
        }
        var fluxRender = IClientFluidTypeExtensions.of(flux);
        assertEquals(TTIds.rl("block/flux_goo"), fluxRender.getStillTexture());
        assertEquals(fluxRender.getStillTexture(), fluxRender.getFlowingTexture());
        var purifyingRender = IClientFluidTypeExtensions.of(purifying);
        assertEquals(ResourceLocation.withDefaultNamespace("block/water_still"), purifyingRender.getStillTexture());
        assertEquals(ResourceLocation.withDefaultNamespace("block/water_flow"), purifyingRender.getFlowingTexture());
        assertEquals(ResourceLocation.withDefaultNamespace("block/water_overlay"), purifyingRender.getOverlayTexture());
        assertEquals(0x77FFEEAA, purifyingRender.getTintColor());
        var deathRender = IClientFluidTypeExtensions.of(death);
        assertEquals(TTIds.rl("block/animatedglow"), deathRender.getStillTexture());
        assertEquals(deathRender.getStillTexture(), deathRender.getFlowingTexture());
        assertEquals(0xFF4D0066, deathRender.getTintColor());
    }
}
