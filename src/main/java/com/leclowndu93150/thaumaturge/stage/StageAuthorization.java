package com.leclowndu93150.thaumaturge.stage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;

/** Saved authority for a deferred operation. The player's current stages are read when work resumes. */
public record StageAuthorization(StageTarget target, UUID player) {
    public static final Codec<StageAuthorization> CODEC = RecordCodecBuilder.create(i -> i.group(
                    StageTarget.CODEC.fieldOf("target").forGetter(StageAuthorization::target),
                    UUIDUtil.CODEC.fieldOf("player").forGetter(StageAuthorization::player))
            .apply(i, StageAuthorization::new));

    public boolean allows(ServerLevel level) {
        return !StageRules.of(level).hasRules(target.family())
                || StageRules.allows(level, target, level.getServer().getPlayerList().getPlayer(player));
    }
}
