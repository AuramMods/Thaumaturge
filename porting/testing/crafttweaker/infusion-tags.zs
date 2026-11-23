import mods.thaumaturge.AspectAmount;
import mods.thaumaturge.ProcessingData;

val infusion = <recipetype:thaumaturge:infusion>;
infusion.addRecipe("tagged_matrix", <item:minecraft:diamond>, <item:minecraft:packed_mud>,
    [<item:minecraft:nether_wart>], [
        AspectAmount.tag(<tag:thaumaturge/aspect:crafttweaker:flexible_cost>, 4),
        AspectAmount.tag(<tag:thaumaturge/aspect:crafttweaker:probe_aspects>, 2),
        new AspectAmount(<resource:thaumaturge:terra>, 2)
    ], 0);
println("TT_TAGGED_MATRIX=" + infusion.definition(<resource:crafttweaker:tagged_matrix>));

ProcessingData.edit(<resource:minecraft:block>, <resource:thaumaturge:infusion_matrix_upgrade>, {
    values: {"minecraft:blue_wool": {cost: 0.5, cycle_time: -4}}
});
