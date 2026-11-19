package com.leclowndu93150.thaumaturge.content.golem;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.content.entity.EntityEnchantmentDamage;
import com.leclowndu93150.thaumaturge.registry.TTEntityTags;
import com.mojang.authlib.GameProfile;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import org.junit.jupiter.api.Test;

class EntityClassificationRuntimeTest {
    private static final Holder<DamageType> DAMAGE = Holder.direct(new DamageType("classification_test", 0.0F));

    @Test
    void breathingHealingAndStatusEffectTagsStayIndependent() throws ReflectiveOperationException {
        var level = level();
        var cow = new ProbeCow(level);
        var zombie = new Zombie(EntityType.ZOMBIE, level);
        var fluidAtEyes = Entity.class.getDeclaredField("forgeFluidTypeOnEyes");
        fluidAtEyes.setAccessible(true);
        fluidAtEyes.set(cow, ForgeMod.WATER_TYPE.get());
        cow.setAirSupply(100);
        when(level.getBlockState(any())).thenReturn(Blocks.WATER.defaultBlockState());

        withTags(Map.of(TTEntityTags.CAN_BREATHE_UNDER_WATER, List.of(EntityType.COW)), () -> {
            assertTrue(cow.canBreatheUnderwater());
            assertTrue(zombie.canBreatheUnderwater());
            assertFalse(cow.isInvertedHealAndHarm());
            assertTrue(cow.canBeAffected(new MobEffectInstance(MobEffects.POISON)));
            ForgeHooks.onLivingBreathe(cow, 1, 4);
            assertEquals(100, cow.getAirSupply());
        });
        withTags(Map.of(TTEntityTags.INVERTED_HEALING_AND_HARM, List.of(EntityType.COW)), () -> {
            assertFalse(cow.canBreatheUnderwater());
            assertTrue(cow.isInvertedHealAndHarm());
            assertTrue(zombie.isInvertedHealAndHarm());
            assertTrue(cow.canBeAffected(new MobEffectInstance(MobEffects.REGENERATION)));
            MobEffects.HEAL.applyInstantenousEffect(null, null, cow, 0, 1.0);
            assertEquals(List.of(6.0F), cow.hits);
        });
        withTags(Map.of(TTEntityTags.IGNORES_POISON_AND_REGEN, List.of(EntityType.COW)), () -> {
            assertFalse(cow.canBeAffected(new MobEffectInstance(MobEffects.POISON)));
            assertFalse(cow.canBeAffected(new MobEffectInstance(MobEffects.REGENERATION)));
            assertTrue(cow.canBeAffected(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN)));
            assertFalse(zombie.canBeAffected(new MobEffectInstance(MobEffects.POISON)));
            assertFalse(cow.isInvertedHealAndHarm());
            assertSame(MobType.UNDEFINED, cow.getMobType());
        });

        assertFalse(cow.canBreatheUnderwater());
        assertFalse(cow.isInvertedHealAndHarm());
        assertTrue(cow.canBeAffected(new MobEffectInstance(MobEffects.POISON)));
        ForgeHooks.onLivingBreathe(cow, 1, 4);
        assertEquals(99, cow.getAirSupply());
    }

    @Test
    void damageTagsApplyInNativeAttacksAndGolemCombatWithoutDoubleCounting() {
        var level = level();
        var target = new ProbeCow(level);
        var mob = new Zombie(EntityType.ZOMBIE, level);
        mob.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(4.0);
        var player = new Player(level, BlockPos.ZERO, 0.0F, new GameProfile(UUID.randomUUID(), "PortClassification")) {
            @Override public boolean isSpectator() { return false; }
            @Override public boolean isCreative() { return false; }
            @Override public float getAttackStrengthScale(float tickDelta) { return 1.0F; }
        };
        player.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(4.0);
        var weapon = new ItemStack(Items.IRON_SWORD);
        weapon.enchant(Enchantments.SMITE, 2);
        weapon.enchant(Enchantments.BANE_OF_ARTHROPODS, 1);
        mob.setItemSlot(EquipmentSlot.MAINHAND, weapon.copy());
        player.setItemSlot(EquipmentSlot.MAINHAND, weapon.copy());

        var trident = new ItemStack(Items.TRIDENT);
        trident.enchant(Enchantments.SMITE, 2);
        Map<TagKey<EntityType<?>>, List<EntityType<?>>> tags = Map.of(
                TTEntityTags.SENSITIVE_TO_SMITE, List.of(EntityType.COW, EntityType.ZOMBIE),
                TTEntityTags.SENSITIVE_TO_BANE_OF_ARTHROPODS, List.of(EntityType.COW));
        withTags(tags, () -> {
            mob.doHurtTarget(target);
            player.attack(target);
            GolemCombat.tryMeleeHit(mob, target);
            new ProbeTrident(level, player, trident).hit(target);
            assertEquals(List.of(11.5F, 11.5F, 11.5F, 13.0F), target.hits);
            Enchantments.BANE_OF_ARTHROPODS.doPostAttack(player, target, 1);
            var slowness = target.getEffect(MobEffects.MOVEMENT_SLOWDOWN);
            assertNotNull(slowness);
            assertEquals(3, slowness.getAmplifier());
            assertEquals(0.0F, EntityEnchantmentDamage.additionalBonus(weapon, MobType.UNDEAD, EntityType.ZOMBIE));
        });
        target.hits.clear();
        mob.doHurtTarget(target);
        assertEquals(List.of(4.0F), target.hits);
    }

    @Test
    void witherUsesTheFriendTagForTargetsAndIncomingAttackers() throws ReflectiveOperationException {
        var level = level();
        var boss = new WitherBoss(EntityType.WITHER, level);
        var cow = new ProbeCow(level);
        var zombie = new Zombie(EntityType.ZOMBIE, level);
        Predicate<LivingEntity> selector = witherSelector();
        var reachedNativeDamage = new ArrayList<Entity>();
        Consumer<LivingAttackEvent> observe = event -> {
            if (event.getEntity() == boss) {
                reachedNativeDamage.add(event.getSource().getEntity());
                event.setCanceled(true);
            }
        };
        MinecraftForge.EVENT_BUS.addListener(observe);
        try {
            assertTrue(selector.test(cow));
            assertFalse(selector.test(zombie));
            withTags(Map.of(TTEntityTags.WITHER_FRIENDS, List.of(EntityType.COW)), () -> {
                assertFalse(selector.test(cow));
                assertFalse(boss.hurt(new DamageSource(DAMAGE, cow), 4.0F));
                assertTrue(reachedNativeDamage.isEmpty());
            });
            assertTrue(selector.test(cow));
            assertFalse(boss.hurt(new DamageSource(DAMAGE, cow), 4.0F));
            assertEquals(List.of(cow), reachedNativeDamage);
        } finally {
            MinecraftForge.EVENT_BUS.unregister(observe);
        }
    }

    @SuppressWarnings("unchecked")
    private static Predicate<LivingEntity> witherSelector() throws ReflectiveOperationException {
        Field selector = WitherBoss.class.getDeclaredField("LIVING_ENTITY_SELECTOR");
        selector.setAccessible(true);
        return (Predicate<LivingEntity>) selector.get(null);
    }

    private static ServerLevel level() {
        var level = mock(ServerLevel.class);
        var sources = mock(DamageSources.class);
        when(level.damageSources()).thenReturn(sources);
        when(level.getScoreboard()).thenReturn(mock(ServerScoreboard.class));
        when(sources.mobAttack(any())).thenAnswer(call -> new DamageSource(DAMAGE, (Entity) call.getArgument(0)));
        when(sources.playerAttack(any())).thenAnswer(call -> new DamageSource(DAMAGE, (Entity) call.getArgument(0)));
        when(sources.trident(any(), any())).thenAnswer(call -> new DamageSource(DAMAGE, call.getArgument(0), call.getArgument(1)));
        when(sources.magic()).thenReturn(new DamageSource(DAMAGE));
        return level;
    }

    private static void withTags(Map<TagKey<EntityType<?>>, List<EntityType<?>>> values, Runnable test) {
        var registry = BuiltInRegistries.ENTITY_TYPE;
        var original = new LinkedHashMap<TagKey<EntityType<?>>, List<Holder<EntityType<?>>>>();
        registry.getTags().forEach(pair -> original.put(pair.getFirst(), pair.getSecond().stream().toList()));
        values.keySet().forEach(tag -> original.putIfAbsent(tag, List.of()));
        var changed = new LinkedHashMap<>(original);
        values.forEach((tag, types) -> changed.put(tag,
                types.stream().<Holder<EntityType<?>>>map(EntityType::builtInRegistryHolder).toList()));
        registry.bindTags(changed);
        try {
            test.run();
        } finally {
            registry.bindTags(original);
        }
    }

    private static final class ProbeCow extends Cow {
        private final List<Float> hits = new ArrayList<>();

        private ProbeCow(Level level) {
            super(EntityType.COW, level);
        }

        @Override public boolean hurt(DamageSource source, float amount) {
            hits.add(amount);
            return false;
        }
    }

    private static final class ProbeTrident extends ThrownTrident {
        private ProbeTrident(Level level, LivingEntity shooter, ItemStack stack) {
            super(level, shooter, stack);
        }

        private void hit(Entity target) {
            onHitEntity(new EntityHitResult(target));
        }
    }
}
