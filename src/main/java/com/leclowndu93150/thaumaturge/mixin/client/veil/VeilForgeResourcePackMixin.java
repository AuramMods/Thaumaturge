package com.leclowndu93150.thaumaturge.mixin.client.veil;

import java.nio.file.Path;
import net.minecraftforge.resource.PathPackResources;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// The redirect targets a method merged by Veil at priority 1000.
@Mixin(value = PathPackResources.class, priority = 1100, remap = false)
abstract class VeilForgeResourcePackMixin {
    @Shadow
    protected abstract Path resolve(String... paths);

    @Dynamic("Added by Veil's ForgePathPackResourcesMixin")
    @Redirect(method = "veil$listResources", at = @At(value = "INVOKE",
            target = "Lnet/minecraftforge/resource/PathPackResources;getSource()Ljava/nio/file/Path;"))
    private Path thaumaturge$resolvePackRoot(PathPackResources pack) {
        // Forge's mod packs override resolve to find entries in the mounted archive.
        return this.resolve(".").toAbsolutePath().normalize();
    }
}
