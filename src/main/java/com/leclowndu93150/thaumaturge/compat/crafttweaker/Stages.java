package com.leclowndu93150.thaumaturge.compat.crafttweaker;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.action.base.IRuntimeAction;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.tag.MCTag;
import com.leclowndu93150.thaumaturge.recipe.RecipeAccess;
import com.leclowndu93150.thaumaturge.stage.StageRules;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.thaumaturge.Stages")
public final class Stages {
    private Stages() {}

    @ZenCodeType.Method
    public static void requireFamily(ResourceLocation family, StageRequirement stages) {
        add(family, StageRules.Selector.family(), stages);
    }

    @ZenCodeType.Method
    public static void requireId(ResourceLocation family, ResourceLocation id, StageRequirement stages) {
        add(family, StageRules.Selector.id(id), stages);
    }

    @ZenCodeType.Method
    public static void requireEntry(
            ResourceLocation family, ResourceLocation registry, ResourceLocation id, StageRequirement stages) {
        add(family, StageRules.Selector.entry(registry, id), stages);
    }

    @ZenCodeType.Method
    public static void requireTag(ResourceLocation family, MCTag tag, StageRequirement stages) {
        add(family, StageRules.Selector.tag(tag.getTagKey()), stages);
    }

    private static void add(ResourceLocation family, StageRules.Selector selector, StageRequirement stages) {
        var manager = CraftTweakerAPI.getAccessibleElementsProvider().recipeManager();
        CraftTweakerAPI.apply(new AddRule(RecipeAccess.of(manager).stages(), family, selector, stages.requirement()));
    }

    private record AddRule(StageRules rules, ResourceLocation family, StageRules.Selector selector,
                           StageRules.Requirement requirement) implements IRuntimeAction {
        @Override
        public void apply() {
            rules.add(family, selector, requirement);
        }

        @Override
        public String describe() {
            return "Adding a GameStages rule for " + family;
        }

        @Override
        public String systemName() {
            return "thaumaturge";
        }
    }
}
