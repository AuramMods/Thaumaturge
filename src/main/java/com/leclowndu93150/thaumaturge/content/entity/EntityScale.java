package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.registry.TTAttributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class EntityScale {
    private EntityScale() {}

    public static float factor(Entity entity) {
        if (entity instanceof LivingEntity living && TTAttributes.SCALE.isPresent()) {
            // Entity's constructor can query dimensions before LivingEntity creates its attribute map.
            var attributes = living.getAttributes();
            var scale = TTAttributes.SCALE.get();
            if (attributes != null && attributes.hasAttribute(scale)) {
                return (float) attributes.getValue(scale);
            }
        }
        return 1.0F;
    }
}
