package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.infusion.InfusionEnchantmentRecipe;
import com.leclowndu93150.thaumaturge.content.infusion.InfusionRecipe;
import com.leclowndu93150.thaumaturge.content.infusion.InfusionRunicAugmentRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.SalisMundusRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.SimpleRecipeSerializer;
import com.leclowndu93150.thaumaturge.content.recipe.crucible.CrucibleRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.dust.DustTriggerMultiblockRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.dust.DustTriggerSimpleRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.dust.DustTriggerTagRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.label.LabelFilterRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.pearl.PrimordialPearlRepairRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneShapedCraftingRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneShapelessCraftingRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public final class TTRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, TTIds.MODID);

    public static final RegistryObject<RecipeSerializer<DustTriggerSimpleRecipe>>
            DUST_TRIGGER_SIMPLE =
                    RECIPE_SERIALIZERS.register("dust_trigger_simple", () -> DustTriggerSimpleRecipe.SERIALIZER);

    public static final RegistryObject<RecipeSerializer<DustTriggerTagRecipe>> DUST_TRIGGER_TAG =
            RECIPE_SERIALIZERS.register("dust_trigger_tag", () -> DustTriggerTagRecipe.SERIALIZER);

    public static final RegistryObject<RecipeSerializer<DustTriggerMultiblockRecipe>>
            DUST_TRIGGER_MULTIBLOCK = RECIPE_SERIALIZERS.register(
                    "dust_trigger_multiblock", () -> DustTriggerMultiblockRecipe.SERIALIZER);

    public static final RegistryObject<RecipeSerializer<InfusionRecipe>> INFUSION =
            RECIPE_SERIALIZERS.register("infusion", () -> InfusionRecipe.SERIALIZER);

    public static final RegistryObject<RecipeSerializer<InfusionEnchantmentRecipe>>
            INFUSION_ENCHANTMENT =
                    RECIPE_SERIALIZERS.register("infusion_enchantment", () -> InfusionEnchantmentRecipe.SERIALIZER);

    public static final RegistryObject<RecipeSerializer<InfusionRunicAugmentRecipe>>
            RUNIC_AUGMENT = RECIPE_SERIALIZERS.register("runic_augment", () -> InfusionRunicAugmentRecipe.SERIALIZER);

    public static final RegistryObject<RecipeSerializer<CrucibleRecipe>> CRUCIBLE =
            RECIPE_SERIALIZERS.register("crucible", () -> CrucibleRecipe.SERIALIZER);

    public static final RegistryObject<RecipeSerializer<ArcaneShapedCraftingRecipe>>
            ARCANE_SHAPED =
                    RECIPE_SERIALIZERS.register("arcane_workbench_shaped", () -> ArcaneShapedCraftingRecipe.SERIALIZER);

    public static final RegistryObject<RecipeSerializer<ArcaneShapelessCraftingRecipe>>
            ARCANE_SHAPELESS = RECIPE_SERIALIZERS.register(
                    "arcane_workbench_shapeless", () -> ArcaneShapelessCraftingRecipe.SERIALIZER);

    public static final RegistryObject<RecipeSerializer<SalisMundusRecipe>> SALIS_MUNDUS =
            RECIPE_SERIALIZERS.register(
                    "salis_mundus",
                    () -> new SimpleRecipeSerializer<>(SalisMundusRecipe.MAP_CODEC, SalisMundusRecipe.STREAM_CODEC));

    public static final RegistryObject<RecipeSerializer<LabelFilterRecipe>> LABEL_FILTER =
            RECIPE_SERIALIZERS.register("label_filter", () -> LabelFilterRecipe.SERIALIZER);

    public static final RegistryObject<RecipeSerializer<PrimordialPearlRepairRecipe>>
            PRIMORDIAL_PEARL_REPAIR = RECIPE_SERIALIZERS.register(
                    "primordial_pearl_repair", () -> PrimordialPearlRepairRecipe.SERIALIZER);

    private TTRecipeSerializers() {}

    public static void register(IEventBus modBus) {
        RECIPE_SERIALIZERS.register(modBus);
    }
}
