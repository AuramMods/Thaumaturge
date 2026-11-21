package com.leclowndu93150.thaumaturge.testing;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.labyrinth.EncounterRole;
import com.leclowndu93150.thaumaturge.api.labyrinth.MazeId;
import com.leclowndu93150.thaumaturge.content.eldritch.encounter.LabyrinthBinding;
import com.leclowndu93150.thaumaturge.content.entity.boss.hierophant.HierophantAction;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.state.AttachmentData;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TTIds.MODID)
@PrefixGameTestTemplate(false)
public final class BossLifecycleGameTests {
    private BossLifecycleGameTests() {}

    @GameTest(template = "golem_room")
    public static void hierophantFinishesDeathOnRetargetTick(GameTestHelper test) {
        var boss = test.spawn(TTEntities.ELDRITCH_HIEROPHANT.get(), new BlockPos(4, 2, 4));
        var target = test.spawnWithNoFreeWill(EntityType.COW, new BlockPos(5, 2, 4));
        boss.setHealth(0);
        boss.die(test.getLevel().damageSources().generic());
        AttachmentData.set(boss, TTAttachments.LABYRINTH_BINDING,
                LabyrinthBinding.of(new MazeId(0), EncounterRole.PRIMARY));
        boss.setTarget(target);
        boss.tickCount = 20;
        boss.deathTime = HierophantAction.DEATH.duration() - 1;
        test.assertTrue(boss.getTarget() == target && !boss.isRemoved(),
                "The dying boss should still have a target before the retarget tick");

        boss.tick();

        test.assertTrue(boss.getRemovalReason() == Entity.RemovalReason.KILLED,
                "The final death tick should remove the boss");
        target.discard();
        test.succeed();
    }
}
