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
Stages.requireTag(<resource:thaumaturge:spa>, <tag:items:forge:ingots/copper>,
    StageRequirement.allOf(["spa_mix"]));
Stages.requireId(<resource:thaumaturge:spa>, <resource:thaumaturge:spa_placement>,
    StageRequirement.allOf(["spa_placement"]));
Stages.requireId(<resource:thaumaturge:pattern_crafter>, <resource:crafttweaker:pattern_milk>,
    StageRequirement.allOf(["pattern_work"]));
Stages.requireId(<resource:thaumaturge:infernal_furnace>, <resource:crafttweaker:infernal_probe>,
    StageRequirement.allOf(["infernal_work"]));
Stages.requireId(<resource:thaumaturge:infusion>, <resource:crafttweaker:tagged_matrix>,
    StageRequirement.allOf(["tagged_infusion"]));
Stages.requireId(<resource:thaumaturge:arcane_workbench>, <resource:crafttweaker:tagged_arcane>,
    StageRequirement.allOf(["tagged_workbench"]));
Stages.requireEntry(<resource:thaumaturge:alchemical_furnace>, <resource:thaumaturge:aspect>,
    <resource:thaumaturge:ignis>, StageRequirement.allOf(["distill_ignis"]));
Stages.requireId(<resource:thaumaturge:advanced_alchemical_furnace>, <resource:minecraft:dead_bush>,
    StageRequirement.allOf(["distill_advanced"]));
Stages.requireId(<resource:thaumaturge:deconstruction_table>, <resource:minecraft:echo_shard>,
    StageRequirement.allOf(["deconstruct_echo"]));
Stages.requireTag(<resource:thaumaturge:golem_assembly>, <tag:thaumaturge/golem_material:crafttweaker:probe_materials>,
    StageRequirement.allOf(["assemble_iron_golem"]));
Stages.requireTag(<resource:thaumaturge:golem_deployment>, <tag:thaumaturge/golem_material:crafttweaker:probe_materials>,
    StageRequirement.allOf(["deploy_iron_golem"]));
Stages.requireEntry(<resource:thaumaturge:golem_seal>, <resource:minecraft:block>, <resource:minecraft:amethyst_block>,
    StageRequirement.allOf(["guard_advanced_seal"]));
Stages.requireTag(<resource:thaumaturge:focus_inscription>, <tag:thaumaturge/spell_part:crafttweaker:staged_spell_parts>,
    StageRequirement.allOf(["inscribe_test_spell"]));
Stages.requireTag(<resource:thaumaturge:spell_cast>, <tag:thaumaturge/spell_part:crafttweaker:staged_spell_parts>,
    StageRequirement.allOf(["cast_test_spell"]));
Stages.requireEntry(<resource:thaumaturge:potion_sprayer>, <resource:minecraft:potion>, <resource:minecraft:swiftness>,
    StageRequirement.allOf(["spray_swiftness"]));
