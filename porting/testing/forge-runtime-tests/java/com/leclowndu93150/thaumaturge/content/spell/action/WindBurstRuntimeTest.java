package com.leclowndu93150.thaumaturge.content.spell.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.content.particle.AirGustParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCandleBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import org.junit.jupiter.api.Test;

class WindBurstRuntimeTest {
    private static final Vec3 CENTER = new Vec3(10.5, 70.5, -8.5);
    private static final DamageSource DAMAGE = new DamageSource(Holder.direct(new DamageType("test_explosion", 0.1F)));

    @Test
    void nativeBlastPushesItsCasterWithoutDamageAndSendsEffects() throws Exception {
        ServerLevel level = level();
        ServerPlayer caster = player(level);
        Entity protectedTarget = mock(Entity.class);
        BlockPos lever = BlockPos.containing(CENTER);
        BlockPos untouchedLever = lever.east();
        BlockState leverState = Blocks.LEVER.defaultBlockState();
        when(level.getBlockState(lever)).thenReturn(leverState);
        when(level.getBlockState(untouchedLever)).thenReturn(leverState);
        when(level.players()).thenReturn(List.of(caster));
        when(level.getEntities(isNull(), any(AABB.class)))
                .thenAnswer(call -> new ArrayList<>(List.of(caster, protectedTarget)));
        AtomicReference<Explosion> observed = new AtomicReference<>();
        Consumer<ExplosionEvent.Detonate> restrict = event -> {
            if (event.getLevel() == level) {
                observed.set(event.getExplosion());
                event.getAffectedEntities().remove(protectedTarget);
                assertTrue(event.getAffectedBlocks().remove(untouchedLever));
            }
        };
        var bus = MinecraftForge.EVENT_BUS;
        bus.addListener(restrict);
        try {
            WindBurstExplosion.burst(level, caster, CENTER, 2.0F);
        } finally {
            bus.unregister(restrict);
        }

        assertSame(caster, observed.get().getIndirectSourceEntity());
        assertTrue(observed.get().interactsWithBlocks());
        verify(caster).setDeltaMovement(new Vec3(0.75, 0, 0));
        verify(caster, never()).hurt(any(), anyFloat());
        verify(protectedTarget, never()).setDeltaMovement(any(Vec3.class));
        verify(caster.connection).send(any(ClientboundSetEntityMotionPacket.class));
        verify(level).sendParticles(caster, new AirGustParticleOptions(2.5F), true,
                CENTER.x, CENTER.y, CENTER.z, 8, 0.35, 0.35, 0.35, 0.0);
        verify(level).playSound(null, CENTER.x, CENTER.y, CENTER.z, TTSounds.WIND.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        verify(level).setBlock(lever, leverState.setValue(LeverBlock.POWERED, true), Block.UPDATE_ALL);
        verify(level, never()).setBlock(org.mockito.ArgumentMatchers.eq(untouchedLever), any(), anyInt());
        verify(level, never()).destroyBlock(any(), anyBoolean());
    }

    @Test
    void normalBlastStillHurtsAndClearsWindLandingCredit() throws Exception {
        ServerLevel level = level();
        ServerPlayer target = player(level);
        when(level.getEntities(isNull(), any(AABB.class))).thenReturn(new ArrayList<>(List.of(target)));
        WindBurstLanding.onBlastImpulse(target, true);
        var explosion = new Explosion(level, null, DAMAGE, null,
                CENTER.x, CENTER.y, CENTER.z, 2.0F, false, Explosion.BlockInteraction.KEEP);
        explosion.explode();
        verify(target).hurt(org.mockito.ArgumentMatchers.same(DAMAGE), anyFloat());
        assertTrue(target.getPersistentData().isEmpty());
    }

    @Test
    void creativeFlightSkipsTheImpulseAndForgeCanVetoTheWind() throws Exception {
        ServerLevel level = level();
        ServerPlayer flyer = player(level);
        when(flyer.isCreative()).thenReturn(true);
        flyer.getAbilities().flying = true;
        when(level.getEntities(isNull(), any(AABB.class))).thenReturn(new ArrayList<>(List.of(flyer)));
        new WindBurstExplosion(level, null, CENTER, 2.0F).explode();
        verify(flyer, never()).setDeltaMovement(any(Vec3.class));
        assertTrue(flyer.getPersistentData().isEmpty());

        Consumer<ExplosionEvent.Start> veto = event -> {
            if (event.getLevel() == level) {
                event.setCanceled(true);
            }
        };
        var bus = MinecraftForge.EVENT_BUS;
        bus.addListener(veto);
        try {
            WindBurstExplosion.burst(level, flyer, CENTER, 2.0F);
        } finally {
            bus.unregister(veto);
        }
        verify(level, never()).players();
        verify(level, never()).playSound(isNull(), anyDouble(), anyDouble(), anyDouble(),
                any(), any(), anyFloat(), anyFloat());
    }

    @Test
    void landingCountsOnlyTheDropBelowTheLaunchHeight() throws Exception {
        ServerLevel level = level();
        ServerPlayer player = player(level);
        WindBurstLanding.onBlastImpulse(player, true);
        when(player.getY()).thenReturn(CENTER.y + 1.0);
        var landingAbove = new LivingFallEvent(player, 9.0F, 1.0F);
        WindBurstLanding.onFall(landingAbove);
        assertEquals(0.0F, landingAbove.getDistance());

        when(player.getY()).thenReturn(CENTER.y - 4.0);
        var landingBelow = new LivingFallEvent(player, 18.0F, 1.0F);
        WindBurstLanding.onFall(landingBelow);
        assertEquals(4.0F, landingBelow.getDistance());

        when(player.onGround()).thenReturn(true);
        var tick = new TickEvent.PlayerTickEvent(TickEvent.Phase.END, player);
        for (int i = 0; i <= 40; i++) {
            WindBurstLanding.onPlayerTick(tick);
        }
        assertTrue(player.getPersistentData().isEmpty());
    }

    @Test
    void nativeBlockTriggersRespectPowerMaterialAndDoorHalf() throws Exception {
        ServerLevel level = level();
        var states = new HashMap<BlockPos, BlockState>();
        when(level.getBlockState(any())).thenAnswer(call -> states.get(call.getArgument(0)));
        when(level.setBlock(any(), any(), anyInt())).thenAnswer(call -> {
            states.put(call.getArgument(0), call.getArgument(1));
            return true;
        });
        when(level.setBlockAndUpdate(any(), any())).thenCallRealMethod();
        var button = place(states, 1, Blocks.STONE_BUTTON.defaultBlockState());
        var lever = place(states, 2, Blocks.LEVER.defaultBlockState());
        var door = place(states, 3, Blocks.OAK_DOOR.defaultBlockState());
        var upper = place(states, 4, Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
        var gate = place(states, 5, Blocks.OAK_FENCE_GATE.defaultBlockState());
        var trapdoor = place(states, 6, Blocks.OAK_TRAPDOOR.defaultBlockState());
        var iron = place(states, 7, Blocks.IRON_TRAPDOOR.defaultBlockState());
        var powered = place(states, 8, Blocks.OAK_FENCE_GATE.defaultBlockState().setValue(FenceGateBlock.POWERED, true));
        var candle = place(states, 9, Blocks.CANDLE.defaultBlockState().setValue(AbstractCandleBlock.LIT, true));
        var bell = place(states, 10, Blocks.BELL.defaultBlockState());
        BellBlockEntity bellEntity = mock(BellBlockEntity.class);
        when(level.getBlockEntity(bell)).thenReturn(bellEntity);

        for (BlockPos pos : List.copyOf(states.keySet())) {
            WindBurstTriggers.trigger(level, pos);
        }
        assertTrue(states.get(button).getValue(ButtonBlock.POWERED));
        assertTrue(states.get(lever).getValue(LeverBlock.POWERED));
        assertTrue(states.get(door).getValue(DoorBlock.OPEN));
        assertFalse(states.get(upper).getValue(DoorBlock.OPEN));
        assertTrue(states.get(gate).getValue(FenceGateBlock.OPEN));
        assertTrue(states.get(trapdoor).getValue(TrapDoorBlock.OPEN));
        assertFalse(states.get(iron).getValue(TrapDoorBlock.OPEN));
        assertFalse(states.get(powered).getValue(FenceGateBlock.OPEN));
        assertFalse(states.get(candle).getValue(AbstractCandleBlock.LIT));
        verify(bellEntity).onHit(any(Direction.class));
    }

    private static BlockPos place(Map<BlockPos, BlockState> states, int x, BlockState state) {
        var pos = new BlockPos(x, 70, 0);
        states.put(pos, state);
        return pos;
    }

    private static ServerLevel level() throws Exception {
        ServerLevel level = mock(ServerLevel.class);
        var random = RandomSource.create(59);
        var randomField = Level.class.getDeclaredField("random");
        randomField.setAccessible(true);
        randomField.set(level, random);
        when(level.getRandom()).thenReturn(random);
        when(level.dimension()).thenReturn(Level.OVERWORLD);
        var damageSources = mock(DamageSources.class);
        when(level.damageSources()).thenReturn(damageSources);
        when(damageSources.explosion(isNull(), any())).thenReturn(DAMAGE);
        when(level.isInWorldBounds(any())).thenReturn(true);
        when(level.getBlockState(any())).thenReturn(Blocks.AIR.defaultBlockState());
        when(level.getFluidState(any())).thenReturn(Fluids.EMPTY.defaultFluidState());
        when(level.clip(any(ClipContext.class))).thenReturn(BlockHitResult.miss(CENTER, Direction.UP, BlockPos.ZERO));
        return level;
    }

    private static ServerPlayer player(ServerLevel level) {
        ServerPlayer player = mock(ServerPlayer.class);
        when(player.level()).thenReturn(level);
        when(player.getPersistentData()).thenReturn(new CompoundTag());
        when(player.getAbilities()).thenReturn(new Abilities());
        when(player.getItemBySlot(any())).thenReturn(ItemStack.EMPTY);
        when(player.getMainHandItem()).thenReturn(ItemStack.EMPTY);
        when(player.distanceToSqr(CENTER)).thenReturn(1.0);
        when(player.getX()).thenReturn(CENTER.x + 1.0);
        when(player.getY()).thenReturn(CENTER.y);
        when(player.getEyeY()).thenReturn(CENTER.y);
        when(player.getZ()).thenReturn(CENTER.z);
        when(player.getBoundingBox()).thenReturn(new AABB(11, 70, -9, 12, 72, -8));
        var delta = new AtomicReference<>(Vec3.ZERO);
        when(player.getDeltaMovement()).thenAnswer(call -> delta.get());
        doAnswer(call -> {
            delta.set(call.getArgument(0));
            return null;
        }).when(player).setDeltaMovement(any(Vec3.class));
        player.connection = mock(ServerGamePacketListenerImpl.class);
        return player;
    }
}
