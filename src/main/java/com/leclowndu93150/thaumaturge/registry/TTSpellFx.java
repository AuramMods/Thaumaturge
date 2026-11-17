package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.spell.fx.SpellFxStyle;
import com.leclowndu93150.thaumaturge.content.spell.fx.SpellFxStyles;
import net.minecraft.core.Registry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public final class TTSpellFx {
    public static final DeferredRegister<SpellFxStyle> STYLES =
            DeferredRegister.create(SpellFxStyle.REGISTRY_KEY, TTIds.MODID);

    private static final java.util.function.Supplier<net.minecraftforge.registries.IForgeRegistry<SpellFxStyle>> REGISTRY =
            STYLES.makeRegistry(() -> new net.minecraftforge.registries.RegistryBuilder<SpellFxStyle>().hasTags());

    public static final RegistryObject<SpellFxStyle> SPARKLE =
            STYLES.register("sparkle", () -> SpellFxStyles::sparkle);
    public static final RegistryObject<SpellFxStyle> MOTE =
            STYLES.register("mote", () -> SpellFxStyles::mote);
    public static final RegistryObject<SpellFxStyle> FLAME =
            STYLES.register("flame", () -> SpellFxStyles::flame);
    public static final RegistryObject<SpellFxStyle> FROST =
            STYLES.register("frost", () -> SpellFxStyles::frost);
    public static final RegistryObject<SpellFxStyle> GUST =
            STYLES.register("gust", () -> SpellFxStyles::gust);
    public static final RegistryObject<SpellFxStyle> PEBBLE =
            STYLES.register("pebble", () -> SpellFxStyles::pebble);
    public static final RegistryObject<SpellFxStyle> FLUX =
            STYLES.register("flux", () -> SpellFxStyles::flux);
    public static final RegistryObject<SpellFxStyle> HEAL =
            STYLES.register("heal", () -> SpellFxStyles::heal);
    public static final RegistryObject<SpellFxStyle> CURSE =
            STYLES.register("curse", () -> SpellFxStyles::curse);
    public static final RegistryObject<SpellFxStyle> CRACK =
            STYLES.register("crack", () -> SpellFxStyles::crack);
    public static final RegistryObject<SpellFxStyle> RIFT =
            STYLES.register("rift", () -> SpellFxStyles::rift);
    public static final RegistryObject<SpellFxStyle> PRIMAL =
            STYLES.register("primal", () -> SpellFxStyles::primal);
    public static final RegistryObject<SpellFxStyle> WARD =
            STYLES.register("ward", () -> SpellFxStyles::ward);
    public static final RegistryObject<SpellFxStyle> BUBBLE =
            STYLES.register("bubble", () -> SpellFxStyles::bubble);
    public static final RegistryObject<SpellFxStyle> SPARK =
            STYLES.register("spark", () -> SpellFxStyles::spark);
    public static final RegistryObject<SpellFxStyle> LEAF =
            STYLES.register("leaf", () -> SpellFxStyles::leaf);
    public static final RegistryObject<SpellFxStyle> SMOKE =
            STYLES.register("smoke", () -> SpellFxStyles::smoke);

    private TTSpellFx() {}

    public static Registry<SpellFxStyle> registry() {
        return ForgeRegistration.vanillaRegistry(REGISTRY);
    }

    public static void register(IEventBus modBus) {
        STYLES.register(modBus);
    }
}
