package com.leclowndu93150.thaumaturge.content.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.items.InvHelper;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.ForgeMod;
import org.junit.jupiter.api.Test;

class PlayerInteractionRuntimeTest {
    @Test
    void blockReachMeasuresTheNearestFaceAndUsesAStrictLimit() {
        Player player = player(mock(Level.class));
        BlockPos block = new BlockPos(3, 64, -2);
        double padding = 0.5;
        assertEquals(4.5, player.getBlockReach());
        moveEyesTo(player, 8.875);
        assertTrue(PlayerReach.canInteractWithBlock(player, block, padding));

        moveEyesTo(player, 9.0);
        assertFalse(PlayerReach.canInteractWithBlock(player, block, padding));
        player.getAbilities().instabuild = true;
        assertEquals(5.0, player.getBlockReach());
        assertTrue(PlayerReach.canInteractWithBlock(player, block, padding));

        moveEyesTo(player, 11.0);
        assertFalse(PlayerReach.canInteractWithBlock(player, block, padding));
        player.getAttribute(ForgeMod.BLOCK_REACH.get()).setBaseValue(7.0);
        assertTrue(PlayerReach.canInteractWithBlock(player, block, padding));
    }

    @Test
    void entityReachUsesThePhysicalBoundsAndRejectsRemovedTargets() {
        Player player = player(mock(Level.class));
        Entity target = mock(Entity.class);
        when(target.getBoundingBox()).thenReturn(new AABB(new BlockPos(3, 64, -2)));
        when(target.getPickRadius()).thenReturn(1.0F);
        assertEquals(3.0, PlayerReach.entityRange(player));

        moveEyesTo(player, 6.875);
        assertTrue(PlayerReach.canInteractWithEntity(player, target, 0.0));
        moveEyesTo(player, 7.0);
        assertFalse(PlayerReach.canInteractWithEntity(player, target, 0.0));
        player.getAbilities().instabuild = true;
        assertEquals(5.0, PlayerReach.entityRange(player));
        assertTrue(PlayerReach.canInteractWithEntity(player, target, 0.0));
        moveEyesTo(player, 9.0);
        assertFalse(PlayerReach.canInteractWithEntity(player, target, 0.0));

        player.getAttribute(ForgeMod.ENTITY_REACH.get()).setBaseValue(4.0);
        assertTrue(PlayerReach.canInteractWithEntity(player, target, 0.0));
        when(target.isRemoved()).thenReturn(true);
        assertFalse(PlayerReach.canInteractWithEntity(player, target, 0.0));
    }

    @Test
    void materialUsePreservesCreativeStacksAndShrinksForOtherUsers() {
        Level level = mock(Level.class);
        Player player = player(level);
        ItemStack held = new ItemStack(Items.COPPER_INGOT, 5);
        held.getOrCreateTag().putString("fixture", "kept");
        var originalTag = held.getTag();
        player.getAbilities().instabuild = true;
        InvHelper.consume(held, 2, player);
        assertEquals(5, held.getCount());

        player.getAbilities().instabuild = false;
        InvHelper.consume(held, 2, player);
        assertEquals(3, held.getCount());
        InvHelper.consume(held, 1, null);
        InvHelper.consume(held, 1, new Zombie(EntityType.ZOMBIE, level));
        assertEquals(1, held.getCount());
        assertSame(originalTag, held.getTag());
    }

    private static Player player(Level level) {
        return new Player(level, BlockPos.ZERO, 0.0F, new GameProfile(UUID.randomUUID(), "PortReach")) {
            @Override public boolean isSpectator() { return false; }
            @Override public boolean isCreative() { return getAbilities().instabuild; }
        };
    }

    private static void moveEyesTo(Player player, double x) {
        player.setPos(x, 64.5 - player.getEyeHeight(), -1.5);
    }
}
