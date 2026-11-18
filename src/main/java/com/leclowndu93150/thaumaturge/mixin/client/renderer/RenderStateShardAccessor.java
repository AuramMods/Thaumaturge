package com.leclowndu93150.thaumaturge.mixin.client.renderer;

import net.minecraft.client.renderer.RenderStateShard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderStateShard.class)
public interface RenderStateShardAccessor {
    @Accessor("PARTICLES_TARGET")
    static RenderStateShard.OutputStateShard thaumaturge$particlesTarget() {
        throw new AssertionError();
    }
}
