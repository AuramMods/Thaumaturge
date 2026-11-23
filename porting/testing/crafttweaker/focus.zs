import mods.thaumaturge.ProcessingData;

ProcessingData.edit(<resource:minecraft:item>, <resource:thaumaturge:spell_focus>, {
    values: {"thaumaturge:focus_3": {complexity: 40, depth: 10, branches: 2, repeats: 2}}
});
ProcessingData.edit(<resource:minecraft:item>, <resource:thaumaturge:focus_inscription>, {
    values: {"thaumaturge:focus_3": {
        levels_multiplier: 2.0, crystals_multiplier: 0.5, vis_multiplier: 0.5, interval: 2, drain: 7.0
    }}
});
