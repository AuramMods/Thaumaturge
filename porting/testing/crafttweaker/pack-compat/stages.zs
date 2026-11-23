import mods.itemstages.ItemStages;
import mods.recipestages.Recipes;

Recipes.setRecipeStage("qa_recipe_stage", <resource:minecraft:stick>);
ItemStages.restrict(<item:minecraft:amethyst_shard>, "qa_item_stage")
    .preventPickup(false).preventInventory(false).hiddenName("Staged amethyst");
println("TT_PACK_STAGES_LOADED");
