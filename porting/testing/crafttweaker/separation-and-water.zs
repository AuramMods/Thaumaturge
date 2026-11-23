import mods.thaumaturge.ProcessingData;
import mods.thaumaturge.StageRequirement;
import mods.thaumaturge.Stages;

ProcessingData.edit(<resource:thaumaturge:aspect>, <resource:thaumaturge:centrifugation>, {
    values: {"thaumaturge:terra": {result: "thaumaturge:ignis", ticks: 3}}
});
ProcessingData.edit(<resource:minecraft:block>, <resource:thaumaturge:everfull_urn>, {
    values: {"thaumaturge:everfull_urn": {
        max_vis: 0.5, water_per_vis: 400, interval: 1, push_amount: 20, cauldron_cost: 80
    }}
});

Stages.requireEntry(<resource:thaumaturge:centrifuge>, <resource:thaumaturge:aspect>, <resource:thaumaturge:terra>,
    StageRequirement.allOf(["separate_terra"]));
Stages.requireEntry(<resource:thaumaturge:everfull_urn>, <resource:minecraft:fluid>, <resource:minecraft:water>,
    StageRequirement.allOf(["conjure_water"]));
