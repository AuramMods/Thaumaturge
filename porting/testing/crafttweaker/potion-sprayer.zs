import mods.thaumaturge.ProcessingData;

brewing.addRecipe(<item:minecraft:potion>.withTag({Potion: "minecraft:luck"}),
    <item:minecraft:diamond>, <item:minecraft:potion>.withTag({Potion: "minecraft:water"}));

ProcessingData.edit(<resource:minecraft:potion>, <resource:thaumaturge:potion_sprayer>, {
    values: {"minecraft:swiftness": {essentia: [{aspect: "thaumaturge:aqua", amount: 3}], max_charges: 1}}
});
