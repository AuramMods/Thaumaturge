package com.leclowndu93150.thaumaturge.content.taint;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.aurammods.thaumaturge.testing.runtime.ForgePortTestRunner;
import com.leclowndu93150.thaumaturge.api.items.MiningSpeedProvider;
import com.leclowndu93150.thaumaturge.compat.NamedAttributeModifiers;
import com.leclowndu93150.thaumaturge.registry.TTAttributes;
import com.leclowndu93150.thaumaturge.registry.TTBlockTags;
import com.leclowndu93150.thaumaturge.registry.TTItemTags;
import com.mojang.authlib.GameProfile;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.junit.jupiter.api.Test;

class TaintMiningRuntimeTest {
    @Test
    void nativeMiningUsesTheEquippedEfficiencyAttributeAndRemovesTheEnchantmentOnSwap() throws Exception {
        var attribute = TTAttributes.MINING_EFFICIENCY.get();
        assertEquals(ResourceLocation.withDefaultNamespace("player.mining_efficiency"), ForgeRegistries.ATTRIBUTES.getKey(attribute));
        assertTrue(attribute.isClientSyncable());
        assertEquals(0.0, attribute.getDefaultValue());
        assertEquals(1024.0, attribute.sanitizeValue(2000.0));

        Consumer<ItemAttributeModifierEvent> listener = TTAttributes::onItemAttributeModifier;
        MinecraftForge.EVENT_BUS.addListener(listener);
        try (var installed = installPlayerAttributes()) {
            var world = mock(ServerLevel.class);
            when(world.getChunkSource()).thenReturn(mock(ServerChunkCache.class));
            var player = new ProbePlayer(world);
            var sword = new ItemStack(Items.IRON_SWORD);
            var plainSword = new ItemStack(Items.IRON_SWORD);
            sword.enchant(Enchantments.BLOCK_EFFICIENCY, 2);
            var efficiency = player.getAttribute(attribute);
            assertNotNull(efficiency);
            efficiency.setBaseValue(2.0);
            efficiency.addTransientModifier(NamedAttributeModifiers.create(
                    ResourceLocation.fromNamespaceAndPath("port_test", "efficiency_total"),
                    0.5, AttributeModifier.Operation.MULTIPLY_TOTAL));

            assertTrue(sword.getAttributeModifiers(EquipmentSlot.OFFHAND).get(attribute).isEmpty());
            player.getInventory().items.set(player.getInventory().selected, sword);
            refreshEquipment(player);
            assertEquals(10.5, efficiency.getValue());
            assertEquals(25.5F, player.getDigSpeed(Blocks.COBWEB.defaultBlockState(), BlockPos.ZERO));
            assertEquals(1.0F, player.getDigSpeed(Blocks.STONE.defaultBlockState(), BlockPos.ZERO));

            player.getInventory().items.set(player.getInventory().selected, plainSword);
            refreshEquipment(player);
            assertEquals(3.0, efficiency.getValue());
            assertEquals(18.0F, player.getDigSpeed(Blocks.COBWEB.defaultBlockState(), BlockPos.ZERO));
        } finally {
            MinecraftForge.EVENT_BUS.unregister(listener);
        }
    }

    @Test
    void forgeBreakSpeedUsesTheBestToolRuleAndTheCurrentEventSpeed() {
        var player = mock(Player.class);
        when(player.getAttributeValue(TTAttributes.MINING_EFFICIENCY.get())).thenReturn(5.0);
        var taint = mock(BlockState.class);
        when(taint.is(TTBlockTags.TAINT_BLOCKS)).thenReturn(true);
        Consumer<PlayerEvent.BreakSpeed> listener = TaintToolEvents::onBreakSpeed;
        MinecraftForge.EVENT_BUS.addListener(listener);
        try {
            var hoe = tagged(new ItemStack(Items.DIAMOND_HOE));
            when(player.getMainHandItem()).thenReturn(hoe);
            assertEquals(130.0F, breakSpeed(player, taint));
            assertEquals(2.5F, breakSpeed(player, Blocks.STONE.defaultBlockState()));

            var addonStack = mock(ItemStack.class);
            var addonTool = mock(Item.class, withSettings().extraInterfaces(MiningSpeedProvider.class));
            when(addonStack.getItem()).thenReturn(addonTool);
            when(addonStack.is(TTItemTags.TAINT_TOOLS)).thenReturn(true);
            when(((MiningSpeedProvider) addonTool).bestMiningSpeed(addonStack)).thenReturn(14.0F);
            when(player.getMainHandItem()).thenReturn(addonStack);
            assertEquals(190.0F, breakSpeed(player, taint));

            when(player.getMainHandItem()).thenReturn(new ItemStack(Items.DIAMOND_HOE));
            assertEquals(2.5F, breakSpeed(player, taint));
            var stick = tagged(new ItemStack(Items.STICK));
            when(player.getMainHandItem()).thenReturn(stick);
            assertEquals(2.5F, breakSpeed(player, taint));
        } finally {
            MinecraftForge.EVENT_BUS.unregister(listener);
        }
    }

    @Test
    void addonTagsCanOptNativeSwordsShearsAndTridentsIntoTheBonus() {
        var player = mock(Player.class);
        var taint = mock(BlockState.class);
        when(taint.is(TTBlockTags.TAINT_BLOCKS)).thenReturn(true);
        Consumer<PlayerEvent.BreakSpeed> listener = TaintToolEvents::onBreakSpeed;
        MinecraftForge.EVENT_BUS.addListener(listener);
        try {
            var sword = tagged(new ItemStack(Items.IRON_SWORD));
            var shears = tagged(new ItemStack(Items.SHEARS));
            var trident = tagged(new ItemStack(Items.TRIDENT));
            when(player.getMainHandItem()).thenReturn(sword);
            assertEquals(150.0F, breakSpeed(player, taint));
            when(player.getMainHandItem()).thenReturn(shears);
            assertEquals(150.0F, breakSpeed(player, taint));
            when(player.getMainHandItem()).thenReturn(trident);
            assertEquals(10.0F, breakSpeed(player, taint));
        } finally {
            MinecraftForge.EVENT_BUS.unregister(listener);
        }
    }

    private static ItemStack tagged(ItemStack real) {
        var stack = spy(real);
        doReturn(true).when(stack).is(TTItemTags.TAINT_TOOLS);
        return stack;
    }

    private static float breakSpeed(Player player, BlockState state) {
        var event = new PlayerEvent.BreakSpeed(player, state, 2.0F, BlockPos.ZERO);
        event.setNewSpeed(2.5F);
        assertFalse(MinecraftForge.EVENT_BUS.post(event));
        return event.getNewSpeed();
    }

    private static void refreshEquipment(Player player) throws ReflectiveOperationException {
        var method = LivingEntity.class.getDeclaredMethod("detectEquipmentUpdates");
        method.setAccessible(true);
        method.invoke(player);
    }

    private static AutoCloseable installPlayerAttributes() throws Exception {
        var builders = new LinkedHashMap<EntityType<? extends LivingEntity>, AttributeSupplier.Builder>();
        ForgePortTestRunner.postModEvent(new EntityAttributeModificationEvent(builders));
        var supplier = new AttributeSupplier.Builder(DefaultAttributes.getSupplier(EntityType.PLAYER));
        supplier.combine(builders.get(EntityType.PLAYER));
        var field = ForgeHooks.class.getDeclaredField("FORGE_ATTRIBUTES");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        var definitions = (Map<EntityType<? extends LivingEntity>, AttributeSupplier>) field.get(null);
        var original = definitions.put(EntityType.PLAYER, supplier.build());
        return () -> {
            if (original == null) definitions.remove(EntityType.PLAYER);
            else definitions.put(EntityType.PLAYER, original);
        };
    }

    private static final class ProbePlayer extends Player {
        ProbePlayer(ServerLevel level) {
            super(level, BlockPos.ZERO, 0.0F, new GameProfile(UUID.randomUUID(), "PortMining"));
        }

        @Override public boolean onGround() { return true; }
        @Override public boolean isSpectator() { return false; }
        @Override public boolean isCreative() { return false; }
    }
}
