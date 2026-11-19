package com.leclowndu93150.thaumaturge.content.device.bore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.items.InfusionEnchantment;
import com.leclowndu93150.thaumaturge.content.aura.node.SourceIgnoringClipContext;
import com.leclowndu93150.thaumaturge.content.equipment.InfusionEnchantmentHelper;
import com.leclowndu93150.thaumaturge.content.equipment.InfusionEnchantments;
import com.leclowndu93150.thaumaturge.content.spell.world.SpellRayTrace;
import com.leclowndu93150.thaumaturge.itemdata.ItemData;
import com.leclowndu93150.thaumaturge.itemdata.ItemDataRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.common.ToolActions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ArcaneBoreRuntimeTest {
    private static final BlockPos POS = new BlockPos(3, 64, -5);
    private static final Vec3 FROM = Vec3.atCenterOf(POS);
    private static final Vec3 TO = FROM.add(0, 0, 7);

    @Test
    void pickaxeActionsAndSavedInfusionLevelsDriveBoreToolStats() {
        Level level = mock(Level.class);
        var catalog = new ItemDataRegistry("thaumaturge");
        var key = catalog.registerComponentType(InfusionEnchantments.ITEM_DATA).get();
        assertSame(key, catalog.get(InfusionEnchantments.ITEM_DATA.id()));
        ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
        assertTrue(tool.canPerformAction(ToolActions.PICKAXE_DIG));
        assertTrue(ArcaneBoreTool.valid(tool));
        assertFalse(ArcaneBoreTool.isPickaxe(new ItemStack(Items.DIAMOND_AXE)));
        assertFalse(ArcaneBoreTool.isPickaxe(ItemStack.EMPTY));

        InfusionEnchantmentHelper.add(tool, InfusionEnchantment.DESTRUCTIVE, 1);
        InfusionEnchantmentHelper.add(tool, InfusionEnchantment.BURROWING, 1);
        InfusionEnchantmentHelper.add(tool, InfusionEnchantment.SOUNDING, 3);
        InfusionEnchantmentHelper.add(tool, InfusionEnchantment.REFINING, 2);
        tool.enchant(Enchantments.BLOCK_FORTUNE, 2);
        tool.enchant(Enchantments.BLOCK_EFFICIENCY, 2);
        tool.enchant(Enchantments.SILK_TOUCH, 1);
        var restored = ItemStack.of(tool.save(new CompoundTag()));
        assertEquals(InfusionEnchantmentHelper.get(tool), ItemData.get(restored, key));
        assertEquals(5, ArcaneBoreTool.digRadius(restored));
        assertEquals(56, ArcaneBoreTool.digDepth(restored));
        assertEquals(3, ArcaneBoreTool.fortune(level, restored));
        assertEquals(2, ArcaneBoreTool.refining(restored));
        BlockState stone = Blocks.STONE.defaultBlockState();
        assertEquals((int) (restored.getDestroySpeed(stone) / 2) + 2, ArcaneBoreTool.digSpeed(level, restored, stone));
        assertTrue(ArcaneBoreTool.silkTouch(level, restored));
        restored.setDamageValue(restored.getMaxDamage() - 1);
        assertFalse(ArcaneBoreTool.valid(restored));
        assertEquals(0, ArcaneBoreTool.fortune(level, restored));
        assertEquals(0, ArcaneBoreTool.digSpeed(level, restored, stone));
    }

    @Test
    void stationaryToolWearUsesNativeUnbreakingAndConsumesABrokenStack() {
        ItemStack tool = new ItemStack(Items.IRON_PICKAXE);
        tool.setDamageValue(tool.getMaxDamage() - 2);
        assertFalse(ArcaneBoreTool.damageStationaryTool(tool, RandomSource.create(7)));
        int almostBroken = tool.getMaxDamage() - 1;
        assertEquals(almostBroken, tool.getDamageValue());
        tool.enchant(Enchantments.UNBREAKING, 1);
        RandomSource random = mock(RandomSource.class);
        when(random.nextInt(2)).thenReturn(1, 0);
        assertFalse(ArcaneBoreTool.damageStationaryTool(tool, random));
        assertEquals(almostBroken, tool.getDamageValue());
        assertTrue(ArcaneBoreTool.damageStationaryTool(tool, random));
        assertTrue(tool.isEmpty());
    }

    @Test
    void weaponInfusionChecksNativeMainHandAttackModifiers() {
        assertTrue(InfusionEnchantmentHelper.canApply(new ItemStack(Items.IRON_SWORD), InfusionEnchantment.ARCING));
        assertFalse(InfusionEnchantmentHelper.canApply(new ItemStack(Items.IRON_BOOTS), InfusionEnchantment.ARCING));
        assertFalse(InfusionEnchantmentHelper.canApply(ItemStack.EMPTY, InfusionEnchantment.ARCING));
    }

    @Test
    void boreClipUsesTheAuthoredCollisionContextForEveryBlockAndIgnoresFluids() {
        var host = mock(ArcaneBoreHost.class);
        var level = mock(Level.class);
        var state = mock(BlockState.class);
        var collision = mock(CollisionContext.class);
        when(host.boreCollisionContext()).thenReturn(collision);
        when(host.boreClip(FROM, TO)).thenCallRealMethod();
        when(state.getCollisionShape(level, POS, collision)).thenReturn(Shapes.block());
        when(state.getCollisionShape(level, POS.south(), collision)).thenReturn(Shapes.empty());

        var clip = host.boreClip(FROM, TO);
        assertEquals(FROM, clip.getFrom());
        assertEquals(TO, clip.getTo());
        assertSame(Shapes.block(), clip.getBlockShape(state, level, POS));
        assertSame(Shapes.empty(), clip.getBlockShape(state, level, POS.south()));
        assertSame(Shapes.empty(), clip.getFluidShape(Fluids.WATER.defaultFluidState(), level, POS));
        verify(host).boreCollisionContext();
    }

    @Test
    void spellAndSourceIgnoringClipsUseNativeEntityAndEmptyContexts() {
        var level = mock(Level.class);
        var entity = new Zombie(EntityType.ZOMBIE, level);
        var spellClip = ArgumentCaptor.forClass(ClipContext.class);
        SpellRayTrace.clipBlocks(level, entity, FROM, TO);
        verify(level).clip(spellClip.capture());
        var state = mock(BlockState.class);
        var collision = ArgumentCaptor.forClass(CollisionContext.class);
        spellClip.getValue().getBlockShape(state, level, POS);
        verify(state).getShape(org.mockito.ArgumentMatchers.eq(level), org.mockito.ArgumentMatchers.eq(POS), collision.capture());
        assertSame(entity, assertInstanceOf(EntityCollisionContext.class, collision.getValue()).getEntity());

        var ignoring = new SourceIgnoringClipContext(POS, FROM, TO, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE);
        when(state.getCollisionShape(level, POS.south(), CollisionContext.empty())).thenReturn(Shapes.block());
        assertSame(Shapes.empty(), ignoring.getBlockShape(state, level, POS));
        assertSame(Shapes.block(), ignoring.getBlockShape(state, level, POS.south()));
    }
}
