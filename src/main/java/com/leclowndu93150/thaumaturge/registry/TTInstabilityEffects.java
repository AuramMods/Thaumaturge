package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.infusion.instability.EjectEffect;
import com.leclowndu93150.thaumaturge.content.infusion.instability.ExplodeEffect;
import com.leclowndu93150.thaumaturge.content.infusion.instability.HarmEffect;
import com.leclowndu93150.thaumaturge.content.infusion.instability.InstabilityEffectType;
import com.leclowndu93150.thaumaturge.content.infusion.instability.WarpEffect;
import com.leclowndu93150.thaumaturge.content.infusion.instability.ZapEffect;
import net.minecraft.core.Registry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public final class TTInstabilityEffects {
    public static final DeferredRegister<InstabilityEffectType<?>> EFFECTS =
            DeferredRegister.create(InstabilityEffectType.REGISTRY_KEY, TTIds.MODID);
    private static final java.util.function.Supplier<net.minecraftforge.registries.IForgeRegistry<InstabilityEffectType<?>>> REGISTRY =
            EFFECTS.makeRegistry(() -> new net.minecraftforge.registries.RegistryBuilder<InstabilityEffectType<?>>().hasTags().disableSync());

    public static final RegistryObject<InstabilityEffectType<EjectEffect>> EJECT =
            EFFECTS.register("eject", () -> new InstabilityEffectType<>(EjectEffect.CODEC));
    public static final RegistryObject<InstabilityEffectType<ZapEffect>> ZAP =
            EFFECTS.register("zap", () -> new InstabilityEffectType<>(ZapEffect.CODEC));
    public static final RegistryObject<InstabilityEffectType<HarmEffect>> HARM =
            EFFECTS.register("harm", () -> new InstabilityEffectType<>(HarmEffect.CODEC));
    public static final RegistryObject<InstabilityEffectType<WarpEffect>> WARP =
            EFFECTS.register("warp", () -> new InstabilityEffectType<>(WarpEffect.CODEC));
    public static final RegistryObject<InstabilityEffectType<ExplodeEffect>> EXPLODE =
            EFFECTS.register("explode", () -> new InstabilityEffectType<>(ExplodeEffect.CODEC));

    private TTInstabilityEffects() {}

    public static Registry<InstabilityEffectType<?>> registry() {
        return ForgeRegistration.vanillaRegistry(REGISTRY);
    }

    public static void register(IEventBus modBus) {
        EFFECTS.register(modBus);
    }
}
