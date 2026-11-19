package com.leclowndu93150.thaumaturge.api.spell;

import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.leclowndu93150.thaumaturge.api.spell.affinity.SpellAction;
import com.leclowndu93150.thaumaturge.api.spell.affinity.SpellActionType;
import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehavior;
import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehaviorType;
import com.leclowndu93150.thaumaturge.api.spell.fx.SpellFxStyle;
import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.core.Registry;

/**
 * Access to the code registries of the spell system and the dispatch codecs built on them.
 *
 * @apiNote {@link #bind} is called once by Thaumaturge during mod construction. Addons register
 *          into the registries through their keys and never call it.
 * @since 1.0.0
 */
public final class SpellRegistries {
    /** Decodes a behaviour by its {@code type} field. Usable once the registries are bound. */
    public static final Codec<SpellBehavior> BEHAVIOR_CODEC = Codecs.lazyInitialized(
            () -> Codecs.dispatch(behaviors().byNameCodec(), SpellBehavior::type, SpellBehaviorType::codec));

    /** Decodes an affinity action by its {@code type} field. Usable once the registries are bound. */
    public static final Codec<SpellAction> ACTION_CODEC =
            Codecs.lazyInitialized(() -> Codecs.dispatch(actions().byNameCodec(), SpellAction::type, SpellActionType::codec));

    private static Supplier<Registry<SpellBehaviorType<?>>> behaviors;
    private static Supplier<Registry<SpellActionType<?>>> actions;
    private static Supplier<Registry<SpellFxStyle>> fxStyles;

    private SpellRegistries() {}

    /**
     * Binds registry access during mod construction. The suppliers resolve the actual registries
     * after Forge creates them.
     *
     * @param behaviorRegistry access to the behaviour type registry
     * @param actionRegistry   access to the action type registry
     * @param fxRegistry       access to the effect style registry
     * @throws IllegalStateException when already bound
     */
    public static void bind(
            Supplier<Registry<SpellBehaviorType<?>>> behaviorRegistry,
            Supplier<Registry<SpellActionType<?>>> actionRegistry,
            Supplier<Registry<SpellFxStyle>> fxRegistry) {
        if (behaviors != null) {
            throw new IllegalStateException("SpellRegistries already bound");
        }
        behaviors = behaviorRegistry;
        actions = actionRegistry;
        fxStyles = fxRegistry;
    }

    /**
     * The behaviour type registry.
     *
     * @return the registry
     * @throws IllegalStateException before binding
     */
    public static Registry<SpellBehaviorType<?>> behaviors() {
        return bound(behaviors);
    }

    /**
     * The action type registry.
     *
     * @return the registry
     * @throws IllegalStateException before binding
     */
    public static Registry<SpellActionType<?>> actions() {
        return bound(actions);
    }

    /**
     * The effect style registry.
     *
     * @return the registry
     * @throws IllegalStateException before binding
     */
    public static Registry<SpellFxStyle> fxStyles() {
        return bound(fxStyles);
    }

    private static <T> Registry<T> bound(Supplier<Registry<T>> registry) {
        if (registry == null) {
            throw new IllegalStateException("SpellRegistries used before Thaumaturge bound them");
        }
        return registry.get();
    }
}
