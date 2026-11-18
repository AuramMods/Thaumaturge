package com.leclowndu93150.thaumaturge.itemdata;

import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimPattern;

/** The source trim component, stored in the native 1.20.1 Trim tag and tooltip flag. */
public record ArmorTrimData(Holder<TrimMaterial> material, Holder<TrimPattern> pattern, boolean showInTooltip) {
    public static final Codec<ArmorTrimData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TrimMaterial.CODEC.fieldOf("material").forGetter(ArmorTrimData::material),
            TrimPattern.CODEC.fieldOf("pattern").forGetter(ArmorTrimData::pattern),
            Codecs.optionalFieldOf(Codec.BOOL, "show_in_tooltip", true).forGetter(ArmorTrimData::showInTooltip)
    ).apply(instance, ArmorTrimData::new));
    static final String TAG = "Trim";
    static final int HIDE_MASK = ItemStack.TooltipPart.UPGRADES.getMask();

    static ArmorTrimData read(ItemStack stack) {
        if (stack.getTag() == null || !stack.getTag().contains(TAG)) return null;
        CompoundTag tag = stack.getTag();
        ArmorTrim trim = ArmorTrim.CODEC.parse(ItemDataContext.ops(), tag.get(TAG)).getOrThrow(false, message -> {
            throw new IllegalArgumentException("Cannot decode armor trim: " + message);
        });
        return new ArmorTrimData(trim.material(), trim.pattern(), (tag.getInt("HideFlags") & HIDE_MASK) == 0);
    }

    void write(ItemStack stack) {
        var encoded = ArmorTrim.CODEC.encodeStart(ItemDataContext.ops(), new ArmorTrim(material, pattern))
                .getOrThrow(false, message -> {
                    throw new IllegalArgumentException("Cannot encode armor trim: " + message);
                });
        CompoundTag tag = stack.getOrCreateTag();
        tag.put(TAG, encoded);
        if (showInTooltip) clearHiddenFlag(tag);
        else stack.hideTooltipPart(ItemStack.TooltipPart.UPGRADES);
    }

    static void remove(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG)) return;
        clearHiddenFlag(tag);
        stack.removeTagKey(TAG);
    }

    private static void clearHiddenFlag(CompoundTag tag) {
        int remaining = tag.getInt("HideFlags") & ~HIDE_MASK;
        if (remaining == 0) tag.remove("HideFlags");
        else tag.putInt("HideFlags", remaining);
    }
}
