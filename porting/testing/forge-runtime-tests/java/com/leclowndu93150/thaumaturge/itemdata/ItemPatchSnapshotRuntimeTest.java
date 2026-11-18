package com.leclowndu93150.thaumaturge.itemdata;

import static org.junit.jupiter.api.Assertions.*;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ItemPatchSnapshotRuntimeTest {
    private static final ItemDataRegistry CATALOG = new ItemDataRegistry("port_snapshot");
    private static final ItemDataKey<Integer> COUNT = CATALOG.<Integer>registerComponentType("count", b -> b.persistent(Codec.INT)).get();
    private static final ResourceKey<Registry<String>> VALUES = ResourceKey.createRegistryKey(new ResourceLocation("port_snapshot", "values"));
    private static final ResourceKey<String> VALUE = ResourceKey.create(VALUES, new ResourceLocation("port_snapshot", "value"));
    private static final ItemDataKey<Holder<String>> HOLDER = CATALOG.<Holder<String>>registerComponentType("holder", b -> b.persistent(RegistryFixedCodec.create(VALUES))).get();
    private static Item item;

    private ItemDataKey<?> lookup(ResourceLocation id) { return id.getNamespace().equals("minecraft") ? VanillaItemData.key(id) : CATALOG.get(id); }

    @BeforeAll
    static void createAnActualItemUsingTheProductionDefaultConstructorMixin() {
        var registry = (ForgeRegistry<?>) ForgeRegistries.ITEMS;
        var view = (net.minecraft.core.MappedRegistry<?>) net.minecraft.core.registries.BuiltInRegistries.ITEM;
        view.unfreeze();
        registry.unfreeze();
        try {
            item = new Item(ItemDataDefaults.with(new Item.Properties().durability(100), COUNT, 7));
            ForgeRegistries.ITEMS.register(new ResourceLocation("port_snapshot", "defaulted"), item);
        } finally { registry.freeze(); view.freeze(); }
    }

    @Test
    void unchangedDefaultsAreOmittedAndSourceConstructorDefaultsAreActuallyBound() {
        var stack = new ItemStack(item);
        assertEquals(7, ItemData.get(stack, COUNT));
        assertTrue(ItemData.snapshot(stack, this::lookup).isEmpty());
        assertTrue(ItemData.snapshot(new ItemStack(Items.PAPER), this::lookup).isEmpty());
        assertSame(ItemDataPatch.EMPTY, ItemData.snapshot(ItemStack.EMPTY, this::lookup));
        ItemData.set(stack, COUNT, 9);
        ItemData.set(stack, COUNT, 7);
        assertTrue(ItemData.snapshot(stack, this::lookup).isEmpty());
    }

    @Test
    void snapshotKeepsChangedModAndNativeValuesAndOwnsTheirDecodedState() {
        var stack = new ItemStack(item);
        ItemData.set(stack, COUNT, 9);
        ItemData.set(stack, VanillaItemData.DAMAGE, 17);
        ItemData.set(stack, VanillaItemData.CUSTOM_NAME, Component.literal("Snapshot"));
        var enchantments = new EnchantmentData(Map.of(new ResourceLocation("minecraft", "fortune"), 2), false);
        ItemData.set(stack, VanillaItemData.ENCHANTMENTS, enchantments);
        var patch = ItemData.snapshot(stack, this::lookup);
        var target = new ItemStack(item);
        patch.apply(target);
        assertEquals(ItemData.comparisonFields(stack), ItemData.comparisonFields(target));
        var codec = ItemDataPatch.codec(this::lookup);
        var encoded = codec.encodeStart(JsonOps.INSTANCE, patch).result().orElseThrow();
        assertTrue(encoded.getAsJsonObject().has("port_snapshot:count"));
        assertTrue(encoded.getAsJsonObject().has("minecraft:damage"));
        assertEquals(patch, codec.parse(JsonOps.INSTANCE, encoded).result().orElseThrow());
        ItemData.set(stack, COUNT, 44);
        stack.setDamageValue(88);
        assertEquals(9, patch.get(COUNT));
        assertEquals(17, patch.get(VanillaItemData.DAMAGE));
    }

    @Test
    void removedDefaultsSurviveSnapshotCodecsAndApplication() {
        var stack = new ItemStack(item);
        ItemData.remove(stack, COUNT);
        ItemData.remove(stack, VanillaItemData.DAMAGE);
        ItemData.remove(stack, VanillaItemData.ENCHANTMENTS);
        var patch = ItemData.snapshot(stack, this::lookup);
        var codec = ItemDataPatch.codec(this::lookup);
        var encoded = codec.encodeStart(NbtOps.INSTANCE, patch).result().orElseThrow();
        var decoded = codec.parse(NbtOps.INSTANCE, encoded).result().orElseThrow();
        var target = new ItemStack(item);
        decoded.apply(target);
        assertTrue(decoded.test(target));
        assertFalse(target.isDamageableItem());
        assertNull(ItemData.get(target, COUNT));
        assertNotEquals(ItemData.comparisonFields(new ItemStack(item)), ItemData.comparisonFields(target));
    }

    private RegistryAccess.Frozen provider(String value) {
        var registry = new MappedRegistry<String>(VALUES, Lifecycle.stable());
        registry.register(VALUE, value, Lifecycle.stable());
        return new RegistryAccess.ImmutableRegistryAccess(List.of(registry)).freeze();
    }

    @Test
    void snapshotResolvesStoredHolderIdsAgainstTheCallersCurrentRegistryOwner() {
        var a = provider("first");
        var b = provider("second");
        var stack = new ItemStack(item);
        ItemDataContext.withRegistries(a, () -> { ItemData.set(stack, HOLDER, a.registryOrThrow(VALUES).getHolderOrThrow(VALUE)); return null; });
        var patch = ItemDataContext.withRegistries(b, () -> ItemData.snapshot(stack, this::lookup));
        assertSame(b.registryOrThrow(VALUES).getHolderOrThrow(VALUE), patch.get(HOLDER));
        assertEquals("second", patch.get(HOLDER).value());
        assertThrows(IllegalArgumentException.class, () -> ItemData.snapshot(stack, this::lookup));
    }

    @Test
    void unknownFieldsAndInvalidRemovalMarkersFailInsteadOfDisappearing() {
        var stack = new ItemStack(item);
        var values = new net.minecraft.nbt.CompoundTag();
        values.putInt("port_snapshot:unknown", 1);
        stack.getOrCreateTag().put(ItemData.TAG, values);
        assertThrows(IllegalArgumentException.class, () -> ItemData.snapshot(stack, this::lookup));
        values.remove("port_snapshot:unknown");
        values.putByte("!port_snapshot:count", (byte) 2);
        assertThrows(IllegalArgumentException.class, () -> ItemData.snapshot(stack, this::lookup));
    }
}
