package com.leclowndu93150.thaumaturge.compat.ae2.client;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.client.golems.GolemAccessoryAnchor;
import com.leclowndu93150.thaumaturge.api.client.golems.RegisterGolemAccessoryRenderersEvent;
import com.leclowndu93150.thaumaturge.compat.ae2.GolemWirelessBackpack;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

final class WirelessBackpackRenderer {
    private WirelessBackpackRenderer() {}

    static void register(RegisterGolemAccessoryRenderersEvent event) {
        var mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("pack", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3, -1, 2, 6, 6, 3)
                .texOffs(0, 11).addBox(-0.5F, 5, 3, 1, 3, 1), PartPose.ZERO);
        mesh.getRoot().addOrReplaceChild("indicator", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-0.65F, 7.4F, 2.85F, 1.3F, 1.3F, 1.3F), PartPose.ZERO);
        var model = LayerDefinition.create(mesh, 32, 32).bakeRoot();
        var pack = model.getChild("pack");
        var indicator = model.getChild("indicator");
        var packType = RenderType.entityCutout(TTIds.rl("textures/entity/golem/wireless_backpack.png"));
        var lightType = RenderType.entityCutout(ResourceLocation.withDefaultNamespace("textures/block/white_wool.png"));
        event.register(GolemWirelessBackpack.ACCESSORY, GolemAccessoryAnchor.BODY, (pose, buffers, context) -> {
            pack.render(pose, buffers.getBuffer(packType), context.lightCoords(), OverlayTexture.NO_OVERLAY);
            boolean connected = context.states().state(GolemWirelessBackpack.BEHAVIOR)
                    .map(GolemWirelessBackpack.State::connected).orElse(false);
            indicator.render(pose, buffers.getBuffer(lightType), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
                    connected ? 0.67F : 0.95F, connected ? 0.42F : 0.07F, connected ? 1.0F : 0.12F, 1.0F);
        });
    }
}
