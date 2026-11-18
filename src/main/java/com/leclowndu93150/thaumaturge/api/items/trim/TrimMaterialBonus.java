package com.leclowndu93150.thaumaturge.api.items.trim;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.datamap.DataMapType;
import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.armortrim.TrimMaterial;

/**
 * Data-map value attached to trim materials under {@code thaumaturge:trim_bonus}. Every armor piece trimmed with the
 * material grants the bonus on its own while worn, so a full set of four trimmed pieces grants it four times.
 *
 * @param visDiscount the vis discount one trimmed piece adds while worn, in percent
 * @param warp        the gear warp one trimmed piece adds
 * @since 1.1.0
 */
public record TrimMaterialBonus(int visDiscount, int warp) {
    public static final Codec<TrimMaterialBonus> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codecs.optionalFieldOf(Codec.intRange(0, 100), "vis_discount", 0).forGetter(TrimMaterialBonus::visDiscount),
                    Codecs.optionalFieldOf(Codec.intRange(0, 500), "warp", 0).forGetter(TrimMaterialBonus::warp))
            .apply(instance, TrimMaterialBonus::new));
    public static final DataMapType<TrimMaterial, TrimMaterialBonus> DATA_MAP =
            DataMapType.builder(TTIds.rl("trim_bonus"), Registries.TRIM_MATERIAL, CODEC)
                    .synced(CODEC, false).build();
}
