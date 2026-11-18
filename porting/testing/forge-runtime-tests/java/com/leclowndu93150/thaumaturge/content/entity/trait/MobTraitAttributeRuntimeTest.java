package com.leclowndu93150.thaumaturge.content.entity.trait;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.entity.trait.MobTrait;
import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraitModifiers;
import com.leclowndu93150.thaumaturge.compat.NamedAttributeModifiers;
import com.mojang.serialization.Lifecycle;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.junit.jupiter.api.Test;

class MobTraitAttributeRuntimeTest {
    private Holder<MobTrait> trait(String namespace, Consumer<MobTraitModifiers> declaration) {
        var traits = new MappedRegistry<MobTrait>(MobTrait.REGISTRY_KEY, Lifecycle.stable());
        var key = ResourceKey.create(MobTrait.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath(namespace, "champion"));
        traits.register(key, new MobTrait() {
            @Override
            public void modifiers(LivingEntity mob, MobTraitModifiers modifiers) {
                declaration.accept(modifiers);
            }
        }, Lifecycle.stable());
        return traits.freeze().getHolderOrThrow(key);
    }

    @Test
    void operationsCombineAndDistinctTraitNamespacesKeepIndependentModifiers() {
        var mob = mock(LivingEntity.class);
        var health = new AttributeInstance(Attributes.MAX_HEALTH, changed -> {});
        health.setBaseValue(20);
        when(mob.getAttribute(Attributes.MAX_HEALTH)).thenReturn(health);
        var first = trait("thaumaturge", mods -> {
            mods.setBase(Attributes.MAX_HEALTH, 30);
            mods.add(Attributes.MAX_HEALTH, 0.2, AttributeModifier.Operation.MULTIPLY_BASE);
            mods.add(Attributes.MAX_HEALTH, 0.5, AttributeModifier.Operation.MULTIPLY_TOTAL);
        });
        var other = trait("addon", mods -> mods.add(Attributes.MAX_HEALTH, 2, AttributeModifier.Operation.ADDITION));

        MobTraitAttributeModifiers.refresh(mob, List.of(first, other));
        assertEquals(57.6, health.getValue(), 1e-9);
        assertEquals(20, health.getBaseValue());
        assertEquals(4, health.getModifiers().size());
        MobTraitAttributeModifiers.refresh(mob, List.of(first, other));
        assertEquals(4, health.getModifiers().size());
        MobTraitAttributeModifiers.refresh(mob, List.of(first));
        assertEquals(54, health.getValue(), 1e-9);
    }

    @Test
    void refreshUpdatesExistingAmountOperationAndAttributeUnderTheSameTraitId() {
        var mob = mock(LivingEntity.class);
        var health = new AttributeInstance(Attributes.MAX_HEALTH, changed -> {});
        var damage = new AttributeInstance(Attributes.ATTACK_DAMAGE, changed -> {});
        health.setBaseValue(20);
        damage.setBaseValue(2);
        when(mob.getAttribute(Attributes.MAX_HEALTH)).thenReturn(health);
        when(mob.getAttribute(Attributes.ATTACK_DAMAGE)).thenReturn(damage);
        var id = TTIds.rl("trait/thaumaturge/champion/0");
        var first = trait("thaumaturge", mods -> mods.add(Attributes.MAX_HEALTH, 5, AttributeModifier.Operation.ADDITION));
        var changed = trait("thaumaturge", mods -> mods.add(Attributes.MAX_HEALTH, 0.5, AttributeModifier.Operation.MULTIPLY_TOTAL));
        var moved = trait("thaumaturge", mods -> mods.add(Attributes.ATTACK_DAMAGE, 4, AttributeModifier.Operation.ADDITION));

        MobTraitAttributeModifiers.refresh(mob, List.of(first));
        assertEquals(25, health.getValue());
        MobTraitAttributeModifiers.refresh(mob, List.of(changed));
        assertEquals(30, health.getValue());
        assertEquals(AttributeModifier.Operation.MULTIPLY_TOTAL,
                health.getModifier(NamedAttributeModifiers.id(id)).getOperation());
        MobTraitAttributeModifiers.refresh(mob, List.of(moved));
        assertEquals(20, health.getValue());
        assertTrue(health.getModifiers().isEmpty());
        assertEquals(6, damage.getValue());
        assertTrue(NamedAttributeModifiers.has(damage, id));
    }

    @Test
    void nativeSaveLoadRetainsOwnedIdsAndRemovalLeavesUnrelatedModifiers() {
        var firstMob = mock(LivingEntity.class);
        var health = new AttributeInstance(Attributes.MAX_HEALTH, changed -> {});
        var traitId = TTIds.rl("trait/thaumaturge/champion/0");
        var unrelated = new AttributeModifier(UUID.randomUUID(), traitId.toString(), 3, AttributeModifier.Operation.ADDITION);
        health.addPermanentModifier(unrelated);
        when(firstMob.getAttribute(Attributes.MAX_HEALTH)).thenReturn(health);
        var declaration = trait("thaumaturge", mods -> {
            mods.add(Attributes.MAX_HEALTH, 5, AttributeModifier.Operation.ADDITION);
            mods.setBase(Attributes.ATTACK_SPEED, 15);
        });
        MobTraitAttributeModifiers.refresh(firstMob, List.of(declaration));

        var loaded = new AttributeInstance(Attributes.MAX_HEALTH, changed -> {});
        loaded.load(health.save());
        assertEquals(2, loaded.getModifiers().size());
        var owned = loaded.getModifier(NamedAttributeModifiers.id(traitId));
        assertEquals(traitId.toString(), owned.getName());
        assertEquals(5, owned.getAmount());
        assertNotNull(loaded.getModifier(unrelated.getId()));
        var loadedMob = mock(LivingEntity.class);
        when(loadedMob.getAttribute(Attributes.MAX_HEALTH)).thenReturn(loaded);
        MobTraitAttributeModifiers.refresh(loadedMob, List.of(declaration));
        assertSame(owned, loaded.getModifier(owned.getId()));
        MobTraitAttributeModifiers.refresh(loadedMob, List.of());
        assertEquals(1, loaded.getModifiers().size());
        assertNull(loaded.getModifier(owned.getId()));
        assertNotNull(loaded.getModifier(unrelated.getId()));
    }
}
