package com.leclowndu93150.thaumaturge.content.eldritch;

import com.leclowndu93150.thaumaturge.content.eldritch.altar.BlockEntityEldritchAltar;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeRecord;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.stage.StageRules;
import com.leclowndu93150.thaumaturge.stage.StageRules.Requirement;
import com.leclowndu93150.thaumaturge.stage.StageTarget;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

public final class EldritchStages {
    private EldritchStages() {}

    public static StageTarget altar(ResourceLocation family, BlockEntityEldritchAltar altar) {
        return StageTarget.recipe(family, altar.site().location())
                .withSubject(altar.site())
                .withSubject(altar.getBlockState().getBlock().builtInRegistryHolder().key())
                .withSubject(TTItems.ELDRITCH_EYE.get().builtInRegistryHolder().key());
    }

    public static @Nullable Requirement entryDenial(ServerLevel level, ServerPlayer player, MazeRecord maze) {
        if (!StageRules.of(level).hasRules(StageTarget.LABYRINTH_ENTRY)) {
            return null;
        }
        var plan = maze.plan();
        StageTarget target = StageTarget.recipe(StageTarget.LABYRINTH_ENTRY, plan.definition().location())
                .withSubject(plan.definition()).withSubject(TTBlocks.ELDRITCH_PORTAL.get().builtInRegistryHolder().key());
        if (plan.encounter().isPresent()) {
            target = target.withSubject(plan.encounter().get());
        }
        for (var room : plan.roomPalette()) {
            target = target.withSubject(room);
        }
        return StageRules.denial(level, target, player);
    }
}
