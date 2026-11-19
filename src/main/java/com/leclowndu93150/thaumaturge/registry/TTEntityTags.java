package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public final class TTEntityTags {
    public static final TagKey<EntityType<?>> UNDEAD = minecraft("undead");
    public static final TagKey<EntityType<?>> ARTHROPOD = minecraft("arthropod");
    public static final TagKey<EntityType<?>> CAN_BREATHE_UNDER_WATER = minecraft("can_breathe_under_water");
    public static final TagKey<EntityType<?>> IGNORES_POISON_AND_REGEN = minecraft("ignores_poison_and_regen");
    public static final TagKey<EntityType<?>> INVERTED_HEALING_AND_HARM = minecraft("inverted_healing_and_harm");
    public static final TagKey<EntityType<?>> SENSITIVE_TO_SMITE = minecraft("sensitive_to_smite");
    public static final TagKey<EntityType<?>> SENSITIVE_TO_BANE_OF_ARTHROPODS = minecraft("sensitive_to_bane_of_arthropods");
    public static final TagKey<EntityType<?>> WITHER_FRIENDS = minecraft("wither_friends");
    public static final TagKey<EntityType<?>> ASPECT_ARMADILLO = key("aspect/armadillo");
    public static final TagKey<EntityType<?>> ASPECT_BOGGED = key("aspect/bogged");
    public static final TagKey<EntityType<?>> ASPECT_BREEZE = key("aspect/breeze");
    public static final TagKey<EntityType<?>> SCAN_FIREBALL = key("scan/fireball");
    public static final TagKey<EntityType<?>> TAINT_CONVERSION_IMMUNE = key("taint_conversion/immune");
    public static final TagKey<EntityType<?>> SABLE_RETAIN_IN_SUB_LEVEL = TagKey.create(
            Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(TTIds.SABLE, "retain_in_sub_level"));

    private TTEntityTags() {}

    private static TagKey<EntityType<?>> minecraft(String path) {
        return TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.withDefaultNamespace(path));
    }

    private static TagKey<EntityType<?>> key(String path) {
        return TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(TTIds.MODID, path));
    }
}
