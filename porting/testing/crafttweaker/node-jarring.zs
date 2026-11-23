import mods.thaumaturge.ProcessingData;
import mods.thaumaturge.StageRequirement;
import mods.thaumaturge.Stages;

ProcessingData.edit(<resource:minecraft:block>, <resource:thaumaturge:node_jarring>, {
    values: {"thaumaturge:node": {vis_per_primal: 2, degrade_chance: 0.0}}
});
Stages.requireEntry(<resource:thaumaturge:node_jarring>, <resource:minecraft:block>, <resource:thaumaturge:jar_node>,
    StageRequirement.allOf(["jar_aura_nodes"]));
