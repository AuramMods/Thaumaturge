package com.leclowndu93150.thaumaturge.compat.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.tag.type.KnownTag;
import com.leclowndu93150.thaumaturge.api.recipe.DustTrigger;
import com.leclowndu93150.thaumaturge.content.recipe.dust.DustTriggerMultiblockRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.dust.DustTriggerSimpleRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.dust.DustTriggerTagRecipe;
import com.leclowndu93150.thaumaturge.registry.TTRecipeTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.thaumaturge.DustTriggerManager")
public enum DustTriggerManager implements ProcessingManager<DustTrigger> {
    INSTANCE;

    @Override
    public RecipeType<DustTrigger> getRecipeType() {
        return TTRecipeTypes.DUST_TRIGGER.get();
    }

    @ZenCodeType.Method
    public void addBlock(String name, IItemStack output, Block target, @ZenCodeType.Optional @ZenCodeType.Nullable ResearchRequirement research) {
        var json = Recipes.recipe(DustTriggerSimpleRecipe.SERIALIZER, research);
        json.addProperty("target", BuiltInRegistries.BLOCK.getKey(target).toString());
        json.add("result", Recipes.stack(output));
        Recipes.add(this, name, json);
    }

    @ZenCodeType.Method
    public void addBlockTag(String name, IItemStack output, KnownTag<Block> target, @ZenCodeType.Optional @ZenCodeType.Nullable ResearchRequirement research) {
        var json = Recipes.recipe(DustTriggerTagRecipe.SERIALIZER, research);
        json.addProperty("target_tag", target.id().toString());
        json.add("result", Recipes.stack(output));
        Recipes.add(this, name, json);
    }

    @ZenCodeType.Method
    public void addMultiblock(String name, IItemStack output, ResourceLocation blueprint,
            @ZenCodeType.Optional @ZenCodeType.Nullable ResearchRequirement research) {
        var json = Recipes.recipe(DustTriggerMultiblockRecipe.SERIALIZER, research);
        json.addProperty("blueprint", blueprint.toString());
        json.add("result", Recipes.stack(output));
        Recipes.add(this, name, json);
    }
}
