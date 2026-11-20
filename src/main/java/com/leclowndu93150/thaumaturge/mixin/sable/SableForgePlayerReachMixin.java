package com.leclowndu93150.thaumaturge.mixin.sable;

import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Player.class)
abstract class SableForgePlayerReachMixin {
    // Sable already projects Forge's modded block reach. Forge also checks raw reach on use.
    public boolean canReachRaw(BlockPos pos, double padding) {
        Player player = (Player) (Object) this;
        double reach = player.getAttributeValue(ForgeMod.BLOCK_REACH.get()) + padding;
        Vec3 center = LevelSpaces.worldCenter(player.level(), pos);
        return player.getEyePosition().distanceToSqr(center) <= reach * reach;
    }
}
