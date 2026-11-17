package com.leclowndu93150.thaumaturge.itemdata;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.leclowndu93150.thaumaturge.serialization.EffectCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.TippedArrowItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;

/** Source potion-content values backed by the native potion/arrow NBT used by Forge. */
public record PotionData(Optional<Holder<Potion>> potion, Optional<Integer> customColor,
                         List<MobEffectInstance> customEffects) {
    public static final PotionData EMPTY = new PotionData(Optional.empty(), Optional.empty(), List.of());
    private static final Codec<Holder<Potion>> POTION = BuiltInRegistries.POTION.holderByNameCodec();
    private static final Codec<PotionData> FULL_CODEC = RecordCodecBuilder.create(i -> i.group(
            Codecs.optionalFieldOf(POTION, "potion").forGetter(PotionData::potion),
            Codecs.optionalFieldOf(Codec.INT, "custom_color").forGetter(PotionData::customColor),
            Codecs.optionalFieldOf(EffectCodecs.CODEC.listOf(), "custom_effects", List.of()).forGetter(PotionData::customEffects))
            .apply(i, PotionData::new));
    public static final Codec<PotionData> CODEC = Codecs.withAlternative(FULL_CODEC, POTION, PotionData::new);
    static final String REMOVED_TAG = "thaumaturge:potion_contents_removed";
    private static final List<String> NATIVE_FIELDS = List.of("Potion", "CustomPotionColor", "CustomPotionEffects");

    public PotionData(Holder<Potion> potion) { this(Optional.of(potion), Optional.empty(), List.of()); }
    public PotionData(Potion potion) { this(BuiltInRegistries.POTION.wrapAsHolder(potion)); }

    public Iterable<MobEffectInstance> getAllEffects() {
        if (potion.isEmpty()) return customEffects;
        if (customEffects.isEmpty()) return potion.orElseThrow().value().getEffects();
        return Iterables.concat(potion.orElseThrow().value().getEffects(), customEffects);
    }

    public void forEachEffect(Consumer<MobEffectInstance> consumer) {
        for (MobEffectInstance effect : getAllEffects()) consumer.accept(EffectCodecs.instance(effect));
    }

    @Override public List<MobEffectInstance> customEffects() {
        // Like the source component, expose copies of the basic effect fields.
        return Lists.transform(customEffects, EffectCodecs::instance);
    }

    public boolean hasEffects() { return !customEffects.isEmpty() || potion.filter(p -> !p.value().getEffects().isEmpty()).isPresent(); }
    public boolean is(Holder<Potion> other) { return potion.filter(p -> p.value() == other.value()).isPresent() && customEffects.isEmpty(); }
    public PotionData withPotion(Holder<Potion> value) { return new PotionData(Optional.of(value), customColor, customEffects); }
    public PotionData withEffectAdded(MobEffectInstance effect) {
        var effects = new java.util.ArrayList<>(customEffects);
        effects.add(effect);
        return new PotionData(potion, customColor, List.copyOf(effects));
    }

    public int getColor() { return customColor.orElseGet(() -> getColorOptional(getAllEffects()).orElse(0xff385dc6)); }

    public static OptionalInt getColorOptional(Iterable<MobEffectInstance> effects) {
        int red = 0, green = 0, blue = 0, weight = 0;
        for (MobEffectInstance effect : effects) {
            if (!effect.isVisible()) continue;
            int color = effect.getEffect().getColor(), count = effect.getAmplifier() + 1;
            red += count * (color >> 16 & 255);
            green += count * (color >> 8 & 255);
            blue += count * (color & 255);
            weight += count;
        }
        return weight == 0 ? OptionalInt.empty()
                : OptionalInt.of(0xff000000 | (red / weight << 16) | (green / weight << 8) | blue / weight);
    }

    private static boolean hasDefault(ItemStack stack) {
        return stack.getItem() instanceof PotionItem || stack.getItem() instanceof TippedArrowItem;
    }

    static boolean hasFields(CompoundTag tag) { return tag != null && NATIVE_FIELDS.stream().anyMatch(tag::contains); }

    static PotionData readItem(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (!hasFields(tag)) {
            if (!hasDefault(stack) || tag != null && tag.getBoolean(REMOVED_TAG)) return null;
            return EMPTY;
        }
        return new PotionData(tag.contains("Potion", Tag.TAG_STRING)
                ? Optional.of(BuiltInRegistries.POTION.wrapAsHolder(PotionUtils.getPotion(stack))) : Optional.empty(),
                tag.contains("CustomPotionColor", Tag.TAG_ANY_NUMERIC) ? Optional.of(tag.getInt("CustomPotionColor")) : Optional.empty(),
                PotionUtils.getCustomEffects(stack));
    }

    static void writeItem(ItemStack stack, PotionData value) {
        CompoundTag tag = stack.getOrCreateTag();
        clear(tag);
        value.potion.ifPresent(p -> tag.putString("Potion", BuiltInRegistries.POTION.getKey(p.value()).toString()));
        value.customColor.ifPresent(color -> tag.putInt("CustomPotionColor", color));
        if (!value.customEffects.isEmpty() || !hasDefault(stack) && value.potion.isEmpty() && value.customColor.isEmpty()) {
            ListTag effects = new ListTag();
            for (MobEffectInstance effect : value.customEffects()) effects.add(effect.save(new CompoundTag()));
            tag.put("CustomPotionEffects", effects);
        }
        if (tag.isEmpty()) stack.setTag(null);
    }

    static void removeItem(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null) clear(tag);
        if (hasDefault(stack)) stack.getOrCreateTag().putBoolean(REMOVED_TAG, true);
        else if (tag != null && tag.isEmpty()) stack.setTag(null);
    }

    private static void clear(CompoundTag tag) {
        NATIVE_FIELDS.forEach(tag::remove);
        tag.remove(REMOVED_TAG);
    }

    /** The three native tags represent one component and must replace each other as a group. */
    static void copyExplicit(CompoundTag source, CompoundTag destination) {
        if (!hasFields(source) && !source.contains(REMOVED_TAG)) return;
        clear(destination);
        for (String field : NATIVE_FIELDS) if (source.contains(field)) destination.put(field, source.get(field).copy());
        if (source.contains(REMOVED_TAG)) destination.put(REMOVED_TAG, source.get(REMOVED_TAG).copy());
    }

    static void canonicalFields(ItemStack stack, java.util.Map<String, Tag> fields) {
        NATIVE_FIELDS.forEach(name -> fields.remove("nbt:" + name));
        fields.remove("nbt:" + REMOVED_TAG);
        PotionData contents = readItem(stack);
        if (contents != null) fields.put("native:minecraft:potion_contents", VanillaItemData.POTION_CONTENTS.encode(ItemDataContext.ops(), contents));
    }
}
