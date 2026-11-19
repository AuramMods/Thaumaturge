package com.leclowndu93150.thaumaturge.content.equipment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Answers.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.registry.TTItemTags;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class ItemMagnetTest {
    @Test
    void pullsAlongNormalizedOffsetAddsLiftAndClampsEachAxis() {
        Vec3 velocity = new Vec3(0.1, 0.0, 0.0);
        Vec3 position = new Vec3(3.0, 4.0, 0.0);

        Vec3 result = ItemMagnet.pull(velocity, position, Vec3.ZERO);

        assertEquals(-0.08, result.x, 1.0E-12);
        assertEquals(-0.14, result.y, 1.0E-12);
        assertEquals(0.0, result.z, 1.0E-12);
        Vec3 capped = ItemMagnet.pull(new Vec3(-0.25, -0.25, 0.0), new Vec3(1.0, 0.0, 0.0), Vec3.ZERO);
        assertEquals(-0.25, capped.x, 1.0E-12);
        assertEquals(-0.15, capped.y, 1.0E-12);
    }

    @Test
    void zeroDistanceReturnsVelocityWithoutNaN() {
        Vec3 velocity = new Vec3(0.1, -0.2, 0.25);

        Vec3 result = ItemMagnet.pull(velocity, new Vec3(2.0, 3.0, 4.0), new Vec3(2.0, 3.0, 4.0));

        assertEquals(velocity, result);
    }

    @Test
    void registeredElementalAxeRetainsNativeToolMaterialProperties() {
        ElementalAxeItem axe = TTItems.ELEMENTAL_AXE.get();
        ItemStack stack = new ItemStack(axe);
        var axeMineable = mock(BlockState.class);
        when(axeMineable.is(BlockTags.MINEABLE_WITH_AXE)).thenReturn(true);
        assertEquals(9.0F, stack.getDestroySpeed(axeMineable));
        assertTrue(stack.isCorrectToolForDrops(axeMineable));

        when(axeMineable.is(ToolTierData.INCORRECT_FOR_DIAMOND_TOOL)).thenReturn(true);
        assertFalse(stack.isCorrectToolForDrops(axeMineable));
        assertEquals(9.0F, stack.getDestroySpeed(axeMineable));
        assertEquals(1500, stack.getMaxDamage());
        assertEquals(1, stack.getMaxStackSize());
        assertEquals(18, axe.getEnchantmentValue());
        assertTrue(axe.canDisableShield(stack, ItemStack.EMPTY, null, null));
    }

    @Test
    void registeredElementalAxeUsesItsThaumiumRepairIngredient() {
        ElementalAxeItem axe = TTItems.ELEMENTAL_AXE.get();
        ItemStack stack = new ItemStack(axe);
        Ingredient thaumiumIngot = Ingredient.of(TTItems.INGOT_THAUMIUM.get());

        try (MockedStatic<Ingredient> ingredients = mockStatic(Ingredient.class, CALLS_REAL_METHODS)) {
            ingredients.when(() -> Ingredient.of(TTItemTags.INGOTS_THAUMIUM)).thenReturn(thaumiumIngot);

            assertTrue(axe.isValidRepairItem(stack, new ItemStack(TTItems.INGOT_THAUMIUM.get())));
            assertFalse(axe.isValidRepairItem(stack, new ItemStack(Blocks.DIRT.asItem())));
        }
    }

    @Test
    void registeredAxeSuccessfulHitDamagesItByTwo() {
        ElementalAxeItem axe = TTItems.ELEMENTAL_AXE.get();
        ItemStack stack = new ItemStack(axe);
        ServerLevel level = mock(ServerLevel.class);
        Player attacker = mock(Player.class);
        LivingEntity target = mock(LivingEntity.class);
        when(attacker.level()).thenReturn(level);
        when(attacker.getAbilities()).thenReturn(new Abilities());
        when(attacker.getRandom()).thenReturn(RandomSource.create(0));

        stack.hurtEnemy(target, attacker);

        assertEquals(2, stack.getDamageValue());
    }
}
