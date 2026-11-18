package com.leclowndu93150.thaumaturge.content.entity.trait;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.entity.trait.MobTrait;
import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraitModifiers;
import com.leclowndu93150.thaumaturge.compat.NamedAttributeModifiers;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/** Owns permanent modifiers derived from the entity's active traits. */
public final class MobTraitAttributeModifiers {
    private static final String MODIFIER_ROOT = "trait/";

    private MobTraitAttributeModifiers() {}

    public static void refresh(LivingEntity mob, List<Holder<MobTrait>> traits) {
        Map<UUID, Expected> expected = new LinkedHashMap<>();
        for (Holder<MobTrait> trait : traits) {
            ResourceLocation id = trait.unwrapKey().orElseThrow().location();
            trait.value().modifiers(mob, new ModifierCollector(mob, id, expected));
        }
        BuiltInRegistries.ATTRIBUTE.forEach(attribute -> {
            AttributeInstance instance = mob.getAttribute(attribute);
            if (instance == null) return;
            for (AttributeModifier modifier : List.copyOf(instance.getModifiers())) {
                Expected entry = expected.get(modifier.getId());
                if ((entry == null || entry.attribute() != attribute) && isTraitModifier(modifier)) {
                    instance.removeModifier(modifier);
                }
            }
        });
        for (Expected entry : expected.values()) {
            AttributeInstance instance = mob.getAttribute(entry.attribute());
            if (instance != null) NamedAttributeModifiers.replacePermanent(instance, entry.modifier());
        }
    }

    private static boolean isTraitModifier(AttributeModifier modifier) {
        ResourceLocation id = ResourceLocation.tryParse(modifier.getName());
        return id != null && id.getNamespace().equals(TTIds.MODID)
                && id.getPath().startsWith(MODIFIER_ROOT)
                && NamedAttributeModifiers.id(id).equals(modifier.getId());
    }

    private record Expected(Attribute attribute, AttributeModifier modifier) {}

    private static final class ModifierCollector implements MobTraitModifiers {
        private final LivingEntity mob;
        private final ResourceLocation trait;
        private final Map<UUID, Expected> expected;
        private int index;

        private ModifierCollector(LivingEntity mob, ResourceLocation trait, Map<UUID, Expected> expected) {
            this.mob = mob;
            this.trait = trait;
            this.expected = expected;
        }

        @Override
        public void add(Holder<Attribute> attribute, double amount, AttributeModifier.Operation operation) {
            ResourceLocation id = TTIds.rl(MODIFIER_ROOT + trait.getNamespace() + "/" + trait.getPath() + "/" + index++);
            AttributeModifier modifier = NamedAttributeModifiers.create(id, amount, operation);
            expected.put(modifier.getId(), new Expected(attribute.value(), modifier));
        }

        @Override
        public void setBase(Holder<Attribute> attribute, double value) {
            AttributeInstance instance = mob.getAttribute(attribute.value());
            if (instance != null) {
                add(attribute, value - instance.getBaseValue(), AttributeModifier.Operation.ADDITION);
            }
        }
    }
}
