import mods.thaumaturge.ProcessingData;

ProcessingData.edit(<resource:minecraft:fluid>, <resource:thaumaturge:spa_mixing>, {
    values: {
        "#minecraft:water": {
            amount: 700,
            additive: {tag: "forge:ingots/copper"},
            items: 2,
            result: "minecraft:lava"
        }
    }
});
ProcessingData.edit(<resource:minecraft:fluid>, <resource:thaumaturge:spa_placement>, {
    values: {"minecraft:water": {amount: 400}}
});
