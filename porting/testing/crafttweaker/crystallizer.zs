import mods.thaumaturge.ProcessingData;

ProcessingData.edit(<resource:thaumaturge:aspect>, <resource:thaumaturge:crystallization>, {
    values: {
        "#crafttweaker:probe_aspects": {
            essentia: 3,
            progress: 1,
            result: {id: "minecraft:amethyst_shard", count: 2}
        },
        "thaumaturge:ignis": {enabled: false}
    }
});
