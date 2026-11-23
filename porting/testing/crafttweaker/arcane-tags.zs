import crafttweaker.api.ingredient.IIngredient;
import mods.thaumaturge.AspectAmount;

val arcane = <recipetype:thaumaturge:arcane_workbench>;
arcane.addShapeless("tagged_arcane", <item:minecraft:emerald>, 1, [
    AspectAmount.tag(<tag:thaumaturge/aspect:crafttweaker:flexible_cost>, 2),
    AspectAmount.tag(<tag:thaumaturge/aspect:crafttweaker:arcane_primal>, 3),
    new AspectAmount(<resource:thaumaturge:terra>, 1)
], [<item:minecraft:amethyst_shard>]);

arcane.addShaped("tagged_arcane_shaped", <item:minecraft:lapis_lazuli>, 1, [
    AspectAmount.tag(<tag:thaumaturge/aspect:crafttweaker:arcane_primal>, 1)
], ["AB"], {"A": <item:minecraft:heart_of_the_sea> as IIngredient, "B": <item:minecraft:stick> as IIngredient});
println("TT_TAGGED_ARCANE=" + arcane.definition(<resource:crafttweaker:tagged_arcane>));
