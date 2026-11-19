package com.leclowndu93150.thaumaturge.mixin.client.player;

import com.leclowndu93150.thaumaturge.state.AttachmentData;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.donator.Donators;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin {
    @Unique
    private static final ResourceLocation THAUMATURGE_CAPE = TTIds.rl("textures/entity/thaumaturge_cape.png");

    @Inject(method = "getCloakTextureLocation", at = @At("RETURN"), cancellable = true)
    private void thaumaturge$donatorCapeTexture(CallbackInfoReturnable<ResourceLocation> callback) {
        if (thaumaturge$showsDonatorCape()) {
            callback.setReturnValue(THAUMATURGE_CAPE);
        }
    }

    @Inject(method = "isCapeLoaded", at = @At("RETURN"), cancellable = true)
    private void thaumaturge$donatorCapeLoaded(CallbackInfoReturnable<Boolean> callback) {
        if (thaumaturge$showsDonatorCape()) {
            callback.setReturnValue(true);
        }
    }

    @Unique
    private boolean thaumaturge$showsDonatorCape() {
        AbstractClientPlayer player = (AbstractClientPlayer) (Object) this;
        return Donators.is(player) && AttachmentData.get(player, TTAttachments.DONATOR_CAPE);
    }
}
