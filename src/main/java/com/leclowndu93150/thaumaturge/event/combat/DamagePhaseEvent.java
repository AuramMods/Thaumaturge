package com.leclowndu93150.thaumaturge.event.combat;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;

public abstract class DamagePhaseEvent extends LivingEvent {
    private final DamageSource source;
    private final float damage;

    private DamagePhaseEvent(LivingEntity entity, DamageSource source, float damage) {
        super(entity);
        this.source = source;
        this.damage = damage;
    }

    public DamageSource getSource() {
        return source;
    }

    public float getNewDamage() {
        return damage;
    }

    public static final class BeforeAbsorption extends DamagePhaseEvent {
        public BeforeAbsorption(LivingEntity entity, DamageSource source, float damage) {
            super(entity, source, damage);
        }
    }

    public static final class AfterDamage extends DamagePhaseEvent {
        public AfterDamage(LivingEntity entity, DamageSource source, float damage) {
            super(entity, source, damage);
        }
    }
}
