import mods.thaumaturge.ProcessingData;
import mods.thaumaturge.StageRequirement;
import mods.thaumaturge.Stages;

ProcessingData.edit(<resource:thaumaturge:obelisk_site>, <resource:thaumaturge:eldritch_ritual>, {
    values: {"thaumaturge:dormant": {eyes: 2, vis: 6.0, channel_ticks: 3}}
});
Stages.requireFamily(<resource:thaumaturge:eldritch_eyes>, StageRequirement.allOf(["seat_eldritch_eyes"]));
Stages.requireEntry(<resource:thaumaturge:eldritch_ritual>, <resource:thaumaturge:obelisk_site>,
    <resource:thaumaturge:dormant>, StageRequirement.allOf(["open_eldritch_altar"]));
Stages.requireTag(<resource:thaumaturge:labyrinth_entry>, <tag:thaumaturge/labyrinth:crafttweaker:probe_labyrinth>,
    StageRequirement.allOf(["enter_eldritch_labyrinth"]));
println("TT_TAG_ENCOUNTER=" + <tag:thaumaturge/labyrinth_encounter:crafttweaker:probe_encounters>
    .contains(<resource:thaumaturge:warden>));
