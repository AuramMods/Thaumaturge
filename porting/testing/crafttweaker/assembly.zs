import mods.thaumaturge.StageRequirement;
import mods.thaumaturge.Stages;

val arcane = <recipetype:thaumaturge:arcane_workbench>;
val wand = <resource:thaumaturge:wand/assembly/gold_wood>;
println("TT_WAND_EDIT=" + arcane.replaceIngredient(wand, <item:minecraft:stick>, <item:minecraft:blaze_rod>));
println("TT_WAND_DEFINITION=" + arcane.definition(wand));

val dust = <recipetype:thaumaturge:dust_trigger>;
dust.addMultiblock("rotated_blueprint", <item:minecraft:barrel>, <resource:crafttweaker:rotated_frame>);
Stages.requireTag(<resource:thaumaturge:dust_trigger>, <tag:thaumaturge/blueprint:crafttweaker:probe_rotated_frames>,
    StageRequirement.allOf(["build_rotated_frame"]));
