package com.leclowndu93150.thaumaturge.event.tick;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.MinecraftForge;

/** Called at the native Entity#tick invocation, after Forge's canUpdate check. */
public final class EntityTickHooks {
    private EntityTickHooks() {}

    public static void tick(Entity entity) {
        if (MinecraftForge.EVENT_BUS.post(new EntityTickEvent.Pre(entity))) return;
        entity.tick();
        MinecraftForge.EVENT_BUS.post(new EntityTickEvent.Post(entity));
    }
}
