import mods.thaumaturge.ProcessingData;

craftingTable.addShapeless("pattern_milk", <item:minecraft:diamond> * 2, [<item:minecraft:milk_bucket>]);
furnace.addRecipe("infernal_probe", <item:minecraft:emerald> * 2, <item:minecraft:echo_shard>, 0.0f, 20);
<recipetype:thaumaturge:dust_trigger>.addMultiblock("infernal_test_frame",
    <item:thaumaturge:infernal_furnace>, <resource:crafttweaker:infernal_test_frame>);

ProcessingData.edit(<resource:minecraft:block>, <resource:thaumaturge:pattern_crafter>, {
    values: {"thaumaturge:pattern_crafter": {interval: 5, vis: 0.0}}
});
ProcessingData.edit(<resource:minecraft:item>, <resource:thaumaturge:infernal_bonus>, {
    values: {"minecraft:echo_shard": [{items: "minecraft:gold_nugget", count: 3, chance: 1.0}]}
});
