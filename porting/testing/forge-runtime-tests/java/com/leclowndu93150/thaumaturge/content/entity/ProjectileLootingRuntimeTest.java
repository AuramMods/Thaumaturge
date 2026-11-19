package com.leclowndu93150.thaumaturge.content.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mojang.authlib.GameProfile;
import com.leclowndu93150.thaumaturge.itemdata.ItemData;
import com.leclowndu93150.thaumaturge.itemdata.PotionData;
import com.leclowndu93150.thaumaturge.itemdata.VanillaItemData;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Illusioner;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

class ProjectileLootingRuntimeTest {
    private static final Holder<DamageType> DAMAGE = Holder.direct(new DamageType("projectile_looting_test", 0.0F));

    @Test
    void pechArrowKeepsPoisonAndFiringBowPropertiesAfterReload() throws Exception {
        ServerLevel level = level();
        Zombie shooter = new Zombie(EntityType.ZOMBIE, level);
        shooter.setPos(3.5, 62.0, -8.0);
        when(level.getEntity(shooter.getUUID())).thenReturn(shooter);
        ItemStack bow = enchanted(new ItemStack(Items.BOW), 2);
        bow.enchant(Enchantments.POWER_ARROWS, 3);
        bow.enchant(Enchantments.PUNCH_ARROWS, 2);
        bow.enchant(Enchantments.FLAMING_ARROWS, 1);
        bow.enchant(Enchantments.PIERCING, 2);
        ItemStack ammunition = poisonArrows();

        Arrow fired = ProjectileArrows.create(level, shooter, ammunition, bow, 3.75);
        assertSame(shooter, fired.getOwner());
        assertEquals(3.5, fired.getX());
        assertEquals(shooter.getEyeY() - 0.1F, fired.getY(), 1e-8);
        assertEquals(-8.0, fired.getZ());
        assertEquals(AbstractArrow.Pickup.DISALLOWED, fired.pickup);
        assertEquals(5.25, fired.getBaseDamage());
        assertEquals(2, fired.getKnockback());
        assertEquals(2, fired.getPierceLevel());
        assertEquals(2000, fired.getRemainingFireTicks());
        assertEquals(3, ammunition.getCount());

        bow.shrink(1);
        var reloaded = new Arrow(EntityType.ARROW, level);
        reloaded.load(fired.saveWithoutId(new CompoundTag()));
        assertSame(shooter, reloaded.getOwner());
        assertEquals(2, ProjectileLooting.levelFor(hit(reloaded, shooter)));
        assertEquals(fired.getColor(), reloaded.getColor());

        var target = mock(LivingEntity.class);
        var applyEffects = Arrow.class.getDeclaredMethod("doPostHurtEffects", LivingEntity.class);
        applyEffects.setAccessible(true);
        applyEffects.invoke(reloaded, target);
        var effect = ArgumentCaptor.forClass(MobEffectInstance.class);
        verify(target).addEffect(effect.capture(), org.mockito.ArgumentMatchers.same(shooter));
        assertSame(MobEffects.POISON, effect.getValue().getEffect());
        assertEquals(40, effect.getValue().getDuration());
    }

    @Test
    void turretArrowUsesAmmunitionEffectsWithoutBorrowingWeaponEnchantments() {
        ServerLevel level = level();
        Zombie turret = new Zombie(EntityType.ZOMBIE, level);
        turret.setItemSlot(EquipmentSlot.MAINHAND, enchanted(new ItemStack(Items.BOW), 4));
        ItemStack ammunition = enchanted(poisonArrows(), 5);
        Arrow fired = ProjectileArrows.create(level, turret, ammunition, null, 7.0);
        assertEquals(7.0, fired.getBaseDamage());
        assertEquals(0, ProjectileLooting.levelFor(hit(fired, turret)));
        assertEquals(0, fired.getKnockback());
        assertTrue(fired.getColor() >= 0);
    }

    private static ItemStack poisonArrows() {
        var ammunition = new ItemStack(Items.TIPPED_ARROW, 3);
        ItemData.set(ammunition, VanillaItemData.POTION_CONTENTS,
                new PotionData(Optional.empty(), Optional.empty(), List.of(new MobEffectInstance(MobEffects.POISON, 40))));
        return ammunition;
    }

    @Test
    void bowKeepsTheFiringWeaponsLootingAfterItBreaksAndTheArrowReloads() {
        ServerLevel level = level();
        Player shooter = new Player(level, BlockPos.ZERO, 0.0F, new GameProfile(UUID.randomUUID(), "PortArchery")) {
            @Override public boolean isSpectator() { return false; }
            @Override public boolean isCreative() { return false; }
            @Override public ItemStack getProjectile(ItemStack weapon) { return new ItemStack(Items.ARROW, 2); }
        };
        ItemStack bow = enchanted(new ItemStack(Items.BOW), 2);
        shooter.setItemInHand(InteractionHand.OFF_HAND, bow);
        bow.setDamageValue(bow.getMaxDamage() - 1);
        Items.BOW.releaseUsing(bow, level, shooter, Items.BOW.getUseDuration(bow) - 20);

        assertTrue(bow.isEmpty());
        shooter.setItemInHand(InteractionHand.MAIN_HAND, enchanted(new ItemStack(Items.IRON_SWORD), 5));
        var original = launchedArrow(level);
        assertEquals(2, ProjectileLooting.levelFor(hit(original, shooter)));

        Arrow loaded = new Arrow(EntityType.ARROW, level);
        loaded.load(original.saveWithoutId(new CompoundTag()));
        assertEquals(2, ProjectileLooting.levelFor(hit(loaded, shooter)));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 3})
    void crossbowUsesItsOwnLootingInsteadOfTheAmmunitionOrTheShootersHand(int rank) {
        ServerLevel level = level();
        Zombie shooter = new Zombie(EntityType.ZOMBIE, level);
        shooter.setItemSlot(EquipmentSlot.MAINHAND, enchanted(new ItemStack(Items.IRON_SWORD), 4));
        ItemStack crossbow = new ItemStack(Items.CROSSBOW);
        if (rank > 0) {
            enchanted(crossbow, rank);
        }
        var charged = new ListTag();
        charged.add(enchanted(new ItemStack(Items.ARROW), 5).save(new CompoundTag()));
        crossbow.getOrCreateTag().put("ChargedProjectiles", charged);
        shooter.setItemSlot(EquipmentSlot.OFFHAND, crossbow);

        CrossbowItem.performShooting(level, shooter, InteractionHand.OFF_HAND, crossbow, 3.15F, 0.0F);

        assertEquals(rank, ProjectileLooting.levelFor(hit(launchedArrow(level), shooter)));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void mobArchersUseTheSelectedBowHand(boolean illusioner) {
        ServerLevel level = level();
        LivingEntity shooter = illusioner ? new Illusioner(EntityType.ILLUSIONER, level)
                : new Skeleton(EntityType.SKELETON, level);
        shooter.setItemSlot(EquipmentSlot.MAINHAND, enchanted(new ItemStack(Items.IRON_SWORD), 5));
        shooter.setItemSlot(EquipmentSlot.OFFHAND, enchanted(new ItemStack(Items.BOW), 2));
        Zombie target = new Zombie(EntityType.ZOMBIE, level);
        target.setPos(6.0, 0.0, -3.0);

        ((RangedAttackMob) shooter).performRangedAttack(target, 1.0F);

        assertEquals(2, ProjectileLooting.levelFor(hit(launchedArrow(level), shooter)));
    }

    @Test
    void tridentKeepsItsOwnStackAcrossSaveAndLoad() {
        ServerLevel level = level();
        Zombie shooter = new Zombie(EntityType.ZOMBIE, level);
        ItemStack weapon = enchanted(new ItemStack(Items.TRIDENT), 3);
        var original = new ThrownTrident(level, shooter, weapon);
        weapon.shrink(1);
        shooter.setItemSlot(EquipmentSlot.MAINHAND, enchanted(new ItemStack(Items.IRON_SWORD), 5));
        assertEquals(3, ProjectileLooting.levelFor(hit(original, shooter)));

        var loaded = new ThrownTrident(EntityType.TRIDENT, level);
        loaded.load(original.saveWithoutId(new CompoundTag()));
        assertEquals(3, ProjectileLooting.levelFor(hit(loaded, shooter)));
    }

    private static ServerLevel level() {
        ServerLevel level = mock(ServerLevel.class);
        when(level.getRandom()).thenReturn(RandomSource.create(37L));
        when(level.getDifficulty()).thenReturn(Difficulty.NORMAL);
        return level;
    }

    private static ItemStack enchanted(ItemStack stack, int rank) {
        stack.enchant(Enchantments.MOB_LOOTING, rank);
        return stack;
    }

    private static Arrow launchedArrow(ServerLevel level) {
        var spawned = ArgumentCaptor.forClass(Entity.class);
        verify(level).addFreshEntity(spawned.capture());
        return assertInstanceOf(Arrow.class, spawned.getValue());
    }

    private static DamageSource hit(Entity projectile, LivingEntity shooter) {
        return new DamageSource(DAMAGE, projectile, shooter);
    }
}
