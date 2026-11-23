package com.leclowndu93150.thaumaturge.content.golem.seals;

import com.leclowndu93150.thaumaturge.api.golems.GolemTrait;
import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealType;
import com.leclowndu93150.thaumaturge.content.world.PlayerReach;
import com.leclowndu93150.thaumaturge.content.golem.GolemStages;
import com.leclowndu93150.thaumaturge.registry.TTSeals;
import com.leclowndu93150.thaumaturge.stage.StageRules;
import com.leclowndu93150.thaumaturge.stage.StageTarget;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

public final class SealAccess {
    private static final double REACH_BUFFER = 1.0;

    private SealAccess() {}

    public static boolean mayEdit(Player player, ISealEntity seal) {
        return (!seal.isLocked() || player.getUUID().equals(seal.owner()) || player.getAbilities().instabuild)
                && (!(player.level() instanceof ServerLevel server)
                        || StageRules.check(server, target(server, seal), player));
    }

    public static boolean isLive(Player player, ISealEntity seal) {
        return SealHandler.getSealEntity(player.level(), seal.pos()) == seal
                && PlayerReach.canInteractWithBlock(player, seal.pos().pos(), REACH_BUFFER);
    }

    public static boolean allows(@Nullable ISealEntity seal, IGolemAPI golem) {
        if (seal == null) {
            return true;
        }
        if (golem.level() instanceof ServerLevel server && !allowsWork(server, seal)) return false;
        if (seal.isLocked()
                && !golem.ownerIdentity()
                        .map(owner -> owner.equals(seal.owner()))
                        .orElse(false)) {
            return false;
        }
        SealType type = seal.type();
        Set<GolemTrait> traits = golem.properties().traits();
        return type.requiredTraits().stream().map(Holder::value).allMatch(traits::contains)
                && type.forbiddenTraits().stream().map(Holder::value).noneMatch(traits::contains);
    }

    static boolean allowsWork(ServerLevel level, ISealEntity seal) {
        return !StageRules.of(level).hasRules(StageTarget.GOLEM_SEAL)
                || StageRules.allowsOwner(level, target(level, seal), seal.owner());
    }

    static StageTarget target(ServerLevel level, ISealEntity seal) {
        var id = TTSeals.registry().getKey(seal.type());
        var target = id == null ? StageTarget.family(StageTarget.GOLEM_SEAL) : GolemStages.seal(id);
        return target.withSubject(level.getBlockState(seal.pos().pos()).getBlock().builtInRegistryHolder().key());
    }
}
