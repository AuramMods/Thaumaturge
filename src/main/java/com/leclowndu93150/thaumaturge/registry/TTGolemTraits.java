package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.golems.GolemTrait;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public final class TTGolemTraits {
    public static final DeferredRegister<GolemTrait> TRAITS =
            DeferredRegister.create(GolemTrait.REGISTRY_KEY, TTIds.MODID);

    private static final java.util.function.Supplier<net.minecraftforge.registries.IForgeRegistry<GolemTrait>> REGISTRY =
            TRAITS.makeRegistry(() -> new net.minecraftforge.registries.RegistryBuilder<GolemTrait>().hasTags());

    private static final ResourceKey<GolemTrait> DEFT_KEY = key("deft");
    private static final ResourceKey<GolemTrait> CLUMSY_KEY = key("clumsy");
    private static final ResourceKey<GolemTrait> HEAVY_KEY = key("heavy");
    private static final ResourceKey<GolemTrait> LIGHT_KEY = key("light");
    private static final ResourceKey<GolemTrait> FRAGILE_KEY = key("fragile");
    private static final ResourceKey<GolemTrait> ARMORED_KEY = key("armored");

    public static final RegistryObject<GolemTrait> SMART = simple("smart");
    public static final RegistryObject<GolemTrait> DEFT = opposed("deft", CLUMSY_KEY);
    public static final RegistryObject<GolemTrait> CLUMSY = opposed("clumsy", DEFT_KEY);
    public static final RegistryObject<GolemTrait> FIGHTER = simple("fighter");
    public static final RegistryObject<GolemTrait> WHEELED = simple("wheeled");
    public static final RegistryObject<GolemTrait> FLYER = simple("flyer");
    public static final RegistryObject<GolemTrait> CLIMBER = simple("climber");
    public static final RegistryObject<GolemTrait> HEAVY = opposed("heavy", LIGHT_KEY);
    public static final RegistryObject<GolemTrait> LIGHT = opposed("light", HEAVY_KEY);
    public static final RegistryObject<GolemTrait> FRAGILE = opposed("fragile", ARMORED_KEY);
    public static final RegistryObject<GolemTrait> REPAIR = simple("repair");
    public static final RegistryObject<GolemTrait> SCOUT = simple("scout");
    public static final RegistryObject<GolemTrait> ARMORED = opposed("armored", FRAGILE_KEY);
    public static final RegistryObject<GolemTrait> BRUTAL = simple("brutal");
    public static final RegistryObject<GolemTrait> FIREPROOF = simple("fireproof");
    public static final RegistryObject<GolemTrait> BREAKER = simple("breaker");
    public static final RegistryObject<GolemTrait> HAULER = simple("hauler");
    public static final RegistryObject<GolemTrait> RANGED = simple("ranged");
    public static final RegistryObject<GolemTrait> BLASTPROOF = simple("blastproof");

    private TTGolemTraits() {}

    private static ResourceKey<GolemTrait> key(String name) {
        return ResourceKey.create(GolemTrait.REGISTRY_KEY, TTIds.rl(name));
    }

    private static RegistryObject<GolemTrait> simple(String name) {
        return ForgeRegistration.registerById(TRAITS, name, id -> GolemTrait.ofId(id, null));
    }

    private static RegistryObject<GolemTrait> opposed(String name, ResourceKey<GolemTrait> opposite) {
        return ForgeRegistration.registerById(TRAITS, name, id -> GolemTrait.ofId(id, opposite));
    }

    public static Registry<GolemTrait> registry() {
        return ForgeRegistration.vanillaRegistry(REGISTRY);
    }

    static Holder.Reference<GolemTrait> traitHolder(RegistryObject<GolemTrait> trait) {
        var registry = registry();
        var key = ResourceKey.create(GolemTrait.REGISTRY_KEY, trait.getId());
        // Golem part registries can fire before the traits registry. Forge binds this same reference on registration.
        return registry.getHolder(key).orElseGet(() ->
                ((WritableRegistry<GolemTrait>) registry).createRegistrationLookup().getOrThrow(key));
    }

    public static void register(IEventBus modBus) {
        TRAITS.register(modBus);
    }
}
