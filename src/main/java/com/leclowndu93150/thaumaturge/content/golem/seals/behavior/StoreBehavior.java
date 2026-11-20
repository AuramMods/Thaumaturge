package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import com.leclowndu93150.thaumaturge.api.golems.GolemDispatch;
import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealBehavior;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealFilter;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealPos;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealSetting;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.api.items.InvHelper;
import com.leclowndu93150.thaumaturge.api.items.StackFilter;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskBoard;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.IItemHandler;
import org.jspecify.annotations.Nullable;

public final class StoreBehavior implements ISealBehavior {
    public static final SealSetting ONLY_EXISTING =
            new SealSetting("only_existing", "gui.thaumaturge.seal.setting.exist", false).readingAlso("pexist");

    private static final int STAGGER = 47;
    private static final int SCAN_PERIOD = 20;
    private static final double NEARBY_RANGE = 1.5;
    private static final double TOSS_SPREAD = 0.02;

    private final SealClock clock = new SealClock(STAGGER);
    private int pendingTask = Integer.MIN_VALUE;

    @Override
    public void tick(ServerLevel level, ISealEntity seal) {
        if (clock.advance() % SCAN_PERIOD != 0) {
            return;
        }
        Task pending = TaskBoard.of(level).find(pendingTask);
        if (pending == null || pending.isClaimed() || pending.isEnded() || pending.isCompleted()) {
            offerWork(level, seal);
        }
    }

    private void offerWork(ServerLevel level, ISealEntity seal) {
        Task task = Task.atBlock(seal.pos(), seal.pos().pos());
        task.setPriority(seal.priority());
        GolemDispatch.postTask(level, task);
        pendingTask = task.id();
    }

    @Override
    public void onTaskStarted(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task) {
        if (!seal.isStoppedByRedstone(level)) {
            offerWork(level, seal);
        }
    }

    @Override
    public boolean completeTask(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task) {
        SealPos at = seal.pos();
        IItemHandler inventory = InvHelper.handlerAt(level, at.pos(), at.face());
        ItemStack offered = offeredLoad(level, seal, golem, inventory);
        if (!offered.isEmpty()) {
            ItemStack load = golem.hands().release(offered);
            if (deliver(level, golem, at, inventory, load) > 0) {
                HandlingSound.play(golem, HandlingSound.LOW);
                golem.grantRankExperience(1);
                golem.animateSwing();
            }
        }
        task.end();
        return true;
    }

    private static int deliver(
            Level level, IGolemAPI golem, SealPos at, @Nullable IItemHandler inventory, ItemStack load) {
        if (load.isEmpty()) {
            return 0;
        }
        if (inventory == null) {
            Vec3 spot = Vec3.atCenterOf(at.pos()).relative(at.face(), 1.0);
            ItemEntity dropped = new ItemEntity(level, spot.x, spot.y, spot.z, load);
            Vec3 toss = tossVelocity(level.getRandom());
            dropped.setDeltaMovement(toss.x, toss.y, toss.z);
            if (level.addFreshEntity(dropped)) {
                return load.getCount();
            }
            golem.hands().hold(load);
            return 0;
        }
        int offered = load.getCount();
        ItemStack refused = InvHelper.insertStack(inventory, load, false);
        int delivered = placedCount(offered, refused);
        golem.hands().hold(refused);
        return delivered;
    }

    static int placedCount(int offered, ItemStack refused) {
        return Math.max(0, offered - (refused.isEmpty() ? 0 : refused.getCount()));
    }

    static Vec3 tossVelocity(RandomSource random) {
        return new Vec3(random.triangle(0.0, TOSS_SPREAD), 0.0, random.triangle(0.0, TOSS_SPREAD));
    }

    private static ItemStack offeredLoad(Level level, ISealEntity seal, IGolemAPI golem,
                                         @Nullable IItemHandler inventory) {
        var matching = ItemMatchSettings.of(seal);
        ISealFilter sealFilter = seal.filter().orElseThrow();
        var filter = new StackFilter(sealFilter.stacks(), sealFilter.limits(), sealFilter.isBlacklist(), matching);
        boolean onlyExisting = inventory != null && seal.setting(ONLY_EXISTING);
        for (ItemStack held : golem.hands().contents()) {
            var match = filter.firstAcceptedWithLimit(List.of(held));
            if (match.stack().isEmpty()) {
                continue;
            }
            boolean limited = sealFilter.usesLimits() && match.sizeLimit() > 0;
            int present = 0;
            if (inventory != null && (limited || onlyExisting)) {
                present = InvHelper.count(inventory, held, matching);
            } else if (limited) {
                present = InvHelper.countDropped(level, seal.pos().pos(), held, NEARBY_RANGE, matching);
            }
            if ((onlyExisting && present == 0) || (limited && present >= match.sizeLimit())) {
                continue;
            }
            int amount = limited ? Math.min(held.getCount(), match.sizeLimit() - present) : held.getCount();
            ItemStack offered = held.copyWithCount(amount);
            if (inventory != null) {
                amount = placedCount(amount, InvHelper.insertStack(inventory, offered, true));
            }
            if (amount > 0) {
                return offered.copyWithCount(amount);
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canPerform(ISealEntity seal, IGolemAPI golem, Task task) {
        SealPos at = seal.pos();
        Level level = golem.level();
        IItemHandler inventory = InvHelper.handlerAt(level, at.pos(), at.face());
        return !offeredLoad(level, seal, golem, inventory).isEmpty();
    }
}
