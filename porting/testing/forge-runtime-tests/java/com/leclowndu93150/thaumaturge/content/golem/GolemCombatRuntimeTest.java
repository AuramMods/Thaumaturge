package com.leclowndu93150.thaumaturge.content.golem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.content.entity.ProjectileLooting;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class GolemCombatRuntimeTest {
    private static final Holder<DamageType> DAMAGE = Holder.direct(new DamageType("golem_combat_test", 0.0F));

    @Test
    void acceptedHitUsesNativeEnchantmentsAndAppliesKnockbackBeforeIgnition() {
        ServerLevel level = mock(ServerLevel.class);
        Zombie attacker = attacker(level, Enchantments.SMITE, 2);
        DamageSource source = new DamageSource(DAMAGE, attacker);
        DamageSources sources = mock(DamageSources.class);
        when(level.damageSources()).thenReturn(sources);
        when(sources.mobAttack(attacker)).thenReturn(source);
        LivingEntity target = mock(LivingEntity.class);
        when(target.getMobType()).thenReturn(MobType.UNDEAD);
        when(target.getAllSlots()).thenReturn(List.of());
        when(target.hurt(source, 9.0F)).thenReturn(true);

        assertTrue(GolemCombat.tryMeleeHit(attacker, target));

        var order = inOrder(target);
        order.verify(target).hurt(source, 9.0F);
        order.verify(target).knockback(2.0, 0.0, -1.0);
        order.verify(target).getAllSlots();
        order.verify(target).setSecondsOnFire(8);
        assertEquals(new Vec3(3.0, 2.0, -3.0), attacker.getDeltaMovement());
        assertSame(target, attacker.getLastHurtMob());
    }

    @Test
    void rejectedHitAndClientSideSkipAfterHitEffects() {
        ServerLevel level = mock(ServerLevel.class);
        Zombie attacker = attacker(level, Enchantments.SHARPNESS, 2);
        DamageSource source = new DamageSource(DAMAGE, attacker);
        DamageSources sources = mock(DamageSources.class);
        when(level.damageSources()).thenReturn(sources);
        when(sources.mobAttack(attacker)).thenReturn(source);
        Entity target = mock(Entity.class);

        assertFalse(GolemCombat.tryMeleeHit(attacker, target));
        var order = inOrder(target);
        order.verify(target).hurt(source, 5.5F);
        order.verifyNoMoreInteractions();
        assertEquals(new Vec3(5.0, 2.0, -5.0), attacker.getDeltaMovement());
        assertNull(attacker.getLastHurtMob());

        Entity clientTarget = mock(Entity.class);
        assertFalse(GolemCombat.tryMeleeHit(attacker(mock(Level.class), Enchantments.SHARPNESS, 2), clientTarget));
        verifyNoInteractions(clientTarget);
    }

    @Test
    void componentRecoveryUsesDirectWeaponAndPreservesRollOrderAndStackCopies() {
        Zombie attacker = attacker(mock(Level.class), Enchantments.MOB_LOOTING, 2);
        ItemStack component = new ItemStack(Items.IRON_INGOT, 4);
        component.getOrCreateTag().putString("marker", "component");
        var components = List.of(ItemStack.EMPTY, component, component);
        RandomSource random = mock(RandomSource.class);
        when(random.nextFloat()).thenReturn(0.2F, 0.5F, 0.6F);
        when(random.nextInt(4)).thenReturn(2);
        List<ItemStack> drops = new ArrayList<>();

        GolemCombat.rollComponentDrops(components, new DamageSource(DAMAGE, attacker), random, drops::add);

        var order = inOrder(random);
        order.verify(random, times(2)).nextFloat();
        order.verify(random).nextInt(4);
        order.verify(random).nextFloat();
        order.verifyNoMoreInteractions();
        assertEquals(2, drops.size());
        assertTrue(drops.get(0).isEmpty());
        assertEquals(2, drops.get(1).getCount());
        assertEquals(4, component.getCount());
        assertEquals(component.getTag(), drops.get(1).getTag());
        assertNotSame(component.getTag(), drops.get(1).getTag());

    }

    @Test
    void componentRecoveryUsesTheArrowSnapshot() {
        Zombie attacker = attacker(mock(Level.class), Enchantments.MOB_LOOTING, 2);
        Arrow arrow = new Arrow(EntityType.ARROW, attacker.level());
        DamageSource shot = new DamageSource(DAMAGE, arrow, attacker);
        ItemStack component = new ItemStack(Items.IRON_INGOT, 4);
        RandomSource random = mock(RandomSource.class);
        when(random.nextFloat()).thenReturn(0.5F);
        when(random.nextInt(4)).thenReturn(3);
        List<ItemStack> drops = new ArrayList<>();

        GolemCombat.rollComponentDrops(List.of(component), shot, random, drops::add);
        assertTrue(drops.isEmpty());

        ProjectileLooting.remember(arrow, 2);
        GolemCombat.rollComponentDrops(List.of(component), shot, random, drops::add);
        assertEquals(1, drops.size());
        assertEquals(1, drops.get(0).getCount());
    }

    private static Zombie attacker(Level level, Enchantment enchantment, int rank) {
        Zombie attacker = new Zombie(EntityType.ZOMBIE, level);
        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        sword.enchant(enchantment, rank);
        sword.enchant(Enchantments.KNOCKBACK, 2);
        sword.enchant(Enchantments.FIRE_ASPECT, 2);
        attacker.setItemSlot(EquipmentSlot.MAINHAND, sword);
        attacker.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(4.0);
        attacker.getAttribute(Attributes.ATTACK_KNOCKBACK).setBaseValue(2.0);
        attacker.setYRot(0.0F);
        attacker.setDeltaMovement(5.0, 2.0, -5.0);
        return attacker;
    }
}
