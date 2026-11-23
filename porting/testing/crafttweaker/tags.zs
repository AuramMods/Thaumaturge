#loader tags

val aspects = <tag:thaumaturge/aspect:crafttweaker:probe_aspects>;
val blueprints = <tag:thaumaturge/blueprint:crafttweaker:staged_blueprints>;
blueprints.addId(<resource:thaumaturge:infusion_altar_ancient>);
aspects.addId(<resource:thaumaturge:aqua>, <resource:thaumaturge:terra>);
aspects.removeId(<resource:thaumaturge:terra>);

val flexible = <tag:thaumaturge/aspect:crafttweaker:flexible_cost>;
flexible.addId(<resource:thaumaturge:aqua>, <resource:thaumaturge:terra>);
<tag:thaumaturge/aspect:crafttweaker:arcane_primal>.addId(<resource:thaumaturge:aqua>, <resource:thaumaturge:motus>);

val materials = <tag:thaumaturge/golem_material:crafttweaker:probe_materials>;
materials.addId(<resource:thaumaturge:wood>, <resource:thaumaturge:iron>);
materials.removeId(<resource:thaumaturge:wood>);

<tag:thaumaturge/research_entry:crafttweaker:paid_research>.addId(<resource:crafttweaker:paid_stage>);
<tag:thaumaturge/scan:crafttweaker:staged_scans>.addId(<resource:crafttweaker:honeycomb>);
<tag:thaumaturge/spell_part:crafttweaker:staged_spell_parts>.addId(<resource:crafttweaker:tagged_projectile>);
