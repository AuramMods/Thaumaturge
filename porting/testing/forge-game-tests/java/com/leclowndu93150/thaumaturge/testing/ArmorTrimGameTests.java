package com.leclowndu93150.thaumaturge.testing;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.items.trim.TTTrimMaterials;
import com.leclowndu93150.thaumaturge.api.items.trim.TTTrimPatterns;
import com.leclowndu93150.thaumaturge.registry.TTItemTags;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimMaterials;
import net.minecraft.world.item.armortrim.TrimPattern;
import net.minecraft.world.item.armortrim.TrimPatterns;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TTIds.MODID)
@PrefixGameTestTemplate(false)
public final class ArmorTrimGameTests {
    private ArmorTrimGameTests() {}

    @GameTest(template = "essentia_room")
    public static void vanillaSmithingTrimsModeledArmor(GameTestHelper test) {
        checkModeledArmor(test, Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE,
                Items.COPPER_INGOT, TrimPatterns.SENTRY, TrimMaterials.COPPER);
        test.succeed();
    }

    @GameTest(template = "essentia_room")
    public static void modSmithingPersistsTrimsOnModeledArmor(GameTestHelper test) {
        checkModeledArmor(test, TTItems.ARCANE_ARMOR_TRIM_SMITHING_TEMPLATE.get(),
                TTItems.INGOT_THAUMIUM.get(), TTTrimPatterns.ARCANE, TTTrimMaterials.THAUMIUM);
        checkModeledArmor(test, TTItems.OCCULT_ARMOR_TRIM_SMITHING_TEMPLATE.get(),
                TTItems.INGOT_THAUMIUM.get(), TTTrimPatterns.OCCULT, TTTrimMaterials.THAUMIUM);
        test.succeed();
    }

    private static void checkModeledArmor(GameTestHelper test, Item template, Item material,
            ResourceKey<TrimPattern> pattern, ResourceKey<TrimMaterial> trimMaterial) {
        var level = test.getLevel();
        var registries = level.registryAccess();
        var modeledArmor = BuiltInRegistries.ITEM.getTagOrEmpty(TTItemTags.MODELLED_TRIMMABLE_ARMOR);
        test.assertTrue(TTItems.FORTRESS_CHEST.get().builtInRegistryHolder().is(TTItemTags.MODELLED_TRIMMABLE_ARMOR),
                "The loaded modeled armor tag must include Fortress armor");
        for (var holder : modeledArmor) {
            var armor = new ItemStack(holder.value());
            armor.setDamageValue(7);
            var name = Component.literal("Modeled trim test");
            armor.setHoverName(name);
            var input = new SimpleContainer(new ItemStack(template), armor, new ItemStack(material));
            var recipe = level.getRecipeManager().getRecipeFor(RecipeType.SMITHING, input, level);
            String description = pattern.location() + " on " + BuiltInRegistries.ITEM.getKey(holder.value());
            test.assertTrue(recipe.isPresent(), "No smithing recipe matches " + description);
            var result = recipe.orElseThrow().assemble(input, registries);
            test.assertTrue(result.is(holder.value()) && result.getCount() == 1 && result.getDamageValue() == 7
                            && result.getHoverName().equals(name),
                    "Smithing changed the armor or its data for " + description);
            var trim = ArmorTrim.getTrim(registries, ItemStack.of(result.save(new CompoundTag())));
            test.assertTrue(trim.isPresent() && trim.get().pattern().is(pattern) && trim.get().material().is(trimMaterial),
                    "The saved result has no matching trim for " + description);
            test.assertTrue(ArmorTrim.getTrim(registries, armor).isEmpty(),
                    "Smithing modified the input armor for " + description);
        }
    }
}
