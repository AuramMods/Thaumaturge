package com.leclowndu93150.thaumaturge.testing;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.content.crucible.BlockEntityCrucible;
import com.leclowndu93150.thaumaturge.content.essentia.thaumatorium.BlockEntityThaumatorium;
import com.leclowndu93150.thaumaturge.content.recipe.crucible.CrucibleRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.crucible.CrucibleRecipeInput;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TTIds.MODID)
@PrefixGameTestTemplate(false)
public final class StageGameTests {
    private static final BlockPos POS = new BlockPos(2, 3, 2);
    private static final ResourceLocation CRUCIBLE_PROBE = new ResourceLocation("crafttweaker", "crucible_probe");
    private static final ResourceLocation TAGGED_COST = new ResourceLocation("crafttweaker", "tagged_cost");
    private static final ResourceLocation EMPTY_TAG_COST = new ResourceLocation("crafttweaker", "empty_tag_cost");

    private StageGameTests() {}

    @GameTest(template = "essentia_room", batch = "crafttweaker_dust", timeoutTicks = 220)
    public static void stageRevocationSuspendsPaidDustTrigger(GameTestHelper test) {
        var denied = player(test);
        var allowed = player(test);
        var stages = GameStageHelper.getPlayerData(allowed);
        stages.addStage("craft_initiate");
        test.setBlock(POS, Blocks.IRON_BLOCK);
        dust(test, denied);
        test.assertTrue(denied.getMainHandItem().getCount() == 2, "The denied player lost Salis Mundus");
        dust(test, allowed);
        test.assertTrue(allowed.getMainHandItem().getCount() == 1, "The eligible player did not pay one dust");
        dust(test, allowed);
        test.assertTrue(allowed.getMainHandItem().getCount() == 1, "An already queued block charged dust again");
        stages.removeStage("craft_initiate");
        test.startSequence().thenIdle(65).thenExecute(() -> {
            test.assertBlockPresent(Blocks.IRON_BLOCK, POS);
            stages.addStage("transform_iron");
        }).thenWaitUntil(() -> test.assertBlockPresent(Blocks.GOLD_BLOCK, POS)).thenExecute(() -> {
            test.setBlock(POS, Blocks.GOLD_ORE);
            dust(test, allowed);
            test.assertTrue(allowed.getMainHandItem().getCount() == 1, "A block tag didn't block the gold ore trigger");
            stages.addStage("transform_gold");
            dust(test, allowed);
            test.assertTrue(allowed.getMainHandItem().isEmpty(), "The unlocked tag trigger didn't charge one dust");
        }).thenWaitUntil(() -> test.assertBlockPresent(Blocks.IRON_BLOCK, POS)).thenExecute(() -> {
            remove(test, denied);
            remove(test, allowed);
        }).thenSucceed();
    }

    @GameTest(template = "essentia_room", batch = "crafttweaker_machine", timeoutTicks = 220)
    public static void machineKeepsItsOwnerAndEssentiaAcrossSave(GameTestHelper test) {
        var actor = player(test);
        var stages = GameStageHelper.getPlayerData(actor);
        stages.addStage("alchemy");
        var level = test.getLevel();
        var ignis = Objects.requireNonNull(Aspects.resolve(level, TTAspects.IGNIS));
        test.setBlock(POS.below(2), Blocks.LAVA);
        test.setBlock(POS.below(), Blocks.SMOOTH_STONE);
        test.setBlock(POS, TTBlocks.THAUMATORIUM.get());
        test.setBlock(POS.north(), Blocks.CHEST);
        var machine = (BlockEntityThaumatorium) test.getBlockEntity(POS);
        var chest = (ChestBlockEntity) test.getBlockEntity(POS.north());
        machine.catalyst().setStackInSlot(0, new ItemStack(Items.SAND, 2));
        machine.toggleRecipe(level, actor, CRUCIBLE_PROBE);
        test.assertTrue(machine.queue().isEmpty(), "A player without the machine stage queued the recipe");
        stages.addStage("automate_alchemy");
        machine.toggleRecipe(level, actor, CRUCIBLE_PROBE);
        test.assertTrue(machine.queue().contains(CRUCIBLE_PROBE), "Eligible player could not queue the recipe");
        test.startSequence().thenWaitUntil(() -> test.assertTrue(machine.roomFor(ignis, Direction.EAST) == 7,
                "Waiting for the scripted recipe to request seven Ignis")).thenExecute(() -> {
            test.assertTrue(machine.insert(ignis, 3, Direction.EAST, false) == 3, "Failed to store three Ignis");
            stages.removeStage("automate_alchemy");
            test.assertTrue(machine.insert(ignis, 4, Direction.EAST, false) == 0,
                    "Revoked owner could still feed the machine");
            machine.load(machine.saveWithFullMetadata());
        }).thenIdle(15).thenExecute(() -> {
            test.assertTrue(machine.catalystStack().getCount() == 2 && machine.essentia().amountOf(ignis) == 3
                            && chest.isEmpty(),
                    "Paused work altered inputs, buffered essentia, or outputs");
            stages.addStage("automate_alchemy");
        }).thenWaitUntil(() -> test.assertTrue(machine.roomFor(ignis, Direction.EAST) == 4,
                "Restored queue did not recover its owner and remaining cost")).thenExecute(() -> {
            test.assertTrue(machine.insert(ignis, 4, Direction.EAST, false) == 4, "Failed to store the rest of the cost");
        }).thenWaitUntil(() -> test.assertTrue(chest.countItem(Items.GOLD_INGOT) == 1,
                "The resumed recipe did not emit one gold ingot")).thenExecute(() -> {
            test.assertTrue(machine.catalystStack().getCount() == 1 && machine.essentia().isEmpty(),
                    "The resumed recipe did not spend one sand and seven Ignis");
            remove(test, actor);
        }).thenSucceed();
    }

    @GameTest(template = "essentia_room", batch = "crafttweaker_tagged_crucible")
    public static void cruciblePaysOverlappingAspectTagsExactlyOnce(GameTestHelper test) {
        var level = test.getLevel();
        var aqua = Objects.requireNonNull(Aspects.resolve(level, TTAspects.AQUA));
        var terra = Objects.requireNonNull(Aspects.resolve(level, TTAspects.TERRA));
        var ignis = Objects.requireNonNull(Aspects.resolve(level, TTAspects.IGNIS));
        var catalyst = new ItemStack(Items.IRON_NUGGET, 2);
        var recipe = (CrucibleRecipe) level.getRecipeManager().byKey(TAGGED_COST).orElseThrow();
        var supply = AspectList.EMPTY.add(aqua, 3).add(terra, 3).add(ignis, 4);
        var insufficient = AspectList.EMPTY.add(aqua, 2).add(terra, 5);
        test.assertFalse(recipe.matches(new CrucibleRecipeInput(catalyst, insufficient), level),
                "The narrow tag accepted Terra after its script removed Terra");

        var empty = (CrucibleRecipe) level.getRecipeManager().byKey(EMPTY_TAG_COST).orElseThrow();
        test.assertFalse(empty.matches(new CrucibleRecipeInput(new ItemStack(Items.GOLD_NUGGET), supply), level),
                "An empty aspect tag matched a catalyst");
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), level.registryAccess());
        try {
            CrucibleRecipe.STREAM_CODEC.encode(buffer, recipe);
            var decoded = CrucibleRecipe.STREAM_CODEC.decode(buffer);
            test.assertTrue(decoded.payment(supply, level.registryAccess()).orElseThrow().totalAmount() == 6
                            && buffer.readableBytes() == 0,
                    "Tagged costs did not round trip in the recipe packet");
        } finally {
            buffer.release();
        }

        test.setBlock(POS, TTBlocks.CRUCIBLE.get());
        var crucible = (BlockEntityCrucible) test.getBlockEntity(POS);
        crucible.getTank().setFluid(new FluidStack(Fluids.WATER, 300));
        for (var aspect : supply.entries()) crucible.addAspects(aspect.aspect(), aspect.amount());
        var output = new ArrayList<ItemStack>();
        var owner = player(test);
        var remaining = crucible.attemptSmeltForAutomation(catalyst.copyWithCount(1), owner, output::add, true);
        test.assertTrue(remaining == null && output.size() == 1 && output.get(0).is(Items.DIAMOND),
                "Overlapping aspect tags failed to craft one diamond");
        test.assertTrue(crucible.getAspects().equals(AspectList.EMPTY.add(ignis, 4))
                        && crucible.getTank().getFluidAmount() == 250,
                "Crucible spent more than three Aqua, three Terra, and fifty millibuckets");
        remove(test, owner);
        test.succeed();
    }

    @GameTest(template = "essentia_room", batch = "crafttweaker_tagged_machine", timeoutTicks = 220)
    public static void thaumatoriumAcceptsAlternativesWithoutOverfilling(GameTestHelper test) {
        var level = test.getLevel();
        var actor = player(test);
        GameStageHelper.getPlayerData(actor).addStage("automate_alchemy");
        var aqua = Objects.requireNonNull(Aspects.resolve(level, TTAspects.AQUA));
        var terra = Objects.requireNonNull(Aspects.resolve(level, TTAspects.TERRA));
        test.setBlock(POS.below(2), Blocks.LAVA);
        test.setBlock(POS.below(), Blocks.SMOOTH_STONE);
        test.setBlock(POS, TTBlocks.THAUMATORIUM.get());
        test.setBlock(POS.north(), Blocks.CHEST);
        var machine = (BlockEntityThaumatorium) test.getBlockEntity(POS);
        var chest = (ChestBlockEntity) test.getBlockEntity(POS.north());
        machine.catalyst().setStackInSlot(0, new ItemStack(Items.IRON_NUGGET, 2));
        machine.toggleRecipe(level, actor, TAGGED_COST);
        test.startSequence().thenWaitUntil(() -> test.assertTrue(machine.roomFor(aqua, Direction.EAST) == 5,
                "Waiting for the tagged recipe to accept Aqua")).thenExecute(() -> {
            test.assertTrue(machine.insert(terra, 9, Direction.EAST, false) == 3,
                    "Terra was counted against two different requirements");
            test.assertTrue(machine.insert(aqua, 2, Direction.EAST, false) == 2, "Aqua insertion was rejected");
            test.assertTrue(machine.roomFor(terra, Direction.EAST) == 0
                            && machine.roomFor(aqua, Direction.EAST) == 1,
                    "The machine requested the wrong final unit");
        }).thenIdle(10).thenExecute(() -> {
            test.assertTrue(chest.isEmpty() && machine.catalystStack().getCount() == 2,
                    "The tagged recipe used a catalyst with insufficient essentia");
            test.assertTrue(machine.insert(aqua, 1, Direction.EAST, false) == 1, "Final Aqua insertion failed");
        }).thenWaitUntil(() -> test.assertTrue(chest.countItem(Items.DIAMOND) == 1,
                "The machine did not finish the tagged recipe")).thenExecute(() -> {
            test.assertTrue(machine.catalystStack().getCount() == 1 && machine.essentia().isEmpty(),
                    "Tagged recipe paid its inputs more than once");
            remove(test, actor);
        }).thenSucceed();
    }

    private static ServerPlayer player(GameTestHelper test) {
        var level = test.getLevel();
        UUID id = UUID.randomUUID();
        var player = new ServerPlayer(level.getServer(), level,
                new GameProfile(id, "StageTest" + id.toString().substring(0, 6)));
        var connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        connection.setProtocol(ConnectionProtocol.PLAY);
        level.getServer().getPlayerList().placeNewPlayer(connection, player);
        player.setGameMode(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(TTItems.SALIS_MUNDUS.get(), 2));
        Vec3 location = Vec3.atBottomCenterOf(test.absolutePos(POS.south(2)));
        player.moveTo(location.x, location.y, location.z);
        return player;
    }

    private static void remove(GameTestHelper test, ServerPlayer player) {
        test.getLevel().getServer().getPlayerList().remove(player);
        ((EmbeddedChannel) player.connection.connection.channel()).finishAndReleaseAll();
    }

    private static void dust(GameTestHelper test, ServerPlayer player) {
        BlockPos pos = test.absolutePos(POS);
        var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        player.getMainHandItem().getItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
    }
}
