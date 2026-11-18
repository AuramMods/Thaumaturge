package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.compat.NamedAttributeModifiers;
import com.leclowndu93150.thaumaturge.content.equipment.ArmorTypes;
import com.leclowndu93150.thaumaturge.serialization.EffectCodecs;
import com.leclowndu93150.thaumaturge.itemdata.ItemDataDefaults;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.items.trim.TTTrimPatterns;
import com.leclowndu93150.thaumaturge.content.aura.node.CreativeNodePlacerItem;
import com.leclowndu93150.thaumaturge.content.aura.node.JarNodeItem;
import com.leclowndu93150.thaumaturge.content.casters.FocusPouchItem;
import com.leclowndu93150.thaumaturge.content.decor.CandleHolderMaterial;
import com.leclowndu93150.thaumaturge.content.device.bore.ArcaneBoreItem;
import com.leclowndu93150.thaumaturge.content.device.mirror.ItemBlockMirror;
import com.leclowndu93150.thaumaturge.content.device.mirror.ItemHandMirror;
import com.leclowndu93150.thaumaturge.content.eldritch.lock.RunedTabletItem;
import com.leclowndu93150.thaumaturge.content.entity.EntityBottleTaint;
import com.leclowndu93150.thaumaturge.content.entity.EntityCausalityCollapser;
import com.leclowndu93150.thaumaturge.content.entity.construct.ConstructDeployment;
import com.leclowndu93150.thaumaturge.content.entity.construct.TurretPlacerItem;
import com.leclowndu93150.thaumaturge.content.equipment.CrimsonBladeItem;
import com.leclowndu93150.thaumaturge.content.equipment.CultistPlateItem;
import com.leclowndu93150.thaumaturge.content.equipment.CultistRobeItem;
import com.leclowndu93150.thaumaturge.content.equipment.ElementalAxeItem;
import com.leclowndu93150.thaumaturge.content.equipment.ElementalHoeItem;
import com.leclowndu93150.thaumaturge.content.equipment.ElementalPickaxeItem;
import com.leclowndu93150.thaumaturge.content.equipment.ElementalShovelItem;
import com.leclowndu93150.thaumaturge.content.equipment.ElementalSwordItem;
import com.leclowndu93150.thaumaturge.content.equipment.FortressArmorItem;
import com.leclowndu93150.thaumaturge.content.equipment.GrappleGunItem;
import com.leclowndu93150.thaumaturge.content.equipment.PrimalCrusherItem;
import com.leclowndu93150.thaumaturge.content.equipment.RobeArmorItem;
import com.leclowndu93150.thaumaturge.content.equipment.TTMaterials;
import com.leclowndu93150.thaumaturge.content.equipment.TravellerBootsItem;
import com.leclowndu93150.thaumaturge.content.equipment.VoidAxeItem;
import com.leclowndu93150.thaumaturge.content.equipment.VoidGearItem;
import com.leclowndu93150.thaumaturge.content.equipment.VoidHoeItem;
import com.leclowndu93150.thaumaturge.content.equipment.VoidPickaxeItem;
import com.leclowndu93150.thaumaturge.content.equipment.VoidRobeArmorItem;
import com.leclowndu93150.thaumaturge.content.equipment.VoidShieldItem;
import com.leclowndu93150.thaumaturge.content.equipment.VoidShovelItem;
import com.leclowndu93150.thaumaturge.content.equipment.VoidSwordItem;
import com.leclowndu93150.thaumaturge.content.equipment.bauble.AmuletVisItem;
import com.leclowndu93150.thaumaturge.content.equipment.bauble.TrinketItem;
import com.leclowndu93150.thaumaturge.content.equipment.bauble.VerdantCharmItem;
import com.leclowndu93150.thaumaturge.content.equipment.bauble.VoidseerCharmItem;
import com.leclowndu93150.thaumaturge.content.equipment.hover.ThaumostaticHarnessItem;
import com.leclowndu93150.thaumaturge.content.equipment.shield.ThaumicShieldItem;
import com.leclowndu93150.thaumaturge.content.essentia.ItemResonator;
import com.leclowndu93150.thaumaturge.content.essentia.advancedfurnace.ItemAdvancedAlchemicalFurnace;
import com.leclowndu93150.thaumaturge.content.essentia.jar.JarBraceItem;
import com.leclowndu93150.thaumaturge.content.essentia.jar.JarBrainItem;
import com.leclowndu93150.thaumaturge.content.essentia.jar.JarItem;
import com.leclowndu93150.thaumaturge.content.golem.ItemGolemBell;
import com.leclowndu93150.thaumaturge.content.golem.ItemGolemPlacer;
import com.leclowndu93150.thaumaturge.content.golem.ItemSealPlacer;
import com.leclowndu93150.thaumaturge.content.golem.press.ItemGolemPress;
import com.leclowndu93150.thaumaturge.content.infernalfurnace.ItemInfernalFurnace;
import com.leclowndu93150.thaumaturge.content.item.CelestialBody;
import com.leclowndu93150.thaumaturge.content.item.CelestialNotesItem;
import com.leclowndu93150.thaumaturge.content.item.LabelItem;
import com.leclowndu93150.thaumaturge.content.item.PhialItem;
import com.leclowndu93150.thaumaturge.content.item.PrimordialPearlItem;
import com.leclowndu93150.thaumaturge.content.item.SalisMundusItem;
import com.leclowndu93150.thaumaturge.content.item.ThaumometerItem;
import com.leclowndu93150.thaumaturge.content.item.ThrowableItem;
import com.leclowndu93150.thaumaturge.content.item.equipment.GogglesItem;
import com.leclowndu93150.thaumaturge.content.manabean.ItemManaBean;
import com.leclowndu93150.thaumaturge.content.misc.ItemCreativeFluxSponge;
import com.leclowndu93150.thaumaturge.content.misc.ItemCurio;
import com.leclowndu93150.thaumaturge.content.misc.alumentum.ThrownAlumentum;
import com.leclowndu93150.thaumaturge.content.pech.PechWandItem;
import com.leclowndu93150.thaumaturge.content.research.book.CheatThaumonomiconItem;
import com.leclowndu93150.thaumaturge.content.research.book.LinkingThaumonomiconItem;
import com.leclowndu93150.thaumaturge.content.research.book.SharingThaumonomiconItem;
import com.leclowndu93150.thaumaturge.content.research.book.ThaumonomiconItem;
import com.leclowndu93150.thaumaturge.content.research.note.ItemResearchNote;
import com.leclowndu93150.thaumaturge.content.spell.item.FocusItem;
import com.leclowndu93150.thaumaturge.content.taint.entity.ability.ThrownTaintedEgg;
import com.leclowndu93150.thaumaturge.content.taint.item.ItemEssentiaCrystal;
import com.leclowndu93150.thaumaturge.content.wands.ItemPrimalCharm;
import com.leclowndu93150.thaumaturge.content.wands.ItemWand;
import com.leclowndu93150.thaumaturge.content.wands.ItemWandCap;
import com.leclowndu93150.thaumaturge.content.wands.ItemWandRod;
import com.leclowndu93150.thaumaturge.content.warding.ItemArcaneKey;
import com.leclowndu93150.thaumaturge.content.warp.ItemSanitySoap;
import com.leclowndu93150.thaumaturge.content.world.mound.LootBagItem;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public final class TTItems {
    private static final int SPAWN_EGG_BASE = 0x51436C;
    private static final int SPAWN_EGG_HIGHLIGHT = 0xB399CF;
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(net.minecraft.core.registries.Registries.ITEM, TTIds.MODID);

    public static final RegistryObject<BlockItem> FOCAL_MANIPULATOR =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.FOCAL_MANIPULATOR);
    public static final RegistryObject<BlockItem> RESEARCH_TABLE = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.RESEARCH_TABLE);
    public static final RegistryObject<BlockItem> DECONSTRUCTION_TABLE =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.DECONSTRUCTION_TABLE);

    public static final RegistryObject<BlockItem> ARCANE_WORKBENCH =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.ARCANE_WORKBENCH);

    public static final RegistryObject<BlockItem> CRUCIBLE = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.CRUCIBLE);

    public static final RegistryObject<BlockItem> ARCANE_WORKBENCH_CHARGER =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.ARCANE_WORKBENCH_CHARGER);

    public static final RegistryObject<BlockItem> ALEMBIC = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.ALEMBIC);

    public static final RegistryObject<BlockItem> BELLOWS = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.BELLOWS);

    public static final RegistryObject<BlockItem> SMELTER_BASIC = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.SMELTER_BASIC);

    public static final RegistryObject<BlockItem> SMELTER_THAUMIUM =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.SMELTER_THAUMIUM);

    public static final RegistryObject<BlockItem> SMELTER_VOID = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.SMELTER_VOID);

    public static final RegistryObject<BlockItem> SMELTER_AUX = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.SMELTER_AUX);

    public static final RegistryObject<BlockItem> SMELTER_VENT = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.SMELTER_VENT);

    public static final RegistryObject<BlockItem> INFERNAL_FURNACE =
            registerSimpleBlockItem(TTBlocks.INFERNAL_FURNACE, ItemInfernalFurnace::new);

    public static final RegistryObject<BlockItem> JAR_NORMAL = registerSimpleBlockItem(TTBlocks.JAR_NORMAL, JarItem::new);

    public static final RegistryObject<BlockItem> JAR_VOID = registerSimpleBlockItem(TTBlocks.JAR_VOID, JarItem::new);

    public static final RegistryObject<BlockItem> JAR_CREATIVE = ForgeRegistration.registerItem(ITEMS,
            "jar_creative",
            props -> new JarItem(TTBlocks.JAR_CREATIVE.get(), props),
            new Item.Properties().component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true));

    public static final RegistryObject<BlockItem> TUBE = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.TUBE);

    public static final RegistryObject<BlockItem> TUBE_VALVE = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.TUBE_VALVE);

    public static final RegistryObject<BlockItem> TUBE_RESTRICT = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.TUBE_RESTRICT);

    public static final RegistryObject<BlockItem> TUBE_FILTER = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.TUBE_FILTER);

    public static final RegistryObject<BlockItem> TUBE_ONEWAY = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.TUBE_ONEWAY);

    public static final RegistryObject<BlockItem> TUBE_BUFFER = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.TUBE_BUFFER);

    public static final RegistryObject<BlockItem> ESSENTIA_RESERVOIR =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.ESSENTIA_RESERVOIR);

    public static final RegistryObject<BlockItem> ESSENTIA_CRYSTALIZER =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.ESSENTIA_CRYSTALIZER);

    public static final RegistryObject<BlockItem> TAINT_ROCK = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.TAINT_ROCK);
    public static final RegistryObject<BlockItem> TAINT_SOIL = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.TAINT_SOIL);
    public static final RegistryObject<BlockItem> TAINT_CRUST = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.TAINT_CRUST);
    public static final RegistryObject<BlockItem> TAINT_GEYSER = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.TAINT_GEYSER);
    public static final RegistryObject<BlockItem> TAINT_LOG = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.TAINT_LOG);
    public static final RegistryObject<BlockItem> TAINT_FEATURE = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.TAINT_FEATURE);
    public static final RegistryObject<BlockItem> TAINT_FIBRE = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.TAINT_FIBRE);
    public static final RegistryObject<BlockItem> TAINT_SPORE_STALK =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.TAINT_SPORE_STALK);
    public static final RegistryObject<BlockItem> ETHEREAL_BLOOM = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.ETHEREAL_BLOOM);

    public static final RegistryObject<BucketItem> BUCKET_LIQUID_DEATH = ForgeRegistration.registerItem(ITEMS,
            "liquid_death_bucket",
            props -> new BucketItem(TTFluids.LIQUID_DEATH_SOURCE.get(), props),
            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1).rarity(Rarity.RARE));

    public static final RegistryObject<BucketItem> BUCKET_FLUX_GOO = ForgeRegistration.registerItem(ITEMS,
            "flux_goo_bucket",
            props -> new BucketItem(TTFluids.FLUX_GOO_SOURCE.get(), props),
            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1));

    public static final RegistryObject<BucketItem> BUCKET_PURIFYING = ForgeRegistration.registerItem(ITEMS,
            "purifying_bucket",
            props -> new BucketItem(TTFluids.PURIFYING_SOURCE.get(), props),
            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1).rarity(Rarity.RARE));

    public static final RegistryObject<JarBraceItem> JAR_BRACE = ForgeRegistration.registerItem(ITEMS, "jar_brace", JarBraceItem::new);

    public static final RegistryObject<LabelItem> LABEL = ForgeRegistration.registerItem(ITEMS, "label", LabelItem::new);

    public static final RegistryObject<ThaumonomiconItem> THAUMONOMICON = ForgeRegistration.registerItem(ITEMS,
            "thaumonomicon",
            ThaumonomiconItem::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

    public static final RegistryObject<CheatThaumonomiconItem> THAUMONOMICON_CHEAT = ForgeRegistration.registerItem(ITEMS,
            "thaumonomicon_cheat",
            CheatThaumonomiconItem::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static final RegistryObject<SharingThaumonomiconItem> THAUMONOMICON_SHARING = ForgeRegistration.registerItem(ITEMS,
            "thaumonomicon_sharing",
            SharingThaumonomiconItem::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

    public static final RegistryObject<LinkingThaumonomiconItem> THAUMONOMICON_LINKING = ForgeRegistration.registerItem(ITEMS,
            "thaumonomicon_linking",
            LinkingThaumonomiconItem::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.RARE));

    public static final RegistryObject<CreativeNodePlacerItem> CREATIVE_NODE_PLACER = ForgeRegistration.registerItem(ITEMS,
            "creative_node_placer", CreativeNodePlacerItem::new, new Item.Properties().rarity(Rarity.EPIC));

    public static final RegistryObject<SalisMundusItem> SALIS_MUNDUS =
            ForgeRegistration.registerItem(ITEMS, "salis_mundus", SalisMundusItem::new, new Item.Properties().rarity(Rarity.UNCOMMON));

    public static final RegistryObject<ItemEssentiaCrystal> ESSENTIA_CRYSTAL =
            ForgeRegistration.registerItem(ITEMS, "essentia_crystal", ItemEssentiaCrystal::new);

    public static final RegistryObject<Item> TAINTED_GOO = ForgeRegistration.registerSimpleItem(ITEMS, "tainted_goo");

    public static final RegistryObject<Item> TAINT_TENDRIL = ForgeRegistration.registerSimpleItem(ITEMS, "taint_tendril");

    public static final RegistryObject<ThrowableItem> BOTTLE_TAINT = ForgeRegistration.registerItem(ITEMS,
            "bottle_taint",
            props -> new ThrowableItem(props, EntityBottleTaint::new, EntityBottleTaint.THROW),
            new Item.Properties().stacksTo(8).rarity(Rarity.UNCOMMON));

    public static final RegistryObject<ThrowableItem> TAINTED_EGG = ForgeRegistration.registerItem(ITEMS,
            "tainted_egg",
            props -> new ThrowableItem(props, ThrownTaintedEgg::new, ThrownTaintedEgg.THROW),
            new Item.Properties().stacksTo(16));

    public static final RegistryObject<BlockItem> THAUMATORIUM = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.THAUMATORIUM);
    public static final RegistryObject<BlockItem> BRAIN_BOX = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.BRAIN_BOX);
    public static final RegistryObject<BlockItem> CONDENSER = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.CONDENSER);
    public static final RegistryObject<BlockItem> FLUX_SCRUBBER = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.FLUX_SCRUBBER);
    public static final RegistryObject<BlockItem> CONDENSER_LATTICE =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.CONDENSER_LATTICE);
    public static final RegistryObject<BlockItem> CONDENSER_LATTICE_DIRTY =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.CONDENSER_LATTICE_DIRTY);
    public static final RegistryObject<BlockItem> STABILIZER = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.STABILIZER);
    public static final RegistryObject<BlockItem> REDSTONE_RELAY = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.REDSTONE_RELAY);
    public static final RegistryObject<BlockItem> VOID_SIPHON = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.VOID_SIPHON);
    public static final RegistryObject<BlockItem> EVERFULL_URN = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.EVERFULL_URN);
    public static final RegistryObject<BlockItem> VIS_GENERATOR = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.VIS_GENERATOR);
    public static final RegistryObject<BlockItem> ESSENTIA_INPUT = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.ESSENTIA_INPUT);
    public static final RegistryObject<BlockItem> ESSENTIA_OUTPUT =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.ESSENTIA_OUTPUT);
    public static final RegistryObject<BlockItem> ARCANE_EAR = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.ARCANE_EAR);
    public static final RegistryObject<BlockItem> ARCANE_EAR_TOGGLE =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.ARCANE_EAR_TOGGLE);
    public static final RegistryObject<BlockItem> LAMP_ARCANE = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.LAMP_ARCANE);
    public static final RegistryObject<BlockItem> LAMP_GROWTH = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.LAMP_GROWTH);
    public static final RegistryObject<BlockItem> LAMP_FERTILITY = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.LAMP_FERTILITY);
    public static final RegistryObject<BlockItem> CENTRIFUGE = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.CENTRIFUGE);
    public static final RegistryObject<BlockItem> HUNGRY_CHEST = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.HUNGRY_CHEST);
    public static final RegistryObject<BlockItem> MATRIX_SPEED = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.MATRIX_SPEED);
    public static final RegistryObject<BlockItem> MATRIX_COST = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.MATRIX_COST);
    public static final RegistryObject<BlockItem> VIS_BATTERY = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.VIS_BATTERY);
    public static final RegistryObject<BlockItem> DIOPTRA = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.DIOPTRA);
    public static final RegistryObject<JarBrainItem> JAR_BRAIN =
            ForgeRegistration.registerItem(ITEMS, "jar_brain", props -> new JarBrainItem(TTBlocks.JAR_BRAIN.get(), props));

    public static final RegistryObject<Item> VIS_RESONATOR = ForgeRegistration.registerSimpleItem(ITEMS, "vis_resonator");

    public static final RegistryObject<Item> THAUMIUM_SWORD = ForgeRegistration.registerItem(ITEMS,
            "thaumium_sword",
            props -> new SwordItem(
                    TTMaterials.TOOL_THAUMIUM,
                    props.attributes(SwordItem.createAttributes(TTMaterials.TOOL_THAUMIUM, 3.0F, -2.4F))));
    public static final RegistryObject<Item> THAUMIUM_PICKAXE = ForgeRegistration.registerItem(ITEMS,
            "thaumium_pickaxe",
            props -> new PickaxeItem(
                    TTMaterials.TOOL_THAUMIUM,
                    props.attributes(DiggerItem.createAttributes(TTMaterials.TOOL_THAUMIUM, 1.0F, -2.8F))));
    public static final RegistryObject<Item> THAUMIUM_AXE = ForgeRegistration.registerItem(ITEMS,
            "thaumium_axe",
            props -> new AxeItem(
                    TTMaterials.TOOL_THAUMIUM,
                    props.attributes(DiggerItem.createAttributes(TTMaterials.TOOL_THAUMIUM, 4.5F, -3.0F))));
    public static final RegistryObject<Item> THAUMIUM_SHOVEL = ForgeRegistration.registerItem(ITEMS,
            "thaumium_shovel",
            props -> new ShovelItem(
                    TTMaterials.TOOL_THAUMIUM,
                    props.attributes(DiggerItem.createAttributes(TTMaterials.TOOL_THAUMIUM, 1.5F, -3.0F))));
    public static final RegistryObject<HoeItem> THAUMIUM_HOE = ForgeRegistration.registerItem(ITEMS,
            "thaumium_hoe",
            props -> new HoeItem(
                    TTMaterials.TOOL_THAUMIUM,
                    props.attributes(HoeItem.createAttributes(TTMaterials.TOOL_THAUMIUM, -3.0F, 0.0F))));
    public static final RegistryObject<Item> THAUMIUM_HELM = ForgeRegistration.registerItem(ITEMS,
            "thaumium_helm",
            props -> new ArmorItem(
                    TTMaterials.ARMOR_THAUMIUM,
                    ArmorItem.Type.HELMET,
                    props.durability(ArmorTypes.durability(ArmorItem.Type.HELMET, TTMaterials.DURABILITY_THAUMIUM))));
    public static final RegistryObject<Item> THAUMIUM_CHEST = ForgeRegistration.registerItem(ITEMS,
            "thaumium_chest",
            props -> new ArmorItem(
                    TTMaterials.ARMOR_THAUMIUM,
                    ArmorItem.Type.CHESTPLATE,
                    props.durability(ArmorTypes.durability(ArmorItem.Type.CHESTPLATE, TTMaterials.DURABILITY_THAUMIUM))));
    public static final RegistryObject<Item> THAUMIUM_LEGS = ForgeRegistration.registerItem(ITEMS,
            "thaumium_legs",
            props -> new ArmorItem(
                    TTMaterials.ARMOR_THAUMIUM,
                    ArmorItem.Type.LEGGINGS,
                    props.durability(ArmorTypes.durability(ArmorItem.Type.LEGGINGS, TTMaterials.DURABILITY_THAUMIUM))));
    public static final RegistryObject<Item> THAUMIUM_BOOTS = ForgeRegistration.registerItem(ITEMS,
            "thaumium_boots",
            props -> new ArmorItem(
                    TTMaterials.ARMOR_THAUMIUM,
                    ArmorItem.Type.BOOTS,
                    props.durability(ArmorTypes.durability(ArmorItem.Type.BOOTS, TTMaterials.DURABILITY_THAUMIUM))));
    public static final RegistryObject<ThaumicShieldItem> THAUMIUM_SHIELD = ForgeRegistration.registerItem(ITEMS,
            "thaumium_shield",
            props -> new ThaumicShieldItem(
                    props, TTItemTags.INGOTS_THAUMIUM, TTMaterials.TOOL_THAUMIUM.getEnchantmentValue()),
            new Item.Properties().durability(512));
    public static final RegistryObject<ThaumicShieldItem> MIRROR_SHIELD = ForgeRegistration.registerItem(ITEMS,
            "mirror_shield",
            props -> new ThaumicShieldItem(props, Tags.Items.INGOTS_GOLD, 0),
            new Item.Properties().durability(336).rarity(Rarity.UNCOMMON));

    public static final RegistryObject<VoidSwordItem> VOID_SWORD = ForgeRegistration.registerItem(ITEMS,
            "void_sword",
            props -> new VoidSwordItem(
                    props.attributes(SwordItem.createAttributes(TTMaterials.TOOL_VOID, 3.0F, -2.4F))));
    public static final RegistryObject<VoidPickaxeItem> VOID_PICKAXE = ForgeRegistration.registerItem(ITEMS,
            "void_pickaxe",
            props -> new VoidPickaxeItem(
                    props.attributes(DiggerItem.createAttributes(TTMaterials.TOOL_VOID, 1.0F, -2.8F))));
    public static final RegistryObject<VoidAxeItem> VOID_AXE = ForgeRegistration.registerItem(ITEMS,
            "void_axe",
            props ->
                    new VoidAxeItem(props.attributes(DiggerItem.createAttributes(TTMaterials.TOOL_VOID, 4.0F, -3.0F))));
    public static final RegistryObject<VoidShovelItem> VOID_SHOVEL = ForgeRegistration.registerItem(ITEMS,
            "void_shovel",
            props -> new VoidShovelItem(
                    props.attributes(DiggerItem.createAttributes(TTMaterials.TOOL_VOID, 1.5F, -3.0F))));
    public static final RegistryObject<VoidHoeItem> VOID_HOE = ForgeRegistration.registerItem(ITEMS,
            "void_hoe",
            props -> new VoidHoeItem(props.attributes(HoeItem.createAttributes(TTMaterials.TOOL_VOID, -3.0F, 0.0F))));
    public static final RegistryObject<VoidGearItem> VOID_HELM = ForgeRegistration.registerItem(ITEMS,
            "void_helm",
            props -> new VoidGearItem(
                    TTMaterials.ARMOR_VOID,
                    ArmorItem.Type.HELMET,
                    props.durability(ArmorTypes.durability(ArmorItem.Type.HELMET, TTMaterials.DURABILITY_VOID))));
    public static final RegistryObject<VoidGearItem> VOID_CHEST = ForgeRegistration.registerItem(ITEMS,
            "void_chest",
            props -> new VoidGearItem(
                    TTMaterials.ARMOR_VOID,
                    ArmorItem.Type.CHESTPLATE,
                    props.durability(ArmorTypes.durability(ArmorItem.Type.CHESTPLATE, TTMaterials.DURABILITY_VOID))));
    public static final RegistryObject<VoidGearItem> VOID_LEGS = ForgeRegistration.registerItem(ITEMS,
            "void_legs",
            props -> new VoidGearItem(
                    TTMaterials.ARMOR_VOID,
                    ArmorItem.Type.LEGGINGS,
                    props.durability(ArmorTypes.durability(ArmorItem.Type.LEGGINGS, TTMaterials.DURABILITY_VOID))));
    public static final RegistryObject<VoidGearItem> VOID_BOOTS = ForgeRegistration.registerItem(ITEMS,
            "void_boots",
            props -> new VoidGearItem(
                    TTMaterials.ARMOR_VOID,
                    ArmorItem.Type.BOOTS,
                    props.durability(ArmorTypes.durability(ArmorItem.Type.BOOTS, TTMaterials.DURABILITY_VOID))));
    public static final RegistryObject<VoidShieldItem> VOID_SHIELD =
            ForgeRegistration.registerItem(ITEMS, "void_shield", VoidShieldItem::new, new Item.Properties().durability(512));

    public static final RegistryObject<ElementalSwordItem> ELEMENTAL_SWORD = ForgeRegistration.registerItem(ITEMS,
            "elemental_sword",
            props -> new ElementalSwordItem(
                    props.attributes(SwordItem.createAttributes(TTMaterials.TOOL_ELEMENTAL, 3.0F, -2.4F))
                            .rarity(Rarity.RARE)));
    public static final RegistryObject<ElementalPickaxeItem> ELEMENTAL_PICKAXE = ForgeRegistration.registerItem(ITEMS,
            "elemental_pickaxe",
            props -> new ElementalPickaxeItem(
                    props.attributes(DiggerItem.createAttributes(TTMaterials.TOOL_ELEMENTAL, 1.0F, -2.8F))
                            .rarity(Rarity.RARE)));
    public static final RegistryObject<ElementalAxeItem> ELEMENTAL_AXE = ForgeRegistration.registerItem(ITEMS,
            "elemental_axe",
            props -> new ElementalAxeItem(
                    props.attributes(DiggerItem.createAttributes(TTMaterials.TOOL_ELEMENTAL, 5.0F, -3.0F))
                            .rarity(Rarity.RARE)));
    public static final RegistryObject<ElementalShovelItem> ELEMENTAL_SHOVEL = ForgeRegistration.registerItem(ITEMS,
            "elemental_shovel",
            props -> new ElementalShovelItem(
                    props.attributes(DiggerItem.createAttributes(TTMaterials.TOOL_ELEMENTAL, 1.5F, -3.0F))
                            .rarity(Rarity.RARE)));
    public static final RegistryObject<ElementalHoeItem> ELEMENTAL_HOE = ForgeRegistration.registerItem(ITEMS,
            "elemental_hoe",
            props -> new ElementalHoeItem(
                    props.attributes(HoeItem.createAttributes(TTMaterials.TOOL_ELEMENTAL, -3.0F, 0.0F))
                            .rarity(Rarity.RARE)));
    public static final RegistryObject<PrimalCrusherItem> PRIMAL_CRUSHER = ForgeRegistration.registerItem(ITEMS,
            "primal_crusher",
            props -> new PrimalCrusherItem(
                    props.attributes(DiggerItem.createAttributes(TTMaterials.TOOL_PRIMAL_VOID, 3.5F, -2.8F))
                            .rarity(Rarity.RARE)));

    public static final RegistryObject<CrimsonBladeItem> CRIMSON_BLADE = ForgeRegistration.registerItem(ITEMS,
            "crimson_blade",
            props -> new CrimsonBladeItem(
                    props.attributes(SwordItem.createAttributes(TTMaterials.TOOL_CRIMSON_VOID, 3.0F, -2.4F))
                            .rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> CRIMSON_PLATE_HELM = ForgeRegistration.registerItem(ITEMS,
            "crimson_plate_helm",
            props -> new CultistPlateItem(
                    TTMaterials.ARMOR_CULTIST_PLATE,
                    ArmorItem.Type.HELMET,
                    props.durability(ArmorTypes.durability(ArmorItem.Type.HELMET, TTMaterials.DURABILITY_CULTIST_PLATE))
                            .rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> CRIMSON_PLATE_CHEST = ForgeRegistration.registerItem(ITEMS,
            "crimson_plate_chest",
            props -> new CultistPlateItem(
                    TTMaterials.ARMOR_CULTIST_PLATE,
                    ArmorItem.Type.CHESTPLATE,
                    props.durability(ArmorTypes.durability(ArmorItem.Type.CHESTPLATE, TTMaterials.DURABILITY_CULTIST_PLATE))
                            .rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> CRIMSON_PLATE_LEGS = ForgeRegistration.registerItem(ITEMS,
            "crimson_plate_legs",
            props -> new CultistPlateItem(
                    TTMaterials.ARMOR_CULTIST_PLATE,
                    ArmorItem.Type.LEGGINGS,
                    props.durability(ArmorTypes.durability(ArmorItem.Type.LEGGINGS, TTMaterials.DURABILITY_CULTIST_PLATE))
                            .rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<ArmorItem> CRIMSON_RANGER_HELM = ForgeRegistration.registerItem(ITEMS,
            "crimson_ranger_helm",
            props -> new ArmorItem(
                    TTMaterials.ARMOR_CULTIST_RANGER,
                    ArmorItem.Type.HELMET,
                    props.durability(ArmorTypes.durability(ArmorItem.Type.HELMET, TTMaterials.DURABILITY_CULTIST_RANGER))
                            .rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<ArmorItem> CRIMSON_RANGER_CHEST = ForgeRegistration.registerItem(ITEMS,
            "crimson_ranger_chest",
            props -> new ArmorItem(
                    TTMaterials.ARMOR_CULTIST_RANGER,
                    ArmorItem.Type.CHESTPLATE,
                    props.durability(ArmorTypes.durability(ArmorItem.Type.CHESTPLATE, TTMaterials.DURABILITY_CULTIST_RANGER))
                            .rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<ArmorItem> CRIMSON_RANGER_LEGS = ForgeRegistration.registerItem(ITEMS,
            "crimson_ranger_legs",
            props -> new ArmorItem(
                    TTMaterials.ARMOR_CULTIST_RANGER,
                    ArmorItem.Type.LEGGINGS,
                    props.durability(ArmorTypes.durability(ArmorItem.Type.LEGGINGS, TTMaterials.DURABILITY_CULTIST_RANGER))
                            .rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<ArmorItem> CRIMSON_CAPTAIN_HELM = ForgeRegistration.registerItem(ITEMS,
            "crimson_captain_helm",
            props -> new ArmorItem(
                    TTMaterials.ARMOR_CULTIST_CAPTAIN,
                    ArmorItem.Type.HELMET,
                    props.durability(ArmorTypes.durability(ArmorItem.Type.HELMET, TTMaterials.DURABILITY_CULTIST_CAPTAIN))
                            .rarity(Rarity.RARE)));
    public static final RegistryObject<ArmorItem> CRIMSON_CAPTAIN_CHEST = ForgeRegistration.registerItem(ITEMS,
            "crimson_captain_chest",
            props -> new ArmorItem(
                    TTMaterials.ARMOR_CULTIST_CAPTAIN,
                    ArmorItem.Type.CHESTPLATE,
                    props.durability(ArmorTypes.durability(ArmorItem.Type.CHESTPLATE, TTMaterials.DURABILITY_CULTIST_CAPTAIN))
                            .rarity(Rarity.RARE)));
    public static final RegistryObject<CultistRobeItem> CRIMSON_BOOTS = ForgeRegistration.registerItem(ITEMS,
            "crimson_boots",
            props -> new CultistRobeItem(
                    TTMaterials.ARMOR_CULTIST_BOOTS,
                    ArmorItem.Type.BOOTS,
                    props.durability(ArmorTypes.durability(ArmorItem.Type.BOOTS, TTMaterials.DURABILITY_CULTIST_BOOTS))
                            .rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<CultistRobeItem> CRIMSON_ROBE_HELM = ForgeRegistration.registerItem(ITEMS,
            "crimson_robe_helm",
            props -> new CultistRobeItem(
                    TTMaterials.ARMOR_CULTIST_ROBE,
                    ArmorItem.Type.HELMET,
                    props.durability(ArmorTypes.durability(ArmorItem.Type.HELMET, TTMaterials.DURABILITY_CULTIST_ROBE))
                            .rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<CultistRobeItem> CRIMSON_ROBE_CHEST = ForgeRegistration.registerItem(ITEMS,
            "crimson_robe_chest",
            props -> new CultistRobeItem(
                    TTMaterials.ARMOR_CULTIST_ROBE,
                    ArmorItem.Type.CHESTPLATE,
                    props.durability(ArmorTypes.durability(ArmorItem.Type.CHESTPLATE, TTMaterials.DURABILITY_CULTIST_ROBE))
                            .rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<CultistRobeItem> CRIMSON_ROBE_LEGS = ForgeRegistration.registerItem(ITEMS,
            "crimson_robe_legs",
            props -> new CultistRobeItem(
                    TTMaterials.ARMOR_CULTIST_ROBE,
                    ArmorItem.Type.LEGGINGS,
                    props.durability(ArmorTypes.durability(ArmorItem.Type.LEGGINGS, TTMaterials.DURABILITY_CULTIST_ROBE))
                            .rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<BlockItem> RECHARGE_PEDESTAL =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.RECHARGE_PEDESTAL);

    public static final RegistryObject<BlockItem> LEVITATOR = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.LEVITATOR);
    public static final RegistryObject<BlockItem> POTION_SPRAYER = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.POTION_SPRAYER);
    public static final RegistryObject<BlockItem> PATTERN_CRAFTER =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.PATTERN_CRAFTER);
    public static final RegistryObject<BlockItem> INLAY = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.INLAY);

    public static final RegistryObject<BlockItem> GOLEM_BUILDER =
            registerSimpleBlockItem(TTBlocks.GOLEM_BUILDER, ItemGolemPress::new);

    public static final RegistryObject<ItemGolemPlacer> GOLEM_PLACER = ForgeRegistration.registerItem(ITEMS, "golem", ItemGolemPlacer::new);
    public static final RegistryObject<ItemGolemBell> GOLEM_BELL =
            ForgeRegistration.registerItem(ITEMS, "golem_bell", ItemGolemBell::new, new Item.Properties().stacksTo(1));

    public static final RegistryObject<Item> GOLEM_TOP_HAT = ForgeRegistration.registerSimpleItem(ITEMS, "golem_top_hat");
    public static final RegistryObject<Item> GOLEM_FEZ = ForgeRegistration.registerSimpleItem(ITEMS, "golem_fez");
    public static final RegistryObject<Item> GOLEM_GLASSES = ForgeRegistration.registerSimpleItem(ITEMS, "golem_glasses");
    public static final RegistryObject<Item> GOLEM_BOWTIE = ForgeRegistration.registerSimpleItem(ITEMS, "golem_bowtie");
    public static final RegistryObject<Item> GOLEM_VISOR = ForgeRegistration.registerSimpleItem(ITEMS, "golem_visor");

    public static final RegistryObject<ItemSealPlacer> SEAL_BLANK = sealItem("seal_blank", null);
    public static final RegistryObject<ItemSealPlacer> SEAL_PICKUP = sealItem("seal_pickup", "pickup");
    public static final RegistryObject<ItemSealPlacer> SEAL_PICKUP_ADVANCED =
            sealItem("seal_pickup_advanced", "pickup_advanced");
    public static final RegistryObject<ItemSealPlacer> SEAL_FILL = sealItem("seal_fill", "fill");
    public static final RegistryObject<ItemSealPlacer> SEAL_FILL_ADVANCED =
            sealItem("seal_fill_advanced", "fill_advanced");
    public static final RegistryObject<ItemSealPlacer> SEAL_EMPTY = sealItem("seal_empty", "empty");
    public static final RegistryObject<ItemSealPlacer> SEAL_EMPTY_ADVANCED =
            sealItem("seal_empty_advanced", "empty_advanced");
    public static final RegistryObject<ItemSealPlacer> SEAL_HARVEST = sealItem("seal_harvest", "harvest");
    public static final RegistryObject<ItemSealPlacer> SEAL_BUTCHER = sealItem("seal_butcher", "butcher");
    public static final RegistryObject<ItemSealPlacer> SEAL_GUARD = sealItem("seal_guard", "guard");
    public static final RegistryObject<ItemSealPlacer> SEAL_GUARD_ADVANCED =
            sealItem("seal_guard_advanced", "guard_advanced");
    public static final RegistryObject<ItemSealPlacer> SEAL_LUMBER = sealItem("seal_lumber", "lumber");
    public static final RegistryObject<ItemSealPlacer> SEAL_BREAKER = sealItem("seal_breaker", "breaker");
    public static final RegistryObject<ItemSealPlacer> SEAL_BREAKER_ADVANCED =
            sealItem("seal_breaker_advanced", "breaker_advanced");
    public static final RegistryObject<ItemSealPlacer> SEAL_USE = sealItem("seal_use", "use");
    public static final RegistryObject<ItemSealPlacer> SEAL_PROVIDER = sealItem("seal_provider", "provider");
    public static final RegistryObject<ItemSealPlacer> SEAL_STOCK = sealItem("seal_stock", "stock");

    private static RegistryObject<ItemSealPlacer> sealItem(String id, String sealPath) {
        return ForgeRegistration.registerItem(ITEMS, id, props -> new ItemSealPlacer(sealPath == null ? null : TTIds.rl(sealPath), props));
    }

    public static final RegistryObject<ThaumostaticHarnessItem> THAUMOSTATIC_HARNESS = ForgeRegistration.registerItem(ITEMS,
            "thaumostatic_harness",
            ThaumostaticHarnessItem::new,
            new Item.Properties().durability(400).rarity(Rarity.EPIC));
    public static final RegistryObject<TravellerBootsItem> TRAVELLER_BOOTS = ForgeRegistration.registerItem(ITEMS,
            "traveller_boots",
            props -> new TravellerBootsItem(props.durability(350).rarity(Rarity.RARE)));

    public static final RegistryObject<RobeArmorItem> CLOTH_CHEST = ForgeRegistration.registerItem(ITEMS,
            "cloth_chest",
            props -> new RobeArmorItem(
                    3,
                    ArmorItem.Type.CHESTPLATE,
                    props.durability(ArmorTypes.durability(ArmorItem.Type.CHESTPLATE, TTMaterials.DURABILITY_ROBES))));
    public static final RegistryObject<RobeArmorItem> CLOTH_LEGS = ForgeRegistration.registerItem(ITEMS,
            "cloth_legs",
            props -> new RobeArmorItem(
                    3,
                    ArmorItem.Type.LEGGINGS,
                    props.durability(ArmorTypes.durability(ArmorItem.Type.LEGGINGS, TTMaterials.DURABILITY_ROBES))));
    public static final RegistryObject<RobeArmorItem> CLOTH_BOOTS = ForgeRegistration.registerItem(ITEMS,
            "cloth_boots",
            props -> new RobeArmorItem(
                    2,
                    ArmorItem.Type.BOOTS,
                    props.durability(ArmorTypes.durability(ArmorItem.Type.BOOTS, TTMaterials.DURABILITY_ROBES))));

    public static final RegistryObject<ItemManaBean> MANA_BEAN = ForgeRegistration.registerItem(ITEMS,
            "mana_bean",
            ItemManaBean::new,
            new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(1)
                            .saturationModifier(0.5F)
                            .alwaysEdible()
                            .fast()
                            .build()));

    public static final RegistryObject<Item> BRAIN = ForgeRegistration.registerItem(ITEMS,
            "brain",
            Item::new,
            new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(4)
                            .saturationModifier(0.2F)
                            .alwaysEdible()
                            .effect(() -> EffectCodecs.instance(MobEffects.HUNGER, 30, 0), 0.8F)
                            .build()));

    public static final RegistryObject<SpawnEggItem> BRAINY_ZOMBIE_SPAWN_EGG =
            registerSpawnEgg("brainy_zombie_spawn_egg", TTEntities.BRAINY_ZOMBIE);
    public static final RegistryObject<SpawnEggItem> BRAINY_DROWNED_SPAWN_EGG =
            registerSpawnEgg("brainy_drowned_spawn_egg", TTEntities.BRAINY_DROWNED);
    public static final RegistryObject<SpawnEggItem> BRAINY_HUSK_SPAWN_EGG =
            registerSpawnEgg("brainy_husk_spawn_egg", TTEntities.BRAINY_HUSK);
    public static final RegistryObject<SpawnEggItem> GIANT_BRAINY_ZOMBIE_SPAWN_EGG =
            registerSpawnEgg("giant_brainy_zombie_spawn_egg", TTEntities.GIANT_BRAINY_ZOMBIE);
    public static final RegistryObject<SpawnEggItem> FIREBAT_SPAWN_EGG =
            registerSpawnEgg("firebat_spawn_egg", TTEntities.FIRE_BAT);
    public static final RegistryObject<SpawnEggItem> MIND_SPIDER_SPAWN_EGG =
            registerSpawnEgg("mind_spider_spawn_egg", TTEntities.MIND_SPIDER);
    public static final RegistryObject<SpawnEggItem> WISP_SPAWN_EGG = registerSpawnEgg("wisp_spawn_egg", TTEntities.WISP);
    public static final RegistryObject<SpawnEggItem> THAUMIC_SLIME_SPAWN_EGG =
            registerSpawnEgg("thaumic_slime_spawn_egg", TTEntities.THAUMIC_SLIME);
    public static final RegistryObject<SpawnEggItem> TAINT_CRAWLER_SPAWN_EGG =
            registerSpawnEgg("taint_crawler_spawn_egg", TTEntities.TAINT_CRAWLER);
    public static final RegistryObject<SpawnEggItem> TAINTACLE_SPAWN_EGG =
            registerSpawnEgg("taintacle_spawn_egg", TTEntities.TAINTACLE);
    public static final RegistryObject<SpawnEggItem> TAINT_SWARM_SPAWN_EGG =
            registerSpawnEgg("taint_swarm_spawn_egg", TTEntities.TAINT_SWARM);
    public static final RegistryObject<SpawnEggItem> ELDRITCH_WARDEN_SPAWN_EGG =
            registerSpawnEgg("eldritch_warden_spawn_egg", TTEntities.ELDRITCH_WARDEN);
    public static final RegistryObject<SpawnEggItem> ELDRITCH_GOLEM_SPAWN_EGG =
            registerSpawnEgg("eldritch_golem_spawn_egg", TTEntities.ELDRITCH_GOLEM);
    public static final RegistryObject<SpawnEggItem> CULTIST_LEADER_SPAWN_EGG =
            registerSpawnEgg("cultist_leader_spawn_egg", TTEntities.CULTIST_LEADER);
    public static final RegistryObject<SpawnEggItem> CULTIST_PORTAL_GREATER_SPAWN_EGG =
            registerSpawnEgg("cultist_portal_greater_spawn_egg", TTEntities.CULTIST_PORTAL_GREATER);
    public static final RegistryObject<SpawnEggItem> TAINTACLE_GIANT_SPAWN_EGG =
            registerSpawnEgg("taintacle_giant_spawn_egg", TTEntities.TAINTACLE_GIANT);
    public static final RegistryObject<SpawnEggItem> TAINT_SEED_SPAWN_EGG =
            registerSpawnEgg("taint_seed_spawn_egg", TTEntities.TAINT_SEED);
    public static final RegistryObject<SpawnEggItem> TAINT_SEED_PRIME_SPAWN_EGG =
            registerSpawnEgg("taint_seed_prime_spawn_egg", TTEntities.TAINT_SEED_PRIME);
    public static final RegistryObject<SpawnEggItem> PECH_SPAWN_EGG = registerSpawnEgg("pech_spawn_egg", TTEntities.PECH);
    public static final RegistryObject<SpawnEggItem> ELDRITCH_CRAB_SPAWN_EGG =
            registerSpawnEgg("eldritch_crab_spawn_egg", TTEntities.ELDRITCH_CRAB);
    public static final RegistryObject<SpawnEggItem> INHABITED_ZOMBIE_SPAWN_EGG =
            registerSpawnEgg("inhabited_zombie_spawn_egg", TTEntities.INHABITED_ZOMBIE);
    public static final RegistryObject<SpawnEggItem> ELDRITCH_GUARDIAN_SPAWN_EGG =
            registerSpawnEgg("eldritch_guardian_spawn_egg", TTEntities.ELDRITCH_GUARDIAN);
    public static final RegistryObject<SpawnEggItem> CULTIST_KNIGHT_SPAWN_EGG =
            registerSpawnEgg("cultist_knight_spawn_egg", TTEntities.CULTIST_KNIGHT);
    public static final RegistryObject<SpawnEggItem> CULTIST_CLERIC_SPAWN_EGG =
            registerSpawnEgg("cultist_cleric_spawn_egg", TTEntities.CULTIST_CLERIC);
    public static final RegistryObject<SpawnEggItem> CULTIST_RANGER_SPAWN_EGG =
            registerSpawnEgg("cultist_ranger_spawn_egg", TTEntities.CULTIST_RANGER);
    public static final RegistryObject<SpawnEggItem> CULTIST_RANGER_CAPTAIN_SPAWN_EGG =
            registerSpawnEgg("cultist_ranger_captain_spawn_egg", TTEntities.CULTIST_RANGER_CAPTAIN);
    public static final RegistryObject<SpawnEggItem> CULTIST_PORTAL_LESSER_SPAWN_EGG =
            registerSpawnEgg("cultist_portal_lesser_spawn_egg", TTEntities.CULTIST_PORTAL_LESSER);

    public static final RegistryObject<PechWandItem> PECH_WAND =
            ForgeRegistration.registerItem(ITEMS, "pech_wand", PechWandItem::new, new Item.Properties().rarity(Rarity.RARE));

    public static final RegistryObject<LootBagItem> LOOT_BAG_COMMON = ForgeRegistration.registerItem(ITEMS,
            "loot_bag_common", props -> new LootBagItem(TTLootTables.LOOT_BAG_COMMON, props.stacksTo(16)));
    public static final RegistryObject<LootBagItem> LOOT_BAG_UNCOMMON = ForgeRegistration.registerItem(ITEMS,
            "loot_bag_uncommon",
            props -> new LootBagItem(
                    TTLootTables.LOOT_BAG_UNCOMMON, props.stacksTo(16).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<LootBagItem> LOOT_BAG_RARE = ForgeRegistration.registerItem(ITEMS,
            "loot_bag_rare",
            props -> new LootBagItem(
                    TTLootTables.LOOT_BAG_RARE, props.stacksTo(16).rarity(Rarity.RARE)));

    public static final RegistryObject<SmithingTemplateItem> ARCANE_ARMOR_TRIM_SMITHING_TEMPLATE = ITEMS.register(
            "arcane_armor_trim_smithing_template",
            () -> SmithingTemplateItem.createArmorTrimTemplate(TTTrimPatterns.ARCANE));
    public static final RegistryObject<SmithingTemplateItem> OCCULT_ARMOR_TRIM_SMITHING_TEMPLATE = ITEMS.register(
            "occult_armor_trim_smithing_template",
            () -> SmithingTemplateItem.createArmorTrimTemplate(TTTrimPatterns.OCCULT));

    public static final RegistryObject<BlockItem> LOOT_URN_COMMON =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.LOOT_URN_COMMON);
    public static final RegistryObject<BlockItem> LOOT_URN_UNCOMMON =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.LOOT_URN_UNCOMMON);
    public static final RegistryObject<BlockItem> LOOT_URN_RARE = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.LOOT_URN_RARE);
    public static final RegistryObject<BlockItem> LOOT_CRATE_COMMON =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.LOOT_CRATE_COMMON);
    public static final RegistryObject<BlockItem> LOOT_CRATE_UNCOMMON =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.LOOT_CRATE_UNCOMMON);
    public static final RegistryObject<BlockItem> LOOT_CRATE_RARE =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.LOOT_CRATE_RARE);

    private static RegistryObject<SpawnEggItem> registerSpawnEgg(
            String name, Supplier<? extends EntityType<? extends Mob>> type) {
        return ForgeRegistration.registerItem(ITEMS,
                name, properties -> new DeferredSpawnEggItem(type, SPAWN_EGG_BASE, SPAWN_EGG_HIGHLIGHT, properties));
    }

    //
    public static final RegistryObject<Item> TALLOW = ForgeRegistration.registerSimpleItem(ITEMS, "tallow");

    public static final Map<DyeColor, RegistryObject<BlockItem>> CANDLES = new EnumMap<>(DyeColor.class);

    static {
        for (DyeColor dye : DyeColor.values()) {
            CANDLES.put(dye, ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.CANDLES.get(dye)));
        }
    }

    public static final Map<CandleHolderMaterial, RegistryObject<BlockItem>> CANDLE_HOLDERS =
            new EnumMap<>(CandleHolderMaterial.class);

    static {
        for (CandleHolderMaterial material : CandleHolderMaterial.values()) {
            CANDLE_HOLDERS.put(material, ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.CANDLE_HOLDERS.get(material)));
        }
    }

    public static final Map<DyeColor, RegistryObject<StandingAndWallBlockItem>> BANNERS = new EnumMap<>(DyeColor.class);

    static {
        for (DyeColor dye : DyeColor.values()) {
            BANNERS.put(
                    dye,
                    ForgeRegistration.registerItem(ITEMS,
                            "banner_" + dye.getName(),
                            props -> new StandingAndWallBlockItem(
                                    TTBlocks.BANNERS.get(dye).get(),
                                    TTBlocks.WALL_BANNERS.get(dye).get(),
                                    props.stacksTo(16),
                                    Direction.DOWN)));
        }
    }

    public static final RegistryObject<StandingAndWallBlockItem> BANNER_CRIMSON_CULT = ForgeRegistration.registerItem(ITEMS,
            "banner_crimson_cult",
            props -> new StandingAndWallBlockItem(
                    TTBlocks.BANNER_CRIMSON_CULT.get(),
                    TTBlocks.WALL_BANNER_CRIMSON_CULT.get(),
                    props.stacksTo(16),
                    Direction.DOWN));

    public static final Map<DyeColor, RegistryObject<BlockItem>> NITORS = new EnumMap<>(DyeColor.class);

    static {
        for (DyeColor dye : DyeColor.values()) {
            NITORS.put(dye, ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.NITORS.get(dye)));
        }
    }

    public static final RegistryObject<BlockItem> SPA = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.SPA);

    public static final RegistryObject<ThrowableItem> ALUMENTUM = ForgeRegistration.registerItem(ITEMS,
            "alumentum", props -> new ThrowableItem(props, ThrownAlumentum::new, ThrownAlumentum.THROW));

    public static final RegistryObject<Item> FABRIC = ForgeRegistration.registerSimpleItem(ITEMS, "fabric");
    public static final RegistryObject<Item> MIRRORED_GLASS = ForgeRegistration.registerSimpleItem(ITEMS, "mirrored_glass");
    public static final RegistryObject<Item> FILTER = ForgeRegistration.registerSimpleItem(ITEMS, "filter");
    public static final RegistryObject<Item> MECHANISM_SIMPLE = ForgeRegistration.registerSimpleItem(ITEMS, "mechanism_simple");
    public static final RegistryObject<Item> MECHANISM_COMPLEX = ForgeRegistration.registerSimpleItem(ITEMS, "mechanism_complex");
    public static final RegistryObject<Item> MIND_CLOCKWORK = ForgeRegistration.registerSimpleItem(ITEMS, "mind_clockwork");
    public static final RegistryObject<Item> MIND_BIOTHAUMIC = ForgeRegistration.registerSimpleItem(ITEMS, "mind_biothaumic");
    public static final RegistryObject<Item> MODULE_VISION = ForgeRegistration.registerSimpleItem(ITEMS, "module_vision");
    public static final RegistryObject<Item> MODULE_AGGRESSION = ForgeRegistration.registerSimpleItem(ITEMS, "module_aggression");
    public static final RegistryObject<Item> MORPHIC_RESONATOR = ForgeRegistration.registerSimpleItem(ITEMS, "morphic_resonator");
    public static final RegistryObject<Item> BATH_SALTS = ForgeRegistration.registerItem(ITEMS, "bath_salts", Item::new);
    public static final RegistryObject<ItemSanitySoap> SANITY_SOAP =
            ForgeRegistration.registerItem(ITEMS, "sanity_soap", ItemSanitySoap::new);

    public static final RegistryObject<Item> CHUNK_BEEF = registerChunk("chunk_beef");
    public static final RegistryObject<Item> CHUNK_CHICKEN = registerChunk("chunk_chicken");
    public static final RegistryObject<Item> CHUNK_PORK = registerChunk("chunk_pork");
    public static final RegistryObject<Item> CHUNK_FISH = registerChunk("chunk_fish");
    public static final RegistryObject<Item> CHUNK_RABBIT = registerChunk("chunk_rabbit");
    public static final RegistryObject<Item> CHUNK_MUTTON = registerChunk("chunk_mutton");

    public static final RegistryObject<Item> TRIPLE_MEAT_TREAT = ForgeRegistration.registerItem(ITEMS,
            "triple_meat_treat",
            props -> new Item(props.food(new FoodProperties.Builder()
                    .nutrition(6)
                    .saturationModifier(0.8F)
                    .alwaysEdible()
                    .effect(() -> EffectCodecs.instance(MobEffects.REGENERATION, 100, 0), 0.66F)
                    .build())));

    private static RegistryObject<Item> registerChunk(String id) {
        return ForgeRegistration.registerItem(ITEMS,
                id,
                props -> new Item(props.food(new FoodProperties.Builder()
                        .nutrition(1)
                        .saturationModifier(0.3F)
                        .fast()
                        .build())));
    }

    public static final RegistryObject<BlockItem> CRYSTAL_AER = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.CRYSTAL_AER);
    public static final RegistryObject<BlockItem> CRYSTAL_IGNIS = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.CRYSTAL_IGNIS);
    public static final RegistryObject<BlockItem> CRYSTAL_AQUA = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.CRYSTAL_AQUA);
    public static final RegistryObject<BlockItem> CRYSTAL_TERRA = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.CRYSTAL_TERRA);
    public static final RegistryObject<BlockItem> CRYSTAL_ORDO = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.CRYSTAL_ORDO);
    public static final RegistryObject<BlockItem> CRYSTAL_PERDITIO =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.CRYSTAL_PERDITIO);
    public static final RegistryObject<BlockItem> CRYSTAL_VITIUM = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.CRYSTAL_VITIUM);

    //

    public static final RegistryObject<BlockItem> STONE_ARCANE = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.STONE_ARCANE);
    public static final RegistryObject<BlockItem> WARDED_GLASS = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.WARDED_GLASS);
    public static final RegistryObject<BlockItem> ARCANE_DOOR = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.ARCANE_DOOR);
    public static final RegistryObject<BlockItem> ARCANE_PRESSURE_PLATE =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.ARCANE_PRESSURE_PLATE);
    public static final RegistryObject<BlockItem> GOLEM_FETTER = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.GOLEM_FETTER);
    public static final RegistryObject<BlockItem> TALLOW_BLOCK = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.TALLOW_BLOCK);
    public static final RegistryObject<BlockItem> ITEM_GRATE = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.ITEM_GRATE);
    public static final RegistryObject<ItemArcaneKey> ARCANE_KEY_IRON =
            ForgeRegistration.registerItem(ITEMS, "arcane_key_iron", ItemArcaneKey::new, new Item.Properties().stacksTo(16));
    public static final RegistryObject<ItemArcaneKey> ARCANE_KEY_GOLD =
            ForgeRegistration.registerItem(ITEMS, "arcane_key_gold", ItemArcaneKey::new, new Item.Properties().stacksTo(16));
    public static final RegistryObject<BlockItem> STONE_ARCANE_BRICK =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.STONE_ARCANE_BRICK);
    public static final RegistryObject<BlockItem> STONE_ANCIENT = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.STONE_ANCIENT);
    public static final RegistryObject<BlockItem> STONE_ANCIENT_TILE =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.STONE_ANCIENT_TILE);
    public static final RegistryObject<BlockItem> STONE_ANCIENT_ROCK =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.STONE_ANCIENT_ROCK);
    public static final RegistryObject<BlockItem> STONE_ANCIENT_GLYPHED =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.STONE_ANCIENT_GLYPHED);
    public static final RegistryObject<BlockItem> STONE_ANCIENT_DOORWAY =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.STONE_ANCIENT_DOORWAY);
    public static final RegistryObject<BlockItem> STONE_ELDRITCH_TILE =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.STONE_ELDRITCH_TILE);
    public static final RegistryObject<BlockItem> STONE_POROUS = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.STONE_POROUS);
    public static final RegistryObject<BlockItem> STAIRS_ARCANE = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.STAIRS_ARCANE);
    public static final RegistryObject<BlockItem> STAIRS_ARCANE_BRICK =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.STAIRS_ARCANE_BRICK);
    public static final RegistryObject<BlockItem> STAIRS_ANCIENT = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.STAIRS_ANCIENT);

    //

    public static final RegistryObject<BlockItem> SAPLING_GREATWOOD =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.SAPLING_GREATWOOD);
    public static final RegistryObject<BlockItem> SAPLING_SILVERWOOD =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.SAPLING_SILVERWOOD);
    public static final RegistryObject<BlockItem> LOG_GREATWOOD = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.LOG_GREATWOOD);
    public static final RegistryObject<BlockItem> WOOD_GREATWOOD = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.WOOD_GREATWOOD);
    public static final RegistryObject<BlockItem> STRIPPED_LOG_GREATWOOD =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.STRIPPED_LOG_GREATWOOD);
    public static final RegistryObject<BlockItem> STRIPPED_WOOD_GREATWOOD =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.STRIPPED_WOOD_GREATWOOD);
    public static final RegistryObject<BlockItem> LOG_SILVERWOOD = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.LOG_SILVERWOOD);
    public static final RegistryObject<BlockItem> WOOD_SILVERWOOD =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.WOOD_SILVERWOOD);
    public static final RegistryObject<BlockItem> STRIPPED_LOG_SILVERWOOD =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.STRIPPED_LOG_SILVERWOOD);
    public static final RegistryObject<BlockItem> STRIPPED_WOOD_SILVERWOOD =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.STRIPPED_WOOD_SILVERWOOD);
    public static final RegistryObject<BlockItem> LEAVES_GREATWOOD =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.LEAVES_GREATWOOD);
    public static final RegistryObject<BlockItem> LEAVES_SILVERWOOD =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.LEAVES_SILVERWOOD);
    public static final RegistryObject<BlockItem> PLANK_GREATWOOD =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.PLANK_GREATWOOD);
    public static final RegistryObject<BlockItem> PLANK_SILVERWOOD =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.PLANK_SILVERWOOD);

    //

    public static final RegistryObject<BlockItem> PLANT_SHIMMERLEAF =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.PLANT_SHIMMERLEAF);
    public static final RegistryObject<BlockItem> PLANT_CINDERPEARL =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.PLANT_CINDERPEARL);
    public static final RegistryObject<BlockItem> PLANT_VISHROOM = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.PLANT_VISHROOM);
    public static final RegistryObject<BlockItem> GRASS_AMBIENT = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.GRASS_AMBIENT);

    // RESOURCES | INGOTS

    public static final RegistryObject<BlockItem> ORE_AMBER = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.ORE_AMBER);
    public static final RegistryObject<BlockItem> ORE_CINNABAR = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.ORE_CINNABAR);
    public static final RegistryObject<BlockItem> ORE_QUARTZ = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.ORE_QUARTZ);
    public static final RegistryObject<BlockItem> DEEPSLATE_ORE_AMBER =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.DEEPSLATE_ORE_AMBER);
    public static final RegistryObject<BlockItem> DEEPSLATE_ORE_CINNABAR =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.DEEPSLATE_ORE_CINNABAR);
    public static final RegistryObject<BlockItem> DEEPSLATE_ORE_QUARTZ =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.DEEPSLATE_ORE_QUARTZ);

    public static final RegistryObject<BlockItem> ALCHEMICAL_CONSTRUCT =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.ALCHEMICAL_CONSTRUCT);
    public static final RegistryObject<BlockItem> ADVANCED_ALCHEMICAL_CONSTRUCT =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT);
    public static final RegistryObject<BlockItem> ADVANCED_ALCHEMICAL_FURNACE =
            registerSimpleBlockItem(TTBlocks.ADVANCED_ALCHEMICAL_FURNACE, ItemAdvancedAlchemicalFurnace::new);

    public static final RegistryObject<BlockItem> METAL_THAUMIUM_BLOCK =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.METAL_THAUMIUM_BLOCK);
    public static final RegistryObject<BlockItem> METAL_BRASS_BLOCK =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.METAL_BRASS_BLOCK);
    public static final RegistryObject<BlockItem> METAL_VOID_BLOCK =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.METAL_VOID_BLOCK);
    public static final RegistryObject<BlockItem> AMBER_BLOCK = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.AMBER_BLOCK);

    public static final RegistryObject<Item> INGOT_BRASS = ForgeRegistration.registerSimpleItem(ITEMS, "ingot_brass");
    public static final RegistryObject<Item> INGOT_THAUMIUM = ForgeRegistration.registerSimpleItem(ITEMS, "ingot_thaumium");
    public static final RegistryObject<Item> INGOT_VOID = ForgeRegistration.registerSimpleItem(ITEMS, "ingot_void");
    public static final RegistryObject<Item> QUICKSILVER = ForgeRegistration.registerSimpleItem(ITEMS, "quicksilver");
    public static final RegistryObject<Item> AMBER = ForgeRegistration.registerSimpleItem(ITEMS, "amber");

    public static final RegistryObject<Item> RARE_EARTH = ForgeRegistration.registerSimpleItem(ITEMS, "rare_earth");

    public static final RegistryObject<Item> NUGGET_BRASS = ForgeRegistration.registerSimpleItem(ITEMS, "nugget_brass");
    public static final RegistryObject<Item> NUGGET_THAUMIUM = ForgeRegistration.registerSimpleItem(ITEMS, "nugget_thaumium");
    public static final RegistryObject<Item> NUGGET_VOID = ForgeRegistration.registerSimpleItem(ITEMS, "nugget_void");
    public static final RegistryObject<Item> NUGGET_QUARTZ = ForgeRegistration.registerSimpleItem(ITEMS, "nugget_quartz");
    public static final RegistryObject<Item> NUGGET_QUICKSILVER = ForgeRegistration.registerSimpleItem(ITEMS, "nugget_quicksilver");

    public static final RegistryObject<Item> PLATE_IRON = ForgeRegistration.registerSimpleItem(ITEMS, "plate_iron");
    public static final RegistryObject<Item> PLATE_BRASS = ForgeRegistration.registerSimpleItem(ITEMS, "plate_brass");
    public static final RegistryObject<Item> PLATE_THAUMIUM = ForgeRegistration.registerSimpleItem(ITEMS, "plate_thaumium");
    public static final RegistryObject<Item> PLATE_VOID = ForgeRegistration.registerSimpleItem(ITEMS, "plate_void");

    public static final RegistryObject<Item> CLUSTER_IRON = ForgeRegistration.registerSimpleItem(ITEMS, "cluster_iron");
    public static final RegistryObject<Item> CLUSTER_GOLD = ForgeRegistration.registerSimpleItem(ITEMS, "cluster_gold");
    public static final RegistryObject<Item> CLUSTER_COPPER = ForgeRegistration.registerSimpleItem(ITEMS, "cluster_copper");
    public static final RegistryObject<Item> CLUSTER_TIN = ForgeRegistration.registerSimpleItem(ITEMS, "cluster_tin");
    public static final RegistryObject<Item> CLUSTER_SILVER = ForgeRegistration.registerSimpleItem(ITEMS, "cluster_silver");
    public static final RegistryObject<Item> CLUSTER_LEAD = ForgeRegistration.registerSimpleItem(ITEMS, "cluster_lead");
    public static final RegistryObject<Item> RAW_CINNABAR = ForgeRegistration.registerSimpleItem(ITEMS, "raw_cinnabar");
    public static final RegistryObject<Item> CLUSTER_CINNABAR = ForgeRegistration.registerSimpleItem(ITEMS, "cluster_cinnabar");
    public static final RegistryObject<Item> CLUSTER_QUARTZ = ForgeRegistration.registerSimpleItem(ITEMS, "cluster_quartz");

    //

    public static final int SCRIBING_TOOLS_DURABILITY = 100;

    public static final RegistryObject<ThaumometerItem> THAUMOMETER = ForgeRegistration.registerItem(ITEMS,
            "thaumometer",
            ThaumometerItem::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

    public static final RegistryObject<Item> SCRIBING_TOOLS = ForgeRegistration.registerItem(ITEMS,
            "scribing_tools", Item::new, new Item.Properties().stacksTo(1).durability(SCRIBING_TOOLS_DURABILITY));

    public static final RegistryObject<CelestialNotesItem> CELESTIAL_NOTES = ForgeRegistration.registerItem(ITEMS,
            "celestial_notes",
            properties -> new CelestialNotesItem(
                    ItemDataDefaults.with(properties, TTDataComponents.CELESTIAL_BODY.get(), CelestialBody.SUN)),
            new Item.Properties());

    //

    public static final RegistryObject<PhialItem> PHIAL = ForgeRegistration.registerItem(ITEMS, "phial", PhialItem::new);

    public static final RegistryObject<ItemResearchNote> RESEARCH_NOTE =
            ForgeRegistration.registerItem(ITEMS, "research_note", ItemResearchNote::new, new Item.Properties().stacksTo(1));

    public static final RegistryObject<BlockItem> ARCANE_GRINDSTONE =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.ARCANE_GRINDSTONE);
    public static final RegistryObject<BlockItem> INFUSION_MATRIX =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.INFUSION_MATRIX);
    public static final RegistryObject<BlockItem> PEDESTAL_ARCANE =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.PEDESTAL_ARCANE);
    public static final RegistryObject<BlockItem> PEDESTAL_ANCIENT =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.PEDESTAL_ANCIENT);
    public static final RegistryObject<BlockItem> PEDESTAL_ELDRITCH =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.PEDESTAL_ELDRITCH);
    public static final RegistryObject<BlockItem> PILLAR_ARCANE = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.PILLAR_ARCANE);
    public static final RegistryObject<BlockItem> PILLAR_ANCIENT = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.PILLAR_ANCIENT);
    public static final RegistryObject<BlockItem> PILLAR_ELDRITCH =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.PILLAR_ELDRITCH);

    public static final RegistryObject<ItemWand> WAND = ForgeRegistration.registerItem(ITEMS,
            "wand", ItemWand::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

    public static final RegistryObject<ItemWandCap> WAND_CAP_IRON =
            ForgeRegistration.registerItem(ITEMS, "wand_cap_iron", props -> new ItemWandCap(props, TTWandParts.CAP_IRON));
    public static final RegistryObject<ItemWandCap> WAND_CAP_COPPER =
            ForgeRegistration.registerItem(ITEMS, "wand_cap_copper", props -> new ItemWandCap(props, TTWandParts.CAP_COPPER));
    public static final RegistryObject<ItemWandCap> WAND_CAP_GOLD =
            ForgeRegistration.registerItem(ITEMS, "wand_cap_gold", props -> new ItemWandCap(props, TTWandParts.CAP_GOLD));
    public static final RegistryObject<Item> WAND_CAP_SILVER_INERT = ForgeRegistration.registerSimpleItem(ITEMS, "wand_cap_silver_inert");
    public static final RegistryObject<ItemWandCap> WAND_CAP_SILVER =
            ForgeRegistration.registerItem(ITEMS, "wand_cap_silver", props -> new ItemWandCap(props, TTWandParts.CAP_SILVER));
    public static final RegistryObject<Item> WAND_CAP_THAUMIUM_INERT =
            ForgeRegistration.registerSimpleItem(ITEMS, "wand_cap_thaumium_inert");
    public static final RegistryObject<ItemWandCap> WAND_CAP_THAUMIUM =
            ForgeRegistration.registerItem(ITEMS, "wand_cap_thaumium", props -> new ItemWandCap(props, TTWandParts.CAP_THAUMIUM));
    public static final RegistryObject<Item> WAND_CAP_VOID_INERT = ForgeRegistration.registerSimpleItem(ITEMS, "wand_cap_void_inert");
    public static final RegistryObject<ItemWandCap> WAND_CAP_VOID =
            ForgeRegistration.registerItem(ITEMS, "wand_cap_void", props -> new ItemWandCap(props, TTWandParts.CAP_VOID));

    public static final RegistryObject<ItemWandRod> WAND_ROD_GREATWOOD =
            ForgeRegistration.registerItem(ITEMS, "wand_rod_greatwood", props -> new ItemWandRod(props, TTWandParts.ROD_GREATWOOD));
    public static final RegistryObject<ItemWandRod> WAND_ROD_OBSIDIAN =
            ForgeRegistration.registerItem(ITEMS, "wand_rod_obsidian", props -> new ItemWandRod(props, TTWandParts.ROD_OBSIDIAN));
    public static final RegistryObject<ItemWandRod> WAND_ROD_BLAZE =
            ForgeRegistration.registerItem(ITEMS, "wand_rod_blaze", props -> new ItemWandRod(props, TTWandParts.ROD_BLAZE));
    public static final RegistryObject<ItemWandRod> WAND_ROD_ICE =
            ForgeRegistration.registerItem(ITEMS, "wand_rod_ice", props -> new ItemWandRod(props, TTWandParts.ROD_ICE));
    public static final RegistryObject<ItemWandRod> WAND_ROD_QUARTZ =
            ForgeRegistration.registerItem(ITEMS, "wand_rod_quartz", props -> new ItemWandRod(props, TTWandParts.ROD_QUARTZ));
    public static final RegistryObject<ItemWandRod> WAND_ROD_BONE =
            ForgeRegistration.registerItem(ITEMS, "wand_rod_bone", props -> new ItemWandRod(props, TTWandParts.ROD_BONE));
    public static final RegistryObject<ItemWandRod> WAND_ROD_REED =
            ForgeRegistration.registerItem(ITEMS, "wand_rod_reed", props -> new ItemWandRod(props, TTWandParts.ROD_REED));
    public static final RegistryObject<ItemWandRod> WAND_ROD_SILVERWOOD =
            ForgeRegistration.registerItem(ITEMS, "wand_rod_silverwood", props -> new ItemWandRod(props, TTWandParts.ROD_SILVERWOOD));

    public static final RegistryObject<ItemWandRod> STAFF_ROD_GREATWOOD =
            ForgeRegistration.registerItem(ITEMS, "staff_rod_greatwood", props -> new ItemWandRod(props, TTWandParts.STAFF_GREATWOOD));
    public static final RegistryObject<ItemWandRod> STAFF_ROD_OBSIDIAN =
            ForgeRegistration.registerItem(ITEMS, "staff_rod_obsidian", props -> new ItemWandRod(props, TTWandParts.STAFF_OBSIDIAN));
    public static final RegistryObject<ItemWandRod> STAFF_ROD_BLAZE =
            ForgeRegistration.registerItem(ITEMS, "staff_rod_blaze", props -> new ItemWandRod(props, TTWandParts.STAFF_BLAZE));
    public static final RegistryObject<ItemWandRod> STAFF_ROD_ICE =
            ForgeRegistration.registerItem(ITEMS, "staff_rod_ice", props -> new ItemWandRod(props, TTWandParts.STAFF_ICE));
    public static final RegistryObject<ItemWandRod> STAFF_ROD_QUARTZ =
            ForgeRegistration.registerItem(ITEMS, "staff_rod_quartz", props -> new ItemWandRod(props, TTWandParts.STAFF_QUARTZ));
    public static final RegistryObject<ItemWandRod> STAFF_ROD_BONE =
            ForgeRegistration.registerItem(ITEMS, "staff_rod_bone", props -> new ItemWandRod(props, TTWandParts.STAFF_BONE));
    public static final RegistryObject<ItemWandRod> STAFF_ROD_REED =
            ForgeRegistration.registerItem(ITEMS, "staff_rod_reed", props -> new ItemWandRod(props, TTWandParts.STAFF_REED));
    public static final RegistryObject<ItemWandRod> STAFF_ROD_SILVERWOOD =
            ForgeRegistration.registerItem(ITEMS, "staff_rod_silverwood", props -> new ItemWandRod(props, TTWandParts.STAFF_SILVERWOOD));
    public static final RegistryObject<ItemWandRod> STAFF_ROD_PRIMAL = ForgeRegistration.registerItem(ITEMS,
            "staff_rod_primal",
            props -> new ItemWandRod(props, TTWandParts.STAFF_PRIMAL),
            new Item.Properties().rarity(Rarity.RARE));

    public static final RegistryObject<ItemPrimalCharm> PRIMAL_CHARM = ForgeRegistration.registerItem(ITEMS,
            "primal_charm",
            ItemPrimalCharm::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

    public static final RegistryObject<BlockItem> VIS_RELAY = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.VIS_RELAY);
    public static final RegistryObject<BlockItem> NODE_STABILIZER =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.NODE_STABILIZER);
    public static final RegistryObject<BlockItem> NODE_STABILIZER_ADVANCED =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.NODE_STABILIZER_ADVANCED);
    public static final RegistryObject<BlockItem> NODE_TRANSDUCER =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.NODE_TRANSDUCER);
    public static final RegistryObject<JarNodeItem> JAR_NODE =
            ForgeRegistration.registerItem(ITEMS, "jar_node", props -> new JarNodeItem(TTBlocks.JAR_NODE.get(), props));

    public static final RegistryObject<FocusItem> FOCUS_1 = ForgeRegistration.registerItem(ITEMS,
            "focus_1", FocusItem::new, new Item.Properties().stacksTo(1).rarity(Rarity.RARE));

    public static final RegistryObject<FocusItem> FOCUS_2 = ForgeRegistration.registerItem(ITEMS,
            "focus_2", FocusItem::new, new Item.Properties().stacksTo(1).rarity(Rarity.RARE));

    public static final RegistryObject<FocusItem> FOCUS_3 = ForgeRegistration.registerItem(ITEMS,
            "focus_3", FocusItem::new, new Item.Properties().stacksTo(1).rarity(Rarity.RARE));

    public static final RegistryObject<ThrowableItem> CAUSALITY_COLLAPSER = ForgeRegistration.registerItem(ITEMS,
            "causality_collapser",
            props -> new ThrowableItem(props, EntityCausalityCollapser::new, EntityCausalityCollapser.THROW),
            new Item.Properties().stacksTo(16));

    public static final RegistryObject<Item> VOID_SEED =
            ForgeRegistration.registerItem(ITEMS, "void_seed", Item::new, new Item.Properties().rarity(Rarity.UNCOMMON));

    public static final RegistryObject<PrimordialPearlItem> PRIMORDIAL_PEARL = ForgeRegistration.registerItem(ITEMS,
            "primordial_pearl",
            PrimordialPearlItem::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON).durability(PrimordialPearlItem.MAX_DAMAGE));

    //

    public static final int GOGGLES_DURABILITY = 350;

    public static final int GOGGLES_ENCHANTMENT_VALUE = 25;

    public static final ResourceLocation GOGGLES_REVEALING_ID = TTIds.rl("goggles_revealing");

    public static final RegistryObject<GogglesItem> GOGGLES_REVEALING = ForgeRegistration.registerItem(ITEMS,
            "goggles_revealing",
            GogglesItem::new,
            new Item.Properties()
                    .stacksTo(1)
                    .durability(GOGGLES_DURABILITY)
                    .rarity(Rarity.RARE)
                    .attributes(ItemAttributeModifiers.builder()
                            .add(
                                    Attributes.ARMOR,
                                    NamedAttributeModifiers.create(
                                            GOGGLES_REVEALING_ID, 1, AttributeModifier.Operation.ADDITION),
                                    EquipmentSlotGroup.HEAD)
                            .add(
                                    Attributes.ARMOR_TOUGHNESS,
                                    NamedAttributeModifiers.create(
                                            GOGGLES_REVEALING_ID, 1, AttributeModifier.Operation.ADDITION),
                                    EquipmentSlotGroup.HEAD)
                            .build()));

    public static final RegistryObject<FortressArmorItem> FORTRESS_HELM = ForgeRegistration.registerItem(ITEMS,
            "fortress_helm",
            props -> new FortressArmorItem(
                    TTMaterials.ARMOR_FORTRESS,
                    ArmorItem.Type.HELMET,
                    props.durability(ArmorTypes.durability(ArmorItem.Type.HELMET, TTMaterials.DURABILITY_FORTRESS))
                            .rarity(Rarity.RARE)));
    public static final RegistryObject<FortressArmorItem> FORTRESS_CHEST = ForgeRegistration.registerItem(ITEMS,
            "fortress_chest",
            props -> new FortressArmorItem(
                    TTMaterials.ARMOR_FORTRESS,
                    ArmorItem.Type.CHESTPLATE,
                    props.durability(ArmorTypes.durability(ArmorItem.Type.CHESTPLATE, TTMaterials.DURABILITY_FORTRESS))
                            .rarity(Rarity.RARE)));
    public static final RegistryObject<FortressArmorItem> FORTRESS_LEGS = ForgeRegistration.registerItem(ITEMS,
            "fortress_legs",
            props -> new FortressArmorItem(
                    TTMaterials.ARMOR_FORTRESS,
                    ArmorItem.Type.LEGGINGS,
                    props.durability(ArmorTypes.durability(ArmorItem.Type.LEGGINGS, TTMaterials.DURABILITY_FORTRESS))
                            .rarity(Rarity.RARE)));

    public static final RegistryObject<VoidRobeArmorItem> VOID_ROBE_HELM = ForgeRegistration.registerItem(ITEMS,
            "void_robe_helm",
            props -> new VoidRobeArmorItem(
                    TTMaterials.ARMOR_VOID_ROBE,
                    ArmorItem.Type.HELMET,
                    ItemDataDefaults.with(props, TTDataComponents.GOGGLES_UPGRADE.get(), Unit.INSTANCE)
                            .durability(ArmorTypes.durability(ArmorItem.Type.HELMET, TTMaterials.DURABILITY_VOID_ROBE))
                            .rarity(Rarity.EPIC)));
    public static final RegistryObject<VoidRobeArmorItem> VOID_ROBE_CHEST = ForgeRegistration.registerItem(ITEMS,
            "void_robe_chest",
            props -> new VoidRobeArmorItem(
                    TTMaterials.ARMOR_VOID_ROBE,
                    ArmorItem.Type.CHESTPLATE,
                    props.durability(ArmorTypes.durability(ArmorItem.Type.CHESTPLATE, TTMaterials.DURABILITY_VOID_ROBE))
                            .rarity(Rarity.EPIC)));
    public static final RegistryObject<VoidRobeArmorItem> VOID_ROBE_LEGS = ForgeRegistration.registerItem(ITEMS,
            "void_robe_legs",
            props -> new VoidRobeArmorItem(
                    TTMaterials.ARMOR_VOID_ROBE,
                    ArmorItem.Type.LEGGINGS,
                    props.durability(ArmorTypes.durability(ArmorItem.Type.LEGGINGS, TTMaterials.DURABILITY_VOID_ROBE))
                            .rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> CRIMSON_PRAETOR_HELM = ForgeRegistration.registerItem(ITEMS,
            "crimson_praetor_helm",
            props -> new ArmorItem(
                    TTMaterials.ARMOR_CULTIST_LEADER,
                    ArmorItem.Type.HELMET,
                    props.durability(ArmorTypes.durability(ArmorItem.Type.HELMET, TTMaterials.DURABILITY_CULTIST_LEADER))
                            .rarity(Rarity.RARE)));
    public static final RegistryObject<Item> CRIMSON_PRAETOR_CHEST = ForgeRegistration.registerItem(ITEMS,
            "crimson_praetor_chest",
            props -> new ArmorItem(
                    TTMaterials.ARMOR_CULTIST_LEADER,
                    ArmorItem.Type.CHESTPLATE,
                    props.durability(ArmorTypes.durability(ArmorItem.Type.CHESTPLATE, TTMaterials.DURABILITY_CULTIST_LEADER))
                            .rarity(Rarity.RARE)));
    public static final RegistryObject<Item> CRIMSON_PRAETOR_LEGS = ForgeRegistration.registerItem(ITEMS,
            "crimson_praetor_legs",
            props -> new ArmorItem(
                    TTMaterials.ARMOR_CULTIST_LEADER,
                    ArmorItem.Type.LEGGINGS,
                    props.durability(ArmorTypes.durability(ArmorItem.Type.LEGGINGS, TTMaterials.DURABILITY_CULTIST_LEADER))
                            .rarity(Rarity.RARE)));

    public static final int TRINKET_APPRENTICE_DISCOUNT = 5;
    private static final int AMULET_VIS_FOUND_INTERVAL = 100;
    private static final int AMULET_VIS_CRAFTED_INTERVAL = 20;

    public static final RegistryObject<TrinketItem> AMULET_MUNDANE =
            ForgeRegistration.registerItem(ITEMS, "amulet_mundane", TrinketItem::new, new Item.Properties().stacksTo(1));
    public static final RegistryObject<TrinketItem> RING_MUNDANE =
            ForgeRegistration.registerItem(ITEMS, "ring_mundane", TrinketItem::new, new Item.Properties().stacksTo(1));
    public static final RegistryObject<TrinketItem> GIRDLE_MUNDANE =
            ForgeRegistration.registerItem(ITEMS, "girdle_mundane", TrinketItem::new, new Item.Properties().stacksTo(1));
    public static final RegistryObject<TrinketItem> RING_APPRENTICE = ForgeRegistration.registerItem(ITEMS,
            "ring_apprentice",
            props -> new TrinketItem(props, TRINKET_APPRENTICE_DISCOUNT),
            new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final RegistryObject<TrinketItem> AMULET_FANCY = ForgeRegistration.registerItem(ITEMS,
            "amulet_fancy", TrinketItem::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final RegistryObject<TrinketItem> RING_FANCY = ForgeRegistration.registerItem(ITEMS,
            "ring_fancy", TrinketItem::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final RegistryObject<TrinketItem> GIRDLE_FANCY = ForgeRegistration.registerItem(ITEMS,
            "girdle_fancy", TrinketItem::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

    public static final RegistryObject<AmuletVisItem> AMULET_VIS = ForgeRegistration.registerItem(ITEMS,
            "amulet_vis",
            props -> new AmuletVisItem(props, AMULET_VIS_FOUND_INTERVAL),
            new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final RegistryObject<AmuletVisItem> AMULET_VIS_CRAFTED = ForgeRegistration.registerItem(ITEMS,
            "amulet_vis_crafted",
            props -> new AmuletVisItem(props, AMULET_VIS_CRAFTED_INTERVAL),
            new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
    public static final RegistryObject<Item> CHARM_UNDYING = ForgeRegistration.registerItem(ITEMS,
            "charm_undying", Item::new, new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
    public static final RegistryObject<Item> CLOUD_RING = ForgeRegistration.registerItem(ITEMS,
            "cloud_ring", Item::new, new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
    public static final RegistryObject<Item> CURIOSITY_BAND = ForgeRegistration.registerItem(ITEMS,
            "curiosity_band", Item::new, new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
    public static final RegistryObject<VerdantCharmItem> VERDANT_CHARM = ForgeRegistration.registerItem(ITEMS,
            "verdant_charm",
            VerdantCharmItem::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
    public static final RegistryObject<VoidseerCharmItem> VOIDSEER_CHARM = ForgeRegistration.registerItem(ITEMS,
            "voidseer_charm",
            VoidseerCharmItem::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
    public static final RegistryObject<FocusPouchItem> FOCUS_POUCH = ForgeRegistration.registerItem(ITEMS,
            "focus_pouch",
            FocusPouchItem::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final RegistryObject<BlockItem> ACTIVATOR_RAIL = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.ACTIVATOR_RAIL);
    public static final RegistryObject<BlockItem> OBSIDIAN_TILE = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.OBSIDIAN_TILE);
    public static final RegistryObject<BlockItem> OBSIDIAN_TOTEM = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.OBSIDIAN_TOTEM);
    public static final RegistryObject<BlockItem> ELDRITCH_STONE = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.ELDRITCH_STONE);
    public static final RegistryObject<BlockItem> ELDRITCH_STONE_INERT =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.ELDRITCH_STONE_INERT);
    public static final RegistryObject<BlockItem> ELDRITCH_ROCK = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.ELDRITCH_ROCK);
    public static final RegistryObject<BlockItem> ELDRITCH_CRUST = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.ELDRITCH_CRUST);
    public static final RegistryObject<BlockItem> ELDRITCH_CRUST_GLOWING =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.ELDRITCH_CRUST_GLOWING);
    public static final RegistryObject<BlockItem> STAIRS_ELDRITCH =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.STAIRS_ELDRITCH);
    public static final RegistryObject<BlockItem> STONE_ELDRITCH_PILLAR =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.STONE_ELDRITCH_PILLAR);
    public static final RegistryObject<BlockItem> STAIRS_ELDRITCH_TILE =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.STAIRS_ELDRITCH_TILE);
    public static final RegistryObject<BlockItem> STAIRS_ELDRITCH_ROCK =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.STAIRS_ELDRITCH_ROCK);
    public static final RegistryObject<BlockItem> ELDRITCH_DOOR = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.ELDRITCH_DOOR);
    public static final RegistryObject<BlockItem> ELDRITCH_PEDESTAL =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.ELDRITCH_PEDESTAL);
    public static final RegistryObject<BlockItem> ELDRITCH_STONE_CRYSTAL =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.ELDRITCH_STONE_CRYSTAL);
    public static final RegistryObject<BlockItem> ELDRITCH_LOCK = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.ELDRITCH_LOCK);
    public static final RegistryObject<BlockItem> ELDRITCH_CRAB_SPAWNER =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.ELDRITCH_CRAB_SPAWNER);
    public static final RegistryObject<BlockItem> ELDRITCH_TRAP = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.ELDRITCH_TRAP);
    public static final RegistryObject<BlockItem> ELDRITCH_ALTAR = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.ELDRITCH_ALTAR);
    public static final RegistryObject<BlockItem> ELDRITCH_OBELISK =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.ELDRITCH_OBELISK);
    public static final RegistryObject<BlockItem> ELDRITCH_PILLAR =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.ELDRITCH_PILLAR);
    public static final RegistryObject<BlockItem> ELDRITCH_CAPSTONE =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.ELDRITCH_CAPSTONE);
    public static final RegistryObject<Item> ELDRITCH_EYE =
            ForgeRegistration.registerItem(ITEMS, "eldritch_eye", Item::new, new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final RegistryObject<RunedTabletItem> RUNED_TABLET =
            ForgeRegistration.registerItem(ITEMS, "runed_tablet", RunedTabletItem::new, new Item.Properties().rarity(Rarity.RARE));
    public static final RegistryObject<BlockItem> SLAB_GREATWOOD = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.SLAB_GREATWOOD);
    public static final RegistryObject<BlockItem> SLAB_SILVERWOOD =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.SLAB_SILVERWOOD);
    public static final RegistryObject<BlockItem> SLAB_ARCANE_STONE =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.SLAB_ARCANE_STONE);
    public static final RegistryObject<BlockItem> SLAB_ARCANE_BRICK =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.SLAB_ARCANE_BRICK);
    public static final RegistryObject<BlockItem> SLAB_ANCIENT = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.SLAB_ANCIENT);
    public static final RegistryObject<BlockItem> SLAB_ELDRITCH = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.SLAB_ELDRITCH);
    public static final RegistryObject<BlockItem> STAIRS_GREATWOOD =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.STAIRS_GREATWOOD);
    public static final RegistryObject<BlockItem> STAIRS_SILVERWOOD =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.STAIRS_SILVERWOOD);
    public static final RegistryObject<DoubleHighBlockItem> DOOR_GREATWOOD = ForgeRegistration.registerItem(ITEMS,
            "door_greatwood", props -> new DoubleHighBlockItem(TTBlocks.DOOR_GREATWOOD.get(), props));
    public static final RegistryObject<BlockItem> TRAPDOOR_GREATWOOD =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.TRAPDOOR_GREATWOOD);
    public static final RegistryObject<BlockItem> FENCE_GREATWOOD =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.FENCE_GREATWOOD);
    public static final RegistryObject<BlockItem> FENCE_GATE_GREATWOOD =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.FENCE_GATE_GREATWOOD);
    public static final RegistryObject<BlockItem> BUTTON_GREATWOOD =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.BUTTON_GREATWOOD);
    public static final RegistryObject<BlockItem> PRESSURE_PLATE_GREATWOOD =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.PRESSURE_PLATE_GREATWOOD);
    public static final RegistryObject<DoubleHighBlockItem> DOOR_SILVERWOOD = ForgeRegistration.registerItem(ITEMS,
            "door_silverwood", props -> new DoubleHighBlockItem(TTBlocks.DOOR_SILVERWOOD.get(), props));
    public static final RegistryObject<BlockItem> TRAPDOOR_SILVERWOOD =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.TRAPDOOR_SILVERWOOD);
    public static final RegistryObject<BlockItem> FENCE_SILVERWOOD =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.FENCE_SILVERWOOD);
    public static final RegistryObject<BlockItem> FENCE_GATE_SILVERWOOD =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.FENCE_GATE_SILVERWOOD);
    public static final RegistryObject<BlockItem> BUTTON_SILVERWOOD =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.BUTTON_SILVERWOOD);
    public static final RegistryObject<BlockItem> PRESSURE_PLATE_SILVERWOOD =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.PRESSURE_PLATE_SILVERWOOD);
    public static final RegistryObject<BlockItem> TABLE_WOOD = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.TABLE_WOOD);
    public static final RegistryObject<BlockItem> TABLE_STONE = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.TABLE_STONE);
    public static final RegistryObject<BlockItem> PAVING_STONE_TRAVEL =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.PAVING_STONE_TRAVEL);
    public static final RegistryObject<BlockItem> PAVING_STONE_BARRIER =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.PAVING_STONE_BARRIER);
    public static final RegistryObject<BlockItem> AMBER_BRICK = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.AMBER_BRICK);
    public static final RegistryObject<BlockItem> FLESH_BLOCK = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.FLESH_BLOCK);
    public static final RegistryObject<TurretPlacerItem> TURRET_BASIC = ForgeRegistration.registerItem(ITEMS,
            "turret_basic",
            props -> new TurretPlacerItem(props, ConstructDeployment.upright(TTEntities.TURRET_CROSSBOW)),
            new Item.Properties().stacksTo(16));
    public static final RegistryObject<TurretPlacerItem> TURRET_ADVANCED = ForgeRegistration.registerItem(ITEMS,
            "turret_advanced",
            props -> new TurretPlacerItem(props, ConstructDeployment.upright(TTEntities.TURRET_CROSSBOW_ADVANCED)),
            new Item.Properties().stacksTo(16));
    public static final RegistryObject<ArcaneBoreItem> ARCANE_BORE = ForgeRegistration.registerItem(ITEMS,
            "arcane_bore",
            props -> new ArcaneBoreItem(TTBlocks.ARCANE_BORE.get(), props),
            new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON));
    public static final RegistryObject<GrappleGunItem> GRAPPLE_GUN = ForgeRegistration.registerItem(ITEMS,
            "grapple_gun",
            GrappleGunItem::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final RegistryObject<Item> GRAPPLE_GUN_TIP = ForgeRegistration.registerSimpleItem(ITEMS, "grapple_gun_tip");
    public static final RegistryObject<Item> GRAPPLE_GUN_SPOOL = ForgeRegistration.registerSimpleItem(ITEMS, "grapple_gun_spool");
    public static final RegistryObject<Item> SANITY_CHECKER = ForgeRegistration.registerItem(ITEMS,
            "sanity_checker", Item::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final RegistryObject<ItemResonator> RESONATOR = ForgeRegistration.registerItem(ITEMS,
            "resonator", ItemResonator::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

    public static final RegistryObject<ItemCurio> CURIO_ARCANE = registerCurio("curio_arcane", ItemCurio.Variant.ARCANE);
    public static final RegistryObject<ItemCurio> CURIO_PRESERVED =
            registerCurio("curio_preserved", ItemCurio.Variant.PRESERVED);
    public static final RegistryObject<ItemCurio> CURIO_ANCIENT =
            registerCurio("curio_ancient", ItemCurio.Variant.ANCIENT);
    public static final RegistryObject<ItemCurio> CURIO_ELDRITCH =
            registerCurio("curio_eldritch", ItemCurio.Variant.ELDRITCH);
    public static final RegistryObject<ItemCurio> CURIO_KNOWLEDGE =
            registerCurio("curio_knowledge", ItemCurio.Variant.KNOWLEDGE);
    public static final RegistryObject<ItemCurio> CURIO_TWISTED =
            registerCurio("curio_twisted", ItemCurio.Variant.TWISTED);
    public static final RegistryObject<ItemCurio> CURIO_RITES = registerCurio("curio_rites", ItemCurio.Variant.RITES);

    public static final RegistryObject<ItemCreativeFluxSponge> CREATIVE_FLUX_SPONGE = ForgeRegistration.registerItem(ITEMS,
            "creative_flux_sponge",
            ItemCreativeFluxSponge::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    public static final RegistryObject<ItemBlockMirror> MIRROR = ForgeRegistration.registerItem(ITEMS,
            "mirror",
            props -> new ItemBlockMirror(TTBlocks.MIRROR.get(), props),
            new Item.Properties().rarity(Rarity.UNCOMMON));

    public static final RegistryObject<ItemBlockMirror> MIRROR_ESSENTIA = ForgeRegistration.registerItem(ITEMS,
            "mirror_essentia",
            props -> new ItemBlockMirror(TTBlocks.MIRROR_ESSENTIA.get(), props),
            new Item.Properties().rarity(Rarity.UNCOMMON));

    public static final RegistryObject<ItemHandMirror> HAND_MIRROR = ForgeRegistration.registerItem(ITEMS,
            "hand_mirror",
            ItemHandMirror::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

    private static RegistryObject<ItemCurio> registerCurio(String name, ItemCurio.Variant variant) {
        return ForgeRegistration.registerItem(ITEMS,
                name, props -> new ItemCurio(props, variant), new Item.Properties().rarity(Rarity.UNCOMMON));
    }

    private TTItems() {}

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }

    public static <T extends BlockItem> RegistryObject<BlockItem> registerSimpleBlockItem(
            RegistryObject<? extends Block> block, BiFunction<Block, Item.Properties, T> constructor) {
        return ForgeRegistration.registerItem(ITEMS,
                block.getId().getPath(),
                p -> constructor.apply(block.get(), p),
                new Item.Properties());
    }

    public static final RegistryObject<BlockItem> WALL_ARCANE_STONE =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.WALL_ARCANE_STONE);
    public static final RegistryObject<BlockItem> WALL_ARCANE_BRICK =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.WALL_ARCANE_BRICK);
    public static final RegistryObject<BlockItem> STONE_ARCANE_PILLAR =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.STONE_ARCANE_PILLAR);
    public static final RegistryObject<BlockItem> WALL_ANCIENT = ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.WALL_ANCIENT);
    public static final RegistryObject<BlockItem> STAIRS_ANCIENT_TILE =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.STAIRS_ANCIENT_TILE);
    public static final RegistryObject<BlockItem> SLAB_ANCIENT_TILE =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.SLAB_ANCIENT_TILE);
    public static final RegistryObject<BlockItem> WALL_ANCIENT_TILE =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.WALL_ANCIENT_TILE);
    public static final RegistryObject<BlockItem> STAIRS_ANCIENT_ROCK =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.STAIRS_ANCIENT_ROCK);
    public static final RegistryObject<BlockItem> SLAB_ANCIENT_ROCK =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.SLAB_ANCIENT_ROCK);
    public static final RegistryObject<BlockItem> WALL_ANCIENT_ROCK =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.WALL_ANCIENT_ROCK);
    public static final RegistryObject<BlockItem> STONE_ANCIENT_PILLAR =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.STONE_ANCIENT_PILLAR);
    public static final RegistryObject<BlockItem> SLAB_ELDRITCH_STONE =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.SLAB_ELDRITCH_STONE);
    public static final RegistryObject<BlockItem> WALL_ELDRITCH_STONE =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.WALL_ELDRITCH_STONE);
    public static final RegistryObject<BlockItem> WALL_ELDRITCH_TILE =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.WALL_ELDRITCH_TILE);
    public static final RegistryObject<BlockItem> SLAB_ELDRITCH_ROCK =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.SLAB_ELDRITCH_ROCK);
    public static final RegistryObject<BlockItem> WALL_ELDRITCH_ROCK =
            ForgeRegistration.registerSimpleBlockItem(ITEMS, TTBlocks.WALL_ELDRITCH_ROCK);
}
