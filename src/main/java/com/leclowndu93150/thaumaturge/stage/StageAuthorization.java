package com.leclowndu93150.thaumaturge.stage;

import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.UUID;
import java.util.Optional;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

/** Saved authority for a deferred operation. The player's current stages are read when work resumes. */
public record StageAuthorization(StageTarget target, Optional<UUID> player) {
    public static final Codec<StageAuthorization> CODEC = RecordCodecBuilder.create(i -> i.group(
                    StageTarget.CODEC.fieldOf("target").forGetter(StageAuthorization::target),
                    Codecs.optionalFieldOf(UUIDUtil.CODEC, "player").forGetter(StageAuthorization::player))
            .apply(i, StageAuthorization::new));

    public static StageAuthorization capture(ServerLevel level, StageTarget target, Player actor) {
        return new StageAuthorization(target,
                Optional.ofNullable(StageRules.verifiedActor(level, actor)).map(Player::getUUID));
    }

    public boolean allows(ServerLevel level) {
        return StageRules.allowsOwner(level, target, player.orElse(null));
    }
}
