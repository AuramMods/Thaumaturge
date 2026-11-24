package com.leclowndu93150.thaumaturge.mixin.world.entity.player;

import com.leclowndu93150.thaumaturge.state.AttachmentData;
import com.leclowndu93150.thaumaturge.state.AttachmentStorage;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Player.class)
abstract class PlayerAttachmentsMixin implements AttachmentData.PlayerAccess {
    @Unique private @Nullable AttachmentStorage thaumaturge$attachments;

    @Override
    public @Nullable AttachmentStorage thaumaturge$getAttachments() {
        return thaumaturge$attachments;
    }

    @Override
    public void thaumaturge$setAttachments(AttachmentStorage storage) {
        thaumaturge$attachments = storage;
    }
}
