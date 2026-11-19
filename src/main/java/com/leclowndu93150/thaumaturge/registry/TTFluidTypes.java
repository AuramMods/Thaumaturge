package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.alchemy.LiquidDeathFluidType;
import com.leclowndu93150.thaumaturge.content.spa.PurifyingFluidType;
import com.leclowndu93150.thaumaturge.content.taint.flux.FluxGooFluidType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class TTFluidTypes {
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, TTIds.MODID);

    public static final RegistryObject<FluxGooFluidType> FLUX_GOO = FLUID_TYPES.register(
            "flux_goo",
            () -> new FluxGooFluidType(FluidType.Properties.create()
                    .descriptionId("fluid_type.thaumaturge.flux_goo")
                    .viscosity(6000)
                    .density(8)
                    .canSwim(false)
                    .canDrown(false)
                    .canPushEntity(false)
                    .canExtinguish(false)
                    .canConvertToSource(false)
                    .sound(SoundActions.BUCKET_FILL, TTSounds.GORE.get())
                    .sound(SoundActions.BUCKET_EMPTY, TTSounds.GORE.get())
                    .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH)
                    .rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<PurifyingFluidType> PURIFYING = FLUID_TYPES.register(
            "purifying",
            () -> new PurifyingFluidType(FluidType.Properties.create()
                    .descriptionId("fluid_type.thaumaturge.purifying")
                    .lightLevel(5)
                    .canSwim(true)
                    .fallDistanceModifier(0.0F)
                    .canConvertToSource(false)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                    .rarity(Rarity.RARE)));

    public static final RegistryObject<LiquidDeathFluidType> LIQUID_DEATH = FLUID_TYPES.register(
            "liquid_death",
            () -> new LiquidDeathFluidType(FluidType.Properties.create()
                    .descriptionId("fluid_type.thaumaturge.liquid_death")
                    .canConvertToSource(false)
                    .canDrown(true)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                    .rarity(Rarity.RARE)));

    private TTFluidTypes() {}

    public static void register(IEventBus modBus) {
        FLUID_TYPES.register(modBus);
    }
}
