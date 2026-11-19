package com.leclowndu93150.thaumaturge.content.equipment;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.crafting.Ingredient;
import org.jspecify.annotations.Nullable;

/** The source material definition, including layers, presented through native Forge armor APIs. */
public record ArmorMaterialData(ResourceLocation id, Map<ArmorItem.Type, Integer> defense, int enchantmentValue,
                                Holder<SoundEvent> equipSound, Supplier<Ingredient> repairIngredient,
                                List<Layer> layers, float toughness, float knockbackResistance) implements ArmorMaterial {
    public static final ResourceKey<Registry<ArmorMaterialData>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(new ResourceLocation("minecraft", "armor_material"));

    // 1.21.1 armor materials do not supply durability. Native defaultDurability(0)
    // leaves the source's explicit per-item durability intact, including its zero default.
    @Override public int getDurabilityForType(ArmorItem.Type type) { return 0; }
    @Override public int getDefenseForType(ArmorItem.Type type) { return defense.getOrDefault(type, 0); }
    @Override public int getEnchantmentValue() { return enchantmentValue; }
    @Override public SoundEvent getEquipSound() { return equipSound.value(); }
    @Override public Ingredient getRepairIngredient() { return repairIngredient.get(); }
    @Override public String getName() { return id.toString(); }
    @Override public float getToughness() { return toughness; }
    @Override public float getKnockbackResistance() { return knockbackResistance; }

    public static List<Layer> layers(ArmorMaterial material) {
        if (material instanceof ArmorMaterialData data) return data.layers;
        ResourceLocation name = new ResourceLocation(material.getName());
        return material == ArmorMaterials.LEATHER
                ? List.of(new Layer(name, "", true), new Layer(name, "_overlay", false))
                : List.of(new Layer(name));
    }

    public static final class Layer {
        private final boolean dyeable;
        private final @Nullable String textureType;
        private final ResourceLocation innerTexture;
        private final ResourceLocation outerTexture;

        public Layer(ResourceLocation assetName, String suffix, boolean dyeable) {
            this.dyeable = dyeable;
            this.textureType = suffix.isEmpty() ? null : suffix.substring(1);
            innerTexture = texture(assetName, suffix, 2);
            outerTexture = texture(assetName, suffix, 1);
        }
        public Layer(ResourceLocation assetName) { this(assetName, "", false); }
        public boolean dyeable() { return dyeable; }
        public @Nullable String textureType() { return textureType; }
        public ResourceLocation texture(boolean leggings) { return leggings ? innerTexture : outerTexture; }
        private static ResourceLocation texture(ResourceLocation assetName, String suffix, int layer) {
            return new ResourceLocation(assetName.getNamespace(), "textures/models/armor/" + assetName.getPath()
                    + "_layer_" + layer + suffix + ".png");
        }
    }
}
