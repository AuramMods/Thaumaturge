package com.leclowndu93150.thaumaturge.event.tick;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.Cancelable;

/** The mod's entity tick phases, posted around non-passenger ticking on either side. */
public abstract class EntityTickEvent extends EntityEvent {
    private EntityTickEvent(Entity entity) {
        super(entity);
    }

    /** Cancellation skips the entity tick and its Post event, leaving passenger traversal intact. */
    @Cancelable
    public static final class Pre extends EntityTickEvent {
        public Pre(Entity entity) {
            super(entity);
        }
    }

    public static final class Post extends EntityTickEvent {
        public Post(Entity entity) {
            super(entity);
        }
    }
}
