package com.leclowndu93150.thaumaturge.content.entity.shield;

import com.leclowndu93150.thaumaturge.compat.NamedAttributeModifiers;
import com.leclowndu93150.thaumaturge.registry.TTAttributes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public final class AbsorptionCapacity {
    private static final ResourceLocation POTION_MODIFIER = ResourceLocation.withDefaultNamespace("effect.absorption");
    private static final int POINTS_PER_LEVEL = 4;

    private AbsorptionCapacity() {}

    public interface LoadingAccess {
        boolean thaumaturge$isReadingAbsorption();
    }

    public static AttributeInstance attribute(LivingEntity entity) {
        var attribute = TTAttributes.MAX_ABSORPTION.orElse(null);
        return attribute == null ? null : entity.getAttribute(attribute);
    }

    public static float clamp(LivingEntity entity, float amount) {
        if (amount <= 0.0F || ((LoadingAccess) entity).thaumaturge$isReadingAbsorption()) {
            return amount;
        }
        var capacity = attribute(entity);
        return capacity == null ? amount : Mth.clamp(amount, 0.0F, (float) capacity.getValue());
    }

    public static void reconcile(LivingEntity entity) {
        float amount = entity.getAbsorptionAmount();
        float bounded = clamp(entity, amount);
        if (bounded < amount) {
            entity.setAbsorptionAmount(bounded);
        }
    }

    public static void restorePotionCapacity(LivingEntity entity) {
        var effect = entity.getEffect(MobEffects.ABSORPTION);
        if (effect != null) {
            var capacity = attribute(entity);
            if (capacity != null) {
                addPotionCapacity(capacity, effect.getAmplifier());
            }
        }
    }

    public static int potionPoints(int amplifier) {
        return POINTS_PER_LEVEL * (amplifier + 1);
    }

    public static void addPotionCapacity(AttributeInstance capacity, int amplifier) {
        NamedAttributeModifiers.replacePermanent(capacity,
                NamedAttributeModifiers.create(POTION_MODIFIER, potionPoints(amplifier), AttributeModifier.Operation.ADDITION));
    }

    public static void removePotionCapacity(AttributeInstance capacity) {
        capacity.removeModifier(NamedAttributeModifiers.id(POTION_MODIFIER));
    }
}
