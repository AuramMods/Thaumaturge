package com.leclowndu93150.thaumaturge.event.combat;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Cancelable;

/** Fired for an eligible hit before sleep, shield, cooldown, or armor handling. */
@Cancelable
public final class IncomingDamageEvent extends LivingEvent {
    private final DamageSource source;
    private final float originalAmount;
    private float amount;

    public IncomingDamageEvent(LivingEntity entity, DamageSource source, float amount) {
        super(entity);
        this.source = source;
        this.originalAmount = amount;
        this.amount = amount;
    }

    public DamageSource getSource() {
        return source;
    }

    public float getOriginalAmount() {
        return originalAmount;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }
}
