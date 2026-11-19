package com.leclowndu93150.thaumaturge.content.equipment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.registry.TTItemTags;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ToolTierRuntimeTest {
    private static final Map<Tier, PickaxeItem> TOOLS = new LinkedHashMap<>();
    private Map<TagKey<Block>, List<Holder<Block>>> blockTags;
    private Map<TagKey<Item>, List<Holder<Item>>> itemTags;

    @BeforeAll
    static void registerActualNativeToolsWithEveryProductionTier() throws ReflectiveOperationException {
        // The normal Forge common-setup/reload phase computes this order.
        var recalculate = TierSortingRegistry.class.getDeclaredMethod("recalculateItemTiers");
        recalculate.setAccessible(true);
        // Load the production definitions before recalculating the native graph.
        var production = List.of(TTMaterials.TOOL_THAUMIUM, TTMaterials.TOOL_VOID, TTMaterials.TOOL_ELEMENTAL,
                TTMaterials.TOOL_PRIMAL_VOID, TTMaterials.TOOL_CRIMSON_VOID);
        recalculate.invoke(null);
        var registry = (ForgeRegistry<?>) ForgeRegistries.ITEMS;
        var view = (net.minecraft.core.MappedRegistry<?>) BuiltInRegistries.ITEM;
        view.unfreeze();
        registry.unfreeze();
        try {
            for (Tier tier : production) {
                var tool = tier == TTMaterials.TOOL_ELEMENTAL
                        ? new ElementalPickaxeItem(new Item.Properties().rarity(Rarity.RARE))
                        : new PickaxeItem(tier, 1, -2.8F, new Item.Properties());
                ForgeRegistries.ITEMS.register(new ResourceLocation("port_tool_tier", TierSortingRegistry.getName(tier).getPath()), tool);
                TOOLS.put(tier, tool);
            }
        } finally { registry.freeze(); view.freeze(); }
    }

    @BeforeEach
    void rememberActualTagBindings() {
        blockTags = new LinkedHashMap<>();
        itemTags = new LinkedHashMap<>();
        BuiltInRegistries.BLOCK.getTags().forEach(pair -> blockTags.put(pair.getFirst(), pair.getSecond().stream().toList()));
        BuiltInRegistries.ITEM.getTags().forEach(pair -> itemTags.put(pair.getFirst(), pair.getSecond().stream().toList()));
    }

    @AfterEach
    void restoreActualTagBindings() {
        BuiltInRegistries.BLOCK.resetTags();
        BuiltInRegistries.BLOCK.bindTags(blockTags);
        BuiltInRegistries.ITEM.resetTags();
        BuiltInRegistries.ITEM.bindTags(itemTags);
    }

    private void bind(List<Holder<Block>> diamondIncorrect, List<Holder<Block>> netheriteIncorrect) {
        var tags = new LinkedHashMap<>(blockTags);
        tags.put(BlockTags.MINEABLE_WITH_PICKAXE, List.of(Blocks.STONE.builtInRegistryHolder(), Blocks.OBSIDIAN.builtInRegistryHolder()));
        tags.put(BlockTags.NEEDS_DIAMOND_TOOL, List.of(Blocks.OBSIDIAN.builtInRegistryHolder()));
        tags.put(ToolTierData.INCORRECT_FOR_DIAMOND_TOOL, diamondIncorrect);
        tags.put(ToolTierData.INCORRECT_FOR_NETHERITE_TOOL, netheriteIncorrect);
        BuiltInRegistries.BLOCK.bindTags(tags);
    }

    @Test
    void everyProductionTierKeepsItsStatsAndNativeIdentity() {
        int[] uses = {500, 150, 1500, 500, 200};
        float[] speed = {7.0F, 8.0F, 9.0F, 8.0F, 8.0F};
        float[] damage = {2.5F, 3.0F, 3.0F, 4.0F, 3.5F};
        int[] enchantability = {22, 10, 18, 20, 20};
        int index = 0;
        for (var entry : TOOLS.entrySet()) {
            var tier = entry.getKey();
            assertTrue(TierSortingRegistry.isTierSorted(tier));
            assertSame(tier, TierSortingRegistry.byName(TierSortingRegistry.getName(tier)));
            assertEquals("thaumaturge", TierSortingRegistry.getName(tier).getNamespace());
            assertEquals(uses[index], tier.getUses());
            assertEquals(uses[index], entry.getValue().getMaxDamage());
            assertEquals(speed[index], tier.getSpeed());
            assertEquals(damage[index], tier.getAttackDamageBonus());
            assertEquals(enchantability[index++], tier.getEnchantmentValue());
        }
    }

    @Test
    void nativeDiggerDispatchUsesSourceDropTagsAndKeepsItsToolCategoryCheck() {
        bind(List.of(), List.of());
        for (var tool : TOOLS.values()) {
            assertTrue(tool.isCorrectToolForDrops(Blocks.STONE.defaultBlockState()));
            assertTrue(tool.isCorrectToolForDrops(Blocks.OBSIDIAN.defaultBlockState()));
            assertFalse(tool.isCorrectToolForDrops(Blocks.OAK_LOG.defaultBlockState()));
        }
    }

    @Test
    void changedTagBindingsImmediatelyChangeNativeHarvestDecisions() {
        bind(List.of(Blocks.STONE.builtInRegistryHolder()), List.of(Blocks.OBSIDIAN.builtInRegistryHolder()));
        assertFalse(TOOLS.get(TTMaterials.TOOL_THAUMIUM).isCorrectToolForDrops(Blocks.STONE.defaultBlockState()));
        assertTrue(TOOLS.get(TTMaterials.TOOL_THAUMIUM).isCorrectToolForDrops(Blocks.OBSIDIAN.defaultBlockState()));
        assertTrue(TOOLS.get(TTMaterials.TOOL_VOID).isCorrectToolForDrops(Blocks.STONE.defaultBlockState()));
        assertFalse(TOOLS.get(TTMaterials.TOOL_VOID).isCorrectToolForDrops(Blocks.OBSIDIAN.defaultBlockState()));
        bind(List.of(Blocks.OBSIDIAN.builtInRegistryHolder()), List.of());
        assertTrue(TOOLS.get(TTMaterials.TOOL_THAUMIUM).isCorrectToolForDrops(Blocks.STONE.defaultBlockState()));
        assertFalse(TOOLS.get(TTMaterials.TOOL_THAUMIUM).isCorrectToolForDrops(Blocks.OBSIDIAN.defaultBlockState()));
        assertTrue(TOOLS.get(TTMaterials.TOOL_VOID).isCorrectToolForDrops(Blocks.OBSIDIAN.defaultBlockState()));
        assertFalse(TierSortingRegistry.isCorrectTierForDrops(Tiers.WOOD, Blocks.OBSIDIAN.defaultBlockState()));
        assertTrue(TierSortingRegistry.isCorrectTierForDrops(Tiers.DIAMOND, Blocks.OBSIDIAN.defaultBlockState()));
    }

    @Test
    void sourceRepairSuppliersUseCurrentNativeItemTagBindings() {
        var tags = new LinkedHashMap<>(itemTags);
        tags.put(TTItemTags.INGOTS_THAUMIUM, List.of(Items.IRON_INGOT.builtInRegistryHolder()));
        tags.put(TTItemTags.INGOTS_VOID_METAL, List.of(Items.NETHERITE_INGOT.builtInRegistryHolder()));
        BuiltInRegistries.ITEM.bindTags(tags);
        assertTrue(TTMaterials.TOOL_THAUMIUM.getRepairIngredient().test(new ItemStack(Items.IRON_INGOT)));
        assertFalse(TTMaterials.TOOL_THAUMIUM.getRepairIngredient().test(new ItemStack(Items.NETHERITE_INGOT)));
        assertTrue(TTMaterials.TOOL_VOID.getRepairIngredient().test(new ItemStack(Items.NETHERITE_INGOT)));
    }

    @Test
    void elementalPickaxeSuppliesTheSourceNativeAttributesOnlyInTheMainHand() {
        var tool = assertInstanceOf(ElementalPickaxeItem.class, TOOLS.get(TTMaterials.TOOL_ELEMENTAL));
        var mainHand = tool.getDefaultAttributeModifiers(EquipmentSlot.MAINHAND);
        assertEquals(4.0, mainHand.get(Attributes.ATTACK_DAMAGE).iterator().next().getAmount());
        assertEquals(-2.8F, mainHand.get(Attributes.ATTACK_SPEED).iterator().next().getAmount());
        assertTrue(tool.getDefaultAttributeModifiers(EquipmentSlot.OFFHAND).isEmpty());
        assertEquals(Rarity.RARE, tool.getRarity(new ItemStack(tool)));
    }

    @Test
    void elementalPickaxeUsesServerIgnitionAndRespectsPlayerProtection() {
        var tool = assertInstanceOf(ElementalPickaxeItem.class, TOOLS.get(TTMaterials.TOOL_ELEMENTAL));
        var stack = new ItemStack(tool);
        var attacker = mock(Player.class);
        var target = mock(Player.class);
        var mob = mock(Entity.class);
        var level = mock(Level.class);
        when(attacker.level()).thenReturn(level);

        assertFalse(tool.onLeftClickEntity(stack, attacker, target));
        verify(target, never()).setSecondsOnFire(anyInt());
        when(attacker.canHarmPlayer(target)).thenReturn(true);
        assertFalse(tool.onLeftClickEntity(stack, attacker, target));
        verify(target).setSecondsOnFire(2);
        when(level.isClientSide()).thenReturn(true);
        assertFalse(tool.onLeftClickEntity(stack, attacker, mob));
        verify(mob, never()).setSecondsOnFire(anyInt());
        when(level.isClientSide()).thenReturn(false);
        assertFalse(tool.onLeftClickEntity(stack, attacker, mob));
        verify(mob).setSecondsOnFire(2);
    }
}
