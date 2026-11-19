package com.leclowndu93150.thaumaturge.api.research.scan;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.google.gson.JsonParser;
import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.research.scan.CarriedTraits;
import com.mojang.serialization.JsonOps;
import java.util.LinkedHashMap;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import org.junit.jupiter.api.Test;

class ScanningTargetsRuntimeTest {
    private static final BlockPos SCANNED_POS = new BlockPos(1, 64, 1);

    @Test
    void nativeScanCodecMatchesDirectAndTaggedEntitiesBlocksAndStackForms() {
        var types = BuiltInRegistries.ENTITY_TYPE;
        var tag = TagKey.create(Registries.ENTITY_TYPE, TTIds.rl("port_scan_target"));
        var previous = new LinkedHashMap<TagKey<EntityType<?>>, List<Holder<EntityType<?>>>>();
        types.getTags().forEach(pair -> previous.put(pair.getFirst(), pair.getSecond().stream().toList()));
        var tags = new LinkedHashMap<>(previous);
        tags.put(tag, List.of(EntityType.WITCH.builtInRegistryHolder()));
        types.bindTags(tags);
        try {
            var registryAccess = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
            var ops = RegistryOps.create(JsonOps.INSTANCE, registryAccess);
            var tagged = ScanEntry.CODEC.parse(ops, JsonParser.parseString("""
                    {"key":"thaumaturge:port_scan_tagged","entities":"#thaumaturge:port_scan_target"}
                    """)).getOrThrow(false, message -> fail(message));
            var direct = ScanEntry.CODEC.parse(ops, JsonParser.parseString("""
                    {"key":"thaumaturge:port_scan_direct","entities":["minecraft:sheep"],
                     "blocks":["minecraft:stone"],"items":["minecraft:book"]}
                    """)).getOrThrow(false, message -> fail(message));

            var witch = mock(Entity.class);
            doReturn(EntityType.WITCH).when(witch).getType();
            var sheep = mock(Entity.class);
            doReturn(EntityType.SHEEP).when(sheep).getType();
            assertTrue(tagged.matches(null, ScanTarget.entity(witch)));
            assertFalse(tagged.matches(null, ScanTarget.entity(sheep)));
            assertTrue(direct.matches(null, ScanTarget.entity(sheep)));
            assertFalse(direct.matches(null, ScanTarget.entity(witch)));

            var player = mock(Player.class);
            var level = mock(Level.class);
            when(player.level()).thenReturn(level);
            when(level.getBlockState(SCANNED_POS)).thenReturn(Blocks.STONE.defaultBlockState());
            assertTrue(direct.matches(player, ScanTarget.block(SCANNED_POS)));
            assertTrue(direct.matches(player, ScanTarget.stack(new ItemStack(Items.STONE))));
            assertFalse(direct.matches(player, ScanTarget.stack(new ItemStack(Items.DIRT))));

            var dropped = mock(ItemEntity.class);
            when(dropped.getItem()).thenReturn(new ItemStack(Items.BOOK));
            assertTrue(direct.matches(player, ScanTarget.entity(dropped)));
            assertFalse(direct.matches(player, ScanTarget.sky()));
        } finally {
            types.bindTags(previous);
        }
    }

    @Test
    void inventoryScanDeduplicatesHandlersAndStacksButKeepsDifferentItemTags() {
        var player = mock(Player.class);
        var level = mock(Level.class);
        var entity = mock(BlockEntity.class);
        when(player.level()).thenReturn(level);
        when(level.hasChunk(0, 0)).thenReturn(true);
        when(level.getBlockEntity(SCANNED_POS)).thenReturn(entity);

        var first = new ItemStackHandler(2);
        var paper = new ItemStack(Items.PAPER, 5);
        first.setStackInSlot(0, paper);
        var second = new ItemStackHandler(2);
        second.setStackInSlot(0, new ItemStack(Items.PAPER, 1));
        var marked = new ItemStack(Items.PAPER);
        marked.setHoverName(Component.literal("Marked paper"));
        second.setStackInSlot(1, marked);
        var firstCap = LazyOptional.of(() -> first);
        var secondCap = LazyOptional.of(() -> second);
        when(entity.getCapability(same(ForgeCapabilities.ITEM_HANDLER), nullable(Direction.class)))
                .thenReturn(LazyOptional.empty());
        when(entity.getCapability(ForgeCapabilities.ITEM_HANDLER, null)).thenReturn(firstCap.cast());
        when(entity.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.EAST)).thenReturn(firstCap.cast());
        when(entity.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.NORTH)).thenReturn(secondCap.cast());

        var target = ScanTarget.block(SCANNED_POS);
        assertTrue(ScanInventories.hasInventory(player, target));
        var found = ScanInventories.contents(player, target);
        assertEquals(2, found.size());
        assertEquals(5, found.get(0).getCount());
        assertEquals(marked.getHoverName(), found.get(1).getHoverName());
        assertNotSame(paper, found.get(0));
        assertNotSame(marked, found.get(1));
        marked.resetHoverName();
        paper.shrink(1);
        assertEquals(5, found.get(0).getCount());
        assertTrue(found.get(1).hasCustomHoverName());
    }

    @Test
    void inventoryScanFallsBackToNativeContainersAndSkipsUnloadedChunks() {
        var player = mock(Player.class);
        var level = mock(Level.class);
        when(player.level()).thenReturn(level);
        var block = mock(BlockEntity.class, withSettings().extraInterfaces(Container.class));
        var container = (Container) block;
        when(block.getCapability(same(ForgeCapabilities.ITEM_HANDLER), nullable(Direction.class)))
                .thenReturn(LazyOptional.empty());
        when(container.getContainerSize()).thenReturn(1);
        var stack = new ItemStack(Items.IRON_INGOT);
        when(container.getItem(0)).thenReturn(stack);
        when(level.getBlockEntity(SCANNED_POS)).thenReturn(block);
        var target = ScanTarget.block(SCANNED_POS);

        assertFalse(ScanInventories.hasInventory(player, target));
        assertTrue(ScanInventories.contents(player, target).isEmpty());
        verify(level, never()).getBlockEntity(SCANNED_POS);

        when(level.hasChunk(0, 0)).thenReturn(true);
        assertTrue(ScanInventories.hasInventory(player, target));
        var found = ScanInventories.contents(player, target);
        assertEquals(1, found.size());
        assertTrue(found.get(0).is(Items.IRON_INGOT));
        assertNotSame(stack, found.get(0));
    }

    @Test
    void carriedEnchantmentScanReadsEnchantmentsOnGearAndStoredBooks() {
        var key = BuiltInRegistries.ENCHANTMENT.getResourceKey(Enchantments.SHARPNESS).orElseThrow();
        var matches = CarriedTraits.enchantment(key);
        var book = EnchantedBookItem.createForEnchantment(new EnchantmentInstance(Enchantments.SHARPNESS, 2));
        var blade = new ItemStack(Items.IRON_SWORD);
        blade.enchant(Enchantments.SHARPNESS, 2);
        assertTrue(matches.test(null, ScanTarget.stack(book)));
        assertTrue(matches.test(null, ScanTarget.stack(blade)));
        assertFalse(matches.test(null, ScanTarget.stack(new ItemStack(Items.BOOK))));
        assertFalse(matches.test(null, ScanTarget.stack(ItemStack.EMPTY)));
        var missing = ResourceKey.create(Registries.ENCHANTMENT, TTIds.rl("port_unknown_enchantment"));
        assertFalse(CarriedTraits.enchantment(missing).test(null, ScanTarget.stack(blade)));
    }
}
