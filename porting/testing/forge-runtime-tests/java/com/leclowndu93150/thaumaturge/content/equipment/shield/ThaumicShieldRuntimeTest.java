package com.leclowndu93150.thaumaturge.content.equipment.shield;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.leclowndu93150.thaumaturge.registry.TTDamageTypeTags;
import com.leclowndu93150.thaumaturge.registry.TTItemTags;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Lifecycle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent.ImpactResult;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ThaumicShieldRuntimeTest {
    private final Map<TagKey<Item>, List<Holder<Item>>> savedTags = new LinkedHashMap<>();
    private final Consumer<ProjectileImpactEvent> reflect = ShieldEvents::onProjectileImpact;

    @BeforeEach
    void bindShields() {
        var items = BuiltInRegistries.ITEM;
        items.getTags().forEach(pair -> savedTags.put(pair.getFirst(), pair.getSecond().stream().toList()));
        var tags = new LinkedHashMap<>(savedTags);
        var shields = List.<Holder<Item>>of(Items.SHIELD.builtInRegistryHolder());
        tags.put(TTItemTags.SHIELDS_WARDING, shields);
        tags.put(TTItemTags.SHIELDS_SAPPING, shields);
        tags.put(TTItemTags.SHIELDS_REFLECTING, shields);
        items.bindTags(tags);
        MinecraftForge.EVENT_BUS.addListener(reflect);
    }

    @AfterEach
    void unbindShields() {
        MinecraftForge.EVENT_BUS.unregister(reflect);
        BuiltInRegistries.ITEM.bindTags(savedTags);
    }

    @Test
    void wardingAdmitsBypassingMagicAndSapsTheAttacker() {
        var level = world();
        var blocker = new Blocker(level);
        var attacker = frontAttacker(level);
        var source = damage(attacker, TTDamageTypeTags.WARDED_BY_SHIELDS, DamageTypeTags.BYPASSES_SHIELD);
        assertFalse(blocker.isDamageSourceBlocked(source));

        assertFalse(blocker.hurt(source, 6));
        assertEquals(List.of(12.0F), blocker.shieldCosts);
        assertEquals(List.of(0.0F), blocker.healthCosts);
        var weakness = attacker.getEffect(MobEffects.WEAKNESS);
        assertNotNull(weakness);
        assertEquals(100, weakness.getDuration());
        assertEquals(0, weakness.getAmplifier());
    }

    @Test
    void forgeCanCancelTheWardOrReduceItsBlockAndPreventDurabilityCost() {
        var level = world();
        var cancelled = new Blocker(level);
        var reduced = new Blocker(level);
        var noWear = new Blocker(level);
        var attacker = frontAttacker(level);
        var source = damage(attacker, TTDamageTypeTags.WARDED_BY_SHIELDS, DamageTypeTags.BYPASSES_SHIELD);
        Consumer<ShieldBlockEvent> change = event -> {
            if (event.getEntity() == cancelled) event.setCanceled(true);
            if (event.getEntity() == reduced) event.setBlockedDamage(4);
            if (event.getEntity() == noWear) event.setShieldTakesDamage(false);
        };
        MinecraftForge.EVENT_BUS.addListener(change);
        try {
            assertTrue(cancelled.hurt(source, 6));
            assertEquals(List.of(6.0F), cancelled.healthCosts);
            assertTrue(cancelled.shieldCosts.isEmpty());
            assertNull(attacker.getEffect(MobEffects.WEAKNESS));

            assertTrue(reduced.hurt(source, 6));
            assertEquals(List.of(2.0F), reduced.healthCosts);
            assertEquals(List.of(8.0F), reduced.shieldCosts);

            assertFalse(noWear.hurt(source, 6));
            assertEquals(List.of(0.0F), noWear.healthCosts);
            assertTrue(noWear.shieldCosts.isEmpty());
        } finally {
            MinecraftForge.EVENT_BUS.unregister(change);
        }
    }

    @Test
    void nativeBlocksKeepNormalCostAndOnlyQualifyingHitsSap() {
        var level = world();
        var attacker = frontAttacker(level);
        var projectile = new Blocker(level);
        var projectileDamage = damage(attacker, DamageTypeTags.IS_PROJECTILE, TTDamageTypeTags.WARDED_BY_SHIELDS);
        assertTrue(projectile.isDamageSourceBlocked(projectileDamage));
        assertFalse(projectile.hurt(projectileDamage, 6));
        assertEquals(List.of(6.0F), projectile.shieldCosts);
        assertNull(attacker.getEffect(MobEffects.WEAKNESS));

        var bypass = new Blocker(level);
        assertTrue(bypass.hurt(damage(attacker, DamageTypeTags.BYPASSES_SHIELD), 6));
        assertEquals(List.of(6.0F), bypass.healthCosts);
        assertTrue(bypass.shieldCosts.isEmpty());
        assertNull(attacker.getEffect(MobEffects.WEAKNESS));

        var lowered = new Blocker(level);
        lowered.raised = false;
        assertTrue(lowered.hurt(damage(attacker, TTDamageTypeTags.WARDED_BY_SHIELDS), 6));
        assertEquals(List.of(6.0F), lowered.healthCosts);
        assertTrue(lowered.shieldCosts.isEmpty());
        assertNull(attacker.getEffect(MobEffects.WEAKNESS));
    }

    @Test
    void playerWardUsesNativeShieldWearAndMirrorKeepsExistingArrowPickupMode() throws ReflectiveOperationException {
        var level = world();
        var player = new Player(level, BlockPos.ZERO, 0, new GameProfile(UUID.randomUUID(), "PortShield")) {
            @Override public boolean isSpectator() { return false; }
            @Override public boolean isCreative() { return false; }

            void holdShield() {
                for (int tick = 0; tick < 5; tick++) updateUsingItem(getUseItem());
            }
        };
        var shield = new ItemStack(Items.SHIELD);
        player.setItemInHand(InteractionHand.OFF_HAND, shield);
        raiseShield(player);
        player.holdShield();
        assertTrue(player.isBlocking());
        var source = damage(frontAttacker(level), TTDamageTypeTags.WARDED_BY_SHIELDS, DamageTypeTags.BYPASSES_SHIELD);
        assertFalse(player.hurt(source, 6));
        assertEquals(13, shield.getDamageValue());

        var arrow = new Arrow(EntityType.ARROW, level);
        arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
        arrow.setPos(player.position().add(0, 0, 2));
        arrow.setDeltaMovement(0, 0, -1.5);
        assertTrue(ForgeEventFactory.onProjectileImpact(arrow, new EntityHitResult(player)));
        assertSame(player, arrow.getOwner());
        assertEquals(AbstractArrow.Pickup.CREATIVE_ONLY, arrow.pickup);
        assertEquals(16, shield.getDamageValue());
    }

    @Test
    void arrowTickReflectsWithoutPermanentlyIgnoringTheShieldBearer() {
        var level = world();
        var blocker = new Blocker(level);
        var arrow = new AimedArrow(level);
        var shooter = frontAttacker(level);
        arrow.setOwner(shooter);
        arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
        arrow.aimAt(blocker);

        arrow.tick();

        assertSame(blocker, arrow.getOwner());
        assertEquals(AbstractArrow.Pickup.CREATIVE_ONLY, arrow.pickup);
        assertTrue(arrow.getDeltaMovement().z > 0);
        assertTrue(arrow.getZ() > 2.0);
        assertTrue(arrow.hasImpulse);
        assertEquals(3, blocker.shield.getDamageValue());
        assertTrue(arrow.canHitEntity(blocker));

        arrow.aimAt(blocker);
        arrow.tick();
        assertEquals(6, blocker.shield.getDamageValue());

        Consumer<ProjectileImpactEvent> otherSkip = event -> event.setImpactResult(ImpactResult.SKIP_ENTITY);
        MinecraftForge.EVENT_BUS.addListener(otherSkip);
        blocker.raised = false;
        try {
            arrow.aimAt(blocker);
            arrow.tick();
            assertFalse(arrow.canHitEntity(blocker));
        } finally {
            MinecraftForge.EVENT_BUS.unregister(otherSkip);
        }
    }

    @Test
    void fireballReflectionReversesAccelerationAndBreaksTheShieldInTheUsedHand() {
        var level = world();
        var blocker = new Blocker(level);
        blocker.shield.setDamageValue(blocker.shield.getMaxDamage() - 2);
        var fireball = new SmallFireball(EntityType.SMALL_FIREBALL, level);
        fireball.setPos(0, -1, 2);
        fireball.setYRot(22);
        fireball.yRotO = 10;
        fireball.setDeltaMovement(0, 0, -1.5);
        fireball.zPower = -0.1;

        assertTrue(ForgeEventFactory.onProjectileImpact(fireball, new EntityHitResult(blocker)));

        assertSame(blocker, fireball.getOwner());
        assertEquals(1.5, fireball.getDeltaMovement().z);
        assertEquals(0.05, fireball.zPower, 1.0E-7);
        assertEquals(202, fireball.getYRot());
        assertEquals(190, fireball.yRotO);
        assertTrue(fireball.hasImpulse);
        assertTrue(blocker.shield.isEmpty());
        assertEquals(List.of(InteractionHand.OFF_HAND), blocker.breaks);
    }

    @Test
    void rearImpactsAndPriorForgeImpactDecisionsLeaveTheProjectileUnchanged() {
        var level = world();
        var blocker = new Blocker(level);
        var shooter = frontAttacker(level);
        var arrow = new Arrow(EntityType.ARROW, level);
        arrow.setOwner(shooter);
        arrow.setPos(0, 0, -2);
        arrow.setDeltaMovement(0, 0, 1);
        var hit = new EntityHitResult(blocker);
        assertEquals(ImpactResult.DEFAULT, ForgeEventFactory.onProjectileImpactResultNullable(arrow, hit));

        arrow.setPos(0, 0, 2);
        var impact = new ProjectileImpactEvent(arrow, hit);
        impact.setImpactResult(ImpactResult.STOP_AT_CURRENT_NO_DAMAGE);
        ShieldEvents.onProjectileImpact(impact);
        assertEquals(ImpactResult.STOP_AT_CURRENT_NO_DAMAGE, impact.getImpactResult());
        assertSame(shooter, arrow.getOwner());
        assertEquals(1.0, arrow.getDeltaMovement().z);
        assertEquals(0, blocker.shield.getDamageValue());
    }

    private static Level world() {
        var world = mock(ServerLevel.class);
        when(world.getDifficulty()).thenReturn(Difficulty.NORMAL);
        when(world.getProfiler()).thenReturn(InactiveProfiler.INSTANCE);
        when(world.getBlockState(any())).thenReturn(Blocks.AIR.defaultBlockState());
        when(world.getFluidState(any())).thenReturn(Fluids.EMPTY.defaultFluidState());
        when(world.clip(any(ClipContext.class))).thenAnswer(call -> {
            var end = call.<ClipContext>getArgument(0).getTo();
            return BlockHitResult.miss(end, Direction.UP, BlockPos.containing(end));
        });
        return world;
    }

    private static void raiseShield(Player player) throws ReflectiveOperationException {
        var field = ForgeConfig.class.getDeclaredField("serverSpec");
        field.setAccessible(true);
        var spec = (ForgeConfigSpec) field.get(null);
        boolean loadConfig = !spec.isLoaded();
        if (loadConfig) {
            var config = CommentedConfig.inMemory();
            spec.correct(config);
            spec.setConfig(config);
        }
        try {
            player.startUsingItem(InteractionHand.OFF_HAND);
        } finally {
            if (loadConfig) spec.setConfig(null);
        }
    }

    private static Zombie frontAttacker(Level level) {
        var attacker = new Zombie(EntityType.ZOMBIE, level);
        attacker.setPos(0, 0, 6);
        return attacker;
    }

    @SafeVarargs
    private static DamageSource damage(Entity attacker, TagKey<DamageType>... tags) {
        var types = new MappedRegistry<DamageType>(Registries.DAMAGE_TYPE, Lifecycle.stable());
        var key = ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.withDefaultNamespace("port_shield"));
        var type = types.register(key, new DamageType("port_shield", 0.0F), Lifecycle.stable());
        types.freeze();
        var bindings = new LinkedHashMap<TagKey<DamageType>, List<Holder<DamageType>>>();
        for (var tag : tags) bindings.put(tag, List.of(type));
        types.bindTags(bindings);
        return new DamageSource(type, attacker);
    }

    private static final class Blocker extends Zombie {
        private final ItemStack shield = new ItemStack(Items.SHIELD);
        private final List<Float> shieldCosts = new ArrayList<>();
        private final List<Float> healthCosts = new ArrayList<>();
        private final List<InteractionHand> breaks = new ArrayList<>();
        private boolean raised = true;

        Blocker(Level level) {
            super(EntityType.ZOMBIE, level);
        }

        @Override public boolean isBlocking() { return raised; }
        @Override public ItemStack getUseItem() { return shield; }
        @Override public InteractionHand getUsedItemHand() { return InteractionHand.OFF_HAND; }
        @Override protected void hurtCurrentlyUsedShield(float amount) { shieldCosts.add(amount); }
        @Override protected void actuallyHurt(DamageSource source, float amount) { healthCosts.add(amount); }
        @Override public void broadcastBreakEvent(InteractionHand hand) { breaks.add(hand); }
    }

    private static final class AimedArrow extends Arrow {
        private EntityHitResult nextHit;

        AimedArrow(Level level) {
            super(EntityType.ARROW, level);
            setNoGravity(true);
        }

        void aimAt(Entity blocker) {
            setPos(0, 0, 2);
            setDeltaMovement(0, 0, -1.5);
            nextHit = new EntityHitResult(blocker);
        }

        @Override
        protected EntityHitResult findHitEntity(Vec3 start, Vec3 end) {
            var hit = nextHit;
            nextHit = null;
            return hit;
        }

        @Override public boolean canHitEntity(Entity entity) { return super.canHitEntity(entity); }
    }
}
