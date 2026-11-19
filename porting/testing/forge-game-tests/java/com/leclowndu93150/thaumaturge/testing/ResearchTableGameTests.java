package com.leclowndu93150.thaumaturge.testing;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.capability.PlayerResearchAccess;
import com.leclowndu93150.thaumaturge.api.research.pool.AspectPoolAccess;
import com.leclowndu93150.thaumaturge.content.research.ResearchManager;
import com.leclowndu93150.thaumaturge.content.research.note.HexGrid;
import com.leclowndu93150.thaumaturge.content.research.note.ResearchNoteData;
import com.leclowndu93150.thaumaturge.content.research.note.ResearchNotes;
import com.leclowndu93150.thaumaturge.content.research.table.BlockEntityResearchTable;
import com.leclowndu93150.thaumaturge.content.research.table.BlockResearchTable;
import com.leclowndu93150.thaumaturge.content.research.table.MenuResearchTable;
import com.leclowndu93150.thaumaturge.content.research.table.ResearchTablePart;
import com.leclowndu93150.thaumaturge.network.ServerboundObtainNotePayload;
import com.leclowndu93150.thaumaturge.network.ServerboundTablePlaceAspectPayload;
import com.leclowndu93150.thaumaturge.network.transport.PayloadContext;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TTIds.MODID)
@PrefixGameTestTemplate(false)
public final class ResearchTableGameTests {
    private static final BlockPos TABLE = new BlockPos(2, 1, 2);
    private static final BlockPos EXTENSION = TABLE.east();
    private static final HexGrid.Hex CENTER = new HexGrid.Hex(0, 0);
    private static final int TOOLS_SLOT = BlockEntityResearchTable.SLOT_SCRIBE_TOOLS;
    private static final int NOTE_SLOT = BlockEntityResearchTable.SLOT_NOTE;

    private ResearchTableGameTests() {}

    @GameTest(template = "essentia_room")
    public static void researchTableSolvesAndRestoresTheoryNote(GameTestHelper test) {
        var player = ResearchGameTests.survivalPlayer(test);
        var context = new GameTestPayloadContext(player);
        var note = obtainNote(test, player, context);
        var menu = openTable(test, player);
        var table = Objects.requireNonNull(menu.blockEntity());
        menu.setCarried(note);
        menu.clicked(NOTE_SLOT, 0, ClickType.PICKUP, player);
        menu.broadcastChanges();
        var puzzle = table.noteData();
        test.assertTrue(menu.getCarried().isEmpty() && puzzle != null && !puzzle.complete(),
                "The research table did not accept the incomplete note");

        solveNote(test, player, context, table, puzzle);
        menu.clicked(NOTE_SLOT, 0, ClickType.PICKUP, player);
        var completed = menu.getCarried();
        var learned = ResearchNotes.dataOf(completed);
        test.assertTrue(table.noteData() == null && learned != null && learned.complete(),
                "Taking the result did not return the completed note");
        menu.setCarried(ItemStack.EMPTY);
        player.closeContainer();
        var spareCopy = completed.copy();
        player.setItemInHand(InteractionHand.MAIN_HAND, completed);
        test.assertTrue(completed.use(test.getLevel(), player, InteractionHand.MAIN_HAND).getResult().consumesAction()
                        && completed.isEmpty(),
                "Reading the completed note did not consume it");
        var research = PlayerResearchAccess.of(player);
        test.assertTrue(research.knows(learned.learnKey()) && research.hasCompleted(learned.entry()),
                "Reading the note did not record the theory and complete Research Expertise");
        player.setItemInHand(InteractionHand.MAIN_HAND, spareCopy);
        test.assertFalse(spareCopy.use(test.getLevel(), player, InteractionHand.MAIN_HAND).getResult().consumesAction(),
                "Reading the same theory twice awarded it again");
        test.assertTrue(spareCopy.getCount() == 1, "The redundant read consumed the second note");
        test.succeed();
    }

    private static ItemStack obtainNote(GameTestHelper test, FakePlayer player, PayloadContext context) {
        var expertise = TTIds.rl("research_expertise");
        var request = new ServerboundObtainNotePayload(expertise, 0);
        var inventory = player.getInventory();
        var tools = new ItemStack(TTItems.SCRIBING_TOOLS.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, tools);
        inventory.setItem(1, new ItemStack(Items.PAPER, 2));
        ServerboundObtainNotePayload.handle(request, context);
        test.assertTrue(tools.getDamageValue() == 0 && inventory.countItem(Items.PAPER) == 2,
                "Requesting a locked theory charged ink or paper");

        ResearchManager.complete(player, TTIds.rl("first_steps"));
        test.assertTrue(ResearchManager.unlockRequested(player, expertise), "First Steps did not open Research Expertise");
        test.assertFalse(ResearchManager.advanceStage(player, expertise), "Research Expertise advanced without its theory");
        ServerboundObtainNotePayload.handle(request, context);
        ServerboundObtainNotePayload.handle(request, context);
        test.assertTrue(tools.getDamageValue() == 1 && inventory.countItem(Items.PAPER) == 1
                        && inventory.countItem(TTItems.RESEARCH_NOTE.get()) == 1,
                "Generating one note did not cost exactly one ink and paper");
        int noteSlot = IntStream.range(0, inventory.items.size())
                .filter(index -> inventory.getItem(index).is(TTItems.RESEARCH_NOTE.get())).findFirst().orElseThrow();
        var note = inventory.removeItem(noteSlot, 1);
        var puzzle = ResearchNotes.dataOf(note);
        test.assertTrue(puzzle != null && puzzle.entry().equals(expertise) && puzzle.index() == 0
                        && !puzzle.complete() && puzzle.cells().size() == 19,
                "Research Expertise did not generate its 19-cell note");
        return note;
    }

    private static MenuResearchTable openTable(GameTestHelper test, FakePlayer player) {
        test.setBlock(TABLE, TTBlocks.TABLE_WOOD.get());
        test.setBlock(EXTENSION, TTBlocks.TABLE_WOOD.get());
        var level = test.getLevel();
        BlockPos main = test.absolutePos(TABLE);
        BlockPos extension = test.absolutePos(EXTENSION);
        test.assertTrue(test.getBlockState(TABLE).use(level, player, InteractionHand.MAIN_HAND, topOf(main))
                        .consumesAction() && player.getMainHandItem().isEmpty(),
                "The scribing tools did not convert the two wooden tables");
        var mainState = test.getBlockState(TABLE);
        var extensionState = test.getBlockState(EXTENSION);
        test.assertTrue(mainState.is(TTBlocks.RESEARCH_TABLE.get())
                        && mainState.getValue(BlockResearchTable.PART) == ResearchTablePart.MAIN
                        && extensionState.is(TTBlocks.RESEARCH_TABLE.get())
                        && extensionState.getValue(BlockResearchTable.PART) == ResearchTablePart.EXT
                        && BlockResearchTable.mainPos(extensionState, extension).equals(main),
                "The two research table halves did not resolve to the same main block");
        test.assertTrue(extensionState.use(level, player, InteractionHand.MAIN_HAND, topOf(extension)).consumesAction()
                        && player.containerMenu instanceof MenuResearchTable,
                "Using the extension did not open the research table menu");
        var menu = (MenuResearchTable) player.containerMenu;
        test.assertTrue(menu.stillValid(player) && menu.pos().equals(main)
                        && menu.getSlot(TOOLS_SLOT).getItem().getDamageValue() == 1,
                "The menu lost its main block or the scribing tools");
        return menu;
    }

    private static void solveNote(GameTestHelper test, FakePlayer player, PayloadContext context,
                                  BlockEntityResearchTable table, ResearchNoteData puzzle) {
        for (var key : List.of(TTAspects.COGNITIO, TTAspects.FABRICO, TTAspects.IGNIS,
                TTAspects.POTENTIA, TTAspects.INSTRUMENTUM)) {
            AspectPoolAccess.grant(player, Objects.requireNonNull(Aspects.resolve(test.getLevel(), key)), 3);
        }
        // Each installed aspect is a component of an adjacent aspect in the path.
        var fireCell = insideOf(rootOf(puzzle, TTAspects.COGNITIO));
        var toolCell = insideOf(rootOf(puzzle, TTAspects.FABRICO));
        var pos = table.getBlockPos();
        var fire = Objects.requireNonNull(Aspects.resolve(test.getLevel(), TTAspects.IGNIS));
        int initialFire = AspectPoolAccess.amount(player, fire);
        place(context, pos.east(), fireCell, TTAspects.IGNIS);
        test.assertTrue(table.noteData().cellAt(fireCell).type() == ResearchNoteData.TYPE_BLANK
                        && AspectPoolAccess.amount(player, fire) == initialFire
                        && table.items().getStackInSlot(TOOLS_SLOT).getDamageValue() == 1,
                "An edit for the extension changed the main menu's note or supplies");

        place(context, pos, fireCell, TTAspects.IGNIS);
        var partial = table.noteData();
        test.assertTrue(!partial.complete() && partial.cellAt(fireCell).aspectOrNull() == fire
                        && AspectPoolAccess.amount(player, fire) == initialFire - 1
                        && table.items().getStackInSlot(TOOLS_SLOT).getDamageValue() == 2,
                "One accepted placement did not cost one aspect point and ink");
        var snapshot = table.saveWithFullMetadata();
        table.items().setStackInSlot(NOTE_SLOT, ItemStack.EMPTY);
        table.load(snapshot);
        test.assertTrue(partial.equals(table.noteData())
                        && table.items().getStackInSlot(TOOLS_SLOT).getDamageValue() == 2,
                "The native block entity NBT lost the partial puzzle or ink damage");

        place(context, pos, CENTER, TTAspects.POTENTIA);
        place(context, pos, toolCell, TTAspects.INSTRUMENTUM);
        var solved = table.noteData();
        test.assertTrue(solved.complete() && solved.cells().size() == 5
                        && table.items().getStackInSlot(TOOLS_SLOT).getDamageValue() == 4,
                "The three-cell path did not connect both roots and consume three ink");
    }

    private static HexGrid.Hex rootOf(ResearchNoteData puzzle, ResourceKey<IAspect> aspect) {
        return puzzle.cells().stream()
                .filter(cell -> cell.type() == ResearchNoteData.TYPE_ROOT && cell.aspectOrNull().is(aspect))
                .findFirst().orElseThrow().hex();
    }

    private static HexGrid.Hex insideOf(HexGrid.Hex outer) {
        return IntStream.range(0, 6).mapToObj(outer::neighbour)
                .filter(hex -> HexGrid.distance(hex, CENTER) == 1).findFirst().orElseThrow();
    }

    private static void place(PayloadContext context, BlockPos pos, HexGrid.Hex cell, ResourceKey<IAspect> aspect) {
        var payload = new ServerboundTablePlaceAspectPayload(pos, cell.q(), cell.r(), Optional.of(aspect.location()));
        ServerboundTablePlaceAspectPayload.handle(payload, context);
    }

    private static BlockHitResult topOf(BlockPos pos) {
        return new BlockHitResult(Vec3.atCenterOf(pos).add(0, 0.5, 0), Direction.UP, pos, false);
    }

    private record GameTestPayloadContext(FakePlayer player) implements PayloadContext {
        @Override
        public CompletableFuture<Void> enqueueWork(Runnable work) {
            work.run();
            return CompletableFuture.completedFuture(null);
        }
    }
}
