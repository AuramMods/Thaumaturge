package com.leclowndu93150.thaumaturge.compat.gamestages;

import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.server.level.ServerPlayer;

/** Loaded only when GameStages is installed. */
public final class GameStagesBridge {
    private GameStagesBridge() {}

    public static boolean hasStage(ServerPlayer player, String stage) {
        return GameStageHelper.hasStage(player, stage);
    }
}
