import crafttweaker.api.tag.type.KnownTag;
import mods.thaumaturge.tags.Aspect;
import mods.thaumaturge.tags.GolemMaterial;

val aspects as KnownTag<Aspect> = <tag:thaumaturge/aspect:crafttweaker:probe_aspects>;
val materials as KnownTag<GolemMaterial> = <tag:thaumaturge/golem_material:crafttweaker:probe_materials>;
println("TT_TAG_ASPECT=" + (aspects.contains(<resource:thaumaturge:aqua>) && !aspects.contains(<resource:thaumaturge:terra>)));
println("TT_TAG_MATERIAL=" + (materials.contains(<resource:thaumaturge:iron>) && !materials.contains(<resource:thaumaturge:wood>)));
