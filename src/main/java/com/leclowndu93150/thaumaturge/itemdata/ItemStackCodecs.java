package com.leclowndu93150.thaumaturge.itemdata;

import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Source item-stack schema over native Forge stacks, including their residual NBT/capabilities. */
public final class ItemStackCodecs {
    public static final Codec<ItemStack> CODEC = Codecs.lazyInitialized(() -> codec(ItemData::key));
    public static final Codec<ItemStack> OPTIONAL_CODEC = optional(CODEC);

    private static final Codec<CompoundTag> FORGE_DATA = Codec.STRING.comapFlatMap(text -> {
        try { return validateForgeData(TagParser.parseTag(text)); }
        catch (CommandSyntaxException ex) { return DataResult.error(() -> "Invalid forge_data: " + ex.getMessage()); }
    }, CompoundTag::toString);

    private ItemStackCodecs() {}

    private record Value(Holder<Item> item, int count, ItemDataPatch components, Optional<CompoundTag> forgeData) {}

    /** The lookup also permits independent addon catalogs without caching a world's registry values. */
    public static Codec<ItemStack> codec(Function<ResourceLocation, ItemDataKey<?>> lookup) {
        Codec<Holder<Item>> item = Codecs.validate(BuiltInRegistries.ITEM.holderByNameCodec(), holder ->
                holder.value() == Items.AIR
                        ? DataResult.error(() -> "Item must not be minecraft:air") : DataResult.success(holder));
        Codec<Value> format = RecordCodecBuilder.create(i -> i.group(
                item.fieldOf("id").forGetter(Value::item),
                Codec.intRange(1, 99).fieldOf("count").orElse(1).forGetter(Value::count),
                Codecs.optionalFieldOf(ItemDataPatch.codec(lookup), "components", ItemDataPatch.EMPTY).forGetter(Value::components),
                Codecs.optionalFieldOf(FORGE_DATA, "forge_data").forGetter(Value::forgeData)
        ).apply(i, Value::new));
        return new Codec<>() {
            @Override public <T> DataResult<Pair<ItemStack, T>> decode(DynamicOps<T> ops, T input) {
                return ItemDataContext.withOps(ops, () -> format.decode(ops, input).flatMap(decoded ->
                        load(decoded.getFirst()).map(stack -> Pair.of(stack, decoded.getSecond()))));
            }

            @Override public <T> DataResult<T> encode(ItemStack stack, DynamicOps<T> ops, T prefix) {
                if (stack.isEmpty()) return DataResult.error(() -> "Cannot encode an empty item stack");
                return ItemDataContext.withOps(ops, () -> {
                    try {
                        var patch = ItemData.snapshot(stack, lookup);
                        var saved = stack.save(new CompoundTag());
                        saved.remove("id");
                        saved.remove("Count");
                        if (saved.contains("tag", Tag.TAG_COMPOUND)) {
                            CompoundTag residual = saved.getCompound("tag");
                            VanillaItemData.stripNativeStorage(residual);
                            residual.remove(ItemData.TAG);
                            if (residual.isEmpty()) saved.remove("tag");
                        }
                        return format.encode(new Value(stack.getItemHolder(), stack.getCount(), patch,
                                saved.isEmpty() ? Optional.empty() : Optional.of(saved)), ops, prefix);
                    } catch (IllegalArgumentException ex) {
                        return DataResult.error(() -> "Cannot encode item stack: " + ex.getMessage());
                    }
                });
            }
        };
    }

    private static DataResult<ItemStack> load(Value value) {
        CompoundTag saved = value.forgeData().map(CompoundTag::copy).orElseGet(CompoundTag::new);
        saved.putString("id", value.item().unwrapKey().orElseThrow().location().toString());
        saved.putByte("Count", (byte) value.count());
        ItemStack stack = ItemStack.of(saved);
        if (stack.isEmpty()) return DataResult.error(() -> "Cannot load item stack " + value.item());
        try {
            value.components().apply(stack);
            return DataResult.success(stack);
        } catch (IllegalArgumentException ex) {
            return DataResult.error(() -> "Cannot apply item components: " + ex.getMessage());
        }
    }

    private static DataResult<CompoundTag> validateForgeData(CompoundTag data) {
        for (String name : data.getAllKeys()) {
            if (!(name.equals("tag") || name.equals("ForgeCaps")) || !data.contains(name, Tag.TAG_COMPOUND)) {
                return DataResult.error(() -> "forge_data only accepts native tag/ForgeCaps compounds");
            }
        }
        CompoundTag tag = data.getCompound("tag");
        CompoundTag residual = tag.copy();
        VanillaItemData.stripNativeStorage(residual);
        if (tag.contains(ItemData.TAG) || !tag.equals(residual)) {
            return DataResult.error(() -> "forge_data cannot contain fields represented by components");
        }
        return DataResult.success(data);
    }

    public static Codec<ItemStack> optional(Codec<ItemStack> nonEmpty) {
        return new Codec<>() {
            @Override public <T> DataResult<Pair<ItemStack, T>> decode(DynamicOps<T> ops, T input) {
                var map = ops.getMap(input).result();
                if (map.isPresent() && map.get().entries().findAny().isEmpty()) {
                    return DataResult.success(Pair.of(ItemStack.EMPTY, ops.empty()));
                }
                return nonEmpty.decode(ops, input);
            }

            @Override public <T> DataResult<T> encode(ItemStack stack, DynamicOps<T> ops, T prefix) {
                return stack.isEmpty() ? ops.mapBuilder().build(prefix) : nonEmpty.encode(stack, ops, prefix);
            }
        };
    }
}
