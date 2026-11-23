import mods.thaumaturge.StageRequirement;
import mods.thaumaturge.Stages;

Stages.requireFamily(<resource:thaumaturge:thaumatorium>, StageRequirement.allOf(["automate_alchemy"]));
Stages.requireId(<resource:thaumaturge:arcane_workbench>, <resource:crafttweaker:test_shapeless>,
    StageRequirement.allOf(["arcane_basics", "craft_initiate"]));
Stages.requireId(<resource:thaumaturge:crucible>, <resource:crafttweaker:crucible_probe>,
    StageRequirement.anyOf(["alchemy", "craft_initiate"]));
Stages.requireEntry(<resource:thaumaturge:dust_trigger>, <resource:minecraft:block>, <resource:minecraft:iron_block>,
    StageRequirement.anyOf(["transform_iron", "craft_initiate"]));
Stages.requireTag(<resource:thaumaturge:dust_trigger>, <tag:blocks:minecraft:gold_ores>,
    StageRequirement.allOf(["transform_gold"]));
Stages.requireTag(<resource:thaumaturge:dust_trigger>, <tag:thaumaturge/blueprint:crafttweaker:staged_blueprints>,
    StageRequirement.allOf(["construct_ancient_altar"]));
Stages.requireTag(<resource:thaumaturge:research>, <tag:thaumaturge/research_entry:crafttweaker:paid_research>,
    StageRequirement.allOf(["study"]));
Stages.requireTag(<resource:thaumaturge:scan>, <tag:thaumaturge/scan:crafttweaker:staged_scans>,
    StageRequirement.anyOf(["observe", "study"]));
Stages.requireId(<resource:thaumaturge:scan>, <resource:crafttweaker:scanned_honeycomb>,
    StageRequirement.anyOf(["observe", "study"]));
Stages.requireTag(<resource:thaumaturge:essentia_crystalizer>, <tag:thaumaturge/aspect:crafttweaker:probe_aspects>,
    StageRequirement.allOf(["crystallize"]));
