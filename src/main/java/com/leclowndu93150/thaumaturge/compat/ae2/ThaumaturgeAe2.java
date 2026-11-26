package com.leclowndu93150.thaumaturge.compat.ae2;

import appeng.api.behaviors.ContainerItemStrategy;
import appeng.api.behaviors.GenericSlotCapacities;
import appeng.api.features.GridLinkables;
import appeng.api.upgrades.Upgrades;
import appeng.api.parts.PartModels;
import appeng.api.parts.PartHelper;
import appeng.api.stacks.AEKeyTypes;
import appeng.api.storage.ITerminalHost;
import appeng.items.contents.CellConfig;
import appeng.items.parts.PartItem;
import appeng.items.storage.BasicStorageCell;
import appeng.items.storage.CreativeCellItem;
import appeng.items.tools.powered.WirelessTerminalItem;
import appeng.parts.automation.StorageLevelEmitterPart;
import appeng.util.ConfigInventory;
import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessories;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.recipe.RegisterWorkbenchAuraSourcesEvent;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealFilterMode;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealType;
import com.leclowndu93150.thaumaturge.compat.ae2.client.Ae2Client;
import com.leclowndu93150.thaumaturge.content.golem.ItemSealPlacer;
import com.leclowndu93150.thaumaturge.content.spell.item.FocusItem;
import com.leclowndu93150.thaumaturge.registry.TTCreativeTabs;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;

public final class ThaumaturgeAe2 {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, TTIds.MODID);
    private static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, TTIds.MODID);
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, TTIds.MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, TTIds.MODID);
    private static final DeferredRegister<SealType> SEALS = DeferredRegister.create(SealType.REGISTRY_KEY, TTIds.MODID);

    public static final List<RegistryObject<Item>> COMPONENTS = List.of(
            component("1k"), component("4k"), component("16k"), component("64k"));
    public static final RegistryObject<Item> CELL_HOUSING =
            ITEMS.register("essentia_cell_housing", () -> new Item(new Item.Properties()));
    public static final List<RegistryObject<? extends Item>> CELLS = List.of(
            cell("1k", 0, 1, 0.5), cell("4k", 1, 4, 1.0), cell("16k", 2, 16, 1.5),
            cell("64k", 3, 64, 2.0), ITEMS.register("essentia_cell_creative",
                    () -> new CreativeCellItem(new Item.Properties().stacksTo(1)) {
                        @Override
                        public ConfigInventory getConfigInventory(ItemStack stack) {
                            return CellConfig.create(EssentiaKeyType.INSTANCE.filter(), stack);
                        }
                    }));
    public static final RegistryObject<PartItem<EssentiaTerminalPart>> ESSENTIA_TERMINAL =
            ITEMS.register("essentia_terminal", () -> new PartItem<>(new Item.Properties(),
                    EssentiaTerminalPart.class, EssentiaTerminalPart::new));
    public static final RegistryObject<MenuType<EssentiaTerminalMenu>> ESSENTIA_TERMINAL_MENU =
            MENUS.register("essentia_terminal", () -> Ae2Menus.create(ITerminalHost.class,
                    EssentiaTerminalMenu::new, host -> host.getMainMenuIcon().getHoverName()));
    public static final RegistryObject<PartItem<ArcaneTerminalPart>> ARCANE_TERMINAL =
            ITEMS.register("arcane_terminal", () -> new PartItem<>(new Item.Properties(),
                    ArcaneTerminalPart.class, ArcaneTerminalPart::new));
    public static final RegistryObject<Item> ARCANE_CHARGING_CARD =
            ITEMS.register("arcane_charging_card", () -> new Item(new Item.Properties()));
    public static final RegistryObject<PartItem<VisP2PTunnelPart>> VIS_P2P_TUNNEL =
            ITEMS.register("vis_p2p_tunnel", () -> new PartItem<>(new Item.Properties(),
                    VisP2PTunnelPart.class, VisP2PTunnelPart::new));
    public static final RegistryObject<WirelessThaumicTerminalItem> WIRELESS_ARCANE_TERMINAL =
            ITEMS.register("wireless_arcane_terminal", () -> new WirelessThaumicTerminalItem(new Item.Properties(), true));
    public static final RegistryObject<WirelessThaumicTerminalItem> WIRELESS_ESSENTIA_TERMINAL =
            ITEMS.register("wireless_essentia_terminal", () -> new WirelessThaumicTerminalItem(new Item.Properties(), false));
    public static final RegistryObject<MenuType<ArcaneTerminalMenu>> ARCANE_TERMINAL_MENU =
            MENUS.register("arcane_terminal", () -> Ae2Menus.create(ArcaneTerminalHost.class,
                    ThaumaturgeAe2::createArcaneTerminalMenu, host -> host.getMainMenuIcon().getHoverName()));
    public static final RegistryObject<EssentiaProviderBlock> INFUSION_PROVIDER =
            BLOCKS.register("infusion_provider", () -> new EssentiaProviderBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK), true));
    public static final RegistryObject<EssentiaProviderBlock> ESSENTIA_PROVIDER =
            BLOCKS.register("essentia_provider", () -> new EssentiaProviderBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK), false));
    public static final RegistryObject<BlockEntityType<EssentiaProviderBlockEntity>> INFUSION_PROVIDER_ENTITY =
            providerEntity(INFUSION_PROVIDER, true);
    public static final RegistryObject<BlockEntityType<EssentiaProviderBlockEntity>> ESSENTIA_PROVIDER_ENTITY =
            providerEntity(ESSENTIA_PROVIDER, false);
    public static final RegistryObject<BlockItem> INFUSION_PROVIDER_ITEM = blockItem(INFUSION_PROVIDER);
    public static final RegistryObject<BlockItem> ESSENTIA_PROVIDER_ITEM = blockItem(ESSENTIA_PROVIDER);
    public static final RegistryObject<ArcaneAssemblerBlock> ARCANE_ASSEMBLER =
            BLOCKS.register("arcane_assembler",
                    () -> new ArcaneAssemblerBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)));
    public static final RegistryObject<BlockEntityType<ArcaneAssemblerBlockEntity>> ARCANE_ASSEMBLER_ENTITY =
            BLOCK_ENTITIES.register("arcane_assembler", () -> BlockEntityType.Builder
                    .of(ArcaneAssemblerBlockEntity::new, ARCANE_ASSEMBLER.get()).build(null));
    public static final RegistryObject<BlockItem> ARCANE_ASSEMBLER_ITEM = blockItem(ARCANE_ASSEMBLER);
    public static final RegistryObject<KnowledgePatternItem> KNOWLEDGE_PATTERN = ITEMS.register("knowledge_pattern",
            () -> new KnowledgePatternItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<VisRelayInterfaceBlock> VIS_RELAY_INTERFACE =
            BLOCKS.register("vis_relay_interface",
                    () -> new VisRelayInterfaceBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)));
    public static final RegistryObject<BlockEntityType<VisRelayInterfaceBlock.InterfaceEntity>> VIS_RELAY_INTERFACE_ENTITY =
            BLOCK_ENTITIES.register("vis_relay_interface", () -> BlockEntityType.Builder
                    .of(VisRelayInterfaceBlock.InterfaceEntity::new, VIS_RELAY_INTERFACE.get()).build(null));
    public static final RegistryObject<BlockItem> VIS_RELAY_INTERFACE_ITEM = blockItem(VIS_RELAY_INTERFACE);
    public static final RegistryObject<WirelessBackpackItem> WIRELESS_GOLEM_BACKPACK = ITEMS.register(
            "wireless_golem_backpack", () -> new WirelessBackpackItem(new Item.Properties()));
    public static final RegistryObject<ItemSealPlacer> WIRELESS_IMPORT_SEAL = exchangeSeal("wireless_import", false);
    public static final RegistryObject<ItemSealPlacer> WIRELESS_EXPORT_SEAL = exchangeSeal("wireless_export", true);
    public static final RegistryObject<EssentiaVibrationChamber> ESSENTIA_VIBRATION_CHAMBER =
            BLOCKS.register("essentia_vibration_chamber",
                    () -> new EssentiaVibrationChamber(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)));
    public static final RegistryObject<BlockEntityType<EssentiaVibrationChamber.ChamberEntity>>
            ESSENTIA_VIBRATION_CHAMBER_ENTITY = BLOCK_ENTITIES.register("essentia_vibration_chamber",
                    () -> BlockEntityType.Builder.of(EssentiaVibrationChamber.ChamberEntity::new,
                            ESSENTIA_VIBRATION_CHAMBER.get()).build(null));
    public static final RegistryObject<BlockItem> ESSENTIA_VIBRATION_CHAMBER_ITEM = blockItem(ESSENTIA_VIBRATION_CHAMBER);
    public static final RegistryObject<CrankGearbox> IRON_GEARBOX = gearbox("iron_gearbox", false);
    public static final RegistryObject<CrankGearbox> THAUMIUM_GEARBOX = gearbox("thaumium_gearbox", true);
    public static final RegistryObject<BlockEntityType<CrankGearbox.GearboxEntity>> IRON_GEARBOX_ENTITY =
            gearboxEntity(IRON_GEARBOX, false);
    public static final RegistryObject<BlockEntityType<CrankGearbox.GearboxEntity>> THAUMIUM_GEARBOX_ENTITY =
            gearboxEntity(THAUMIUM_GEARBOX, true);
    public static final RegistryObject<BlockItem> IRON_GEARBOX_ITEM = blockItem(IRON_GEARBOX);
    public static final RegistryObject<BlockItem> THAUMIUM_GEARBOX_ITEM = blockItem(THAUMIUM_GEARBOX);
    public static final RegistryObject<FocusItem> WRENCH_FOCUS =
            ITEMS.register("focus_ae_wrench", () -> new FocusItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<PartItem<EssentiaMonitorPart>> ESSENTIA_STORAGE_MONITOR =
            monitor("essentia_storage_monitor", false);
    public static final RegistryObject<PartItem<EssentiaMonitorPart>> ESSENTIA_CONVERSION_MONITOR =
            monitor("essentia_conversion_monitor", true);
    public static final RegistryObject<PartItem<StorageLevelEmitterPart>> ESSENTIA_LEVEL_EMITTER =
            ITEMS.register("essentia_level_emitter", () -> new PartItem<>(new Item.Properties(),
                    StorageLevelEmitterPart.class, StorageLevelEmitterPart::new));

    private ThaumaturgeAe2() {}

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        BLOCK_ENTITIES.register(bus);
        ITEMS.register(bus);
        MENUS.register(bus);
        SEALS.register(bus);
        GolemAccessories.register(GolemWirelessBackpack.ACCESSORY);
        PartModels.registerModels(EssentiaTerminalPart.models());
        PartModels.registerModels(ArcaneTerminalPart.models());
        PartModels.registerModels(VisP2PTunnelPart.MODEL);
        bus.addListener(ThaumaturgeAe2::registerKeyType);
        bus.addListener(ThaumaturgeAe2::setup);
        bus.addListener(ThaumaturgeAe2::addCreativeItems);
        bus.addListener(ThaumicResearchPack::register);
        bus.addListener(ThaumaturgeAe2::registerWorkbenchAura);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> Ae2Client.register(bus));
    }

    private static void registerKeyType(RegisterEvent event) {
        // AE2 initializes custom key types during the block register event.
        if (event.getRegistryKey().equals(Registries.BLOCK)) {
            AEKeyTypes.register(EssentiaKeyType.INSTANCE);
        }
    }

    private static void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            var type = EssentiaKeyType.INSTANCE;
            ContainerItemStrategy.register(type, EssentiaKey.class, EssentiaVessels.INSTANCE);
            GenericSlotCapacities.register(type, 64L);
            EssentiaBusAccess.register();
            Upgrades.add(ARCANE_CHARGING_CARD.get(), ARCANE_TERMINAL.get(), 1);
            GridLinkables.register(WIRELESS_ARCANE_TERMINAL.get(), WirelessTerminalItem.LINKABLE_HANDLER);
            GridLinkables.register(WIRELESS_ESSENTIA_TERMINAL.get(), WirelessTerminalItem.LINKABLE_HANDLER);
        });
    }

    private static void registerWorkbenchAura(RegisterWorkbenchAuraSourcesEvent event) {
        event.register((context, player, inventory, requested, transaction) -> {
            var level = context.level();
            var pos = context.position();
            if (pos == null) {
                if (!WirelessThaumicTerminalItem.ownsWorkbench(player, context.hostIdentity())) return 0;
                pos = player.blockPosition();
            } else if (!(level.getBlockEntity(pos) instanceof ArcaneAssemblerBlockEntity)) {
                boolean terminal = false;
                for (var side : net.minecraft.core.Direction.values()) {
                    var part = PartHelper.getPart(ARCANE_TERMINAL.get(), level, pos, side);
                    if (part != null && part.context(player).equals(context)) {
                        terminal = true;
                        break;
                    }
                }
                if (!terminal) return 0;
            }
            return (int) AuraHelper.drainVis(level, pos, requested, transaction);
        });
    }

    private static void addCreativeItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().location().equals(TTCreativeTabs.THAUMATURGE.getId())) {
            event.accept(ESSENTIA_TERMINAL);
            event.accept(ARCANE_TERMINAL);
            event.accept(ARCANE_CHARGING_CARD);
            event.accept(WIRELESS_ARCANE_TERMINAL);
            event.accept(WIRELESS_ESSENTIA_TERMINAL);
            event.accept(CELL_HOUSING);
            event.accept(INFUSION_PROVIDER_ITEM);
            event.accept(ESSENTIA_PROVIDER_ITEM);
            event.accept(ESSENTIA_STORAGE_MONITOR);
            event.accept(ESSENTIA_CONVERSION_MONITOR);
            event.accept(ESSENTIA_LEVEL_EMITTER);
            event.accept(ARCANE_ASSEMBLER_ITEM);
            event.accept(VIS_P2P_TUNNEL);
            event.accept(VIS_RELAY_INTERFACE_ITEM);
            event.accept(WIRELESS_GOLEM_BACKPACK);
            event.accept(WIRELESS_IMPORT_SEAL);
            event.accept(WIRELESS_EXPORT_SEAL);
            event.accept(ESSENTIA_VIBRATION_CHAMBER_ITEM);
            event.accept(IRON_GEARBOX_ITEM);
            event.accept(THAUMIUM_GEARBOX_ITEM);
            event.accept(WRENCH_FOCUS);
            COMPONENTS.forEach(event::accept);
            CELLS.forEach(event::accept);
        }
    }

    private static RegistryObject<Item> component(String tier) {
        return ITEMS.register("essentia_component_" + tier, () -> new Item(new Item.Properties()));
    }

    private static ArcaneTerminalMenu createArcaneTerminalMenu(
            int id, net.minecraft.world.entity.player.Inventory player, ArcaneTerminalHost terminal) {
        return new ArcaneTerminalMenu(ARCANE_TERMINAL_MENU.get(), id, player, terminal);
    }

    private static RegistryObject<BasicStorageCell> cell(String tier, int component, int kilobytes, double idleDrain) {
        return ITEMS.register("essentia_cell_" + tier, () -> new BasicStorageCell(
                new Item.Properties().stacksTo(1), COMPONENTS.get(component).get(), CELL_HOUSING.get(),
                idleDrain, kilobytes, kilobytes * 8, 63, EssentiaKeyType.INSTANCE));
    }

    private static RegistryObject<BlockItem> blockItem(RegistryObject<? extends Block> block) {
        return ITEMS.register(block.getId().getPath(), () -> new BlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<CrankGearbox> gearbox(String id, boolean thaumium) {
        return BLOCKS.register(id, () -> new CrankGearbox(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK), thaumium));
    }

    private static RegistryObject<BlockEntityType<CrankGearbox.GearboxEntity>> gearboxEntity(
            RegistryObject<CrankGearbox> block, boolean thaumium) {
        return BLOCK_ENTITIES.register(block.getId().getPath(), () -> BlockEntityType.Builder
                .of((pos, state) -> new CrankGearbox.GearboxEntity(pos, state, thaumium), block.get()).build(null));
    }

    private static RegistryObject<PartItem<EssentiaMonitorPart>> monitor(String name, boolean conversion) {
        return ITEMS.register(name, () -> new PartItem<>(new Item.Properties(),
                EssentiaMonitorPart.class, item -> new EssentiaMonitorPart(item, conversion)));
    }

    private static RegistryObject<ItemSealPlacer> exchangeSeal(String name, boolean export) {
        var item = ITEMS.register("seal_" + name, () -> new ItemSealPlacer(TTIds.rl(name), new Item.Properties()));
        SEALS.register(name, () -> SealType.builder(() -> new WirelessExchangeSeal(export))
                .filter(1, SealFilterMode.PLAIN)
                .icon(TTIds.rl(export ? "textures/item/seal_fill.png" : "textures/item/seal_empty.png"))
                .placer(item).build());
        return item;
    }

    private static RegistryObject<BlockEntityType<EssentiaProviderBlockEntity>> providerEntity(
            RegistryObject<EssentiaProviderBlock> block, boolean infusion) {
        return BLOCK_ENTITIES.register(block.getId().getPath(), () ->
                BlockEntityType.Builder.of((pos, state) -> new EssentiaProviderBlockEntity(pos, state, infusion),
                        block.get()).build(null));
    }
}
