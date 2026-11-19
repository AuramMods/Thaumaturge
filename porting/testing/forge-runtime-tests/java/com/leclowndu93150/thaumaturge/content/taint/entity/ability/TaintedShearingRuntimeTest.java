package com.leclowndu93150.thaumaturge.content.taint.entity.ability;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.registry.TTItemTags;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagLoader;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.common.ToolActions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

class TaintedShearingRuntimeTest {
    @Test
    void nativeAndAddonShearingToolsUseTheSameInteractionAllowance(@TempDir Path pack) throws Exception {
        assertTrue(TaintedShearing.canShear(new ItemStack(Items.SHEARS)));
        assertFalse(TaintedShearing.canShear(new ItemStack(Items.STICK)));
        var actionOnly = mock(ItemStack.class);
        when(actionOnly.canPerformAction(ToolActions.SHEARS_HARVEST)).thenReturn(true);
        assertTrue(TaintedShearing.canShear(actionOnly));
        var tagOnly = mock(ItemStack.class);
        when(tagOnly.is(TTItemTags.SHEARS)).thenReturn(true);
        assertTrue(TaintedShearing.canShear(tagOnly));

        var shipping = Path.of(System.getProperty("thaumaturge.testResourceRoot"));
        var base = pack.resolve("thaumaturge");
        var tagPath = Path.of("data/c/tags/items/tools/shear.json");
        Files.createDirectories(base.resolve(tagPath).getParent());
        Files.copy(shipping.resolve(tagPath), base.resolve(tagPath));
        var addon = pack.resolve("addon");
        var forge = Files.createDirectories(addon.resolve("data/forge/tags/items"));
        Files.writeString(forge.resolve("shears.json"), "{\"values\":[\"minecraft:stick\"]}");
        var tags = new TagLoader<>(BuiltInRegistries.ITEM::getOptional, "tags/items");
        try (var resources = new MultiPackResourceManager(PackType.SERVER_DATA, List.of(
                new PathPackResources("thaumaturge", base, false), new PathPackResources("addon", addon, false)))) {
            assertEquals(Set.of(Items.SHEARS, Items.STICK),
                    Set.copyOf(tags.loadAndBuild(resources).get(TTItemTags.SHEARS.location())));
        }
    }

    @Test
    void nativeShearingDropsPurpleWoolPollutesAuraAndBreaksTheActualHandOnce() throws Exception {
        var level = mock(ServerLevel.class);
        var random = mock(RandomSource.class);
        when(level.getRandom()).thenReturn(random);
        when(random.nextInt(3)).thenReturn(2);
        var randomField = Level.class.getDeclaredField("random");
        randomField.setAccessible(true);
        randomField.set(level, random);
        var sheep = new Sheep(EntityType.SHEEP, level);
        sheep.setPos(4.5, 65.0, -2.5);
        var player = mock(Player.class);
        when(player.level()).thenReturn(level);
        when(player.getAbilities()).thenReturn(new Abilities());
        when(player.getRandom()).thenReturn(RandomSource.create(0));
        var tool = new ItemStack(Items.SHEARS);
        tool.setDamageValue(tool.getMaxDamage() - 1);

        try (var aura = mockStatic(AuraHelper.class)) {
            assertEquals(InteractionResult.SUCCESS, TaintedShearing.shear(player, InteractionHand.OFF_HAND, tool, sheep));
            assertEquals(InteractionResult.CONSUME, TaintedShearing.shear(player, InteractionHand.MAIN_HAND, tool, sheep));
            aura.verify(() -> AuraHelper.polluteAura(level, sheep.blockPosition(), 3.0F, true));
            aura.verifyNoMoreInteractions();
        }
        assertTrue(sheep.isSheared());
        assertTrue(tool.isEmpty());
        verify(player).broadcastBreakEvent(InteractionHand.OFF_HAND);
        verify(player, never()).broadcastBreakEvent(InteractionHand.MAIN_HAND);
        verify(level).playSound(null, sheep, SoundEvents.SHEEP_SHEAR, SoundSource.PLAYERS, 1.0F, 1.0F);
        verify(level).gameEvent(player, GameEvent.SHEAR, sheep.position());
        var dropped = ArgumentCaptor.forClass(ItemEntity.class);
        verify(level).addFreshEntity(dropped.capture());
        var drop = dropped.getValue();
        assertTrue(drop.getItem().is(Items.PURPLE_WOOL));
        assertEquals(3, drop.getItem().getCount());
        assertEquals(sheep.position().add(0.0, 1.0, 0.0), drop.position());
    }

    @Test
    void clientPredictionAndAnUnreadySheepConsumeWithoutSideEffects() {
        var clientSheep = new Sheep(EntityType.SHEEP, mock(Level.class));
        var level = mock(ServerLevel.class);
        var baby = new Sheep(EntityType.SHEEP, level);
        baby.setAge(-24000);
        var player = mock(Player.class);
        var tool = new ItemStack(Items.SHEARS);
        try (var aura = mockStatic(AuraHelper.class)) {
            assertEquals(InteractionResult.CONSUME, TaintedShearing.shear(player, InteractionHand.MAIN_HAND, tool, clientSheep));
            assertEquals(InteractionResult.CONSUME, TaintedShearing.shear(player, InteractionHand.MAIN_HAND, tool, baby));
            aura.verifyNoInteractions();
        }
        assertFalse(clientSheep.isSheared());
        assertFalse(baby.isSheared());
        assertEquals(0, tool.getDamageValue());
        verify(level, never()).addFreshEntity(any());
        verifyNoInteractions(player);
    }
}
