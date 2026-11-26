package com.leclowndu93150.thaumaturge.compat.ae2.client;

import appeng.api.client.AEKeyRenderHandler;
import com.leclowndu93150.thaumaturge.api.client.AspectRendering;
import com.leclowndu93150.thaumaturge.compat.ae2.EssentiaKey;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

final class EssentiaKeyRenderer implements AEKeyRenderHandler<EssentiaKey> {
    @Override
    public void drawInGui(Minecraft minecraft, GuiGraphics graphics, int x, int y, EssentiaKey key) {
        var aspect = key.resolve(minecraft.level);
        if (aspect == null) {
            AspectRendering.renderMissingGui(graphics, x, y);
        } else {
            AspectRendering.renderGui(graphics, minecraft.font, x, y, aspect, 0);
        }
    }

    @Override
    public void drawOnBlockFace(PoseStack pose, MultiBufferSource buffers, EssentiaKey key,
            float scale, int combinedLight, Level level) {
        var aspect = key.resolve(level);
        pose.pushPose();
        pose.scale(scale, scale, scale);
        if (aspect == null) {
            AspectRendering.renderMissingBillboard(pose, buffers, 1, 1, combinedLight, AspectRendering.BlendMode.ALPHA);
        } else {
            AspectRendering.renderBillboard(
                    pose, buffers, aspect, 1, 1, false, combinedLight, AspectRendering.BlendMode.ALPHA);
        }
        pose.popPose();
    }

    @Override
    public Component getDisplayName(EssentiaKey key) {
        return key.getDisplayName();
    }
}
