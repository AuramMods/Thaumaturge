package com.leclowndu93150.thaumaturge.content.taint.entity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.level.ExplosionEvent;
import org.junit.jupiter.api.Test;

class TaintCreeperBlastRuntimeTest {
    @Test
    void forgeCancelsTheOriginalCreeperBlastAndStartsOneReplacementAtBodyHeight() {
        var level = mock(ServerLevel.class);
        var creeper = new Creeper(EntityType.CREEPER, level);
        creeper.setPos(4.5, 64.0, -8.5);
        var original = explosion(level, creeper, 6.0F, Explosion.BlockInteraction.DESTROY);
        var replacement = explosion(level, creeper, 1.5F, Explosion.BlockInteraction.KEEP);
        when(level.explode(creeper, creeper.getX(), creeper.getY(0.5), creeper.getZ(),
                1.5F, false, Level.ExplosionInteraction.NONE)).thenAnswer(call -> {
            assertFalse(ForgeEventFactory.onExplosionStart(level, replacement));
            assertFalse(replacement.interactsWithBlocks());
            return replacement;
        });

        var events = new ArrayList<Explosion>();
        var sources = new ArrayList<Creeper>();
        Consumer<ExplosionEvent.Start> handler = event -> {
            if (event.getLevel() == level) {
                events.add(event.getExplosion());
                var source = TaintCreeperBlast.replace(level, event, mob -> mob == creeper);
                if (source != null) sources.add(source);
            }
        };
        MinecraftForge.EVENT_BUS.addListener(handler);
        try {
            assertTrue(ForgeEventFactory.onExplosionStart(level, original));
        } finally {
            MinecraftForge.EVENT_BUS.unregister(handler);
        }
        assertEquals(List.of(original, replacement), events);
        assertEquals(List.of(creeper), sources);
        verify(level).explode(creeper, creeper.getX(), creeper.getY(0.5), creeper.getZ(),
                1.5F, false, Level.ExplosionInteraction.NONE);
    }

    @Test
    void ordinaryCreepersAndOtherSourcesKeepTheirBlast() {
        var level = mock(ServerLevel.class);
        var creeper = new Creeper(EntityType.CREEPER, level);
        var ordinary = new ExplosionEvent.Start(level, explosion(level, creeper, 3.0F, Explosion.BlockInteraction.DESTROY));
        assertNull(TaintCreeperBlast.replace(level, ordinary, mob -> false));
        assertFalse(ordinary.isCanceled());
        var other = new ExplosionEvent.Start(level, explosion(level, null, 4.0F, Explosion.BlockInteraction.DESTROY));
        assertNull(TaintCreeperBlast.replace(level, other, mob -> true));
        assertFalse(other.isCanceled());
        verify(level, never()).explode(any(), anyDouble(), anyDouble(), anyDouble(), anyFloat(), anyBoolean(), any());
    }

    private static Explosion explosion(ServerLevel level, Entity source, float radius, Explosion.BlockInteraction interaction) {
        var damage = new DamageSource(Holder.direct(new DamageType("test_creeper_blast", 0.1F)), source);
        return new Explosion(level, source, damage, null, 4.5, 64.0, -8.5, radius, false, interaction);
    }
}
