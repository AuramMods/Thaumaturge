package com.leclowndu93150.thaumaturge.testing;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.equipment.bauble.BaubleEvents;
import com.leclowndu93150.thaumaturge.network.ServerboundCloudJumpPayload;
import com.leclowndu93150.thaumaturge.network.transport.PayloadContext;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.concurrent.CompletableFuture;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import top.theillusivec4.curios.api.CuriosApi;

@GameTestHolder(TTIds.MODID)
@PrefixGameTestTemplate(false)
public final class CloudstepperGameTests {
    private CloudstepperGameTests() {}

    @GameTest(template = "golem_room", batch = "cloudstepper_jump")
    public static void airborneJumpGrantsOneProtectedLanding(GameTestHelper test) {
        var player = ResearchGameTests.survivalPlayer(test);
        var context = new ServerContext(player);
        var jump = new ServerboundCloudJumpPayload();
        player.setOnGround(false);
        player.fallDistance = 9.0F;
        ServerboundCloudJumpPayload.handle(jump, context);
        test.assertTrue(player.fallDistance == 9.0F && !BaubleEvents.hasPendingCloudJump(player),
                "A player without the ring should keep their fall distance");

        var curios = CuriosApi.getCuriosInventory(player).resolve().orElseThrow();
        curios.setEquippedCurio("ring", 0, new ItemStack(TTItems.CLOUD_RING.get()));
        player.setOnGround(true);
        ServerboundCloudJumpPayload.handle(jump, context);
        test.assertTrue(player.fallDistance == 9.0F && !BaubleEvents.hasPendingCloudJump(player),
                "A grounded wearer should keep their fall distance");

        player.setOnGround(false);
        ServerboundCloudJumpPayload.handle(jump, context);
        test.assertTrue(player.fallDistance == 0.0F && BaubleEvents.hasPendingCloudJump(player),
                "A valid airborne jump should reset the fall and arm one landing");
        player.fallDistance = 5.0F;
        ServerboundCloudJumpPayload.handle(jump, context);
        test.assertTrue(player.fallDistance == 5.0F, "Another request before landing should leave fall distance intact");

        float[] protectedFall = ForgeHooks.onLivingFall(player, 24.0F, 1.0F);
        float[] nextFall = ForgeHooks.onLivingFall(player, 24.0F, 1.0F);
        test.assertTrue(protectedFall != null && protectedFall[0] == 8.0F && protectedFall[1] == 1.0F,
                "Forge's fall event should remove sixteen blocks of distance from the protected landing");
        test.assertTrue(nextFall != null && nextFall[0] == 24.0F && !BaubleEvents.hasPendingCloudJump(player),
                "The next landing should use its full fall distance");
        test.succeed();
    }

    private record ServerContext(ServerPlayer player) implements PayloadContext {
        @Override
        public CompletableFuture<Void> enqueueWork(Runnable work) {
            work.run();
            return CompletableFuture.completedFuture(null);
        }
    }
}
