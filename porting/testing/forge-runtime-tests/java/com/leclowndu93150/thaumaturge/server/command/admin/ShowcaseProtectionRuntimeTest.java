package com.leclowndu93150.thaumaturge.server.command.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.Event;
import org.junit.jupiter.api.Test;

class ShowcaseProtectionRuntimeTest {
    @Test
    void eitherMarkerProtectsNonlivingSpecimensFromDamageThatBypassesVanillaInvulnerability() {
        var level = mock(ServerLevel.class);
        var specimen = new ItemEntity(EntityType.ITEM, level);
        specimen.setItem(new ItemStack(Items.BOOK));
        var damage = mock(DamageSource.class);
        when(damage.is(DamageTypeTags.BYPASSES_INVULNERABILITY)).thenReturn(true);
        specimen.setInvulnerable(true);
        assertFalse(specimen.isInvulnerableTo(damage));

        ShowcaseMarker.apply(specimen);
        var styledName = specimen.getCustomName();
        specimen.setCustomName(null);
        assertFalse(specimen.hurt(damage, 6.0F));

        specimen.getTags().clear();
        specimen.setCustomName(styledName);
        assertFalse(specimen.hurt(damage, 6.0F));
        assertFalse(specimen.isRemoved());

        specimen.setCustomName(null);
        assertTrue(specimen.hurt(damage, 6.0F));
        assertTrue(specimen.isRemoved());
    }

    @Test
    void markedPersistentMobsSurvivePeacefulWhileOrdinaryPersistentMobsAreRemoved() {
        var level = mock(ServerLevel.class);
        when(level.getDifficulty()).thenReturn(Difficulty.PEACEFUL);
        var specimen = new Zombie(EntityType.ZOMBIE, level);
        var ordinary = new Zombie(EntityType.ZOMBIE, level);
        specimen.setPersistenceRequired();
        ordinary.setPersistenceRequired();
        ShowcaseMarker.apply(specimen);

        specimen.checkDespawn();
        ordinary.checkDespawn();
        assertFalse(specimen.isRemoved());
        assertTrue(ordinary.isRemoved());
    }

    @Test
    void unmarkedMobsStillUseForgeDespawnDecisions() {
        var level = mock(ServerLevel.class);
        when(level.getDifficulty()).thenReturn(Difficulty.NORMAL);
        var specimen = new Zombie(EntityType.ZOMBIE, level);
        var ordinary = new Zombie(EntityType.ZOMBIE, level);
        ShowcaseMarker.apply(specimen);
        var observed = new ArrayList<Zombie>();
        Consumer<MobSpawnEvent.AllowDespawn> allow = event -> {
            if (event.getEntity() == specimen || event.getEntity() == ordinary) {
                observed.add((Zombie) event.getEntity());
                event.setResult(Event.Result.ALLOW);
            }
        };
        MinecraftForge.EVENT_BUS.addListener(allow);
        try {
            specimen.checkDespawn();
            ordinary.checkDespawn();
            assertEquals(List.of(ordinary), observed);
            assertFalse(specimen.isRemoved());
            assertTrue(ordinary.isRemoved());
        } finally {
            MinecraftForge.EVENT_BUS.unregister(allow);
        }
    }
}
