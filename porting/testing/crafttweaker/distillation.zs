import mods.thaumaturge.ProcessingData;

ProcessingData.edit(<resource:minecraft:item>, <resource:thaumaturge:distillation>, {
    values: {"minecraft:echo_shard": {output: [{aspect: "thaumaturge:ignis", amount: 4}]}}
});
ProcessingData.edit(<resource:minecraft:item>, <resource:thaumaturge:advanced_distillation>, {
    values: {"minecraft:dead_bush": {output: [{aspect: "thaumaturge:aqua", amount: 6}]}}
});
ProcessingData.edit(<resource:minecraft:block>, <resource:thaumaturge:smelter_stats>, {
    values: {"thaumaturge:smelter_void": {smelt_interval: 15, efficiency: 1.0}}
});
ProcessingData.edit(<resource:minecraft:item>, <resource:thaumaturge:deconstruction>, {
    values: {"minecraft:echo_shard": {result: "thaumaturge:terra", points: 3}}
});
