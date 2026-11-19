package com.leclowndu93150.thaumaturge.registry;

import static com.leclowndu93150.thaumaturge.registry.TTGolemTraits.traitHolder;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealFilterMode;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealType;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealPlacements;
import com.leclowndu93150.thaumaturge.content.golem.seals.behavior.BreakBehavior;
import com.leclowndu93150.thaumaturge.content.golem.seals.behavior.ButcherBehavior;
import com.leclowndu93150.thaumaturge.content.golem.seals.behavior.CollectBehavior;
import com.leclowndu93150.thaumaturge.content.golem.seals.behavior.EmptyBehavior;
import com.leclowndu93150.thaumaturge.content.golem.seals.behavior.FellBehavior;
import com.leclowndu93150.thaumaturge.content.golem.seals.behavior.GuardBehavior;
import com.leclowndu93150.thaumaturge.content.golem.seals.behavior.HarvestBehavior;
import com.leclowndu93150.thaumaturge.content.golem.seals.behavior.ItemMatchSettings;
import com.leclowndu93150.thaumaturge.content.golem.seals.behavior.ProvideBehavior;
import com.leclowndu93150.thaumaturge.content.golem.seals.behavior.StockBehavior;
import com.leclowndu93150.thaumaturge.content.golem.seals.behavior.StoreBehavior;
import com.leclowndu93150.thaumaturge.content.golem.seals.behavior.UseBehavior;
import net.minecraft.core.Registry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public final class TTSeals {
    public static final DeferredRegister<SealType> SEALS = DeferredRegister.create(SealType.REGISTRY_KEY, TTIds.MODID);
    private static final java.util.function.Supplier<net.minecraftforge.registries.IForgeRegistry<SealType>> REGISTRY =
            SEALS.makeRegistry(() -> new net.minecraftforge.registries.RegistryBuilder<SealType>().hasTags().disableSync());

    public static final RegistryObject<SealType> PICKUP = SEALS.register(
            "pickup", () -> collect(1).placer(TTItems.SEAL_PICKUP).build());
    public static final RegistryObject<SealType> PICKUP_ADVANCED = SEALS.register(
            "pickup_advanced",
            () -> collect(9)
                    .showSettings()
                    .requires(traitHolder(TTGolemTraits.SMART))
                    .placer(TTItems.SEAL_PICKUP_ADVANCED)
                    .build());
    public static final RegistryObject<SealType> FILL =
            SEALS.register("fill", () -> store(1).placer(TTItems.SEAL_FILL).build());
    public static final RegistryObject<SealType> FILL_ADVANCED = SEALS.register(
            "fill_advanced",
            () -> store(9).showSettings()
                    .requires(traitHolder(TTGolemTraits.SMART))
                    .placer(TTItems.SEAL_FILL_ADVANCED)
                    .build());
    public static final RegistryObject<SealType> EMPTY =
            SEALS.register("empty", () -> empty(1).placer(TTItems.SEAL_EMPTY).build());
    public static final RegistryObject<SealType> EMPTY_ADVANCED = SEALS.register(
            "empty_advanced",
            () -> empty(9).showSettings()
                    .requires(traitHolder(TTGolemTraits.SMART))
                    .placer(TTItems.SEAL_EMPTY_ADVANCED)
                    .build());
    public static final RegistryObject<SealType> HARVEST = SEALS.register(
            "harvest",
            () -> SealType.builder(HarvestBehavior::new)
                    .persistent(HarvestBehavior.CODEC, HarvestBehavior.class)
                    .area()
                    .settings(HarvestBehavior.REPLANT, HarvestBehavior.REQUEST_SEEDS)
                    .showSettings()
                    .requires(traitHolder(TTGolemTraits.DEFT), traitHolder(TTGolemTraits.SMART))
                    .placer(TTItems.SEAL_HARVEST)
                    .build());
    public static final RegistryObject<SealType> BUTCHER = SEALS.register(
            "butcher",
            () -> SealType.builder(ButcherBehavior::new)
                    .area()
                    .requires(traitHolder(TTGolemTraits.FIGHTER), traitHolder(TTGolemTraits.SMART))
                    .placer(TTItems.SEAL_BUTCHER)
                    .build());
    public static final RegistryObject<SealType> GUARD = SEALS.register(
            "guard",
            () -> guard().requires(traitHolder(TTGolemTraits.FIGHTER))
                    .placer(TTItems.SEAL_GUARD)
                    .build());
    public static final RegistryObject<SealType> GUARD_ADVANCED = SEALS.register(
            "guard_advanced",
            () -> guard().showSettings()
                    .requires(traitHolder(TTGolemTraits.FIGHTER), traitHolder(TTGolemTraits.SMART))
                    .placer(TTItems.SEAL_GUARD_ADVANCED)
                    .build());
    public static final RegistryObject<SealType> LUMBER = SEALS.register(
            "lumber",
            () -> SealType.builder(FellBehavior::new)
                    .area()
                    .requires(traitHolder(TTGolemTraits.BREAKER), traitHolder(TTGolemTraits.SMART))
                    .placer(TTItems.SEAL_LUMBER)
                    .build());
    public static final RegistryObject<SealType> BREAKER = SEALS.register(
            "breaker", () -> breaker(1).placer(TTItems.SEAL_BREAKER).build());
    public static final RegistryObject<SealType> USE = SEALS.register(
            "use",
            () -> SealType.builder(UseBehavior::new)
                    .filter(1, SealFilterMode.PLAIN)
                    .settings(ItemMatchSettings.ALL)
                    .settings(
                            UseBehavior.LEFT_CLICK,
                            UseBehavior.INTO_AIR,
                            UseBehavior.BARE_HANDED,
                            UseBehavior.SNEAKING,
                            UseBehavior.REQUEST_ITEMS)
                    .showSettings()
                    .requires(traitHolder(TTGolemTraits.DEFT), traitHolder(TTGolemTraits.SMART))
                    .placement(SealPlacements.ANYWHERE)
                    .placer(TTItems.SEAL_USE)
                    .build());
    public static final RegistryObject<SealType> PROVIDER = SEALS.register(
            "provider",
            () -> SealType.builder(ProvideBehavior::new)
                    .filter(9, SealFilterMode.PLAIN)
                    .settings(ItemMatchSettings.ALL)
                    .settings(ProvideBehavior.SINGLE_ITEM, ProvideBehavior.LEAVE_ONE)
                    .showSettings()
                    .forbids(traitHolder(TTGolemTraits.CLUMSY))
                    .placement(SealPlacements.CONTAINER)
                    .placer(TTItems.SEAL_PROVIDER)
                    .build());
    public static final RegistryObject<SealType> STOCK = SEALS.register(
            "stock",
            () -> SealType.builder(StockBehavior::new)
                    .filter(9, SealFilterMode.WHITELIST_WITH_LIMITS)
                    .settings(ItemMatchSettings.ALL)
                    .showSettings()
                    .forbids(traitHolder(TTGolemTraits.CLUMSY))
                    .placement(SealPlacements.CONTAINER)
                    .placer(TTItems.SEAL_STOCK)
                    .build());
    public static final RegistryObject<SealType> BREAKER_ADVANCED = SEALS.register(
            "breaker_advanced",
            () -> breaker(9)
                    .settings(BreakBehavior.SILK_TOUCH)
                    .requires(traitHolder(TTGolemTraits.SMART))
                    .placer(TTItems.SEAL_BREAKER_ADVANCED)
                    .build());

    private TTSeals() {}

    private static SealType.Builder collect(int slots) {
        return SealType.builder(CollectBehavior::new)
                .filter(slots, SealFilterMode.PLAIN)
                .area()
                .settings(ItemMatchSettings.ALL)
                .forbids(traitHolder(TTGolemTraits.CLUMSY));
    }

    private static SealType.Builder store(int slots) {
        return SealType.builder(StoreBehavior::new)
                .filter(slots, SealFilterMode.LIMITS_WHEN_WHITELIST)
                .settings(ItemMatchSettings.ALL)
                .settings(StoreBehavior.ONLY_EXISTING)
                .forbids(traitHolder(TTGolemTraits.CLUMSY));
    }

    private static SealType.Builder empty(int slots) {
        return SealType.builder(EmptyBehavior::new)
                .filter(slots, SealFilterMode.PLAIN)
                .settings(ItemMatchSettings.ALL)
                .settings(EmptyBehavior.CYCLE, EmptyBehavior.LEAVE_ONE)
                .forbids(traitHolder(TTGolemTraits.CLUMSY))
                .placement(SealPlacements.CONTAINER);
    }

    private static SealType.Builder guard() {
        return SealType.builder(GuardBehavior::new)
                .area()
                .settings(GuardBehavior.MONSTERS, GuardBehavior.ANIMALS, GuardBehavior.PLAYERS);
    }

    private static SealType.Builder breaker(int slots) {
        return SealType.builder(BreakBehavior::new)
                .filter(slots, SealFilterMode.PLAIN)
                .area()
                .settings(ItemMatchSettings.MATCH_DAMAGE)
                .showSettings()
                .requires(traitHolder(TTGolemTraits.BREAKER));
    }

    public static Registry<SealType> registry() {
        return ForgeRegistration.vanillaRegistry(REGISTRY);
    }

    public static void register(IEventBus modBus) {
        SEALS.register(modBus);
    }
}
