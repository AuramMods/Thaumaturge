package com.leclowndu93150.thaumaturge.client.model.entity;

import com.leclowndu93150.thaumaturge.content.entity.IBatAnimated;
import java.util.function.Function;
import java.util.Set;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Mob;

public final class FireBatModel<T extends Mob & IBatAnimated> extends HierarchicalModel<T> {
    private static final int ATLAS_SIZE = 32;
    private static final int FLAP_PERIOD_MS = 500;
    private static final float FOLD_DURATION_MS = 150.0F;
    private static final float BODY_HEIGHT = 16.75F;
    private static final Set<Direction> MEMBRANE_FACES = Set.of(Direction.NORTH, Direction.SOUTH);

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart leftTip;
    private final ModelPart rightTip;
    private final ModelPart feet;

    public FireBatModel(ModelPart root) {
        this(root, RenderType::entityCutoutNoCull);
    }

    public FireBatModel(ModelPart root, Function<ResourceLocation, RenderType> renderType) {
        super(renderType);
        this.root = root;
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.leftWing = body.getChild("left_wing");
        this.rightWing = body.getChild("right_wing");
        this.leftTip = leftWing.getChild("left_wing_tip");
        this.rightTip = rightWing.getChild("right_wing_tip");
        this.feet = body.getChild("feet");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // Both Thaumaturge textures use the same atlas. The ears, wings and feet are two-sided planes.
        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 7).addBox(-2.0F, -2.8F, -1.25F, 4.0F, 3.0F, 2.0F),
                PartPose.offset(0.0F, BODY_HEIGHT - 0.4F, -0.2F));
        head.addOrReplaceChild("right_ear", membrane(1, 15, -2.35F, -3.85F, 3.0F, 5.0F),
                PartPose.offsetAndRotation(-1.4F, -2.25F, -0.15F, 0.0F, 0.0F, -0.07F));
        head.addOrReplaceChild("left_ear", membrane(8, 15, -0.65F, -3.85F, 3.0F, 5.0F),
                PartPose.offsetAndRotation(1.4F, -2.25F, -0.15F, 0.0F, 0.0F, 0.07F));

        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, 0.1F, -0.65F, 3.0F, 5.0F, 2.0F),
                PartPose.offset(0.0F, BODY_HEIGHT, 0.0F));
        PartDefinition rightWing = body.addOrReplaceChild("right_wing",
                membrane(12, 0, -1.85F, -1.8F, 2.0F, 7.0F), PartPose.offset(-1.35F, 0.2F, 0.35F));
        rightWing.addOrReplaceChild("right_wing_tip",
                membrane(16, 0, -5.95F, -1.65F, 6.0F, 8.0F), PartPose.offset(-1.8F, -0.15F, 0.0F));
        PartDefinition leftWing = body.addOrReplaceChild("left_wing",
                membrane(12, 7, -0.15F, -1.8F, 2.0F, 7.0F), PartPose.offset(1.35F, 0.2F, 0.35F));
        leftWing.addOrReplaceChild("left_wing_tip",
                membrane(16, 8, -0.05F, -1.65F, 6.0F, 8.0F), PartPose.offset(1.8F, -0.15F, 0.0F));
        body.addOrReplaceChild("feet", membrane(16, 16, -1.5F, 0.0F, 3.0F, 2.0F),
                PartPose.offset(0.0F, 5.1F, 0.45F));
        return LayerDefinition.create(mesh, ATLAS_SIZE, ATLAS_SIZE);
    }

    private static CubeListBuilder membrane(int u, int v, float x, float y, float width, float height) {
        return CubeListBuilder.create().texOffs(u, v).addBox(x, y, 0.0F, width, height, 0.0F, MEMBRANE_FACES);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(
            T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        if (entity.isResting()) {
            poseHanging(elapsed(entity.restAnimation(), ageInTicks), netHeadYaw);
        } else {
            poseFlying(elapsed(entity.flyAnimation(), ageInTicks));
        }
    }

    private static long elapsed(AnimationState animation, float ageInTicks) {
        if (!animation.isStarted()) {
            return 0L;
        }
        animation.updateTime(ageInTicks, 1.0F);
        return Math.max(0L, animation.getAccumulatedTime());
    }

    private void poseFlying(long elapsedMs) {
        float phase = (elapsedMs % FLAP_PERIOD_MS) * (Mth.TWO_PI / FLAP_PERIOD_MS);
        float downstroke = Mth.sin(phase);
        float lift = Mth.cos(phase);
        float tipStroke = Mth.sin(phase - 0.6F);
        head.y += 0.45F * lift;
        body.y += 0.45F * lift;
        body.xRot = 0.22F + 0.12F * downstroke;
        feet.xRot = -0.45F - 0.15F * downstroke;

        rightWing.zRot = 0.18F + 0.75F * downstroke;
        leftWing.zRot = -rightWing.zRot;
        rightWing.yRot = 0.12F + 0.33F * lift;
        leftWing.yRot = -rightWing.yRot;
        rightTip.zRot = 0.25F * tipStroke;
        leftTip.zRot = -rightTip.zRot;
        rightTip.yRot = 0.16F + 0.22F * Mth.cos(phase - 0.6F);
        leftTip.yRot = -rightTip.yRot;
    }

    private void poseHanging(long elapsedMs, float netHeadYaw) {
        head.xRot = Mth.PI;
        head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        body.xRot = Mth.PI;
        float fold = Mth.clamp(elapsedMs / FOLD_DURATION_MS, 0.0F, 1.0F);
        rightWing.yRot = Mth.lerp(fold, -1.0F, -1.45F);
        leftWing.yRot = -rightWing.yRot;
        rightTip.yRot = Mth.lerp(fold, -0.7F, -0.51F);
        leftTip.yRot = -rightTip.yRot;
        rightWing.zRot = -0.12F;
        leftWing.zRot = -rightWing.zRot;
    }
}
