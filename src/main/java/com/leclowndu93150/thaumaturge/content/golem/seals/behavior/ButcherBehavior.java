package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealArea;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

public final class ButcherBehavior extends HuntBehavior {
    private static final int STAGGER = 200;
    private static final int SCAN_PERIOD = 200;
    private static final int HERD_SIZE = 3;

    private final SealClock clock = new SealClock(STAGGER);
    private @Nullable Task hunt;
    private @Nullable Mob hunter;

    @Override
    public void tick(ServerLevel level, ISealEntity seal) {
        if (clock.advance() % SCAN_PERIOD != 0 || continuesHunt(level, seal)) {
            return;
        }
        hunt = null;
        hunter = null;
        AABB pasture = SealArea.bounds(seal);
        BlockPos anchor = seal.pos().pos();
        for (LivingEntity candidate :
                LevelSpaces.entitiesIn(level, LivingEntity.class, anchor, pasture, EntitySelector.NO_SPECTATORS)) {
            if (canSpare(level, anchor, pasture, candidate)) {
                hunt = mark(level, seal, candidate);
                return;
            }
        }
    }

    private boolean continuesHunt(ServerLevel level, ISealEntity seal) {
        if (hunt == null) {
            return false;
        }
        if (hunter == null) {
            return !hunt.isEnded();
        }
        if (hunter.isAlive() && hunt.entity() instanceof LivingEntity target && hunter.getTarget() == target) {
            if (isQuarry(level, seal, target)) {
                return true;
            }
            hunter.setTarget(null);
        }
        return false;
    }

    private static boolean canSpare(ServerLevel level, BlockPos anchor, AABB pasture, LivingEntity member) {
        if (!isLivestock(member)) {
            return false;
        }
        var herd = LevelSpaces.entitiesIn(level, member.getClass(), anchor, pasture, EntitySelector.NO_SPECTATORS);
        return herd.contains(member) && herd.stream()
                .filter(kin -> kin.getType() == member.getType() && isLivestock(kin))
                .limit(HERD_SIZE).count() == HERD_SIZE;
    }

    private static boolean isLivestock(LivingEntity entity) {
        if (!(entity instanceof Animal || entity instanceof WaterAnimal)
                || entity instanceof Enemy
                || entity instanceof AbstractGolem) {
            return false;
        }
        return entity.isAlive() && !(entity instanceof TamableAnimal pet && pet.isTame()) && !entity.isBaby();
    }

    @Override
    protected boolean isQuarry(ServerLevel level, ISealEntity seal, LivingEntity target) {
        return canSpare(level, seal.pos().pos(), SealArea.bounds(seal), target);
    }

    @Override
    public void onTaskStarted(ServerLevel level, ISealEntity seal, IGolemAPI golem, Task task) {
        super.onTaskStarted(level, seal, golem, task);
        if (hunt == task && golem.asEntity() instanceof Mob actor && actor.getTarget() == task.entity()) {
            hunter = actor;
        }
    }

    @Override
    public boolean canPerform(ISealEntity seal, IGolemAPI golem, Task task) {
        if (task == hunt && !task.isEnded() && golem.level() instanceof ServerLevel level
                && task.entity() instanceof LivingEntity target && isQuarry(level, seal, target)) {
            return true;
        }
        task.end();
        return false;
    }
}
