package com.leclowndu93150.thaumaturge.content.item;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.content.essentia.jar.BlockEntityJar;
import com.leclowndu93150.thaumaturge.itemdata.ItemData;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.serialization.RegistryContext;
import com.leclowndu93150.thaumaturge.testing.AspectRegistryFixture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class PhialItemTest {
    private Level level;
    private Player player;
    private Inventory inventory;
    private BlockEntityJar jar;
    private UseOnContext context;
    private Holder<IAspect> aspect;
    private RegistryAccess registries;
    private ItemStack held;

    @BeforeEach
    void setUp() {
        level = mock(ServerLevel.class);
        player = mock(Player.class);
        inventory = mock(Inventory.class);
        jar = mock(BlockEntityJar.class);
        context = mock(UseOnContext.class);
        registries = AspectRegistryFixture.create(TTAspects.ORDO);
        aspect = registries.registryOrThrow(IAspect.REGISTRY_KEY).getHolderOrThrow(TTAspects.ORDO);
        held = new ItemStack(TTItems.PHIAL.get());
        when(level.registryAccess()).thenReturn(registries);
        when(context.getLevel()).thenReturn(level);
        when(context.getClickedPos()).thenReturn(BlockPos.ZERO);
        when(context.getPlayer()).thenReturn(player);
        when(context.getHand()).thenReturn(InteractionHand.MAIN_HAND);
        when(context.getItemInHand()).thenReturn(held);
        when(level.getBlockEntity(BlockPos.ZERO)).thenReturn(jar);
        when(player.getAbilities()).thenReturn(new Abilities());
        when(player.getInventory()).thenReturn(inventory);
        when(inventory.add(any(ItemStack.class))).thenReturn(true);
        when(jar.heldAspect(Direction.UP)).thenReturn(aspect);
        when(jar.heldAmount(Direction.UP)).thenReturn(10);
    }

    @Test
    void completeWithdrawalConsumesOneEmptyPhialAndReturnsActualContents() {
        when(jar.extract(aspect, 10, Direction.UP, false)).thenReturn(10);
        assertEquals(InteractionResult.SUCCESS,
                RegistryContext.withRegistries(registries, () -> TTItems.PHIAL.get().useOn(context)));
        assertTrue(held.isEmpty());
        verify(player).setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        ArgumentCaptor<ItemStack> output = ArgumentCaptor.forClass(ItemStack.class);
        verify(inventory).add(output.capture());
        var contents = RegistryContext.withRegistries(
                registries, () -> ItemData.get(output.getValue(), TTDataComponents.ASPECTS.get()));
        assertEquals(10, contents.totalAmount());
        assertSame(aspect, contents.entries().get(0).aspect());
    }

    @Test
    void partialWithdrawalIsRefundedWithoutGivingOrConsumingAnItem() {
        when(jar.extract(aspect, 10, Direction.UP, false)).thenReturn(6);
        assertEquals(InteractionResult.PASS, TTItems.PHIAL.get().useOn(context));
        verify(jar).insert(aspect, 6, Direction.UP, false);
        assertEquals(1, held.getCount());
        verifyNoInteractions(inventory);
        verify(player, never()).setItemInHand(any(), any());
    }

    @Test
    void creativeWithdrawalDoesNotConsumeHeldPhial() {
        player.getAbilities().instabuild = true;
        when(jar.extract(aspect, 10, Direction.UP, false)).thenReturn(10);
        assertEquals(InteractionResult.SUCCESS,
                RegistryContext.withRegistries(registries, () -> TTItems.PHIAL.get().useOn(context)));
        assertEquals(1, held.getCount());
        verify(inventory).add(any(ItemStack.class));
        verify(player, never()).setItemInHand(any(), any());
    }

    @Test
    void clientPreviewUsesSimulationAndDoesNotPredictAnImpossibleTransfer() {
        when(level.isClientSide()).thenReturn(true);
        when(jar.extract(aspect, 10, Direction.UP, true)).thenReturn(0);
        assertEquals(InteractionResult.PASS, TTItems.PHIAL.get().useOn(context));
        verify(player, never()).swing(any());
        verify(jar, never()).extract(aspect, 10, Direction.UP, false);
        verifyNoInteractions(inventory);
        when(jar.extract(aspect, 10, Direction.UP, true)).thenReturn(10);
        assertEquals(InteractionResult.SUCCESS, TTItems.PHIAL.get().useOn(context));
        verify(player).swing(InteractionHand.MAIN_HAND);
        assertEquals(1, held.getCount());
    }
}
