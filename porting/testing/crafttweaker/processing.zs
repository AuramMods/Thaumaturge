import crafttweaker.api.ingredient.IIngredient;
import mods.thaumaturge.AspectAmount;
import mods.thaumaturge.ResearchRequirement;

val earth = new AspectAmount(<resource:thaumaturge:terra>, 4);
val water = AspectAmount.tag(<tag:thaumaturge/aspect:crafttweaker:probe_aspects>, 4);
val sights = ResearchRequirement.stage(<resource:thaumaturge:first_steps>, 0);
val arcane = <recipetype:thaumaturge:arcane_workbench>;
arcane.addShaped("test_shaped", <item:minecraft:stone> * 2, 25, [earth], [" I", "S "],
    {"I": <item:minecraft:iron_ingot> as IIngredient, "S": <item:minecraft:stick> as IIngredient}, sights);
arcane.addShapeless("test_shapeless", <item:minecraft:gravel>, 15, [earth],
    [<item:minecraft:flint>, <item:minecraft:dirt>]);
println("TT_PROCESS_SHAPED=" + arcane.definition(<resource:crafttweaker:test_shaped>));
println("TT_PROCESS_SHAPELESS=" + arcane.definition(<resource:crafttweaker:test_shapeless>));

val infusion = <recipetype:thaumaturge:infusion>;
infusion.addRecipe("test_infusion", <item:minecraft:gold_ingot>, <item:minecraft:copper_ingot>,
    [<item:minecraft:gold_nugget>, <item:minecraft:gold_nugget>], [earth], 12);
infusion.addCatalystPatch("test_patch", {"thaumaturge:fortress_mask": 2}, <item:thaumaturge:fortress_helm>,
    [<item:minecraft:gold_ingot>], [earth], 8);
infusion.addJson("overlapping_items", "{\"catalyst\":{\"item\":\"minecraft:clay_ball\"},"
    + "\"components\":[[{\"item\":\"minecraft:iron_ingot\"},{\"item\":\"minecraft:copper_ingot\"}],"
    + "{\"item\":\"minecraft:iron_ingot\"}],\"aspects\":[{\"aspect\":\"thaumaturge:terra\",\"amount\":4}],"
    + "\"result\":{\"id\":\"minecraft:redstone\"},\"instability\":0}");
println("TT_PROCESS_INFUSION=" + infusion.definition(<resource:crafttweaker:test_infusion>));
println("TT_PROCESS_PATCH=" + infusion.definition(<resource:crafttweaker:test_patch>));

val enchants = <recipetype:thaumaturge:infusion_enchantment>;
enchants.addRecipe("test_enchantment", "collector", <item:minecraft:diamond_pickaxe>,
    [<item:minecraft:apple>], [water]);
println("TT_PROCESS_ENCHANT=" + enchants.definition(<resource:crafttweaker:test_enchantment>));

val runic = <recipetype:thaumaturge:runic_augment>;
runic.addRecipe("test_runic", <item:minecraft:diamond_chestplate>,
    [<item:minecraft:leather>], <item:minecraft:iron_ingot>, [water], sights.negated());
println("TT_PROCESS_RUNIC=" + runic.definition(<resource:crafttweaker:test_runic>));

val dust = <recipetype:thaumaturge:dust_trigger>;
dust.addBlock("test_dust_block", <item:minecraft:gold_block>, <block:minecraft:iron_block>);
dust.addBlockTag("test_dust_tag", <item:minecraft:iron_block>, <tag:blocks:minecraft:gold_ores>);
dust.addMultiblock("test_dust_blueprint", <item:thaumaturge:golem_builder>, <resource:thaumaturge:infusion_altar_ancient>);
println("TT_PROCESS_DUST_BLOCK=" + dust.definition(<resource:crafttweaker:test_dust_block>));
println("TT_PROCESS_DUST_TAG=" + dust.definition(<resource:crafttweaker:test_dust_tag>));
println("TT_PROCESS_DUST_BLUEPRINT=" + dust.definition(<resource:crafttweaker:test_dust_blueprint>));
