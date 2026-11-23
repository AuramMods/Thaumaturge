import mods.thaumaturge.AspectAmount;

val crucible = <recipetype:thaumaturge:crucible>;
crucible.addRecipe("tagged_cost", <item:minecraft:diamond>, <item:minecraft:iron_nugget>, [
    AspectAmount.tag(<tag:thaumaturge/aspect:crafttweaker:flexible_cost>, 2),
    AspectAmount.tag(<tag:thaumaturge/aspect:crafttweaker:probe_aspects>, 3),
    new AspectAmount(<resource:thaumaturge:terra>, 1)
]);
println("TT_TAGGED_COST=" + crucible.definition(<resource:crafttweaker:tagged_cost>));
crucible.addRecipe("empty_tag_cost", <item:minecraft:diamond>, <item:minecraft:gold_nugget>, [
    new AspectAmount(<resource:thaumaturge:terra>, 1),
    AspectAmount.tag(<tag:thaumaturge/aspect:crafttweaker:empty_cost>, 1)
]);
