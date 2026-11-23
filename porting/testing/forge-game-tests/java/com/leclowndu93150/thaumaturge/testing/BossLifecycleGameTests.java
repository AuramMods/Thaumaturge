package com.leclowndu93150.thaumaturge.testing;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.labyrinth.EncounterRole;
import com.leclowndu93150.thaumaturge.api.labyrinth.MazeId;
import com.leclowndu93150.thaumaturge.content.eldritch.encounter.LabyrinthBinding;
import com.leclowndu93150.thaumaturge.content.entity.boss.hierophant.HierophantAction;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.state.AttachmentData;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TTIds.MODID)
@PrefixGameTestTemplate(false)
public final class BossLifecycleGameTests {
    private BossLifecycleGameTests() {}

    @GameTest(template = "golem_room")
    public static void chunkLoadSkipsItemsDiscardedDuringDeserialization(GameTestHelper test) {
        var level = test.getLevel();
        var pos = test.absolutePos(new BlockPos(4, 2, 4));
        var valid = new ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Items.STICK));
        var emptyTag = new CompoundTag();
        test.assertTrue(valid.save(emptyTag), "The item should be serializable");
        emptyTag.remove("Item");
        emptyTag.remove("UUID");
        var discarded = EntityType.loadEntityRecursive(emptyTag, level, Function.identity());
        test.assertTrue(discarded instanceof ItemEntity && discarded.getRemovalReason() == Entity.RemovalReason.DISCARDED,
                "An empty item should be discarded during deserialization");

        level.addLegacyChunkEntities(Stream.of(discarded, valid));

        test.assertTrue(level.getEntity(discarded.getUUID()) == null, "The discarded item should stay out of the world");
        test.assertTrue(level.getEntity(valid.getUUID()) == valid, "The valid item should still join the world");
        valid.discard();
        test.succeed();
    }

    @GameTest(template = "golem_room")
    public static void chunkLoadRejectsOrphanedLabyrinthMob(GameTestHelper test) {
        var level = test.getLevel();
        var pos = test.absolutePos(new BlockPos(4, 2, 4));
        var cow = Objects.requireNonNull(EntityType.COW.create(level));
        cow.moveTo(pos.getX(), pos.getY(), pos.getZ());
        AttachmentData.set(cow, TTAttachments.LABYRINTH_BINDING,
                LabyrinthBinding.of(new MazeId(Integer.MAX_VALUE), EncounterRole.MINION));

        level.addLegacyChunkEntities(Stream.of(cow));

        test.assertTrue(level.getEntity(cow.getUUID()) == null,
                "A mob bound to a missing labyrinth should stay out of the world");
        test.succeed();
    }

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
