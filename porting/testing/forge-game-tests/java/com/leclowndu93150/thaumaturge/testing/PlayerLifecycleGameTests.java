package com.leclowndu93150.thaumaturge.testing;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraits;
import com.leclowndu93150.thaumaturge.content.entity.trait.MobTraitState;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import com.leclowndu93150.thaumaturge.registry.TTMobTraits;
import com.leclowndu93150.thaumaturge.state.AttachmentData;
import java.util.List;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TTIds.MODID)
@PrefixGameTestTemplate(false)
public final class PlayerLifecycleGameTests {
    private PlayerLifecycleGameTests() {}

    @GameTest(template = "essentia_room")
    public static void dyingPlayerRetainsStateWhenForgeCapabilitiesAreInvalidated(GameTestHelper test) {
        var player = ResearchGameTests.survivalPlayer(test);
        var storage = AttachmentData.storage(player);
        var trait = TTMobTraits.holder(TTMobTraits.FLUX_MILK);
        var dream = TTIds.rl("gotdream");
        var knowledge = storage.get(TTAttachments.KNOWLEDGE);
        knowledge.learn(dream);
        knowledge.markComplete(dream);
        storage.set(TTAttachments.MOB_TRAITS, new MobTraitState(List.of(trait)));

        // Ars Magica Legacy revives and then invalidates Forge caps when it looks up a dying player.
        player.setHealth(0);
        player.reviveCaps();
        player.invalidateCaps();
        test.assertTrue(player.isDeadOrDying() && !player.isRemoved() && !AttachmentData.findStorage(player).isPresent(),
                "The dying player should remain in the world with its Forge capabilities invalidated");
        test.assertTrue(MobTraits.traits(player).equals(List.of(trait)),
                "The renderer's trait query should still find the dying player's trait");
        MinecraftForge.EVENT_BUS.post(new EntityJoinLevelEvent(player, test.getLevel()));
        ForgeEventFactory.firePlayerLoggedOut(player);

        var replacement = ResearchGameTests.survivalPlayer(test);
        ForgeEventFactory.onPlayerClone(replacement, player, true);
        test.assertTrue(AttachmentData.get(replacement, TTAttachments.KNOWLEDGE).hasCompleted(dream)
                        && MobTraits.traits(replacement).equals(List.of(trait)),
                "Respawn should preserve the player's research and trait state");
        test.succeed();
    }

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
