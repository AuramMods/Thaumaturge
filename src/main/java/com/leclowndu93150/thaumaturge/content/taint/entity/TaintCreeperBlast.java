package com.leclowndu93150.thaumaturge.content.taint.entity;

import com.leclowndu93150.thaumaturge.mixin.world.level.ExplosionRadiusAccessor;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.level.ExplosionEvent;
import org.jspecify.annotations.Nullable;

final class TaintCreeperBlast {
    private static final float BLAST_STRENGTH = 1.5F;
    private static final double BODY_MIDDLE = 0.5;

    private TaintCreeperBlast() {}

    static @Nullable Creeper replace(ServerLevel level, ExplosionEvent.Start event, Predicate<Creeper> tainted) {
        var explosion = event.getExplosion();
        if (((ExplosionRadiusAccessor) explosion).thaumaturge$getRadius() == BLAST_STRENGTH
                || !(explosion.getDirectSourceEntity() instanceof Creeper creeper)
                || !tainted.test(creeper)) {
            return null;
        }
        event.setCanceled(true);
        level.explode(creeper, creeper.getX(), creeper.getY(BODY_MIDDLE), creeper.getZ(),
                BLAST_STRENGTH, false, Level.ExplosionInteraction.NONE);
        return creeper;
    }
}
