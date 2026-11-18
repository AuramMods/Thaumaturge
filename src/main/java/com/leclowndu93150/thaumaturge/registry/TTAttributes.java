package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.compat.NamedAttributeModifiers;
import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.items.IVisDiscountGear;
import com.leclowndu93150.thaumaturge.content.equipment.trim.TrimBonuses;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

@EventBusSubscriber(modid = TTIds.MODID)
public final class TTAttributes {
    private static final double DEFAULT_ATTACK_DAMAGE = 2.0;

    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(ForgeRegistries.ATTRIBUTES, TTIds.MODID);
    private static final DeferredRegister<Attribute> VANILLA_ATTRIBUTES =
            DeferredRegister.create(ForgeRegistries.ATTRIBUTES, "minecraft");

    public static final RegistryObject<Attribute> JUMP_STRENGTH = VANILLA_ATTRIBUTES.register(
            "generic.jump_strength",
            () -> new RangedAttribute("attribute.name.generic.jump_strength", 0.42F, 0.0, 32.0).setSyncable(true));

    public static final RegistryObject<Attribute> VIS_DISCOUNT = ATTRIBUTES.register(
            "vis_discount",
            VisDiscountAttribute::new);

    private static final float PERCENT = 100.0F;
    private static final Map<EquipmentSlot, ResourceLocation> TRIM_DISCOUNT_IDS = trimDiscountIds();

    private TTAttributes() {}

    public static void register(IEventBus modBus) {
        ATTRIBUTES.register(modBus);
        VANILLA_ATTRIBUTES.register(modBus);
        modBus.addListener(TTAttributes::onAttributesCreated);
    }

    @SubscribeEvent
    public static void onItemAttributeModifier(ItemAttributeModifierEvent event) {
        addTrimDiscount(event);
        if (event.getItemStack().getItem() instanceof IVisDiscountGear gear
                && gear.appliesInSlot(event.getItemStack(), event.getSlotType())) {
            float contribution = (float) gear.getVisDiscount(event.getItemStack()) / 100;
            if (contribution != 0) {
                event.addModifier(
                        VIS_DISCOUNT.get(),
                        NamedAttributeModifiers.create(
                                BuiltInRegistries.ITEM.getKey(
                                        event.getItemStack().getItem()),
                                contribution,
                                AttributeModifier.Operation.ADDITION));
            }
        }
    }

    private static void addTrimDiscount(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        int discount = TrimBonuses.of(stack).visDiscount();
        Equipable equipable = Equipable.get(stack);
        if (discount != 0 && equipable != null && event.getSlotType() == equipable.getEquipmentSlot()) {
            EquipmentSlot slot = equipable.getEquipmentSlot();
            event.addModifier(
                    VIS_DISCOUNT.get(),
                    NamedAttributeModifiers.create(
                            TRIM_DISCOUNT_IDS.get(slot), discount / PERCENT, AttributeModifier.Operation.ADDITION));
        }
    }

    private static Map<EquipmentSlot, ResourceLocation> trimDiscountIds() {
        Map<EquipmentSlot, ResourceLocation> ids = new EnumMap<>(EquipmentSlot.class);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ids.put(slot, TTIds.rl("trim_vis_discount/" + slot.getName()));
        }
        return ids;
    }

    public static void onAttributesCreated(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, VIS_DISCOUNT.get());
        event.add(EntityType.PLAYER, JUMP_STRENGTH.get());
        for (EntityType<? extends LivingEntity> type : event.getTypes()) {
            if (type != EntityType.PLAYER) {
                if (!event.has(type, Attributes.ATTACK_DAMAGE)) {
                    event.add(type, Attributes.ATTACK_DAMAGE, DEFAULT_ATTACK_DAMAGE);
                }
            }
        }
    }

    public static final class VisDiscountAttribute extends RangedAttribute {
        private VisDiscountAttribute() {
            super("attributes.thaumaturge.vis_discount", 0, 0, 1);
            setSyncable(true);
        }
    }
}
