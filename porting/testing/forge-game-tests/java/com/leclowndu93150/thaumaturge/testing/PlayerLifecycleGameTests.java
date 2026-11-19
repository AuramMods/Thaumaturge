package com.leclowndu93150.thaumaturge.testing;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import com.leclowndu93150.thaumaturge.state.AttachmentData;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TTIds.MODID)
@PrefixGameTestTemplate(false)
public final class PlayerLifecycleGameTests {
    private PlayerLifecycleGameTests() {}

    @GameTest(template = "essentia_room")
    public static void removedPlayerCanFinishTickAndLogout(GameTestHelper test) {
        var player = ResearchGameTests.survivalPlayer(test);
        AttachmentData.get(player, TTAttachments.KNOWLEDGE);
        player.remove(Entity.RemovalReason.KILLED);
        test.assertTrue(player.isRemoved(), "The fixture should reach Forge's removed player lifecycle");

        ForgeEventFactory.onPlayerPostTick(player);
        ForgeEventFactory.firePlayerLoggedOut(player);
        test.succeed();
    }
}
