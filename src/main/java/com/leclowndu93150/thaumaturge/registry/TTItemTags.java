package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class TTItemTags {
    public static final TagKey<Item> FLOATING_DROPS = key("floating_drops");
    public static final TagKey<Item> WANDS = key("wands");
    public static final TagKey<Item> WAND_RODS = key("wand_rods");
    public static final TagKey<Item> WAND_CAPS = key("wand_caps");
    public static final TagKey<Item> VIS_CRYSTALS = key("vis_crystals");
    public static final TagKey<Item> PLACEABLE_CRYSTALS = key("placeable_crystals");
    public static final TagKey<Item> GOLEM_SEALS = key("golem_seals");
    public static final TagKey<Item> RESEARCH_NOTES = key("research_notes");
    public static final TagKey<Item> LOOT_CONTAINERS = key("loot_containers");
    public static final TagKey<Item> RUNIC_SHIELDABLE = key("runic_shieldable");
    public static final TagKey<Item> SHIELDS_WARDING = key("shields/warding");
    public static final TagKey<Item> SHIELDS_REFLECTING = key("shields/reflecting");
    public static final TagKey<Item> SHIELDS_SAPPING = key("shields/sapping");
    public static final TagKey<Item> SCAN_IRON = key("scan/f_matiron");
    public static final TagKey<Item> NITORS = key("nitors");
    public static final TagKey<Item> MEAT_CHUNKS = key("meat_chunks");
    public static final TagKey<Item> SCANNABLE_DYNAMIC_TREE_BRANCHES = key("scan/dynamic_tree_branches");

    public static final TagKey<Item> STONES = common("stones");
    public static final TagKey<Item> CRAFTING_TABLES = common("player_workstations/crafting_tables");
    public static final TagKey<Item> FURNACES = common("player_workstations/furnaces");
    public static final TagKey<Item> CACTUS = common("crops/cactus");
    public static final TagKey<Item> SUGAR_CANE = common("crops/sugar_cane");
    public static final TagKey<Item> EMPTY_BUCKETS = common("buckets/empty");
    public static final TagKey<Item> WATER_BUCKETS = common("buckets/water");
    public static final TagKey<Item> MILK_BUCKETS = common("buckets/milk");
    public static final TagKey<Item> STRIPPED_LOGS = common("stripped_logs");
    public static final TagKey<Item> STRIPPED_WOODS = common("stripped_woods");
    public static final TagKey<Item> SHIELDS = common("tools/shield");
    public static final TagKey<Item> SHEARS = common("tools/shear");
    public static final TagKey<Item> MELEE_WEAPONS = common("tools/melee_weapon");
    public static final TagKey<Item> MINING_TOOLS = common("tools/mining_tool");

    public static final TagKey<Item> DYEABLE = vanilla("dyeable");
    public static final TagKey<Item> DURABILITY_ENCHANTABLE = vanilla("enchantable/durability");
    public static final TagKey<Item> HEAD_ARMOR = vanilla("head_armor");
    public static final TagKey<Item> CHEST_ARMOR = vanilla("chest_armor");
    public static final TagKey<Item> LEG_ARMOR = vanilla("leg_armor");
    public static final TagKey<Item> FOOT_ARMOR = vanilla("foot_armor");

    public static final TagKey<Item> GREATWOOD_LOGS = key("greatwood_logs");
    public static final TagKey<Item> SILVERWOOD_LOGS = key("silverwood_logs");
    public static final TagKey<Item> PLANKS = common("planks");
    public static final TagKey<Item> PLANKS_GREATWOOD = common("planks/greatwood");
    public static final TagKey<Item> PLANKS_SILVERWOOD = common("planks/silverwood");

    public static final TagKey<Item> RARE_EARTH_CHANCE_LOW = key("rare_earth_chance_low");
    public static final TagKey<Item> RARE_EARTH_CHANCE_NORMAL = key("rare_earth_chance_normal");
    public static final TagKey<Item> RARE_EARTH_CHANCE_HIGH = key("rare_earth_chance_high");
    public static final TagKey<Item> CLUSTERS = key("clusters");

    public static final TagKey<Item> INGOTS_TIN = common("ingots/tin");
    public static final TagKey<Item> INGOTS_LEAD = common("ingots/lead");
    public static final TagKey<Item> INGOTS_SILVER = common("ingots/silver");
    public static final TagKey<Item> INGOTS_BRASS = common("ingots/brass");
    public static final TagKey<Item> INGOTS_THAUMIUM = common("ingots/thaumium");
    public static final TagKey<Item> INGOTS_VOID_METAL = common("ingots/void_metal");

    public static final TagKey<Item> GEMS_QUICKSILVER = common("gems/quicksilver");
    public static final TagKey<Item> GEMS_AMBER = common("gems/amber");

    public static final TagKey<Item> NUGGETS_BRASS = common("nuggets/brass");
    public static final TagKey<Item> NUGGETS_THAUMIUM = common("nuggets/thaumium");
    public static final TagKey<Item> NUGGETS_VOID_METAL = common("nuggets/void_metal");
    public static final TagKey<Item> NUGGETS_QUICKSILVER = common("nuggets/quicksilver");
    public static final TagKey<Item> NUGGETS_QUARTZ = common("nuggets/quartz");
    public static final TagKey<Item> NUGGETS_TIN = common("nuggets/tin");
    public static final TagKey<Item> NUGGETS_COPPER = common("nuggets/copper");
    public static final TagKey<Item> NUGGETS_LEAD = common("nuggets/lead");
    public static final TagKey<Item> NUGGETS_SILVER = common("nuggets/silver");

    public static final TagKey<Item> PLATES = common("plates");
    public static final TagKey<Item> PLATES_IRON = common("plates/iron");
    public static final TagKey<Item> PLATES_BRASS = common("plates/brass");
    public static final TagKey<Item> PLATES_THAUMIUM = common("plates/thaumium");
    public static final TagKey<Item> PLATES_VOID_METAL = common("plates/void_metal");

    public static final TagKey<Item> ORES_TIN = common("ores/tin");
    public static final TagKey<Item> ORES_LEAD = common("ores/lead");
    public static final TagKey<Item> ORES_SILVER = common("ores/silver");
    public static final TagKey<Item> ORES_AMBER = common("ores/amber");
    public static final TagKey<Item> ORES_CINNABAR = common("ores/cinnabar");
    public static final TagKey<Item> ORES_QUARTZ = common("ores/quartz");
    public static final TagKey<Item> RAW_MATERIALS_CINNABAR = common("raw_materials/cinnabar");

    public static final TagKey<Item> STORAGE_BLOCKS_BRASS = common("storage_blocks/brass");
    public static final TagKey<Item> STORAGE_BLOCKS_THAUMIUM = common("storage_blocks/thaumium");
    public static final TagKey<Item> STORAGE_BLOCKS_VOID_METAL = common("storage_blocks/void_metal");
    public static final TagKey<Item> STORAGE_BLOCKS_AMBER = common("storage_blocks/amber");
    public static final TagKey<Item> STORAGE_BLOCKS_FLESH = common("storage_blocks/flesh");
    public static final TagKey<Item> STORAGE_BLOCKS_TALLOW = common("storage_blocks/tallow");

    public static final TagKey<Item> CANDLES = key("candles");

    public static final TagKey<Item> MAGICAL_PLANTS = key("magical_plants");

    public static final TagKey<Item> ARMORS_HELMETS = common("armors/helmets");
    public static final TagKey<Item> ARMORS_CHESTPLATES = common("armors/chestplates");
    public static final TagKey<Item> ARMORS_LEGGINGS = common("armors/leggings");
    public static final TagKey<Item> ARMORS_BOOTS = common("armors/boots");

    public static final TagKey<Item> SCRIBING_TOOLS = key("scribing_tools");
    public static final TagKey<Item> CURIOSITIES = key("curiosities");
    public static final TagKey<Item> TAINT_TOOLS = key("taint_tools");
    public static final TagKey<Item> MODELLED_TRIMMABLE_ARMOR = key("modelled_trimmable_armor");
    public static final TagKey<Item> TRIMMABLE_ARMOR = key("trimmable_armor");
    public static final TagKey<Item> TRIM_MATERIALS = key("trim_materials");
    public static final TagKey<Item> DT_BRANCHES_THAT_BURN = dynamicTrees("branches_that_burn");
    public static final TagKey<Item> DT_SEEDS = dynamicTrees("seeds");

    private TTItemTags() {}

    private static TagKey<Item> key(String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(TTIds.MODID, path));
    }

    private static TagKey<Item> common(String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", path));
    }

    private static TagKey<Item> vanilla(String path) {
        return ItemTags.create(ResourceLocation.withDefaultNamespace(path));
    }

    private static TagKey<Item> dynamicTrees(String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(TTIds.DYNAMIC_TREES, path));
    }
}
