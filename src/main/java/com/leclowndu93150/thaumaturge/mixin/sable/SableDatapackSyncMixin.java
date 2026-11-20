package com.leclowndu93150.thaumaturge.mixin.sable;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.ryanhcode.sable.physics.config.block_properties.PhysicsBlockPropertiesDefinitionLoader;
import net.minecraft.server.TickTask;
import net.minecraftforge.event.OnDatapackSyncEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = "dev.ryanhcode.sable.forge.SableForgeBootstrap", remap = false)
public abstract class SableDatapackSyncMixin {
    @WrapMethod(method = "syncDataPack")
    private void thaumaturge$syncAfterForgeTags(OnDatapackSyncEvent event, Operation<Void> original) {
        if (event.getPlayer() == null) {
            // A reload has rebound the server tags and loaded the new definitions.
            PhysicsBlockPropertiesDefinitionLoader.INSTANCE.applyAll();
        }
        var server = event.getPlayerList().getServer();
        // Forge queues its registry tag packet after posting OnDatapackSyncEvent.
        server.tell(new TickTask(server.getTickCount(), () -> original.call(event)));
    }
}
