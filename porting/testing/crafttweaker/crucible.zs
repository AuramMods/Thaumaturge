import mods.thaumaturge.AspectAmount;

val crucible = <recipetype:thaumaturge:crucible>;
println("TT_CRUCIBLE_STOCK=" + crucible.definition(<resource:thaumaturge:crucible/nitor_yellow>));

crucible.addRecipe("crucible_probe", <item:minecraft:diamond> * 2, <item:minecraft:clay_ball>, [
    new AspectAmount(<resource:thaumaturge:aqua>, 3)
]);
println("TT_CRUCIBLE_ADD=" + crucible.definition(<resource:crafttweaker:crucible_probe>));

crucible.replaceRecipe(<resource:crafttweaker:crucible_probe>, {
    catalyst: {item: "minecraft:clay_ball"},
    aspects: [{aspect: "thaumaturge:terra", amount: 5}],
    result: {id: "minecraft:emerald", count: 2}
});
println("TT_CRUCIBLE_REPLACE=" + crucible.definition(<resource:crafttweaker:crucible_probe>));

crucible.removeByName("crafttweaker:crucible_probe");
println("TT_CRUCIBLE_REMOVED=" + (crucible.getRecipeByName("crafttweaker:crucible_probe") == null));

crucible.addJsonRecipe("crucible_probe", {
    catalyst: {item: "minecraft:sand"},
    aspects: [{aspect: "thaumaturge:ignis", amount: 7}],
    result: {id: "minecraft:gold_ingot", count: 1}
});
println("TT_CRUCIBLE_READD=" + crucible.definition(<resource:crafttweaker:crucible_probe>));
