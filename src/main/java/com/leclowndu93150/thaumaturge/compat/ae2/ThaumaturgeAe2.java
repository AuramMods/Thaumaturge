package com.leclowndu93150.thaumaturge.compat.ae2;

import appeng.api.behaviors.ContainerItemStrategy;
import appeng.api.behaviors.GenericSlotCapacities;
import appeng.api.parts.PartModels;
import appeng.api.stacks.AEKeyTypes;
import appeng.api.storage.ITerminalHost;
import appeng.items.contents.CellConfig;
import appeng.items.parts.PartItem;
import appeng.items.storage.BasicStorageCell;
import appeng.items.storage.CreativeCellItem;
import appeng.parts.automation.StorageLevelEmitterPart;
import appeng.util.ConfigInventory;
import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.compat.ae2.client.Ae2Client;
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
        PartModels.registerModels(EssentiaTerminalPart.models());
        bus.addListener(ThaumaturgeAe2::registerKeyType);
        bus.addListener(ThaumaturgeAe2::setup);
        bus.addListener(ThaumaturgeAe2::addCreativeItems);
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
        });
    }

    private static void addCreativeItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().location().equals(TTCreativeTabs.THAUMATURGE.getId())) {
            event.accept(ESSENTIA_TERMINAL);
            event.accept(CELL_HOUSING);
            event.accept(INFUSION_PROVIDER_ITEM);
            event.accept(ESSENTIA_PROVIDER_ITEM);
            event.accept(ESSENTIA_STORAGE_MONITOR);
            event.accept(ESSENTIA_CONVERSION_MONITOR);
            event.accept(ESSENTIA_LEVEL_EMITTER);
            COMPONENTS.forEach(event::accept);
            CELLS.forEach(event::accept);
        }
    }

    private static RegistryObject<Item> component(String tier) {
        return ITEMS.register("essentia_component_" + tier, () -> new Item(new Item.Properties()));
    }

    private static RegistryObject<BasicStorageCell> cell(String tier, int component, int kilobytes, double idleDrain) {
        return ITEMS.register("essentia_cell_" + tier, () -> new BasicStorageCell(
                new Item.Properties().stacksTo(1), COMPONENTS.get(component).get(), CELL_HOUSING.get(),
                idleDrain, kilobytes, kilobytes * 8, 63, EssentiaKeyType.INSTANCE));
    }

    private static RegistryObject<BlockItem> blockItem(RegistryObject<? extends Block> block) {
        return ITEMS.register(block.getId().getPath(), () -> new BlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<PartItem<EssentiaMonitorPart>> monitor(String name, boolean conversion) {
        return ITEMS.register(name, () -> new PartItem<>(new Item.Properties(),
                EssentiaMonitorPart.class, item -> new EssentiaMonitorPart(item, conversion)));
    }

    private static RegistryObject<BlockEntityType<EssentiaProviderBlockEntity>> providerEntity(
            RegistryObject<EssentiaProviderBlock> block, boolean infusion) {
        return BLOCK_ENTITIES.register(block.getId().getPath(), () ->
                BlockEntityType.Builder.of((pos, state) -> new EssentiaProviderBlockEntity(pos, state, infusion),
                        block.get()).build(null));
    }
}
