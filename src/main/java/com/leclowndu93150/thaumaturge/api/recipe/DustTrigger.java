package com.leclowndu93150.thaumaturge.api.recipe;

import com.leclowndu93150.thaumaturge.recipe.RegistryRecipe;
import com.leclowndu93150.thaumaturge.stage.StageTarget;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public interface DustTrigger extends RegistryRecipe<DustTriggerInput>, ResearchGated {
    default StageTarget stageTarget(DustTriggerInput input) {
        return StageTarget.recipe(StageTarget.DUST_TRIGGER, getId())
                .withSubject(input.clicked().getBlock().builtInRegistryHolder().key());
    }

    @Override
    default boolean isSpecial() {
        return true;
    }

    default List<BlockPos> sparkle(Level level, Player player, BlockPos pos, DustTriggerPlacement placement) {
        return List.of(pos);
    }

    default @Nullable DustTriggerPlacement findPlacement(DustTriggerInput input) {
        return null;
    }

    default boolean isMultiblock() {
        return false;
    }

    @Override
    default boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    void execute(DustTriggerInput input, Player player, @Nullable DustTriggerPlacement placement, Direction useFace);

    default boolean tryExecute(
            DustTriggerInput input, Player player, @Nullable DustTriggerPlacement placement, Direction useFace) {
        execute(input, player, placement, useFace);
        return true;
    }
}
