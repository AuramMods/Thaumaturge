import mods.thaumaturge.ProcessingData;

ProcessingData.edit(<resource:minecraft:item>, <resource:thaumaturge:base_aspects>, {
    values: {
        "minecraft:dirt": {
            replace: true,
            value: [{aspect: "thaumaturge:terra", amount: 71}]
        }
    }
});
