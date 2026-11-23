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

crucible.addRecipe("clay_from_sand", <item:minecraft:clay_ball> * 2, <item:minecraft:sand>, [
    new AspectAmount(<resource:thaumaturge:aqua>, 3),
    new AspectAmount(<resource:thaumaturge:terra>, 5)
]);
println("TT_DOC_CLAY=" + crucible.definition(<resource:crafttweaker:clay_from_sand>));

val nitor = <resource:thaumaturge:crucible/nitor_yellow>;
val stock = crucible.definition(nitor);
crucible.replaceRecipe(nitor, {
    catalyst: {tag: "forge:dusts/glowstone"},
    aspects: [
        {aspect: "thaumaturge:potentia", amount: 8},
        {aspect: "thaumaturge:ignis", amount: 8},
        {aspect: "thaumaturge:lux", amount: 8}
    ],
    result: {id: "thaumaturge:nitor_yellow", count: 1},
    research: {entry: "thaumaturge:unlock_alchemy", stage: 2}
});
println("TT_DOC_NITOR=" + crucible.definition(nitor));
crucible.removeByName("thaumaturge:crucible/nitor_yellow");
crucible.addJson("thaumaturge:crucible/nitor_yellow", stock);
