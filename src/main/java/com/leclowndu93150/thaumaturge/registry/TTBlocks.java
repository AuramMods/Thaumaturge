package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.content.alchemy.BlockLiquidDeath;
import com.leclowndu93150.thaumaturge.content.aura.BlockRechargePedestal;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockJarNode;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockNode;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockNodeStabilizer;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockNodeTransducer;
import com.leclowndu93150.thaumaturge.content.aura.relay.BlockVisRelay;
import com.leclowndu93150.thaumaturge.content.crucible.BlockCrucible;
import com.leclowndu93150.thaumaturge.content.decor.BlockAmber;
import com.leclowndu93150.thaumaturge.content.decor.BlockBarrier;
import com.leclowndu93150.thaumaturge.content.decor.BlockCandle;
import com.leclowndu93150.thaumaturge.content.decor.BlockCandleHolder;
import com.leclowndu93150.thaumaturge.content.decor.BlockEffectShock;
import com.leclowndu93150.thaumaturge.content.decor.BlockObsidianTotem;
import com.leclowndu93150.thaumaturge.content.decor.BlockObsidianTotemCharged;
import com.leclowndu93150.thaumaturge.content.decor.BlockPavingStone;
import com.leclowndu93150.thaumaturge.content.decor.BlockStairsTT;
import com.leclowndu93150.thaumaturge.content.decor.BlockTable;
import com.leclowndu93150.thaumaturge.content.decor.CandleHolderMaterial;
import com.leclowndu93150.thaumaturge.content.decor.banner.BannerStandingBlock;
import com.leclowndu93150.thaumaturge.content.decor.banner.BannerWallBlock;
import com.leclowndu93150.thaumaturge.content.device.BlockArcaneEar;
import com.leclowndu93150.thaumaturge.content.device.BlockCondenser;
import com.leclowndu93150.thaumaturge.content.device.BlockCondenserLattice;
import com.leclowndu93150.thaumaturge.content.device.BlockDioptra;
import com.leclowndu93150.thaumaturge.content.device.BlockEverfullUrn;
import com.leclowndu93150.thaumaturge.content.device.BlockHungryChest;
import com.leclowndu93150.thaumaturge.content.device.BlockInlay;
import com.leclowndu93150.thaumaturge.content.device.BlockItemGrate;
import com.leclowndu93150.thaumaturge.content.device.BlockLampArcane;
import com.leclowndu93150.thaumaturge.content.device.BlockLampFertility;
import com.leclowndu93150.thaumaturge.content.device.BlockLampGrowth;
import com.leclowndu93150.thaumaturge.content.device.BlockRedstoneRelay;
import com.leclowndu93150.thaumaturge.content.device.BlockStabilizer;
import com.leclowndu93150.thaumaturge.content.device.BlockVisBattery;
import com.leclowndu93150.thaumaturge.content.device.BlockVisGenerator;
import com.leclowndu93150.thaumaturge.content.device.BlockVoidSiphon;
import com.leclowndu93150.thaumaturge.content.device.bore.BlockArcaneBore;
import com.leclowndu93150.thaumaturge.content.device.fluxscrubber.BlockFluxScrubber;
import com.leclowndu93150.thaumaturge.content.device.levitator.BlockLevitator;
import com.leclowndu93150.thaumaturge.content.device.mirror.BlockMirror;
import com.leclowndu93150.thaumaturge.content.device.patterncrafter.BlockPatternCrafter;
import com.leclowndu93150.thaumaturge.content.device.sprayer.BlockPotionSprayer;
import com.leclowndu93150.thaumaturge.content.eldritch.altar.BlockEldritchAltar;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEldritchCap;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEldritchCrabSpawner;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEldritchInset;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEldritchNothing;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEldritchNothingDormant;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEldritchObelisk;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEldritchPillar;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEldritchTrap;
import com.leclowndu93150.thaumaturge.content.eldritch.lock.BlockEldritchDoor;
import com.leclowndu93150.thaumaturge.content.eldritch.lock.BlockEldritchLock;
import com.leclowndu93150.thaumaturge.content.eldritch.portal.BlockEldritchPortal;
import com.leclowndu93150.thaumaturge.content.eldritch.reliquary.BlockEldritchReliquary;
import com.leclowndu93150.thaumaturge.content.equipment.BlockEffectGlimmer;
import com.leclowndu93150.thaumaturge.content.essentia.BlockCentrifuge;
import com.leclowndu93150.thaumaturge.content.essentia.BlockEssentiaPort;
import com.leclowndu93150.thaumaturge.content.essentia.advancedfurnace.BlockAdvancedAlchemicalFurnace;
import com.leclowndu93150.thaumaturge.content.essentia.advancedfurnace.BlockAdvancedFurnaceNozzle;
import com.leclowndu93150.thaumaturge.content.essentia.bellows.BlockBellows;
import com.leclowndu93150.thaumaturge.content.essentia.crystalizer.BlockEssentiaCrystalizer;
import com.leclowndu93150.thaumaturge.content.essentia.jar.BlockJar;
import com.leclowndu93150.thaumaturge.content.essentia.jar.BlockJarBrain;
import com.leclowndu93150.thaumaturge.content.essentia.jar.BlockJarCreative;
import com.leclowndu93150.thaumaturge.content.essentia.jar.BlockJarVoid;
import com.leclowndu93150.thaumaturge.content.essentia.reservoir.BlockEssentiaReservoir;
import com.leclowndu93150.thaumaturge.content.essentia.smeltery.BlockAlembic;
import com.leclowndu93150.thaumaturge.content.essentia.smeltery.BlockSmelter;
import com.leclowndu93150.thaumaturge.content.essentia.smeltery.BlockSmelterAux;
import com.leclowndu93150.thaumaturge.content.essentia.smeltery.BlockSmelterVent;
import com.leclowndu93150.thaumaturge.content.essentia.thaumatorium.BlockBrainBox;
import com.leclowndu93150.thaumaturge.content.essentia.thaumatorium.BlockThaumatorium;
import com.leclowndu93150.thaumaturge.content.essentia.thaumatorium.BlockThaumatoriumTop;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockTube;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockTubeBuffer;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockTubeFilter;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockTubeOneway;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockTubeRestrict;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockTubeValve;
import com.leclowndu93150.thaumaturge.content.golem.BlockGolemFetter;
import com.leclowndu93150.thaumaturge.content.golem.press.BlockGolemBuilder;
import com.leclowndu93150.thaumaturge.content.infernalfurnace.BlockInfernalFurnace;
import com.leclowndu93150.thaumaturge.content.infernalfurnace.BlockPlaceholder;
import com.leclowndu93150.thaumaturge.content.infusion.BlockInfusionMatrix;
import com.leclowndu93150.thaumaturge.content.infusion.BlockPedestal;
import com.leclowndu93150.thaumaturge.content.infusion.BlockPillar;
import com.leclowndu93150.thaumaturge.content.infusion.grindstone.BlockArcaneGrindstone;
import com.leclowndu93150.thaumaturge.content.manabean.BlockManaPod;
import com.leclowndu93150.thaumaturge.content.metal.BlockMetalTT;
import com.leclowndu93150.thaumaturge.content.misc.nitor.BlockNitor;
import com.leclowndu93150.thaumaturge.content.research.decon.BlockDeconstructionTable;
import com.leclowndu93150.thaumaturge.content.research.table.BlockResearchTable;
import com.leclowndu93150.thaumaturge.content.spa.BlockPurifyingFluid;
import com.leclowndu93150.thaumaturge.content.spa.BlockSpa;
import com.leclowndu93150.thaumaturge.content.spell.block.BlockEffectSap;
import com.leclowndu93150.thaumaturge.content.spell.block.BlockHole;
import com.leclowndu93150.thaumaturge.content.spell.manipulator.BlockFocalManipulator;
import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintCrust;
import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintFeature;
import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintFibre;
import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintGeyser;
import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintLog;
import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintRock;
import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintSoil;
import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintSporeStalk;
import com.leclowndu93150.thaumaturge.content.taint.ecology.BlockEtherealBloom;
import com.leclowndu93150.thaumaturge.content.taint.flux.BlockFluxGas;
import com.leclowndu93150.thaumaturge.content.taint.flux.BlockFluxGoo;
import com.leclowndu93150.thaumaturge.content.taint.flux.FluxGooRefs;
import com.leclowndu93150.thaumaturge.content.warding.BlockArcaneDoor;
import com.leclowndu93150.thaumaturge.content.warding.BlockArcanePressurePlate;
import com.leclowndu93150.thaumaturge.content.warding.BlockWardedGlass;
import com.leclowndu93150.thaumaturge.content.workbench.BlockArcaneWorkbench;
import com.leclowndu93150.thaumaturge.content.workbench.BlockArcaneWorkbenchCharger;
import com.leclowndu93150.thaumaturge.content.world.crystal.BlockCrystal;
import com.leclowndu93150.thaumaturge.content.world.mound.BlockLoot;
import com.leclowndu93150.thaumaturge.content.world.plant.BlockGrassAmbient;
import com.leclowndu93150.thaumaturge.content.world.plant.MagicPlantBlock;
import com.leclowndu93150.thaumaturge.content.world.plant.MagicPlantTraits;
import com.leclowndu93150.thaumaturge.content.world.tree.BlockGreatwoodLog;
import com.leclowndu93150.thaumaturge.content.world.tree.BlockSaplingTT;
import com.leclowndu93150.thaumaturge.content.world.tree.BlockSilverwoodNodeLog;
import com.leclowndu93150.thaumaturge.content.world.tree.TTTreeGrowers;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public final class TTBlocks {
    private static final int WOODEN_BUTTON_PRESS_TICKS = 30;

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(net.minecraft.core.registries.Registries.BLOCK, TTIds.MODID);

    public static final RegistryObject<BlockWardedGlass> WARDED_GLASS = ForgeRegistration.registerBlock(BLOCKS,
            "warded_glass",
            BlockWardedGlass::new,
            BlockBehaviour.Properties.of().strength(0.3F).sound(SoundType.GLASS).noOcclusion());
    public static final RegistryObject<BlockArcaneDoor> ARCANE_DOOR = ForgeRegistration.registerBlock(BLOCKS,
            "arcane_door",
            BlockArcaneDoor::new,
            BlockBehaviour.Properties.of().strength(3.0F).sound(SoundType.WOOD).noOcclusion());
    public static final RegistryObject<BlockArcanePressurePlate> ARCANE_PRESSURE_PLATE = ForgeRegistration.registerBlock(BLOCKS,
            "arcane_pressure_plate",
            BlockArcanePressurePlate::new,
            BlockBehaviour.Properties.of().strength(0.5F).sound(SoundType.WOOD).noCollission());
    public static final RegistryObject<BlockGolemFetter> GOLEM_FETTER = ForgeRegistration.registerBlock(BLOCKS,
            "golem_fetter",
            BlockGolemFetter::new,
            BlockBehaviour.Properties.of().strength(2.0F).sound(SoundType.STONE));
    public static final RegistryObject<Block> TALLOW_BLOCK = ForgeRegistration.registerSimpleBlock(BLOCKS,
            "tallow_block", BlockBehaviour.Properties.of().strength(0.5F).sound(SoundType.HONEY_BLOCK));
    public static final RegistryObject<BlockItemGrate> ITEM_GRATE = ForgeRegistration.registerBlock(BLOCKS,
            "item_grate",
            BlockItemGrate::new,
            BlockBehaviour.Properties.of().strength(2.0F).sound(SoundType.METAL).noOcclusion());

    public static final RegistryObject<BlockFocalManipulator> FOCAL_MANIPULATOR = ForgeRegistration.registerBlock(BLOCKS,
            "focal_manipulator",
            BlockFocalManipulator::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.0F, 20.0F)
                    .sound(SoundType.STONE)
                    .noOcclusion());

    public static final RegistryObject<BlockResearchTable> RESEARCH_TABLE = ForgeRegistration.registerBlock(BLOCKS,
            "research_table",
            BlockResearchTable::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(1.5F, 2.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
                    .pushReaction(PushReaction.BLOCK));

    public static final RegistryObject<BlockDeconstructionTable> DECONSTRUCTION_TABLE = ForgeRegistration.registerBlock(BLOCKS,
            "deconstruction_table",
            BlockDeconstructionTable::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.5F)
                    .sound(SoundType.WOOD)
                    .noOcclusion());

    public static final RegistryObject<BlockArcaneWorkbench> ARCANE_WORKBENCH = ForgeRegistration.registerBlock(BLOCKS,
            "arcane_workbench",
            BlockArcaneWorkbench::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 5.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion());

    public static final RegistryObject<BlockCrucible> CRUCIBLE = ForgeRegistration.registerBlock(BLOCKS,
            "crucible",
            BlockCrucible::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(2.0F, 20.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion());

    public static final RegistryObject<BlockArcaneWorkbenchCharger> ARCANE_WORKBENCH_CHARGER = ForgeRegistration.registerBlock(BLOCKS,
            "arcane_workbench_charger",
            BlockArcaneWorkbenchCharger::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(1.25F, 10.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
                    .requiresCorrectToolForDrops());

    public static final RegistryObject<BlockAlembic> ALEMBIC = ForgeRegistration.registerBlock(BLOCKS,
            "alembic",
            BlockAlembic::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2F, 20.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
                    .instrument(NoteBlockInstrument.BASEDRUM));

    public static final RegistryObject<BlockBellows> BELLOWS = ForgeRegistration.registerBlock(BLOCKS,
            "bellows",
            BlockBellows::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(1F, 20.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
                    .instrument(NoteBlockInstrument.BASEDRUM));

    public static final RegistryObject<BlockSmelter> SMELTER_BASIC = ForgeRegistration.registerBlock(BLOCKS,
            "smelter_basic",
            BlockSmelter::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(2F, 20.0F)
                    .sound(SoundType.METAL)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .lightLevel(bs -> bs.getValue(BlockSmelter.LIT) ? 13 : 0));

    public static final RegistryObject<BlockSmelter> SMELTER_THAUMIUM = ForgeRegistration.registerBlock(BLOCKS,
            "smelter_thaumium",
            BlockSmelter::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(2F, 20.0F)
                    .sound(SoundType.METAL)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .lightLevel(bs -> bs.getValue(BlockSmelter.LIT) ? 13 : 0));

    public static final RegistryObject<BlockSmelter> SMELTER_VOID = ForgeRegistration.registerBlock(BLOCKS,
            "smelter_void",
            BlockSmelter::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(2F, 20.0F)
                    .sound(SoundType.METAL)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .lightLevel(bs -> bs.getValue(BlockSmelter.LIT) ? 13 : 0));

    public static final RegistryObject<BlockSmelterAux> SMELTER_AUX = ForgeRegistration.registerBlock(BLOCKS,
            "smelter_aux",
            BlockSmelterAux::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(1F, 20.0F)
                    .sound(SoundType.METAL)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .noOcclusion()
                    .requiresCorrectToolForDrops());

    public static final RegistryObject<BlockSmelterVent> SMELTER_VENT = ForgeRegistration.registerBlock(BLOCKS,
            "smelter_vent",
            BlockSmelterVent::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(1F, 20.0F)
                    .sound(SoundType.METAL)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops());

    public static final RegistryObject<BlockJar> JAR_NORMAL = ForgeRegistration.registerBlock(BLOCKS,
            "jar_normal",
            BlockJar::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NONE)
                    .strength(0.3F)
                    .sound(TTSoundTypes.JAR)
                    .noOcclusion());

    public static final RegistryObject<BlockJarVoid> JAR_VOID = ForgeRegistration.registerBlock(BLOCKS,
            "jar_void",
            BlockJarVoid::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NONE)
                    .strength(0.3F)
                    .sound(TTSoundTypes.JAR)
                    .noOcclusion());

    public static final RegistryObject<BlockJarCreative> JAR_CREATIVE = ForgeRegistration.registerBlock(BLOCKS,
            "jar_creative",
            BlockJarCreative::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NONE)
                    .strength(0.3F)
                    .sound(TTSoundTypes.JAR)
                    .noOcclusion());

    public static final RegistryObject<BlockTube> TUBE = ForgeRegistration.registerBlock(BLOCKS, "tube", BlockTube::new, tubeProps());

    public static final RegistryObject<BlockTubeValve> TUBE_VALVE =
            ForgeRegistration.registerBlock(BLOCKS, "tube_valve", BlockTubeValve::new, tubeProps());

    public static final RegistryObject<BlockTubeRestrict> TUBE_RESTRICT =
            ForgeRegistration.registerBlock(BLOCKS, "tube_restrict", BlockTubeRestrict::new, tubeProps());

    public static final RegistryObject<BlockTubeFilter> TUBE_FILTER =
            ForgeRegistration.registerBlock(BLOCKS, "tube_filter", BlockTubeFilter::new, tubeProps());

    public static final RegistryObject<BlockTubeOneway> TUBE_ONEWAY =
            ForgeRegistration.registerBlock(BLOCKS, "tube_oneway", BlockTubeOneway::new, tubeProps());

    public static final RegistryObject<BlockTubeBuffer> TUBE_BUFFER =
            ForgeRegistration.registerBlock(BLOCKS, "tube_buffer", BlockTubeBuffer::new, tubeProps());

    public static final RegistryObject<BlockEssentiaReservoir> ESSENTIA_RESERVOIR = ForgeRegistration.registerBlock(BLOCKS,
            "essentia_reservoir",
            BlockEssentiaReservoir::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(2.0F, 17.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion());

    public static final RegistryObject<BlockEssentiaCrystalizer> ESSENTIA_CRYSTALIZER = ForgeRegistration.registerBlock(BLOCKS,
            "essentia_crystalizer",
            BlockEssentiaCrystalizer::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(1.0F, 10.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion());

    public static final RegistryObject<BlockFluxGoo> FLUX_GOO = ForgeRegistration.registerBlock(BLOCKS,
            "flux_goo",
            props -> new BlockFluxGoo(FluxGooRefs.sourceFluid(), props),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PINK)
                    .replaceable()
                    .noCollission()
                    .strength(100.0F)
                    .pushReaction(PushReaction.DESTROY)
                    .sound(TTSoundTypes.GORE)
                    .noLootTable()
                    .liquid());

    public static final RegistryObject<BlockFluxGas> FLUX_GAS = ForgeRegistration.registerBlock(BLOCKS,
            "flux_gas",
            BlockFluxGas::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PINK)
                    .replaceable()
                    .noCollission()
                    .noOcclusion()
                    .strength(100.0F)
                    .pushReaction(PushReaction.DESTROY)
                    .lightLevel(state -> 7)
                    .sound(TTSoundTypes.GORE)
                    .noLootTable()
                    .randomTicks());

    public static final RegistryObject<BlockPurifyingFluid> PURIFYING_FLUID = ForgeRegistration.registerBlock(BLOCKS,
            "purifying_fluid",
            props -> new BlockPurifyingFluid(TTFluids.PURIFYING_SOURCE.get(), props),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .replaceable()
                    .noCollission()
                    .strength(100.0F)
                    .pushReaction(PushReaction.DESTROY)
                    .lightLevel(state -> 5)
                    .noLootTable()
                    .liquid());

    public static final RegistryObject<BlockLiquidDeath> LIQUID_DEATH = ForgeRegistration.registerBlock(BLOCKS,
            "liquid_death",
            props -> new BlockLiquidDeath(TTFluids.LIQUID_DEATH_SOURCE.get(), props),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .replaceable()
                    .noCollission()
                    .strength(100.0F)
                    .pushReaction(PushReaction.DESTROY)
                    .noLootTable()
                    .liquid());

    public static final RegistryObject<BlockSpa> SPA = ForgeRegistration.registerBlock(BLOCKS,
            "spa",
            BlockSpa::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.0F, 10.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());

    public static final RegistryObject<BlockTaintRock> TAINT_ROCK =
            ForgeRegistration.registerBlock(BLOCKS, "taint_rock", BlockTaintRock::new, taintBlockProps());

    public static final RegistryObject<BlockTaintSoil> TAINT_SOIL =
            ForgeRegistration.registerBlock(BLOCKS, "taint_soil", BlockTaintSoil::new, taintBlockProps());

    public static final RegistryObject<BlockTaintCrust> TAINT_CRUST =
            ForgeRegistration.registerBlock(BLOCKS, "taint_crust", BlockTaintCrust::new, taintBlockProps());

    public static final RegistryObject<BlockTaintGeyser> TAINT_GEYSER =
            ForgeRegistration.registerBlock(BLOCKS, "taint_geyser", BlockTaintGeyser::new, taintBlockProps());

    public static final RegistryObject<BlockTaintLog> TAINT_LOG = ForgeRegistration.registerBlock(BLOCKS,
            "taint_log",
            BlockTaintLog::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(3.0F, 100.0F)
                    .sound(TTSoundTypes.GORE)
                    .randomTicks()
                    .ignitedByLava());

    public static final RegistryObject<BlockTaintFeature> TAINT_FEATURE = ForgeRegistration.registerBlock(BLOCKS,
            "taint_feature",
            BlockTaintFeature::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(0.1F, 0.1F)
                    .sound(TTSoundTypes.GORE)
                    .noOcclusion()
                    .lightLevel(s -> 10)
                    .pushReaction(PushReaction.DESTROY)
                    .randomTicks());

    public static final RegistryObject<BlockTaintFibre> TAINT_FIBRE = ForgeRegistration.registerBlock(BLOCKS,
            "taint_fibre",
            BlockTaintFibre::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(1.0F)
                    .sound(TTSoundTypes.GORE)
                    .noOcclusion()
                    .noCollission()
                    .replaceable()
                    .pushReaction(PushReaction.DESTROY)
                    .randomTicks()
                    .lightLevel(s -> {
                        if (s.getValue(BlockTaintFibre.GROWTH3)) return 12;
                        if (s.getValue(BlockTaintFibre.GROWTH2) || s.getValue(BlockTaintFibre.GROWTH4)) return 6;
                        return 0;
                    }));

    public static final RegistryObject<BlockTaintSporeStalk> TAINT_SPORE_STALK = ForgeRegistration.registerBlock(BLOCKS,
            "taint_spore_stalk",
            BlockTaintSporeStalk::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(0.4F)
                    .sound(TTSoundTypes.GORE)
                    .noOcclusion()
                    .noCollission()
                    .pushReaction(PushReaction.DESTROY)
                    .randomTicks()
                    .lightLevel(state -> state.getValue(BlockTaintSporeStalk.MATURE) ? 10 : 0));

    private static BlockBehaviour.Properties pressPlaceholderProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(2.5F, 3600000.0F)
                .sound(SoundType.STONE)
                .noOcclusion()
                .dynamicShape();
    }

    private static BlockBehaviour.Properties advancedFurnacePlaceholderProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(2.5F, 3600000.0F)
                .sound(SoundType.METAL)
                .noOcclusion()
                .dynamicShape();
    }

    private static BlockBehaviour.Properties pedestalProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(2.0F, 17.5F)
                .sound(SoundType.STONE)
                .noOcclusion()
                .requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties pillarProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(2.0F, 17.5F)
                .sound(SoundType.STONE)
                .noOcclusion()
                .requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties amberProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_ORANGE)
                .strength(0.5F)
                .sound(SoundType.STONE)
                .noOcclusion()
                .isValidSpawn((state, level, pos, entityType) -> false)
                .isRedstoneConductor((state, level, pos) -> false)
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false);
    }

    private static BlockBehaviour.Properties taintBlockProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_PURPLE)
                .strength(10.0F, 100.0F)
                .sound(TTSoundTypes.GORE)
                .noOcclusion()
                .randomTicks();
    }

    private static BlockBehaviour.Properties tubeProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(0.5F, 5.0F)
                .sound(SoundType.METAL)
                .noOcclusion();
    }

    //

    public static final RegistryObject<Block> ORE_AMBER = ForgeRegistration.registerBlock(BLOCKS,
            "ore_amber",
            Block::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(1.5F, 5.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());

    public static final RegistryObject<Block> ORE_CINNABAR = ForgeRegistration.registerBlock(BLOCKS,
            "ore_cinnabar",
            Block::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.0F, 5.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());

    public static final RegistryObject<Block> ORE_QUARTZ = ForgeRegistration.registerBlock(BLOCKS,
            "ore_quartz",
            Block::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(3.0F, 5.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());

    public static final RegistryObject<Block> DEEPSLATE_ORE_AMBER = ForgeRegistration.registerBlock(BLOCKS,
            "deepslate_ore_amber",
            Block::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DEEPSLATE)
                    .strength(3.0F, 5.0F)
                    .sound(SoundType.DEEPSLATE)
                    .requiresCorrectToolForDrops());

    public static final RegistryObject<Block> DEEPSLATE_ORE_CINNABAR = ForgeRegistration.registerBlock(BLOCKS,
            "deepslate_ore_cinnabar",
            Block::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DEEPSLATE)
                    .strength(3.5F, 5.0F)
                    .sound(SoundType.DEEPSLATE)
                    .requiresCorrectToolForDrops());

    public static final RegistryObject<Block> DEEPSLATE_ORE_QUARTZ = ForgeRegistration.registerBlock(BLOCKS,
            "deepslate_ore_quartz",
            Block::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DEEPSLATE)
                    .strength(4.5F, 5.0F)
                    .sound(SoundType.DEEPSLATE)
                    .requiresCorrectToolForDrops());

    public static final Map<DyeColor, RegistryObject<BlockNitor>> NITORS = new EnumMap<>(DyeColor.class);

    static {
        for (DyeColor dye : DyeColor.values()) {
            NITORS.put(
                    dye,
                    ForgeRegistration.registerBlock(BLOCKS,
                            "nitor_" + dye.getName(), props -> new BlockNitor(dye, props), nitorProps(dye)));
        }
    }

    public static final Map<DyeColor, RegistryObject<BlockCandle>> CANDLES = new EnumMap<>(DyeColor.class);

    static {
        for (DyeColor dye : DyeColor.values()) {
            CANDLES.put(
                    dye,
                    ForgeRegistration.registerBlock(BLOCKS,
                            "candle_" + dye.getName(), props -> new BlockCandle(dye, props), candleProps(dye)));
        }
    }

    public static final Map<CandleHolderMaterial, RegistryObject<BlockCandleHolder>> CANDLE_HOLDERS =
            new EnumMap<>(CandleHolderMaterial.class);

    static {
        for (CandleHolderMaterial material : CandleHolderMaterial.values()) {
            CANDLE_HOLDERS.put(
                    material,
                    ForgeRegistration.registerBlock(BLOCKS,
                            "candle_holder_" + material.getSerializedName(),
                            props -> new BlockCandleHolder(material, props),
                            candleHolderProps(material)));
        }
    }

    public static final Map<DyeColor, RegistryObject<BannerStandingBlock>> BANNERS = new EnumMap<>(DyeColor.class);
    public static final Map<DyeColor, RegistryObject<BannerWallBlock>> WALL_BANNERS = new EnumMap<>(DyeColor.class);

    static {
        for (DyeColor dye : DyeColor.values()) {
            BANNERS.put(
                    dye,
                    ForgeRegistration.registerBlock(BLOCKS,
                            "banner_" + dye.getName(), props -> new BannerStandingBlock(dye, props), bannerProps(dye)));
            WALL_BANNERS.put(
                    dye,
                    ForgeRegistration.registerBlock(BLOCKS,
                            "wall_banner_" + dye.getName(),
                            props -> new BannerWallBlock(dye, props),
                            bannerProps(dye)));
        }
    }

    public static final RegistryObject<BannerStandingBlock> BANNER_CRIMSON_CULT = ForgeRegistration.registerBlock(BLOCKS,
            "banner_crimson_cult", props -> new BannerStandingBlock(null, props), bannerProps(null));

    public static final RegistryObject<BannerWallBlock> WALL_BANNER_CRIMSON_CULT = ForgeRegistration.registerBlock(BLOCKS,
            "wall_banner_crimson_cult", props -> new BannerWallBlock(null, props), bannerProps(null));

    private static BlockBehaviour.Properties bannerProps(DyeColor dye) {
        BlockBehaviour.Properties props = BlockBehaviour.Properties.of()
                .strength(1.0F)
                .sound(SoundType.WOOD)
                .noOcclusion();
        return dye == null ? props.mapColor(MapColor.COLOR_RED) : props.mapColor(dye.getMapColor());
    }

    private static BlockBehaviour.Properties candleProps(DyeColor dye) {
        return BlockBehaviour.Properties.of()
                .mapColor(dye.getMapColor())
                .strength(0.1F)
                .sound(SoundType.WOOL)
                .lightLevel(state -> 14)
                .noOcclusion();
    }

    private static BlockBehaviour.Properties candleHolderProps(CandleHolderMaterial material) {
        return BlockBehaviour.Properties.of()
                .mapColor(material.mapColor())
                .strength(material.strength())
                .sound(SoundType.METAL)
                .lightLevel(BlockCandleHolder::lightEmission)
                .noOcclusion();
    }

    private static BlockBehaviour.Properties nitorProps(DyeColor dye) {
        return BlockBehaviour.Properties.of()
                .mapColor(dye.getMapColor())
                .strength(0.1F)
                .sound(SoundType.WOOL)
                .lightLevel(state -> 15)
                .noOcclusion()
                .noCollission()
                .pushReaction(PushReaction.DESTROY);
    }

    //

    public static final RegistryObject<BlockCrystal> CRYSTAL_AER = registerCrystal("crystal_aer", TTAspects.AER, false);
    public static final RegistryObject<BlockCrystal> CRYSTAL_IGNIS =
            registerCrystal("crystal_ignis", TTAspects.IGNIS, false);
    public static final RegistryObject<BlockCrystal> CRYSTAL_AQUA =
            registerCrystal("crystal_aqua", TTAspects.AQUA, false);
    public static final RegistryObject<BlockCrystal> CRYSTAL_TERRA =
            registerCrystal("crystal_terra", TTAspects.TERRA, false);
    public static final RegistryObject<BlockCrystal> CRYSTAL_ORDO =
            registerCrystal("crystal_ordo", TTAspects.ORDO, false);
    public static final RegistryObject<BlockCrystal> CRYSTAL_PERDITIO =
            registerCrystal("crystal_perditio", TTAspects.PERDITIO, false);
    public static final RegistryObject<BlockCrystal> CRYSTAL_VITIUM =
            registerCrystal("crystal_vitium", TTAspects.VITIUM, true);

    private static RegistryObject<BlockCrystal> registerCrystal(String name, ResourceKey<IAspect> aspect, boolean flux) {
        return ForgeRegistration.registerBlock(BLOCKS,
                name,
                props -> new BlockCrystal(props, aspect, flux),
                BlockBehaviour.Properties.of()
                        .mapColor(MapColor.NONE)
                        .strength(0.25F)
                        .sound(TTSoundTypes.CRYSTAL)
                        .lightLevel(state -> 1)
                        .noOcclusion()
                        .randomTicks()
                        .pushReaction(PushReaction.DESTROY));
    }

    //

    public static final RegistryObject<BlockArcaneGrindstone> ARCANE_GRINDSTONE = ForgeRegistration.registerBlock(BLOCKS,
            "arcane_grindstone",
            BlockArcaneGrindstone::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .requiresCorrectToolForDrops()
                    .strength(2.0F, 6.0F)
                    .sound(SoundType.STONE)
                    .pushReaction(PushReaction.BLOCK));

    public static final RegistryObject<BlockInfusionMatrix> INFUSION_MATRIX = ForgeRegistration.registerBlock(BLOCKS,
            "infusion_matrix",
            BlockInfusionMatrix::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(5.0F, 10.0F)
                    .sound(SoundType.STONE)
                    .noOcclusion());

    public static final RegistryObject<BlockPedestal> PEDESTAL_ARCANE =
            ForgeRegistration.registerBlock(BLOCKS, "pedestal_arcane", BlockPedestal::new, pedestalProps());

    public static final RegistryObject<BlockRechargePedestal> RECHARGE_PEDESTAL =
            ForgeRegistration.registerBlock(BLOCKS, "recharge_pedestal", BlockRechargePedestal::new, pedestalProps());

    public static final RegistryObject<BlockInlay> INLAY = ForgeRegistration.registerBlock(BLOCKS,
            "inlay",
            BlockInlay::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(0.5F)
                    .sound(SoundType.METAL)
                    .noOcclusion()
                    .noCollission()
                    .lightLevel(state -> 1));

    public static final RegistryObject<BlockPatternCrafter> PATTERN_CRAFTER = ForgeRegistration.registerBlock(BLOCKS,
            "pattern_crafter",
            BlockPatternCrafter::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(2.0F, 20.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion()
                    .isRedstoneConductor((state, level, pos) -> false));

    public static final RegistryObject<BlockPotionSprayer> POTION_SPRAYER = ForgeRegistration.registerBlock(BLOCKS,
            "potion_sprayer",
            BlockPotionSprayer::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(2.0F, 20.0F)
                    .sound(SoundType.METAL));

    public static final RegistryObject<BlockLevitator> LEVITATOR = ForgeRegistration.registerBlock(BLOCKS,
            "levitator",
            BlockLevitator::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 20.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion());

    public static final RegistryObject<BlockGolemBuilder> GOLEM_BUILDER = ForgeRegistration.registerBlock(BLOCKS,
            "golem_builder",
            BlockGolemBuilder::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.0F, 20.0F)
                    .sound(SoundType.STONE)
                    .noOcclusion());

    public static final RegistryObject<BlockArcaneBore> ARCANE_BORE = ForgeRegistration.registerBlock(BLOCKS,
            "arcane_bore",
            BlockArcaneBore::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(2.0F, 20.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion());

    public static final RegistryObject<BlockPlaceholder> PLACEHOLDER_IRON_BARS =
            ForgeRegistration.registerBlock(BLOCKS, "placeholder_iron_bars", BlockPlaceholder::new, pressPlaceholderProps());

    public static final RegistryObject<BlockPlaceholder> PLACEHOLDER_CAULDRON =
            ForgeRegistration.registerBlock(BLOCKS, "placeholder_cauldron", BlockPlaceholder::new, pressPlaceholderProps());

    public static final RegistryObject<BlockPlaceholder> PLACEHOLDER_ANVIL =
            ForgeRegistration.registerBlock(BLOCKS, "placeholder_anvil", BlockPlaceholder::new, pressPlaceholderProps());

    public static final RegistryObject<BlockPlaceholder> PLACEHOLDER_TABLE =
            ForgeRegistration.registerBlock(BLOCKS, "placeholder_table", BlockPlaceholder::new, pressPlaceholderProps());

    public static final RegistryObject<BlockPlaceholder> ADVANCED_ALCHEMICAL_FURNACE_ALEMBIC_PLACEHOLDER =
            ForgeRegistration.registerBlock(BLOCKS,
                    "advanced_alchemical_furnace_alembic_placeholder",
                    BlockPlaceholder::new,
                    advancedFurnacePlaceholderProps());

    public static final RegistryObject<BlockPlaceholder> ADVANCED_ALCHEMICAL_FURNACE_CONSTRUCT_PLACEHOLDER =
            ForgeRegistration.registerBlock(BLOCKS,
                    "advanced_alchemical_furnace_construct_placeholder",
                    BlockPlaceholder::new,
                    advancedFurnacePlaceholderProps());

    public static final RegistryObject<BlockPlaceholder> ADVANCED_ALCHEMICAL_FURNACE_ADVANCED_CONSTRUCT_PLACEHOLDER =
            ForgeRegistration.registerBlock(BLOCKS,
                    "advanced_alchemical_furnace_advanced_construct_placeholder",
                    BlockPlaceholder::new,
                    advancedFurnacePlaceholderProps());

    public static final RegistryObject<BlockPlaceholder> ADVANCED_ALCHEMICAL_FURNACE_NOZZLE = ForgeRegistration.registerBlock(BLOCKS,
            "advanced_alchemical_furnace_nozzle",
            BlockAdvancedFurnaceNozzle::new,
            advancedFurnacePlaceholderProps().noLootTable().noOcclusion());

    public static final RegistryObject<BlockPedestal> PEDESTAL_ANCIENT =
            ForgeRegistration.registerBlock(BLOCKS, "pedestal_ancient", BlockPedestal::new, pedestalProps());

    public static final RegistryObject<BlockPedestal> PEDESTAL_ELDRITCH =
            ForgeRegistration.registerBlock(BLOCKS, "pedestal_eldritch", BlockPedestal::new, pedestalProps());

    public static final RegistryObject<BlockPillar> PILLAR_ARCANE =
            ForgeRegistration.registerBlock(BLOCKS, "pillar_arcane", BlockPillar::new, pillarProps());

    public static final RegistryObject<BlockPillar> PILLAR_ANCIENT =
            ForgeRegistration.registerBlock(BLOCKS, "pillar_ancient", BlockPillar::new, pillarProps());

    public static final RegistryObject<BlockPillar> PILLAR_ELDRITCH =
            ForgeRegistration.registerBlock(BLOCKS, "pillar_eldritch", BlockPillar::new, pillarProps());

    public static final RegistryObject<Block> STONE_ARCANE =
            ForgeRegistration.registerBlock(BLOCKS, "stone_arcane", Block::new, stoneProps());

    public static final RegistryObject<Block> STONE_ARCANE_BRICK =
            ForgeRegistration.registerBlock(BLOCKS, "stone_arcane_brick", Block::new, stoneProps());

    public static final RegistryObject<Block> STONE_ANCIENT =
            ForgeRegistration.registerBlock(BLOCKS, "stone_ancient", Block::new, stoneProps());

    public static final RegistryObject<Block> STONE_ANCIENT_TILE =
            ForgeRegistration.registerBlock(BLOCKS, "stone_ancient_tile", Block::new, stoneProps());

    public static final RegistryObject<Block> STONE_ANCIENT_ROCK =
            ForgeRegistration.registerBlock(BLOCKS, "stone_ancient_rock", Block::new, unbreakableProps());

    public static final RegistryObject<Block> STONE_ANCIENT_GLYPHED =
            ForgeRegistration.registerBlock(BLOCKS, "stone_ancient_glyphed", Block::new, stoneProps());

    public static final RegistryObject<Block> STONE_ANCIENT_DOORWAY =
            ForgeRegistration.registerBlock(BLOCKS, "stone_ancient_doorway", Block::new, unbreakableProps());

    public static final RegistryObject<Block> STONE_ELDRITCH_TILE =
            ForgeRegistration.registerBlock(BLOCKS, "stone_eldritch_tile", Block::new, eldritchTileProps());

    public static final RegistryObject<Block> STONE_POROUS =
            ForgeRegistration.registerBlock(BLOCKS, "stone_porous", Block::new, porousProps());

    public static final RegistryObject<BlockStairsTT> STAIRS_ARCANE = ForgeRegistration.registerBlock(BLOCKS,
            "stairs_arcane", props -> new BlockStairsTT(STONE_ARCANE.get().defaultBlockState(), props), stoneProps());

    public static final RegistryObject<BlockStairsTT> STAIRS_ARCANE_BRICK = ForgeRegistration.registerBlock(BLOCKS,
            "stairs_arcane_brick",
            props -> new BlockStairsTT(STONE_ARCANE_BRICK.get().defaultBlockState(), props),
            stoneProps());

    public static final RegistryObject<BlockStairsTT> STAIRS_ANCIENT = ForgeRegistration.registerBlock(BLOCKS,
            "stairs_ancient", props -> new BlockStairsTT(STONE_ANCIENT.get().defaultBlockState(), props), stoneProps());

    public static final RegistryObject<Block> MATRIX_SPEED =
            ForgeRegistration.registerBlock(BLOCKS, "matrix_speed", Block::new, stoneProps());

    public static final RegistryObject<Block> MATRIX_COST =
            ForgeRegistration.registerBlock(BLOCKS, "matrix_cost", Block::new, stoneProps());

    public static final RegistryObject<BlockVisBattery> VIS_BATTERY = ForgeRegistration.registerBlock(BLOCKS,
            "vis_battery",
            BlockVisBattery::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(0.5F)
                    .sound(SoundType.GLASS)
                    .randomTicks()
                    .lightLevel(state -> state.getValue(BlockVisBattery.CHARGE)));

    public static final RegistryObject<BlockDioptra> DIOPTRA = ForgeRegistration.registerBlock(BLOCKS,
            "dioptra",
            BlockDioptra::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.0F, 10.0F)
                    .sound(SoundType.STONE)
                    .noOcclusion());

    public static final RegistryObject<BlockJarBrain> JAR_BRAIN = ForgeRegistration.registerBlock(BLOCKS,
            "jar_brain",
            BlockJarBrain::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NONE)
                    .strength(0.3F)
                    .sound(TTSoundTypes.JAR)
                    .noOcclusion());

    public static final RegistryObject<BlockArcaneEar> ARCANE_EAR =
            ForgeRegistration.registerBlock(BLOCKS, "arcane_ear", props -> new BlockArcaneEar(false, props), earProps());

    public static final RegistryObject<BlockArcaneEar> ARCANE_EAR_TOGGLE =
            ForgeRegistration.registerBlock(BLOCKS, "arcane_ear_toggle", props -> new BlockArcaneEar(true, props), earProps());

    public static final RegistryObject<BlockLampArcane> LAMP_ARCANE =
            ForgeRegistration.registerBlock(BLOCKS, "lamp_arcane", BlockLampArcane::new, lampProps());

    public static final RegistryObject<BlockLampGrowth> LAMP_GROWTH =
            ForgeRegistration.registerBlock(BLOCKS, "lamp_growth", BlockLampGrowth::new, lampProps());

    public static final RegistryObject<BlockLampFertility> LAMP_FERTILITY =
            ForgeRegistration.registerBlock(BLOCKS, "lamp_fertility", BlockLampFertility::new, lampProps());

    public static final RegistryObject<BlockCentrifuge> CENTRIFUGE = ForgeRegistration.registerBlock(BLOCKS,
            "centrifuge",
            BlockCentrifuge::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(2.0F, 10.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion());

    public static final RegistryObject<BlockHungryChest> HUNGRY_CHEST = ForgeRegistration.registerBlock(BLOCKS,
            "hungry_chest",
            BlockHungryChest::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.5F)
                    .sound(SoundType.WOOD)
                    .noOcclusion());

    public static final RegistryObject<BlockEverfullUrn> EVERFULL_URN = ForgeRegistration.registerBlock(BLOCKS,
            "everfull_urn",
            BlockEverfullUrn::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.0F, 10.0F)
                    .sound(SoundType.STONE)
                    .noOcclusion());

    public static final RegistryObject<BlockVisGenerator> VIS_GENERATOR = ForgeRegistration.registerBlock(BLOCKS,
            "vis_generator",
            BlockVisGenerator::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(1.5F)
                    .sound(SoundType.WOOD)
                    .noOcclusion());

    public static final RegistryObject<BlockEssentiaPort> ESSENTIA_INPUT =
            ForgeRegistration.registerBlock(BLOCKS, "essentia_input", props -> new BlockEssentiaPort(true, props), portProps());

    public static final RegistryObject<BlockEssentiaPort> ESSENTIA_OUTPUT =
            ForgeRegistration.registerBlock(BLOCKS, "essentia_output", props -> new BlockEssentiaPort(false, props), portProps());

    public static final RegistryObject<BlockCondenser> CONDENSER = ForgeRegistration.registerBlock(BLOCKS,
            "condenser",
            BlockCondenser::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(2.0F, 10.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion());

    public static final RegistryObject<BlockFluxScrubber> FLUX_SCRUBBER = ForgeRegistration.registerBlock(BLOCKS,
            "flux_scrubber",
            BlockFluxScrubber::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.0F, 10.0F)
                    .sound(SoundType.STONE)
                    .noOcclusion());

    public static final RegistryObject<BlockCondenserLattice> CONDENSER_LATTICE = ForgeRegistration.registerBlock(BLOCKS,
            "condenser_lattice",
            props -> new BlockCondenserLattice(false, props),
            latticeProps().lightLevel(state -> 5));

    public static final RegistryObject<BlockCondenserLattice> CONDENSER_LATTICE_DIRTY = ForgeRegistration.registerBlock(BLOCKS,
            "condenser_lattice_dirty", props -> new BlockCondenserLattice(true, props), latticeProps());

    public static final RegistryObject<BlockStabilizer> STABILIZER = ForgeRegistration.registerBlock(BLOCKS,
            "stabilizer", BlockStabilizer::new, stoneProps().noOcclusion());

    public static final RegistryObject<BlockRedstoneRelay> REDSTONE_RELAY = ForgeRegistration.registerBlock(BLOCKS,
            "redstone_relay",
            BlockRedstoneRelay::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .instabreak()
                    .sound(SoundType.WOOD)
                    .noOcclusion());

    public static final RegistryObject<BlockVoidSiphon> VOID_SIPHON = ForgeRegistration.registerBlock(BLOCKS,
            "void_siphon",
            BlockVoidSiphon::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(3.0F, 20.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion());

    public static final RegistryObject<BlockThaumatorium> THAUMATORIUM = ForgeRegistration.registerBlock(BLOCKS,
            "thaumatorium",
            BlockThaumatorium::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.0F, 20.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion());

    public static final RegistryObject<BlockThaumatoriumTop> THAUMATORIUM_TOP = ForgeRegistration.registerBlock(BLOCKS,
            "thaumatorium_top",
            BlockThaumatoriumTop::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.0F, 20.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion()
                    .noLootTable());

    public static final RegistryObject<BlockBrainBox> BRAIN_BOX = ForgeRegistration.registerBlock(BLOCKS,
            "brain_box",
            BlockBrainBox::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(1.5F)
                    .sound(SoundType.WOOD)
                    .noOcclusion());

    private static BlockBehaviour.Properties latticeProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(0.5F, 5.0F)
                .sound(SoundType.METAL)
                .noOcclusion();
    }

    private static BlockBehaviour.Properties portProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(1.5F)
                .sound(SoundType.METAL)
                .noOcclusion();
    }

    private static BlockBehaviour.Properties earProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(1.0F)
                .sound(SoundType.WOOD)
                .noOcclusion();
    }

    private static BlockBehaviour.Properties lampProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(1.0F)
                .sound(SoundType.METAL)
                .noOcclusion()
                .lightLevel(state -> state.getValue(BlockStateProperties.ENABLED) ? 15 : 0);
    }

    private static BlockBehaviour.Properties stoneProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(2.0F, 10.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties unbreakableProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(-1.0F, 3600000.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties eldritchTileProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(15.0F, 1000.0F)
                .sound(SoundType.STONE)
                .lightLevel(state -> 12)
                .requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties porousProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(1.0F, 5.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops();
    }

    //

    public static final RegistryObject<BlockSaplingTT> SAPLING_GREATWOOD = ForgeRegistration.registerBlock(BLOCKS,
            "sapling_greatwood",
            props -> new BlockSaplingTT(TTTreeGrowers.GREATWOOD, props),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .randomTicks()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .pushReaction(PushReaction.DESTROY));

    public static final RegistryObject<BlockSaplingTT> SAPLING_SILVERWOOD = ForgeRegistration.registerBlock(BLOCKS,
            "sapling_silverwood",
            props -> new BlockSaplingTT(TTTreeGrowers.SILVERWOOD, props),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .randomTicks()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .pushReaction(PushReaction.DESTROY));

    public static final RegistryObject<FlowerPotBlock> POTTED_SAPLING_GREATWOOD =
            pottedPlant("potted_sapling_greatwood", SAPLING_GREATWOOD);
    public static final RegistryObject<FlowerPotBlock> POTTED_SAPLING_SILVERWOOD =
            pottedPlant("potted_sapling_silverwood", SAPLING_SILVERWOOD);

    public static final RegistryObject<BlockGreatwoodLog> LOG_GREATWOOD = ForgeRegistration.registerBlock(BLOCKS,
            "log_greatwood",
            BlockGreatwoodLog::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 5.0F)
                    .sound(SoundType.WOOD)
                    .ignitedByLava());

    public static final RegistryObject<RotatedPillarBlock> WOOD_GREATWOOD = ForgeRegistration.registerBlock(BLOCKS,
            "greatwood",
            RotatedPillarBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 5.0F)
                    .sound(SoundType.WOOD)
                    .ignitedByLava());

    public static final RegistryObject<RotatedPillarBlock> STRIPPED_LOG_GREATWOOD = ForgeRegistration.registerBlock(BLOCKS,
            "stripped_log_greatwood",
            RotatedPillarBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 5.0F)
                    .sound(SoundType.WOOD)
                    .ignitedByLava());

    public static final RegistryObject<RotatedPillarBlock> STRIPPED_WOOD_GREATWOOD = ForgeRegistration.registerBlock(BLOCKS,
            "stripped_greatwood",
            RotatedPillarBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 5.0F)
                    .sound(SoundType.WOOD)
                    .ignitedByLava());

    public static final RegistryObject<RotatedPillarBlock> LOG_SILVERWOOD = ForgeRegistration.registerBlock(BLOCKS,
            "log_silverwood",
            RotatedPillarBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 5.0F)
                    .sound(SoundType.WOOD)
                    .lightLevel(state -> 5)
                    .ignitedByLava());

    public static final RegistryObject<BlockSilverwoodNodeLog> SILVERWOOD_NODE_LOG = ForgeRegistration.registerBlock(BLOCKS,
            "silverwood_node_log",
            BlockSilverwoodNodeLog::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 5.0F)
                    .sound(SoundType.WOOD)
                    .lightLevel(state -> 5)
                    .ignitedByLava());

    public static final RegistryObject<RotatedPillarBlock> WOOD_SILVERWOOD = ForgeRegistration.registerBlock(BLOCKS,
            "silverwood",
            RotatedPillarBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 5.0F)
                    .sound(SoundType.WOOD)
                    .lightLevel(state -> 5)
                    .ignitedByLava());

    public static final RegistryObject<RotatedPillarBlock> STRIPPED_LOG_SILVERWOOD = ForgeRegistration.registerBlock(BLOCKS,
            "stripped_log_silverwood",
            RotatedPillarBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 5.0F)
                    .sound(SoundType.WOOD)
                    .lightLevel(state -> 5)
                    .ignitedByLava());

    public static final RegistryObject<RotatedPillarBlock> STRIPPED_WOOD_SILVERWOOD = ForgeRegistration.registerBlock(BLOCKS,
            "stripped_silverwood",
            RotatedPillarBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 5.0F)
                    .sound(SoundType.WOOD)
                    .lightLevel(state -> 5)
                    .ignitedByLava());

    public static final RegistryObject<LeavesBlock> LEAVES_GREATWOOD =
            ForgeRegistration.registerBlock(BLOCKS, "leaves_greatwood", LeavesBlock::new, leavesProps());

    public static final RegistryObject<LeavesBlock> LEAVES_SILVERWOOD = ForgeRegistration.registerBlock(BLOCKS,
            "leaves_silverwood", LeavesBlock::new, leavesProps().mapColor(MapColor.COLOR_LIGHT_BLUE));

    public static final RegistryObject<PoweredRailBlock> ACTIVATOR_RAIL = ForgeRegistration.registerBlock(BLOCKS,
            "activator_rail",
            properties -> new PoweredRailBlock(properties, true),
            BlockBehaviour.Properties.of().noCollission().strength(0.7F).sound(SoundType.METAL));

    public static final RegistryObject<Block> PLANK_GREATWOOD = ForgeRegistration.registerBlock(BLOCKS,
            "plank_greatwood",
            Block::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 3.0F)
                    .sound(SoundType.WOOD)
                    .ignitedByLava());

    public static final RegistryObject<Block> PLANK_SILVERWOOD = ForgeRegistration.registerBlock(BLOCKS,
            "plank_silverwood",
            Block::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 3.0F)
                    .sound(SoundType.WOOD)
                    .ignitedByLava());

    private static BlockBehaviour.Properties leavesProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.PLANT)
                .strength(0.2F)
                .randomTicks()
                .sound(SoundType.GRASS)
                .noOcclusion()
                .ignitedByLava()
                .pushReaction(PushReaction.DESTROY);
    }

    //

    public static final RegistryObject<MagicPlantBlock> PLANT_SHIMMERLEAF = ForgeRegistration.registerBlock(BLOCKS,
            "shimmerleaf",
            props -> new MagicPlantBlock(
                    TTBlockTags.SHIMMERLEAF_SOIL, MagicPlantTraits.SHIMMERLEAF_GLINTS, Optional.empty(), props),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .lightLevel(state -> 6)
                    .pushReaction(PushReaction.DESTROY)
                    .noOcclusion());

    public static final RegistryObject<BlockEtherealBloom> ETHEREAL_BLOOM = ForgeRegistration.registerBlock(BLOCKS,
            "ethereal_bloom",
            BlockEtherealBloom::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .lightLevel(state -> 12)
                    .pushReaction(PushReaction.DESTROY)
                    .noOcclusion());

    public static final RegistryObject<MagicPlantBlock> PLANT_CINDERPEARL = ForgeRegistration.registerBlock(BLOCKS,
            "cinderpearl",
            props -> new MagicPlantBlock(
                    TTBlockTags.CINDERPEARL_SOIL, MagicPlantTraits.CINDERPEARL_EMBERS, Optional.empty(), props),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_ORANGE)
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .lightLevel(state -> 8)
                    .pushReaction(PushReaction.DESTROY)
                    .noOcclusion());

    public static final RegistryObject<MagicPlantBlock> PLANT_VISHROOM = ForgeRegistration.registerBlock(BLOCKS,
            "vishroom",
            props -> new MagicPlantBlock(
                    TTBlockTags.VISHROOM_SOIL,
                    MagicPlantTraits.VISHROOM_SPORES,
                    MagicPlantTraits.VISHROOM_NAUSEA,
                    props),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .lightLevel(state -> 6)
                    .pushReaction(PushReaction.DESTROY)
                    .noOcclusion());

    public static final RegistryObject<FlowerPotBlock> POTTED_SHIMMERLEAF =
            pottedPlant("potted_shimmerleaf", PLANT_SHIMMERLEAF);
    public static final RegistryObject<FlowerPotBlock> POTTED_CINDERPEARL =
            pottedPlant("potted_cinderpearl", PLANT_CINDERPEARL);
    public static final RegistryObject<FlowerPotBlock> POTTED_VISHROOM = pottedPlant("potted_vishroom", PLANT_VISHROOM);

    public static final RegistryObject<BlockGrassAmbient> GRASS_AMBIENT = ForgeRegistration.registerBlock(BLOCKS,
            "grass_ambient", BlockGrassAmbient::new, BlockBehaviour.Properties.ofFullCopy(Blocks.GRASS_BLOCK));

    private static RegistryObject<FlowerPotBlock> pottedPlant(String name, RegistryObject<? extends Block> plant) {
        return ForgeRegistration.registerBlock(BLOCKS,
                name,
                properties -> new FlowerPotBlock(() -> (FlowerPotBlock) Blocks.FLOWER_POT, plant, properties),
                BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_OAK_SAPLING));
    }

    private static void registerPottedPlants() {
        FlowerPotBlock flowerPot = (FlowerPotBlock) Blocks.FLOWER_POT;
        flowerPot.addPlant(SAPLING_GREATWOOD.getId(), POTTED_SAPLING_GREATWOOD);
        flowerPot.addPlant(SAPLING_SILVERWOOD.getId(), POTTED_SAPLING_SILVERWOOD);
        flowerPot.addPlant(PLANT_SHIMMERLEAF.getId(), POTTED_SHIMMERLEAF);
        flowerPot.addPlant(PLANT_CINDERPEARL.getId(), POTTED_CINDERPEARL);
        flowerPot.addPlant(PLANT_VISHROOM.getId(), POTTED_VISHROOM);
    }

    //
    public static final RegistryObject<BlockMetalTT> ALCHEMICAL_CONSTRUCT = ForgeRegistration.registerBlock(BLOCKS,
            "alchemical_construct",
            BlockMetalTT::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(4.0F, 10.0F)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops());

    public static final RegistryObject<BlockMetalTT> ADVANCED_ALCHEMICAL_CONSTRUCT = ForgeRegistration.registerBlock(BLOCKS,
            "advanced_alchemical_construct",
            BlockMetalTT::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(4.0F, 10.0F)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops());

    public static final RegistryObject<BlockAdvancedAlchemicalFurnace> ADVANCED_ALCHEMICAL_FURNACE =
            ForgeRegistration.registerBlock(BLOCKS,
                    "advanced_alchemical_furnace",
                    BlockAdvancedAlchemicalFurnace::new,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(5.0F, 12.0F)
                            .sound(SoundType.METAL)
                            .noOcclusion()
                            .requiresCorrectToolForDrops()
                            .lightLevel(state -> state.getValue(BlockAdvancedAlchemicalFurnace.LIT) ? 10 : 0));

    public static final RegistryObject<BlockMetalTT> METAL_THAUMIUM_BLOCK = ForgeRegistration.registerBlock(BLOCKS,
            "metal_thaumium",
            BlockMetalTT::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_ORANGE)
                    .strength(4.0F, 10.0F)
                    .sound(SoundType.AMETHYST)
                    .requiresCorrectToolForDrops());

    public static final RegistryObject<BlockMetalTT> METAL_BRASS_BLOCK = ForgeRegistration.registerBlock(BLOCKS,
            "metal_brass",
            BlockMetalTT::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(4.0F, 10.0F)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops());

    public static final RegistryObject<BlockMetalTT> METAL_VOID_BLOCK = ForgeRegistration.registerBlock(BLOCKS,
            "metal_void",
            BlockMetalTT::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(4.0F, 10.0F)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops());

    public static final RegistryObject<SlabBlock> SLAB_GREATWOOD = ForgeRegistration.registerBlock(BLOCKS,
            "slab_greatwood",
            SlabBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(1.2F, 2.0F)
                    .sound(SoundType.WOOD)
                    .ignitedByLava());

    public static final RegistryObject<SlabBlock> SLAB_SILVERWOOD = ForgeRegistration.registerBlock(BLOCKS,
            "slab_silverwood",
            SlabBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.QUARTZ)
                    .strength(1.0F, 2.0F)
                    .sound(SoundType.WOOD)
                    .ignitedByLava());

    public static final RegistryObject<SlabBlock> SLAB_ARCANE_STONE = ForgeRegistration.registerBlock(BLOCKS,
            "slab_arcane_stone",
            SlabBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.0F, 10.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());

    public static final RegistryObject<SlabBlock> SLAB_ARCANE_BRICK = ForgeRegistration.registerBlock(BLOCKS,
            "slab_arcane_brick",
            SlabBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.0F, 10.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());

    public static final RegistryObject<SlabBlock> SLAB_ANCIENT = ForgeRegistration.registerBlock(BLOCKS,
            "slab_ancient",
            SlabBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.0F, 10.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());

    public static final RegistryObject<SlabBlock> SLAB_ELDRITCH = ForgeRegistration.registerBlock(BLOCKS,
            "slab_eldritch",
            SlabBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.0F, 10.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());

    public static final RegistryObject<BlockStairsTT> STAIRS_GREATWOOD = ForgeRegistration.registerBlock(BLOCKS,
            "stairs_greatwood",
            props -> new BlockStairsTT(PLANK_GREATWOOD.get().defaultBlockState(), props),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F, 3.0F)
                    .sound(SoundType.WOOD)
                    .ignitedByLava());

    public static final RegistryObject<BlockStairsTT> STAIRS_SILVERWOOD = ForgeRegistration.registerBlock(BLOCKS,
            "stairs_silverwood",
            props -> new BlockStairsTT(PLANK_SILVERWOOD.get().defaultBlockState(), props),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.QUARTZ)
                    .strength(2.0F, 3.0F)
                    .sound(SoundType.WOOD)
                    .ignitedByLava());

    public static final RegistryObject<DoorBlock> DOOR_GREATWOOD = ForgeRegistration.registerBlock(BLOCKS,
            "door_greatwood",
            props -> new DoorBlock(props, BlockSetType.OAK),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(3.0F)
                    .noOcclusion()
                    .ignitedByLava()
                    .pushReaction(PushReaction.DESTROY));

    public static final RegistryObject<TrapDoorBlock> TRAPDOOR_GREATWOOD = ForgeRegistration.registerBlock(BLOCKS,
            "trapdoor_greatwood",
            props -> new TrapDoorBlock(props, BlockSetType.OAK),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(3.0F)
                    .noOcclusion()
                    .isValidSpawn((state, level, pos, type) -> false)
                    .ignitedByLava());

    public static final RegistryObject<FenceBlock> FENCE_GREATWOOD = ForgeRegistration.registerBlock(BLOCKS,
            "fence_greatwood",
            FenceBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .forceSolidOn()
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(2.0F, 3.0F)
                    .sound(SoundType.WOOD)
                    .ignitedByLava());

    public static final RegistryObject<FenceGateBlock> FENCE_GATE_GREATWOOD = ForgeRegistration.registerBlock(BLOCKS,
            "fence_gate_greatwood",
            props -> new FenceGateBlock(props, WoodType.OAK),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .forceSolidOn()
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(2.0F, 3.0F)
                    .ignitedByLava());

    public static final RegistryObject<ButtonBlock> BUTTON_GREATWOOD = ForgeRegistration.registerBlock(BLOCKS,
            "button_greatwood",
            props -> new ButtonBlock(props, BlockSetType.OAK, WOODEN_BUTTON_PRESS_TICKS, true),
            BlockBehaviour.Properties.of().noCollission().strength(0.5F).pushReaction(PushReaction.DESTROY));

    public static final RegistryObject<PressurePlateBlock> PRESSURE_PLATE_GREATWOOD = ForgeRegistration.registerBlock(BLOCKS,
            "pressure_plate_greatwood",
            props -> new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING, props, BlockSetType.OAK),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .forceSolidOn()
                    .instrument(NoteBlockInstrument.BASS)
                    .noCollission()
                    .strength(0.5F)
                    .ignitedByLava()
                    .pushReaction(PushReaction.DESTROY));

    public static final RegistryObject<DoorBlock> DOOR_SILVERWOOD = ForgeRegistration.registerBlock(BLOCKS,
            "door_silverwood",
            props -> new DoorBlock(props, BlockSetType.OAK),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.QUARTZ)
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(3.0F)
                    .noOcclusion()
                    .ignitedByLava()
                    .pushReaction(PushReaction.DESTROY));

    public static final RegistryObject<TrapDoorBlock> TRAPDOOR_SILVERWOOD = ForgeRegistration.registerBlock(BLOCKS,
            "trapdoor_silverwood",
            props -> new TrapDoorBlock(props, BlockSetType.OAK),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.QUARTZ)
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(3.0F)
                    .noOcclusion()
                    .isValidSpawn((state, level, pos, type) -> false)
                    .ignitedByLava());

    public static final RegistryObject<FenceBlock> FENCE_SILVERWOOD = ForgeRegistration.registerBlock(BLOCKS,
            "fence_silverwood",
            FenceBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.QUARTZ)
                    .forceSolidOn()
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(2.0F, 3.0F)
                    .sound(SoundType.WOOD)
                    .ignitedByLava());

    public static final RegistryObject<FenceGateBlock> FENCE_GATE_SILVERWOOD = ForgeRegistration.registerBlock(BLOCKS,
            "fence_gate_silverwood",
            props -> new FenceGateBlock(props, WoodType.OAK),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.QUARTZ)
                    .forceSolidOn()
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(2.0F, 3.0F)
                    .ignitedByLava());

    public static final RegistryObject<ButtonBlock> BUTTON_SILVERWOOD = ForgeRegistration.registerBlock(BLOCKS,
            "button_silverwood",
            props -> new ButtonBlock(props, BlockSetType.OAK, WOODEN_BUTTON_PRESS_TICKS, true),
            BlockBehaviour.Properties.of().noCollission().strength(0.5F).pushReaction(PushReaction.DESTROY));

    public static final RegistryObject<PressurePlateBlock> PRESSURE_PLATE_SILVERWOOD = ForgeRegistration.registerBlock(BLOCKS,
            "pressure_plate_silverwood",
            props -> new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING, props, BlockSetType.OAK),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.QUARTZ)
                    .forceSolidOn()
                    .instrument(NoteBlockInstrument.BASS)
                    .noCollission()
                    .strength(0.5F)
                    .ignitedByLava()
                    .pushReaction(PushReaction.DESTROY));

    public static final RegistryObject<BlockTable> TABLE_WOOD = ForgeRegistration.registerBlock(BLOCKS,
            "table_wood",
            BlockTable::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
                    .ignitedByLava());

    public static final RegistryObject<BlockTable> TABLE_STONE = ForgeRegistration.registerBlock(BLOCKS,
            "table_stone",
            BlockTable::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.5F)
                    .sound(SoundType.STONE)
                    .noOcclusion());

    public static final RegistryObject<BlockPavingStone> PAVING_STONE_TRAVEL = ForgeRegistration.registerBlock(BLOCKS,
            "paving_stone_travel",
            props -> new BlockPavingStone(false, props),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.5F)
                    .sound(SoundType.STONE)
                    .noOcclusion());

    public static final RegistryObject<BlockPavingStone> PAVING_STONE_BARRIER = ForgeRegistration.registerBlock(BLOCKS,
            "paving_stone_barrier",
            props -> new BlockPavingStone(true, props),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.5F)
                    .sound(SoundType.STONE)
                    .noOcclusion());

    public static final RegistryObject<BlockBarrier> BARRIER = ForgeRegistration.registerBlock(BLOCKS,
            "barrier",
            BlockBarrier::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NONE)
                    .strength(-1.0F, 999.0F)
                    .noOcclusion()
                    .noLootTable()
                    .dynamicShape()
                    .isValidSpawn((state, level, pos, type) -> false));

    public static final RegistryObject<BlockManaPod> MANA_POD = ForgeRegistration.registerBlock(BLOCKS,
            "mana_pod",
            BlockManaPod::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .strength(0.5F)
                    .sound(SoundType.CROP)
                    .noOcclusion()
                    .randomTicks()
                    .pushReaction(PushReaction.DESTROY)
                    .lightLevel(state -> state.getValue(BlockManaPod.AGE))
                    .isValidSpawn((state, level, pos, type) -> false));

    public static final RegistryObject<BlockNode> NODE = ForgeRegistration.registerBlock(BLOCKS,
            "node",
            BlockNode::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NONE)
                    .strength(2.0F, 3600000.0F)
                    .noOcclusion()
                    .noLootTable()
                    .isValidSpawn((state, level, pos, type) -> false));

    public static final RegistryObject<BlockJarNode> JAR_NODE = ForgeRegistration.registerBlock(BLOCKS,
            "jar_node",
            BlockJarNode::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NONE)
                    .strength(0.3F)
                    .sound(TTSoundTypes.JAR)
                    .noOcclusion());

    public static final RegistryObject<BlockNodeStabilizer> NODE_STABILIZER = ForgeRegistration.registerBlock(BLOCKS,
            "node_stabilizer",
            props -> new BlockNodeStabilizer(props, false),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.0F, 10.0F)
                    .noOcclusion());

    public static final RegistryObject<BlockNodeStabilizer> NODE_STABILIZER_ADVANCED = ForgeRegistration.registerBlock(BLOCKS,
            "node_stabilizer_advanced",
            props -> new BlockNodeStabilizer(props, true),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.0F, 10.0F)
                    .noOcclusion());

    public static final RegistryObject<BlockVisRelay> VIS_RELAY = ForgeRegistration.registerBlock(BLOCKS,
            "vis_relay",
            BlockVisRelay::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(1.5F)
                    .noOcclusion()
                    .sound(SoundType.AMETHYST));

    public static final RegistryObject<BlockNodeTransducer> NODE_TRANSDUCER = ForgeRegistration.registerBlock(BLOCKS,
            "node_transducer",
            BlockNodeTransducer::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.0F, 10.0F)
                    .noOcclusion());

    public static final RegistryObject<BlockAmber> AMBER_BRICK =
            ForgeRegistration.registerBlock(BLOCKS, "amber_brick", BlockAmber::new, amberProps());

    public static final RegistryObject<Block> FLESH_BLOCK = ForgeRegistration.registerBlock(BLOCKS,
            "flesh_block",
            Block::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_RED)
                    .strength(0.25F, 2.0F)
                    .sound(TTSoundTypes.GORE));

    public static final RegistryObject<BlockEffectShock> EFFECT_SHOCK = ForgeRegistration.registerBlock(BLOCKS,
            "effect_shock",
            BlockEffectShock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_CYAN)
                    .strength(0.0F, 999.0F)
                    .replaceable()
                    .noCollission()
                    .noOcclusion()
                    .lightLevel(state -> 7)
                    .randomTicks()
                    .noLootTable()
                    .pushReaction(PushReaction.DESTROY));

    public static final RegistryObject<Block> OBSIDIAN_TILE = ForgeRegistration.registerBlock(BLOCKS,
            "obsidian_tile",
            Block::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(50.0F, 1200.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());

    public static final RegistryObject<BlockObsidianTotem> OBSIDIAN_TOTEM = ForgeRegistration.registerBlock(BLOCKS,
            "obsidian_totem",
            BlockObsidianTotem::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(50.0F, 1200.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());

    public static final RegistryObject<BlockObsidianTotemCharged> OBSIDIAN_TOTEM_CHARGED = ForgeRegistration.registerBlock(BLOCKS,
            "obsidian_totem_charged",
            BlockObsidianTotemCharged::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(50.0F, 1200.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());

    public static final RegistryObject<Block> ELDRITCH_STONE = ForgeRegistration.registerBlock(BLOCKS,
            "eldritch_stone",
            Block::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(2.0F, 10.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());

    public static final RegistryObject<Block> ELDRITCH_STONE_INERT = ForgeRegistration.registerBlock(BLOCKS,
            "eldritch_stone_inert",
            Block::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(2.0F, 10.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()
                    .isValidSpawn((state, level, pos, type) -> false));

    public static final RegistryObject<Block> ELDRITCH_ROCK = ForgeRegistration.registerBlock(BLOCKS,
            "eldritch_rock",
            Block::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(2.0F, 10.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());

    public static final RegistryObject<Block> ELDRITCH_CRUST = ForgeRegistration.registerBlock(BLOCKS,
            "eldritch_crust",
            Block::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(2.0F, 10.0F)
                    .sound(TTSoundTypes.GORE));

    public static final RegistryObject<BlockEldritchInset> ELDRITCH_CRUST_GLOWING = ForgeRegistration.registerBlock(BLOCKS,
            "eldritch_crust_glowing",
            props -> new BlockEldritchInset(ConstantInt.of(0), props),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(2.0F, 30.0F)
                    .sound(SoundType.STONE)
                    .lightLevel(state -> 12)
                    .noOcclusion());

    public static final RegistryObject<BlockMirror> MIRROR = ForgeRegistration.registerBlock(BLOCKS,
            "mirror",
            props -> new BlockMirror(props, false),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(0.1F)
                    .sound(TTSoundTypes.JAR)
                    .noOcclusion());

    public static final RegistryObject<BlockMirror> MIRROR_ESSENTIA = ForgeRegistration.registerBlock(BLOCKS,
            "mirror_essentia",
            props -> new BlockMirror(props, true),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(0.1F)
                    .sound(TTSoundTypes.JAR)
                    .noOcclusion());

    public static final RegistryObject<BlockStairsTT> STAIRS_ELDRITCH = ForgeRegistration.registerBlock(BLOCKS,
            "stairs_eldritch",
            props -> new BlockStairsTT(ELDRITCH_STONE.get().defaultBlockState(), props),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(2.0F, 10.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());

    public static final RegistryObject<RotatedPillarBlock> STONE_ELDRITCH_PILLAR = ForgeRegistration.registerBlock(BLOCKS,
            "stone_eldritch_pillar",
            RotatedPillarBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(2.0F, 10.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());
    public static final RegistryObject<BlockStairsTT> STAIRS_ELDRITCH_TILE = ForgeRegistration.registerBlock(BLOCKS,
            "stairs_eldritch_tile",
            props -> new BlockStairsTT(STONE_ELDRITCH_TILE.get().defaultBlockState(), props),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(2.0F, 10.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());
    public static final RegistryObject<BlockStairsTT> STAIRS_ELDRITCH_ROCK = ForgeRegistration.registerBlock(BLOCKS,
            "stairs_eldritch_rock",
            props -> new BlockStairsTT(ELDRITCH_ROCK.get().defaultBlockState(), props),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(2.0F, 10.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());

    public static final RegistryObject<BlockEldritchDoor> ELDRITCH_DOOR = ForgeRegistration.registerBlock(BLOCKS,
            "eldritch_door",
            BlockEldritchDoor::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(-1.0F, Float.MAX_VALUE)
                    .sound(SoundType.STONE)
                    .lightLevel(state -> 12));

    public static final RegistryObject<Block> ELDRITCH_PEDESTAL = ForgeRegistration.registerBlock(BLOCKS,
            "eldritch_pedestal",
            Block::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(2.0F, 10.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());

    public static final RegistryObject<BlockEldritchInset> ELDRITCH_STONE_CRYSTAL = ForgeRegistration.registerBlock(BLOCKS,
            "eldritch_stone_crystal",
            props -> new BlockEldritchInset(UniformInt.of(1, 4), props),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(2.0F, 30.0F)
                    .sound(SoundType.STONE)
                    .lightLevel(state -> 12)
                    .noOcclusion());

    public static final RegistryObject<BlockEldritchNothing> ELDRITCH_NOTHING = ForgeRegistration.registerBlock(BLOCKS,
            "eldritch_nothing",
            BlockEldritchNothing::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(-1.0F, 6000000.0F)
                    .sound(SoundType.WOOL)
                    .lightLevel(state -> 3)
                    .noOcclusion()
                    .noLootTable()
                    .dynamicShape());

    public static final RegistryObject<BlockEldritchNothingDormant> ELDRITCH_NOTHING_DORMANT = ForgeRegistration.registerBlock(BLOCKS,
            "eldritch_nothing_dormant",
            BlockEldritchNothingDormant::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(-1.0F, 6000000.0F)
                    .sound(SoundType.WOOL)
                    .noLootTable()
                    .pushReaction(PushReaction.BLOCK)
                    .isValidSpawn((state, level, pos, type) -> false));

    public static final RegistryObject<BlockEldritchReliquary> ELDRITCH_RELIQUARY = ForgeRegistration.registerBlock(BLOCKS,
            "eldritch_reliquary",
            BlockEldritchReliquary::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(-1.0F, 6000000.0F)
                    .sound(SoundType.STONE)
                    .noLootTable()
                    .noOcclusion()
                    .pushReaction(PushReaction.BLOCK)
                    .isValidSpawn((state, level, pos, type) -> false));

    public static final RegistryObject<BlockEldritchLock> ELDRITCH_LOCK = ForgeRegistration.registerBlock(BLOCKS,
            "eldritch_lock",
            BlockEldritchLock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(-1.0F, Float.MAX_VALUE)
                    .sound(SoundType.STONE)
                    .lightLevel(state -> 5)
                    .noLootTable());

    public static final RegistryObject<BlockEldritchCrabSpawner> ELDRITCH_CRAB_SPAWNER = ForgeRegistration.registerBlock(BLOCKS,
            "eldritch_crab_spawner",
            BlockEldritchCrabSpawner::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(7.0F, 20.0F)
                    .sound(SoundType.STONE)
                    .lightLevel(state -> 4)
                    .noOcclusion()
                    .requiresCorrectToolForDrops());

    public static final RegistryObject<BlockEldritchTrap> ELDRITCH_TRAP = ForgeRegistration.registerBlock(BLOCKS,
            "eldritch_trap",
            BlockEldritchTrap::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(15.0F, 30.0F)
                    .sound(SoundType.STONE)
                    .noLootTable());

    public static final RegistryObject<BlockEldritchAltar> ELDRITCH_ALTAR = ForgeRegistration.registerBlock(BLOCKS,
            "eldritch_altar",
            BlockEldritchAltar::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(50.0F, 20000.0F)
                    .sound(SoundType.STONE)
                    .lightLevel(state -> 12)
                    .noOcclusion()
                    .noLootTable());

    public static final RegistryObject<BlockEldritchObelisk> ELDRITCH_OBELISK = ForgeRegistration.registerBlock(BLOCKS,
            "eldritch_obelisk",
            BlockEldritchObelisk::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(50.0F, 20000.0F)
                    .sound(SoundType.STONE)
                    .lightLevel(state -> state.getValue(BlockEldritchObelisk.PART) == 0 ? 8 : 0)
                    .noOcclusion()
                    .noLootTable());

    public static final RegistryObject<BlockEldritchPillar> ELDRITCH_PILLAR = ForgeRegistration.registerBlock(BLOCKS,
            "eldritch_pillar",
            BlockEldritchPillar::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(50.0F, 20000.0F)
                    .sound(SoundType.STONE)
                    .lightLevel(state -> 8)
                    .noOcclusion()
                    .noLootTable());

    public static final RegistryObject<BlockEldritchCap> ELDRITCH_CAPSTONE = ForgeRegistration.registerBlock(BLOCKS,
            "eldritch_capstone",
            BlockEldritchCap::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(50.0F, 20000.0F)
                    .sound(SoundType.STONE)
                    .lightLevel(state -> 8)
                    .noOcclusion()
                    .noLootTable());

    public static final RegistryObject<BlockEldritchPortal> ELDRITCH_PORTAL = ForgeRegistration.registerBlock(BLOCKS,
            "eldritch_portal",
            BlockEldritchPortal::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(-1.0F, 200000.0F)
                    .lightLevel(state -> 15)
                    .noOcclusion()
                    .noLootTable()
                    .noCollission()
                    .pushReaction(PushReaction.BLOCK));

    public static final RegistryObject<BlockAmber> AMBER_BLOCK =
            ForgeRegistration.registerBlock(BLOCKS, "amber_block", BlockAmber::new, amberProps());

    public static final RegistryObject<BlockPlaceholder> OBSIDIAN_PLACEHOLDER = ForgeRegistration.registerBlock(BLOCKS,
            "placeholder_obsidian",
            props -> new BlockPlaceholder(props, true),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.5F, 3600000.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());

    public static final RegistryObject<BlockPlaceholder> NETHER_BRICKS_PLACEHOLDER = ForgeRegistration.registerBlock(BLOCKS,
            "placeholder_nether_bricks",
            props -> new BlockPlaceholder(props, true),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.5F, 3600000.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());

    public static final RegistryObject<BlockInfernalFurnace> INFERNAL_FURNACE = ForgeRegistration.registerBlock(BLOCKS,
            "infernal_furnace",
            BlockInfernalFurnace::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.5F, 3600000.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .noLootTable()
                    .lightLevel(state -> 13));

    public static final RegistryObject<BlockHole> HOLE = ForgeRegistration.registerBlock(BLOCKS,
            "hole",
            BlockHole::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(-1.0F, 6000000.0F)
                    .sound(SoundType.WOOL)
                    .lightLevel(state -> 10)
                    .noOcclusion()
                    .noLootTable()
                    .pushReaction(PushReaction.BLOCK));

    public static final RegistryObject<BlockEffectSap> EFFECT_SAP = ForgeRegistration.registerBlock(BLOCKS,
            "effect_sap",
            BlockEffectSap::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(0.0F, 999.0F)
                    .replaceable()
                    .noCollission()
                    .noOcclusion()
                    .lightLevel(state -> 7)
                    .randomTicks()
                    .noLootTable()
                    .pushReaction(PushReaction.DESTROY));

    public static final RegistryObject<BlockEffectGlimmer> EFFECT_GLIMMER = ForgeRegistration.registerBlock(BLOCKS,
            "effect_glimmer",
            BlockEffectGlimmer::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NONE)
                    .strength(0.0F, 999.0F)
                    .replaceable()
                    .noCollission()
                    .noOcclusion()
                    .lightLevel(state -> 15)
                    .noLootTable()
                    .pushReaction(PushReaction.DESTROY));

    public static final RegistryObject<BlockLoot> LOOT_URN_COMMON =
            lootBlock("loot_urn_common", BlockLoot.LootType.COMMON, false);
    public static final RegistryObject<BlockLoot> LOOT_URN_UNCOMMON =
            lootBlock("loot_urn_uncommon", BlockLoot.LootType.UNCOMMON, false);
    public static final RegistryObject<BlockLoot> LOOT_URN_RARE =
            lootBlock("loot_urn_rare", BlockLoot.LootType.RARE, false);
    public static final RegistryObject<BlockLoot> LOOT_CRATE_COMMON =
            lootBlock("loot_crate_common", BlockLoot.LootType.COMMON, true);
    public static final RegistryObject<BlockLoot> LOOT_CRATE_UNCOMMON =
            lootBlock("loot_crate_uncommon", BlockLoot.LootType.UNCOMMON, true);
    public static final RegistryObject<BlockLoot> LOOT_CRATE_RARE =
            lootBlock("loot_crate_rare", BlockLoot.LootType.RARE, true);

    private static RegistryObject<BlockLoot> lootBlock(String id, BlockLoot.LootType type, boolean crate) {
        return ForgeRegistration.registerBlock(BLOCKS,
                id,
                props -> new BlockLoot(type, crate, props),
                BlockBehaviour.Properties.of()
                        .mapColor(crate ? MapColor.WOOD : MapColor.STONE)
                        .strength(0.15F, 0.0F)
                        .sound(crate ? SoundType.WOOD : TTSoundTypes.URN)
                        .noOcclusion());
    }

    private TTBlocks() {}

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        registerPottedPlants();
    }

    public static final RegistryObject<WallBlock> WALL_ARCANE_STONE =
            ForgeRegistration.registerBlock(BLOCKS, "wall_arcane_stone", WallBlock::new, stoneWallProps());
    public static final RegistryObject<WallBlock> WALL_ARCANE_BRICK =
            ForgeRegistration.registerBlock(BLOCKS, "wall_arcane_brick", WallBlock::new, stoneWallProps());
    public static final RegistryObject<RotatedPillarBlock> STONE_ARCANE_PILLAR =
            ForgeRegistration.registerBlock(BLOCKS, "stone_arcane_pillar", RotatedPillarBlock::new, stoneProps());
    public static final RegistryObject<WallBlock> WALL_ANCIENT =
            ForgeRegistration.registerBlock(BLOCKS, "wall_ancient", WallBlock::new, stoneWallProps());
    public static final RegistryObject<BlockStairsTT> STAIRS_ANCIENT_TILE = ForgeRegistration.registerBlock(BLOCKS,
            "stairs_ancient_tile",
            props -> new BlockStairsTT(STONE_ANCIENT_TILE.get().defaultBlockState(), props),
            stoneProps());
    public static final RegistryObject<SlabBlock> SLAB_ANCIENT_TILE =
            ForgeRegistration.registerBlock(BLOCKS, "slab_ancient_tile", SlabBlock::new, stoneProps());
    public static final RegistryObject<WallBlock> WALL_ANCIENT_TILE =
            ForgeRegistration.registerBlock(BLOCKS, "wall_ancient_tile", WallBlock::new, stoneWallProps());
    public static final RegistryObject<BlockStairsTT> STAIRS_ANCIENT_ROCK = ForgeRegistration.registerBlock(BLOCKS,
            "stairs_ancient_rock",
            props -> new BlockStairsTT(STONE_ANCIENT_ROCK.get().defaultBlockState(), props),
            stoneProps());
    public static final RegistryObject<SlabBlock> SLAB_ANCIENT_ROCK =
            ForgeRegistration.registerBlock(BLOCKS, "slab_ancient_rock", SlabBlock::new, stoneProps());
    public static final RegistryObject<WallBlock> WALL_ANCIENT_ROCK =
            ForgeRegistration.registerBlock(BLOCKS, "wall_ancient_rock", WallBlock::new, stoneWallProps());
    public static final RegistryObject<RotatedPillarBlock> STONE_ANCIENT_PILLAR =
            ForgeRegistration.registerBlock(BLOCKS, "stone_ancient_pillar", RotatedPillarBlock::new, stoneProps());
    public static final RegistryObject<SlabBlock> SLAB_ELDRITCH_STONE =
            ForgeRegistration.registerBlock(BLOCKS, "slab_eldritch_stone", SlabBlock::new, eldritchStoneProps());
    public static final RegistryObject<WallBlock> WALL_ELDRITCH_STONE =
            ForgeRegistration.registerBlock(BLOCKS, "wall_eldritch_stone", WallBlock::new, eldritchWallProps());
    public static final RegistryObject<WallBlock> WALL_ELDRITCH_TILE =
            ForgeRegistration.registerBlock(BLOCKS, "wall_eldritch_tile", WallBlock::new, eldritchWallProps());
    public static final RegistryObject<SlabBlock> SLAB_ELDRITCH_ROCK =
            ForgeRegistration.registerBlock(BLOCKS, "slab_eldritch_rock", SlabBlock::new, eldritchStoneProps());
    public static final RegistryObject<WallBlock> WALL_ELDRITCH_ROCK =
            ForgeRegistration.registerBlock(BLOCKS, "wall_eldritch_rock", WallBlock::new, eldritchWallProps());

    private static BlockBehaviour.Properties stoneWallProps() {
        return stoneProps().forceSolidOn();
    }

    private static BlockBehaviour.Properties eldritchStoneProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_BLACK)
                .strength(2.0F, 10.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties eldritchWallProps() {
        return eldritchStoneProps().forceSolidOn();
    }
}
