package com.leclowndu93150.thaumaturge.compat.ae2;

import appeng.api.config.Actionable;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.features.GridLinkables;
import appeng.api.networking.security.IActionSource;
import appeng.api.parts.PartHelper;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.util.AEColor;
import appeng.api.stacks.GenericStack;
import appeng.blockentity.storage.DriveBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.helpers.InventoryAction;
import appeng.menu.SlotSemantics;
import appeng.menu.slot.CraftingTermSlot;
import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaAccess;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCrystalAccess;
import com.leclowndu93150.thaumaturge.api.recipe.ArcaneCraftingTransaction;
import com.leclowndu93150.thaumaturge.api.research.IResearchCategory;
import com.leclowndu93150.thaumaturge.api.transaction.Transaction;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNode;
import com.leclowndu93150.thaumaturge.content.essentia.advancedfurnace.BlockEntityAdvancedAlchemicalFurnace;
import com.leclowndu93150.thaumaturge.content.essentia.jar.BlockEntityJar;
import com.leclowndu93150.thaumaturge.content.infusion.BlockEntityInfusionMatrix;
import com.leclowndu93150.thaumaturge.content.infusion.BlockEntityPedestal;
import com.leclowndu93150.thaumaturge.content.research.ResearchManager;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import com.leclowndu93150.thaumaturge.content.workbench.InventoryArcaneWorkbench;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

@GameTestHolder(TTIds.MODID)
@PrefixGameTestTemplate(false)
public final class ThaumicEnergisticsGameTests {
    private static final BlockPos POWER = new BlockPos(2, 3, 3);
    private static final BlockPos DRIVE = new BlockPos(3, 3, 3);
    private static final BlockPos BUS = new BlockPos(4, 3, 3);
    private static final BlockPos ARM = new BlockPos(5, 3, 3);
    private static final BlockPos WIRELESS = ARM.north();
    private static final IActionSource SOURCE = IActionSource.empty();

    private ThaumicEnergisticsGameTests() {}

    @GameTest(template = "infusion_room", batch = "thaumic_energistics", timeoutTicks = 600)
    public static void distillationBusesCellsTerminalAndPower(GameTestHelper test) {
        var level = test.getLevel();
        var input = new ItemStack(Items.COAL);
        var pattern = PatternDetailsHelper.decodePattern(ThaumicPatterns.distillation(input), level);
        test.assertTrue(pattern != null && pattern.getOutputs().length > 0, "No usable native distillation pattern");
        var target = pattern.getOutputs()[0];
        var aspect = ((EssentiaKey) target.what()).resolve(level);
        var drive = grid(test);
        var controller = BUS.north(2);
        placeFurnace(test, controller);
        var furnace = (BlockEntityAdvancedAlchemicalFurnace) test.getBlockEntity(controller);
        var state = furnace.saveWithoutMetadata();
        state.putInt("Heat", 500);
        state.putInt("Perditio", 500);
        state.putInt("Aqua", 500);
        furnace.load(state);

        var bus = PartHelper.setPart(level, test.absolutePos(BUS), Direction.NORTH, null, AEParts.IMPORT_BUS.asItem());
        var terminal = PartHelper.setPart(level, test.absolutePos(ARM), Direction.EAST, null,
                ThaumaturgeAe2.ESSENTIA_TERMINAL.get());
        var monitor = PartHelper.setPart(level, test.absolutePos(ARM), Direction.UP, null,
                ThaumaturgeAe2.ESSENTIA_CONVERSION_MONITOR.get());
        var emitter = PartHelper.setPart(level, test.absolutePos(ARM), Direction.SOUTH, null,
                ThaumaturgeAe2.ESSENTIA_LEVEL_EMITTER.get());
        emitter.getConfig().setStack(0, target);
        emitter.setReportingValue(0);

        test.startSequence().thenWaitUntil(() -> {
            test.assertTrue(bus.getMainNode().isActive() && terminal.getMainNode().isActive(), "Waiting for ME channels");
            var hatch = furnace.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.UP).orElseThrow(IllegalStateException::new);
            test.assertTrue(hatch.insertItem(0, input, true).isEmpty(), "Waiting for the assembled furnace hatch");
        }).thenExecute(() -> {
            var hatch = furnace.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.UP).orElseThrow(IllegalStateException::new);
            test.assertTrue(hatch.insertItem(0, input, false).isEmpty(), "Furnace rejected its pattern input");
        }).thenWaitUntil(() -> {
            var storage = drive.getMainNode().getGrid().getStorageService().getCachedInventory();
            for (var output : pattern.getOutputs()) {
                if (storage.get(output.what()) != output.amount()) {
                    var port = EssentiaAccess.conduit(level, test.absolutePos(BUS.north()), Direction.SOUTH);
                    test.fail("Waiting for the import bus to collect " + output + " (stored "
                            + storage.get(output.what()) + ", furnace " + furnace.aspects().entries()
                            + ", nozzle access " + (port != null) + ")");
                }
            }
        }).thenExecute(() -> {
            var actor = connectedPlayer(test, ARM.above());
            var menu = new EssentiaTerminalMenu(11, actor.getInventory(), terminal);
            actor.containerMenu = menu;
            test.assertTrue(menu.isKeyVisible(target.what()), "The essentia terminal concealed its aspect key");
            menu.setCarried(new ItemStack(TTItems.JAR_NORMAL.get()));
            menu.broadcastChanges();
            long serial = 0;
            // The native update helper assigns sequential IDs during the first full update.
            while (serial < pattern.getOutputs().length + 1 && EssentiaVessels.INSTANCE.getContainedStack(menu.getCarried()) == null) {
                menu.handleInteraction(serial++, InventoryAction.FILL_ITEM);
            }
            var inJar = EssentiaVessels.INSTANCE.getContainedStack(menu.getCarried());
            test.assertTrue(inJar != null && inJar.amount() > 0, "The terminal could not fill a carried vessel");
            menu.handleInteraction(-1, InventoryAction.EMPTY_ITEM);
            test.assertTrue(EssentiaVessels.INSTANCE.getContainedStack(menu.getCarried()) == null, "The terminal did not empty the vessel");
            actor.setItemInHand(InteractionHand.MAIN_HAND, EssentiaCrystalAccess.create(aspect, 1));
            monitor.onPartActivate(actor, InteractionHand.MAIN_HAND, Vec3.ZERO);
            test.assertTrue(target.what().equals(monitor.getDisplayed()), "Conversion monitor did not select the aspect");

            letPlayerLeave(test, actor);
            PartHelper.getOrPlacePartHost(level, test.absolutePos(BUS), false, null).removePart(bus);
            test.setBlock(ARM.below(), TTBlocks.JAR_NORMAL.get());
            var jar = (BlockEntityJar) test.getBlockEntity(ARM.below());
            jar.setAspectFilter(TTAspects.ORDO);
            jar.setStorageContents(TTAspects.ORDO, 17);
            PartHelper.setPart(level, test.absolutePos(ARM), Direction.DOWN, null, AEParts.STORAGE_BUS.asItem());
            var later = EssentiaKey.of(Aspects.resolve(level, TTAspects.ORDO));
            test.startSequence().thenWaitUntil(() -> test.assertTrue(drive.getMainNode().getGrid().getStorageService()
                    .getCachedInventory().get(later) >= 17, "Waiting for the labelled jar to mount as external ME storage"))
                    .thenExecute(() -> {
                        verifyCoexistingCells(test, drive);
                        PartHelper.getOrPlacePartHost(level, test.absolutePos(ARM), false, null)
                                .removePart(PartHelper.getPart(level, test.absolutePos(ARM), Direction.DOWN));
                        jar.setAspectFilter(TTAspects.ORDO);
                        test.assertTrue(new EssentiaBusAccess(level, test.absolutePos(ARM.below()), Direction.UP)
                                        .push(target.what(), 1, Actionable.SIMULATE) == 0,
                                "An export ignored the labelled jar's filter");
                        var saved = drive.saveWithFullMetadata();
                        test.setBlock(DRIVE, Blocks.AIR);
                        test.setBlock(POWER, Blocks.AIR);
                        test.setBlock(DRIVE, AEBlocks.DRIVE.block());
                        ((DriveBlockEntity) test.getBlockEntity(DRIVE)).load(saved);
                    }).thenWaitUntil(() -> test.assertFalse(terminal.getMainNode().isActive(), "Waiting for the grid to lose power"))
                    .thenExecute(() -> test.setBlock(POWER, AEBlocks.CREATIVE_ENERGY_CELL.block()))
                    .thenWaitUntil(() -> {
                        var restored = (DriveBlockEntity) test.getBlockEntity(DRIVE);
                        var node = restored.getMainNode();
                        test.assertTrue(node.isActive() && node.getGrid().getStorageService().getCachedInventory().get(target.what())
                                >= target.amount(), "Restored drive lost its stored essentia");
                    }).thenSucceed();
        });
    }

    @GameTest(template = "infusion_room", batch = "thaumic_energistics", timeoutTicks = 300)
    public static void arcaneMenusPatternsAndWirelessAccess(GameTestHelper test) {
        var level = test.getLevel();
        var drive = grid(test);
        test.setBlock(WIRELESS, AEBlocks.WIRELESS_ACCESS_POINT.block());
        test.setBlock(BUS.south(), ThaumaturgeAe2.ARCANE_ASSEMBLER.get());
        var terminal = PartHelper.setPart(level, test.absolutePos(ARM), Direction.SOUTH, null, ThaumaturgeAe2.ARCANE_TERMINAL.get());
        var actor = connectedPlayer(test, ARM.east());
        var assembler = (ArcaneAssemblerBlockEntity) test.getBlockEntity(BUS.south());
        var category = level.registryAccess().registryOrThrow(IResearchCategory.REGISTRY_KEY);
        test.assertTrue(category.containsKey(TTIds.rl("thaumic_energistics")), "Thaumonomicon category was not loaded");
        var wand = new ItemStack(TTItems.WAND.get());
        for (var primal : TTAspects.PRIMALS) WandVisHelper.storeVis(wand, primal, WandVisHelper.getMaxVis(wand));
        var workbench = terminal.workbench();
        test.startSequence().thenWaitUntil(() -> test.assertTrue(terminal.getMainNode().isActive()
                && assembler.acceptsPlans(), "Waiting for the arcane terminal and assembler to power on"))
                .thenExecute(() -> {
                    workbench.setItemDirect(1, new ItemStack(TTItems.JAR_NORMAL.get()));
                    workbench.setItemDirect(0, AEBlocks.INTERFACE.stack());
                    workbench.setItemDirect(2, new ItemStack(ThaumaturgeAe2.COMPONENTS.get(0).get()));
                    workbench.setItemDirect(InventoryArcaneWorkbench.WAND_SLOT, wand.copy());
                    AuraHelper.addVis(level, test.absolutePos(BUS), 100);
                    var activeGrid = drive.getMainNode().getGrid();
                    var network = activeGrid.getStorageService().getInventory();
                    var job = new ArcaneTerminalCrafting(terminal, actor, network, activeGrid.getEnergyService(), SOURCE);
                    test.assertTrue(job.inspect().failure() == ArcaneCraftingTransaction.Failure.RESEARCH_LOCKED,
                            "Arcane terminal allowed the provider recipe before its research");
                    ResearchManager.complete(actor, TTIds.rl("ae2_transport"));
                    var inputs = java.util.stream.IntStream.range(0, 9).mapToObj(workbench::getStackInSlot).toList();
                    var encoded = ThaumicPatterns.knowledge(actor, inputs, job.inspect());
                    var decoded = PatternDetailsHelper.decodePattern(encoded, level);
                    test.assertTrue(decoded != null, "AE2 could not decode the encoded knowledge pattern");
                    var received = Arrays.stream(decoded.getInputs()).map(input -> {
                        var key = new KeyCounter();
                        var exact = input.getPossibleInputs()[0];
                        key.add(exact.what(), exact.amount() * input.getMultiplier());
                        return key;
                    }).toArray(KeyCounter[]::new);
                    assembler.exchangeWand(wand.copy());
                    test.assertTrue(assembler.pushPattern(decoded, received, Direction.NORTH), "Assembler refused a valid known pattern");

                    var menu = new ArcaneTerminalMenu(ThaumaturgeAe2.ARCANE_TERMINAL_MENU.get(), 12, actor.getInventory(), terminal);
                    actor.containerMenu = menu;
                    menu.broadcastChanges();
                    var slot = (CraftingTermSlot) menu.getSlots(SlotSemantics.CRAFTING_RESULT).get(0);
                    slot.doClick(InventoryAction.CRAFT_ITEM, actor);
                    test.assertTrue(menu.getCarried().is(ThaumaturgeAe2.ESSENTIA_PROVIDER_ITEM.get()),
                            "Arcane terminal did not place its crafted provider on the cursor");
                    var wireless = ThaumaturgeAe2.WIRELESS_ARCANE_TERMINAL.get();
                    var handheld = new ItemStack(wireless);
                    wireless.injectAEPower(handheld, 1_000, Actionable.MODULATE);
                    var handler = GridLinkables.get(wireless);
                    handler.link(handheld, GlobalPos.of(level.dimension(), test.absolutePos(WIRELESS)));
                    actor.getInventory().setItem(0, handheld);
                    actor.doCloseContainer();
                    test.assertTrue(wireless.openFromInventory(actor, 0) && actor.containerMenu instanceof ArcaneTerminalMenu,
                            "A charged and linked wireless arcane terminal did not open");
                }).thenWaitUntil(() -> test.assertTrue(drive.getMainNode().getGrid().getStorageService().getCachedInventory()
                        .get(AEItemKey.of(ThaumaturgeAe2.ESSENTIA_PROVIDER_ITEM.get())) == 1,
                        "Waiting for the assembler to deliver the finished provider"))
                .thenExecute(() -> {
                    movePlayer(actor, test.absolutePos(ARM.east(85)));
                    actor.containerMenu.broadcastChanges();
                    test.assertFalse(actor.containerMenu.stillValid(actor), "A wireless terminal remained open beyond range");
                    letPlayerLeave(test, actor);
                }).thenSucceed();
    }

    @GameTest(template = "infusion_room", batch = "thaumic_energistics", timeoutTicks = 450)
    public static void infusionAndGolemUseTheNetwork(GameTestHelper test) {
        var level = test.getLevel();
        var drive = grid(test);
        var matrixPos = new BlockPos(6, 5, 6);
        var center = matrixPos.below(2);
        var providerPos = ARM.south();
        test.setBlock(providerPos, ThaumaturgeAe2.INFUSION_PROVIDER.get());
        test.setBlock(WIRELESS, AEBlocks.WIRELESS_ACCESS_POINT.block());
        test.setBlock(matrixPos, TTBlocks.INFUSION_MATRIX.get());
        test.setBlock(center, TTBlocks.PEDESTAL_ARCANE.get());
        test.setBlock(center.east(2), TTBlocks.PEDESTAL_ARCANE.get());
        test.setBlock(center.west(2), TTBlocks.PEDESTAL_ARCANE.get());
        for (int x : new int[]{-1, 1}) {
            for (int z : new int[]{-1, 1}) test.setBlock(center.offset(x, 0, z), TTBlocks.PILLAR_ARCANE.get());
        }
        var matrix = (BlockEntityInfusionMatrix) test.getBlockEntity(matrixPos);
        var provider = (EssentiaProviderBlockEntity) test.getBlockEntity(providerPos);
        var catalyst = (BlockEntityPedestal) test.getBlockEntity(center);
        var ordo = Aspects.resolve(level, TTAspects.ORDO);
        var vitreus = EssentiaKey.of(Aspects.resolve(level, TTAspects.VITREUS));
        var vinculum = EssentiaKey.of(Aspects.resolve(level, TTAspects.VINCULUM));

        test.startSequence().thenWaitUntil(() -> test.assertFalse(provider.isBlocked(), "Waiting for the infusion provider"))
                .thenExecute(() -> {
                    var actor = connectedPlayer(test, center.south(2));
                    ResearchManager.complete(actor, TTIds.rl("crystal_farmer"));
                    var storage = drive.getMainNode().getGrid().getStorageService().getInventory();
                    var ordoKey = EssentiaKey.of(ordo);
                    storage.insert(ordoKey, 10, Actionable.MODULATE, SOURCE);
                    storage.insert(vitreus, 10, Actionable.MODULATE, SOURCE);
                    storage.insert(vinculum, 5, Actionable.MODULATE, SOURCE);
                    catalyst.setItem(EssentiaCrystalAccess.create(ordo, 1));
                    ((BlockEntityPedestal) test.getBlockEntity(center.east(2))).setItem(new ItemStack(Items.WHEAT_SEEDS));
                    ((BlockEntityPedestal) test.getBlockEntity(center.west(2))).setItem(new ItemStack(TTItems.SALIS_MUNDUS.get()));

                    var backpack = new ItemStack(ThaumaturgeAe2.WIRELESS_GOLEM_BACKPACK.get());
                    actor.setItemInHand(InteractionHand.MAIN_HAND, backpack);
                    var pos = test.absolutePos(WIRELESS);
                    var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
                    test.assertTrue(backpack.getItem().onItemUseFirst(backpack,
                            new UseOnContext(actor, InteractionHand.MAIN_HAND, hit)).consumesAction(), "Backpack refused the access point");
                    var golem = TTEntities.THAUMATURGE_GOLEM.get().create(level);
                    golem.setOwner(actor);
                    var near = Vec3.atCenterOf(pos.above());
                    golem.moveTo(near.x, near.y, near.z);
                    golem.interact(actor, InteractionHand.MAIN_HAND);
                    test.assertTrue(GolemWirelessBackpack.gather(golem, actor.getUUID(), new ItemStack(Items.FLINT, 3), false) == 3
                                    && storage.getAvailableStacks().get(AEItemKey.of(Items.FLINT)) == 3,
                            "The equipped golem did not deposit gathered items");
                    golem.moveTo(near.x + 128, near.y, near.z);
                    test.assertTrue(GolemWirelessBackpack.gather(golem, actor.getUUID(), new ItemStack(Items.FLINT), false) == 0,
                            "The golem could still deposit out of range");
                    golem.discard();

                    // Allow the provider's storage watcher to see the newly inserted aspects.
                    test.startSequence().thenIdle(20).thenExecute(() -> {
                        matrix.onRightClick(level, actor);
                        var stable = matrix.saveWithoutMetadata();
                        stable.putFloat("Stability", BlockEntityInfusionMatrix.STABILITY_CAP);
                        matrix.load(stable);
                        matrix.onRightClick(level, actor);
                        test.assertTrue(matrix.isCrafting(), "The matrix could not start from network essentia");
                    }).thenWaitUntil(() -> test.assertTrue(catalyst.getItem().is(TTItems.CRYSTAL_ORDO.get()),
                            "Waiting for the ME infusion to complete"))
                            .thenExecute(() -> {
                                var available = storage.getAvailableStacks();
                                test.assertTrue(available.get(ordoKey) == 0 && available.get(vitreus) == 0 && available.get(vinculum) == 0,
                                        "The matrix did not deduct exactly the listed essentia");
                                letPlayerLeave(test, actor);
                            }).thenSucceed();
                });
    }

    @GameTest(template = "infusion_room", batch = "thaumic_energistics", timeoutTicks = 300)
    public static void pairedVisTunnelsReachNativeSource(GameTestHelper test) {
        var level = test.getLevel();
        grid(test);
        var sourcePos = BUS.north();
        var receiverPos = ARM.south();
        test.setBlock(sourcePos, TTBlocks.NODE.get());
        test.setBlock(receiverPos, ThaumaturgeAe2.VIS_RELAY_INTERFACE.get());
        var source = (BlockEntityNode) test.getBlockEntity(sourcePos);
        var aer = Aspects.resolve(level, TTAspects.AER);
        source.setAspects(AspectList.EMPTY.add(aer, 100));
        source.setEnergized(true);
        var receiver = (VisRelayInterfaceBlock.InterfaceEntity) test.getBlockEntity(receiverPos);
        var input = PartHelper.setPart(level, test.absolutePos(BUS), Direction.NORTH, null, ThaumaturgeAe2.VIS_P2P_TUNNEL.get());
        var output = PartHelper.setPart(level, test.absolutePos(ARM), Direction.SOUTH, null, ThaumaturgeAe2.VIS_P2P_TUNNEL.get());

        test.startSequence().thenWaitUntil(() -> test.assertTrue(input.getMainNode().isActive() && output.getMainNode().isActive(),
                "Waiting for paired tunnel channels")).thenExecute(() -> {
            var actor = connectedPlayer(test, ARM.above());
            actor.setItemInHand(InteractionHand.MAIN_HAND, AEItems.MEMORY_CARD.stack());
            test.assertTrue(input.onPartShiftActivate(actor, InteractionHand.MAIN_HAND, Vec3.ZERO)
                    && output.onPartActivate(actor, InteractionHand.MAIN_HAND, Vec3.ZERO), "Native memory card failed to pair Vis P2P");
            test.assertTrue(receiver.canSupply(), "Vis interface did not resolve its native source");
            try (var transaction = Transaction.openRoot()) {
                test.assertTrue(receiver.drainCentivis(TTAspects.AER, 5, transaction) == 5, "No primal vis reached the output");
                transaction.commit();
            }
            letPlayerLeave(test, actor);
        }).thenSucceed();
    }

    private static void placeFurnace(GameTestHelper test, BlockPos controller) {
        test.setBlock(controller, TTBlocks.ADVANCED_ALCHEMICAL_FURNACE.get());
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue;
                boolean corner = x != 0 && z != 0;
                var lower = corner ? TTBlocks.ADVANCED_ALCHEMICAL_FURNACE_ADVANCED_CONSTRUCT_PLACEHOLDER
                        : TTBlocks.ADVANCED_ALCHEMICAL_FURNACE_NOZZLE;
                var upper = corner ? TTBlocks.ADVANCED_ALCHEMICAL_FURNACE_ALEMBIC_PLACEHOLDER
                        : TTBlocks.ADVANCED_ALCHEMICAL_FURNACE_CONSTRUCT_PLACEHOLDER;
                test.setBlock(controller.offset(x, 0, z), lower.get());
                test.setBlock(controller.offset(x, 1, z), upper.get());
            }
        }
    }

    private static DriveBlockEntity grid(GameTestHelper test) {
        test.setBlock(POWER, AEBlocks.CREATIVE_ENERGY_CELL.block());
        test.setBlock(DRIVE, AEBlocks.DRIVE.block());
        var drive = (DriveBlockEntity) test.getBlockEntity(DRIVE);
        drive.getInternalInventory().setItemDirect(0, new ItemStack(ThaumaturgeAe2.CELLS.get(0).get()));
        drive.getInternalInventory().setItemDirect(1, AEItems.ITEM_CELL_1K.stack());
        for (var pos : List.of(BUS, ARM)) {
            PartHelper.setPart(test.getLevel(), test.absolutePos(pos), null, null, AEParts.GLASS_CABLE.item(AEColor.TRANSPARENT));
        }
        return drive;
    }

    private static void verifyCoexistingCells(GameTestHelper test, DriveBlockEntity drive) {
        var storage = drive.getMainNode().getGrid().getStorageService().getInventory();
        var item = AEItemKey.of(Items.DIRT);
        test.assertTrue(storage.insert(item, 32, Actionable.MODULATE, SOURCE) == 32
                        && storage.extract(item, 32, Actionable.MODULATE, SOURCE) == 32,
                "AE2 item storage did not round-trip with an essentia cell installed");
        if (ModList.get().isLoaded("appbot")) {
            try {
                var mana = (AEKey) Class.forName("appbot.ae2.ManaKey").getField("KEY").get(null);
                var cell = ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("appbot", "mana_storage_cell_1k"));
                drive.getInternalInventory().setItemDirect(2, new ItemStack(cell));
                test.assertTrue(storage.insert(mana, 300, Actionable.MODULATE, SOURCE) == 300
                                && storage.extract(mana, 300, Actionable.MODULATE, SOURCE) == 300,
                        "Applied Botanics mana did not round-trip in the same drive");
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Applied Botanics' mana key was unavailable", e);
            }
        }
    }

    private static ServerPlayer connectedPlayer(GameTestHelper test, BlockPos position) {
        var level = test.getLevel();
        var id = UUID.randomUUID();
        var player = new ServerPlayer(level.getServer(), level, new GameProfile(id, "METest" + id.toString().substring(0, 6)));
        var connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        connection.setProtocol(ConnectionProtocol.PLAY);
        level.getServer().getPlayerList().placeNewPlayer(connection, player);
        player.setGameMode(GameType.SURVIVAL);
        movePlayer(player, test.absolutePos(position));
        return player;
    }

    private static void movePlayer(ServerPlayer player, BlockPos position) {
        var location = Vec3.atBottomCenterOf(position);
        player.moveTo(location.x, location.y, location.z);
    }

    private static void letPlayerLeave(GameTestHelper test, ServerPlayer player) {
        test.getLevel().getServer().getPlayerList().remove(player);
        ((EmbeddedChannel) player.connection.connection.channel()).finishAndReleaseAll();
    }
}
