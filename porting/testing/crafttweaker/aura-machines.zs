import mods.thaumaturge.ProcessingData;
import mods.thaumaturge.StageRequirement;
import mods.thaumaturge.Stages;

ProcessingData.edit(<resource:minecraft:block>, <resource:thaumaturge:condenser>, {
    values: {"thaumaturge:condenser": {essentia: 2, interval: 1, flux: 3.0, vitium: 2}}
});
ProcessingData.edit(<resource:minecraft:block>, <resource:thaumaturge:vis_generator>, {
    values: {"thaumaturge:vis_generator": {vis: 2.0, energy: 600, max_push: 12}}
});
ProcessingData.edit(<resource:minecraft:block>, <resource:thaumaturge:flux_scrubber>, {
    values: {"thaumaturge:flux_scrubber": {power: 3, removal: 2, charges_per_roll: 2, chance: 1.0, essentia: 2}}
});

Stages.requireEntry(<resource:thaumaturge:condenser>, <resource:thaumaturge:aspect>, <resource:thaumaturge:vitium>,
    StageRequirement.allOf(["operate_aura_machines"]));
Stages.requireId(<resource:thaumaturge:vis_generator>, <resource:thaumaturge:vis_generator>,
    StageRequirement.allOf(["operate_aura_machines"]));
Stages.requireFamily(<resource:thaumaturge:flux_scrubber>, StageRequirement.allOf(["operate_aura_machines"]));
