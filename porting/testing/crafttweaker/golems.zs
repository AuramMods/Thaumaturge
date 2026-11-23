import mods.thaumaturge.ProcessingData;

ProcessingData.edit(<resource:thaumaturge:golem_material>, <resource:thaumaturge:golem_assembly>, {
    values: {"#crafttweaker:probe_materials": {
        components: [{id: "minecraft:copper_ingot", count: 3}],
        machina_delta: 2
    }}
});
