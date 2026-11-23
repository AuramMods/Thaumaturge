package com.leclowndu93150.thaumaturge.testing;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.content.essentia.thaumatorium.BlockEntityThaumatorium;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TTIds.MODID)
@PrefixGameTestTemplate(false)
public final class StageGameTests {
    private static final BlockPos POS = new BlockPos(2, 3, 2);
    private static final ResourceLocation CRUCIBLE_PROBE = new ResourceLocation("crafttweaker", "crucible_probe");

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
