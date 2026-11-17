package com.leclowndu93150.thaumaturge.itemdata;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;

/** Immutable enchantment levels; JSON retains the baseline's named levels map. */
public record EnchantmentData(Map<ResourceLocation, Integer> levels, boolean showInTooltip) {
    public static final EnchantmentData EMPTY = new EnchantmentData(Map.of(), true);
    private static final Codec<Map<ResourceLocation, Integer>> LEVELS_CODEC =
            Codec.unboundedMap(ResourceLocation.CODEC, Codec.INT)
                    .flatXmap(EnchantmentData::validateLevels, EnchantmentData::validateLevels);
    public static final Codec<EnchantmentData> CODEC = RecordCodecBuilder.create(i -> i.group(
            LEVELS_CODEC.fieldOf("levels").forGetter(EnchantmentData::levels),
            Codec.BOOL.optionalFieldOf("show_in_tooltip", true).forGetter(EnchantmentData::showInTooltip))
            .apply(i, EnchantmentData::new));

    private static DataResult<Map<ResourceLocation, Integer>> validateLevels(Map<ResourceLocation, Integer> levels) {
        for (var entry : levels.entrySet()) {
            if (entry.getValue() < 0 || entry.getValue() > 255) {
                return DataResult.error(() -> "Invalid enchantment level for " + entry.getKey() + ": " + entry.getValue());
            }
        }
        return DataResult.success(levels);
    }

    public EnchantmentData {
        Map<ResourceLocation, Integer> copy = new LinkedHashMap<>();
        levels.forEach((id, level) -> {
            if (id == null || level == null || level < 0 || level > 255) {
                throw new IllegalArgumentException("Invalid enchantment level for " + id + ": " + level);
            }
            if (level > 0) copy.put(id, level);
        });
        levels = java.util.Collections.unmodifiableMap(copy);
    }

    public int getLevel(ResourceLocation id) { return levels.getOrDefault(id, 0); }
    public Set<ResourceLocation> keySet() { return levels.keySet(); }

    public boolean containsAtLeast(EnchantmentData required) {
        return required.levels.entrySet().stream().allMatch(e -> getLevel(e.getKey()) >= e.getValue());
    }

    public static EnchantmentData fromNbt(ListTag list, boolean showInTooltip) {
        Map<ResourceLocation, Integer> levels = new LinkedHashMap<>();
        for (Tag entry : list) {
            if (!(entry instanceof CompoundTag tag) || !tag.contains("id", Tag.TAG_STRING)
                    || !tag.contains("lvl", Tag.TAG_ANY_NUMERIC)) {
                throw new IllegalArgumentException("Invalid enchantment NBT entry");
            }
            ResourceLocation id = ResourceLocation.tryParse(tag.getString("id"));
            int level = tag.getInt("lvl");
            if (id == null || level < 0 || level > 255) throw new IllegalArgumentException("Invalid enchantment NBT level/id");
            levels.merge(id, level, Math::max);
        }
        return new EnchantmentData(levels, showInTooltip);
    }

    public ListTag toNbt() {
        ListTag list = new ListTag();
        levels.forEach((id, level) -> {
            CompoundTag tag = new CompoundTag();
            tag.putString("id", id.toString());
            tag.putShort("lvl", level.shortValue());
            list.add(tag);
        });
        return list;
    }

    public static EnchantmentData readItemTag(CompoundTag tag, String name, int hideFlag) {
        if (tag == null) return EMPTY;
        Tag raw = tag.get(name);
        if (raw == null) return new EnchantmentData(Map.of(), (tag.getInt("HideFlags") & hideFlag) == 0);
        if (!(raw instanceof ListTag list) || (!list.isEmpty() && list.getElementType() != Tag.TAG_COMPOUND)) {
            throw new IllegalArgumentException("Invalid " + name + " item NBT");
        }
        return fromNbt(list, (tag.getInt("HideFlags") & hideFlag) == 0);
    }

    public void writeItemTag(CompoundTag tag, String name, int hideFlag) {
        if (levels.isEmpty()) tag.remove(name);
        else tag.put(name, toNbt());
        int flags = tag.getInt("HideFlags");
        flags = showInTooltip ? flags & ~hideFlag : flags | hideFlag;
        if (flags == 0) tag.remove("HideFlags");
        else tag.putInt("HideFlags", flags);
    }

    public static final class Mutable {
        private final Map<ResourceLocation, Integer> levels;
        private final boolean showInTooltip;

        public Mutable(EnchantmentData initial) {
            levels = new LinkedHashMap<>(initial.levels);
            showInTooltip = initial.showInTooltip;
        }

        public void set(Holder<Enchantment> enchantment, int level) {
            ResourceLocation id = enchantment.unwrapKey().map(net.minecraft.resources.ResourceKey::location)
                    .orElseGet(() -> BuiltInRegistries.ENCHANTMENT.getKey(enchantment.value()));
            if (id == null || level < 0 || level > 255) throw new IllegalArgumentException("Invalid enchantment/level");
            if (level == 0) levels.remove(id);
            else levels.put(id, level);
        }

        public EnchantmentData toImmutable() { return new EnchantmentData(levels, showInTooltip); }
    }
}
