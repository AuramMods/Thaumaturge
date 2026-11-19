package com.leclowndu93150.thaumaturge.client.extensions;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.entity.TTModelLayers;
import com.leclowndu93150.thaumaturge.client.model.gear.ClericArmorModel;
import com.leclowndu93150.thaumaturge.client.model.gear.CrimsonBootsModel;
import com.leclowndu93150.thaumaturge.client.model.gear.FortressArmorModel;
import com.leclowndu93150.thaumaturge.client.model.gear.KnightArmorModel;
import com.leclowndu93150.thaumaturge.client.model.gear.PraetorArmorModel;
import com.leclowndu93150.thaumaturge.client.model.gear.RangerArmorModel;
import com.leclowndu93150.thaumaturge.client.model.gear.RobeArmorModel;
import com.leclowndu93150.thaumaturge.client.model.gear.ThaumostaticHarnessModel;
import com.leclowndu93150.thaumaturge.content.equipment.TTMaterials;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = TTIds.MODID, value = Dist.CLIENT)
public final class ArmorModelClientExtensions implements IClientItemExtensions {
    public static final ArmorModelClientExtensions INSTANCE = new ArmorModelClientExtensions();

    private static final Map<ArmorMaterial, ArmorModels> MODELS = Map.of(
            TTMaterials.ARMOR_CULTIST_LEADER, headChestLegs(PraetorArmorModel::new,
                    TTModelLayers.PRAETOR_ARMOR_HEAD, TTModelLayers.PRAETOR_ARMOR_CHEST, TTModelLayers.PRAETOR_ARMOR_LEGS),
            TTMaterials.ARMOR_CULTIST_PLATE, headChestLegs(KnightArmorModel::new,
                    TTModelLayers.KNIGHT_ARMOR_HEAD, TTModelLayers.KNIGHT_ARMOR_CHEST, TTModelLayers.KNIGHT_ARMOR_LEGS),
            TTMaterials.ARMOR_CULTIST_RANGER, headChestLegs(RangerArmorModel::new,
                    TTModelLayers.RANGER_ARMOR_HEAD, TTModelLayers.RANGER_ARMOR_CHEST, TTModelLayers.RANGER_ARMOR_LEGS),
            TTMaterials.ARMOR_CULTIST_CAPTAIN, new ArmorModels(RangerArmorModel::new, Map.of(
                    EquipmentSlot.HEAD, TTModelLayers.CAPTAIN_ARMOR_HEAD,
                    EquipmentSlot.CHEST, TTModelLayers.CAPTAIN_ARMOR_CHEST)),
            TTMaterials.ARMOR_CULTIST_ROBE, headChestLegs(ClericArmorModel::new,
                    TTModelLayers.CLERIC_ARMOR_HEAD, TTModelLayers.CLERIC_ARMOR_CHEST, TTModelLayers.CLERIC_ARMOR_LEGS),
            TTMaterials.ARMOR_CULTIST_BOOTS, new ArmorModels(CrimsonBootsModel::new,
                    Map.of(EquipmentSlot.FEET, TTModelLayers.CRIMSON_BOOTS)),
            TTMaterials.ARMOR_VOID_ROBE, headChestLegs(RobeArmorModel::new,
                    TTModelLayers.ROBE_ARMOR_HEAD, TTModelLayers.ROBE_ARMOR_CHEST, TTModelLayers.ROBE_ARMOR_LEGS),
            TTMaterials.ARMOR_FORTRESS, headChestLegs(FortressArmorModel::new,
                    TTModelLayers.FORTRESS_ARMOR_HEAD, TTModelLayers.FORTRESS_ARMOR_CHEST, TTModelLayers.FORTRESS_ARMOR_LEGS),
            TTMaterials.ARMOR_THAUMOSTATIC_HARNESS, new ArmorModels(ThaumostaticHarnessModel::new,
                    Map.of(EquipmentSlot.CHEST, TTModelLayers.THAUMOSTATIC_HARNESS)));

    private final Map<ModelLayerLocation, HumanoidModel<?>> bakedModels = new HashMap<>();

    private ArmorModelClientExtensions() {}

    @Override
    public HumanoidModel<?> getHumanoidArmorModel(
            LivingEntity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
        if (!(stack.getItem() instanceof ArmorItem item)) {
            return original;
        }
        ArmorModels models = MODELS.get(item.getMaterial());
        ModelLayerLocation layer = models == null ? null : models.layers().get(slot);
        if (layer == null) {
            return original;
        }
        return bakedModels.computeIfAbsent(layer,
                key -> models.factory().apply(Minecraft.getInstance().getEntityModels().bakeLayer(key)));
    }

    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        INSTANCE.bakedModels.clear();
    }

    private static ArmorModels headChestLegs(Function<ModelPart, HumanoidModel<?>> factory,
            ModelLayerLocation head, ModelLayerLocation chest, ModelLayerLocation legs) {
        return new ArmorModels(factory, Map.of(EquipmentSlot.HEAD, head, EquipmentSlot.CHEST, chest,
                EquipmentSlot.LEGS, legs));
    }

    private record ArmorModels(Function<ModelPart, HumanoidModel<?>> factory,
                               Map<EquipmentSlot, ModelLayerLocation> layers) {}
}
