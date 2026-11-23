#loader tags

val aspects = <tag:thaumaturge/aspect:crafttweaker:probe_aspects>;
val blueprints = <tag:thaumaturge/blueprint:crafttweaker:staged_blueprints>;
blueprints.addId(<resource:thaumaturge:infusion_altar_ancient>);
aspects.addId(<resource:thaumaturge:aqua>, <resource:thaumaturge:terra>);
aspects.removeId(<resource:thaumaturge:terra>);

val materials = <tag:thaumaturge/golem_material:crafttweaker:probe_materials>;
materials.addId(<resource:thaumaturge:wood>, <resource:thaumaturge:iron>);
materials.removeId(<resource:thaumaturge:wood>);
