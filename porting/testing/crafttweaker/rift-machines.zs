import mods.thaumaturge.ProcessingData;
import mods.thaumaturge.StageRequirement;
import mods.thaumaturge.Stages;

ProcessingData.edit(<resource:minecraft:block>, <resource:thaumaturge:void_siphon>, {
    values: {"thaumaturge:void_siphon": {
        progress: 8, interval: 1, stability_scale: 3.75, shrink_chance: 0,
        result: {id: "minecraft:echo_shard", count: 2}
    }}
});
ProcessingData.edit(<resource:minecraft:block>, <resource:thaumaturge:stabilizer>, {
    values: {"thaumaturge:stabilizer": {reach: 5}}
});
Stages.requireEntry(<resource:thaumaturge:void_siphon>, <resource:minecraft:item>, <resource:minecraft:echo_shard>,
    StageRequirement.allOf(["siphon_rifts"]));
