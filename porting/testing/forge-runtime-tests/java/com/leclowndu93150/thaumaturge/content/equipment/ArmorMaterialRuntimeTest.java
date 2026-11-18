package com.leclowndu93150.thaumaturge.content.equipment;

import static org.junit.jupiter.api.Assertions.*;

import com.leclowndu93150.thaumaturge.registry.ForgeRegistration;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ArmorMaterialRuntimeTest {
    private static final ResourceLocation ID = new ResourceLocation("port_armor_material", "fixture");
    private static final AtomicInteger REPAIR_READS = new AtomicInteger();
    private static final ArmorMaterialData MATERIAL = new ArmorMaterialData(ID,
            Map.of(ArmorItem.Type.HELMET, 2, ArmorItem.Type.CHESTPLATE, 6, ArmorItem.Type.LEGGINGS, 5, ArmorItem.Type.BOOTS, 2),
            25, BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.ARMOR_EQUIP_IRON),
            () -> { REPAIR_READS.incrementAndGet(); return Ingredient.of(Items.IRON_INGOT); },
            List.of(new ArmorMaterialData.Layer(ID), new ArmorMaterialData.Layer(ID, "_cloth", true)), 1.5F, 0.125F);
    private static final Map<ArmorItem.Type, RegistryObject<ArmorItem>> ITEMS = new EnumMap<>(ArmorItem.Type.class);
    private static RegistryObject<ArmorMaterialData> material;
    private static RegistryObject<ArmorItem> undamageable;
    private static Registry<ArmorMaterialData> registry;
    private static boolean itemsConstructedBeforeMaterialBinding;
    private static boolean constructionKeptRepairLazy;

    private static RegisterEvent event(ResourceKey<? extends Registry<?>> key, ForgeRegistry<?> forge, Registry<?> vanilla) throws Exception {
        var ctor = RegisterEvent.class.getDeclaredConstructor(ResourceKey.class, ForgeRegistry.class, Registry.class);
        ctor.setAccessible(true);
        return ctor.newInstance(key, forge, vanilla);
    }

    @BeforeAll
    static void createTheActualNativeRegistryAndRegisterItemsBeforeMaterialEntries() throws Exception {
        var materials = DeferredRegister.create(ArmorMaterialData.REGISTRY_KEY, ID.getNamespace());
        var supplier = materials.makeRegistry(() -> new RegistryBuilder<ArmorMaterialData>().hasTags());
        material = materials.register(ID.getPath(), () -> MATERIAL);
        var items = DeferredRegister.create(ForgeRegistries.ITEMS, ID.getNamespace());
        for (var type : ArmorItem.Type.values()) ITEMS.put(type, items.register(type.getName(),
                () -> new ArmorItem(MATERIAL, type, new Item.Properties().durability(ArmorTypes.durability(type, 25)))));
        undamageable = items.register("undamageable", () -> new ArmorItem(MATERIAL, ArmorItem.Type.HELMET, new Item.Properties()));
        var bus = BusBuilder.builder().build();
        materials.register(bus);
        items.register(bus);
        var created = new NewRegistryEvent();
        bus.post(created);
        var fill = NewRegistryEvent.class.getDeclaredMethod("fill");
        fill.setAccessible(true);
        fill.invoke(created);
        registry = ForgeRegistration.vanillaRegistry(supplier);
        var itemRegistry = (ForgeRegistry<?>) ForgeRegistries.ITEMS;
        var itemView = (net.minecraft.core.MappedRegistry<?>) BuiltInRegistries.ITEM;
        itemView.unfreeze();
        itemRegistry.unfreeze();
        try { bus.post(event(ForgeRegistries.Keys.ITEMS, itemRegistry, BuiltInRegistries.ITEM)); }
        finally { itemRegistry.freeze(); itemView.freeze(); }
        itemsConstructedBeforeMaterialBinding = !material.isPresent() && ITEMS.values().stream().allMatch(RegistryObject::isPresent);
        constructionKeptRepairLazy = REPAIR_READS.get() == 0;
        var materialRegistry = (ForgeRegistry<?>) supplier.get();
        materialRegistry.unfreeze();
        try { bus.post(event(ArmorMaterialData.REGISTRY_KEY, materialRegistry, registry)); }
        finally { materialRegistry.freeze(); }
    }

    @Test
    void customRegistryRetainsTheSourceRootAndEntryIdsWithoutAnItemOrderingDependency() {
        assertEquals(new ResourceLocation("minecraft", "armor_material"), ArmorMaterialData.REGISTRY_KEY.location());
        assertTrue(itemsConstructedBeforeMaterialBinding);
        assertSame(MATERIAL, material.get());
        assertSame(MATERIAL, registry.get(ID));
        assertEquals(ID, registry.getKey(MATERIAL));
        assertEquals(ID.toString(), MATERIAL.getName());
    }

    @Test
    void nativeArmorConstructorsKeepPerItemDurabilityAndSourceMaterialStats() {
        int[] expected = {275, 400, 375, 325};
        int index = 0;
        for (var type : ArmorItem.Type.values()) {
            var item = ITEMS.get(type).get();
            assertSame(MATERIAL, item.getMaterial());
            assertEquals(expected[index++], item.getMaxDamage());
            assertEquals(MATERIAL.defense().get(type).intValue(), item.getDefense());
            assertEquals(1.5F, item.getToughness());
            assertEquals(25, item.getEnchantmentValue());
            assertSame(SoundEvents.ARMOR_EQUIP_IRON, item.getEquipSound());
            var attributes = item.getDefaultAttributeModifiers(type.getSlot());
            assertEquals(MATERIAL.defense().get(type).doubleValue(), attributes.get(Attributes.ARMOR).iterator().next().getAmount());
            assertEquals(1.5, attributes.get(Attributes.ARMOR_TOUGHNESS).iterator().next().getAmount());
            assertEquals(0.125, attributes.get(Attributes.KNOCKBACK_RESISTANCE).iterator().next().getAmount());
        }
    }

    @Test
    void absentPerItemDurabilityKeepsTheSourceZeroDefault() {
        assertEquals(0, undamageable.get().getMaxDamage());
        assertFalse(new ItemStack(undamageable.get()).isDamageableItem());
    }

    @Test
    void repairIngredientsStayLazyAndFeedTheNativeArmorRepairPredicate() {
        assertTrue(constructionKeptRepairLazy);
        var item = ITEMS.get(ArmorItem.Type.HELMET).get();
        assertTrue(item.isValidRepairItem(new ItemStack(item), new ItemStack(Items.IRON_INGOT)));
        assertFalse(item.isValidRepairItem(new ItemStack(item), new ItemStack(Items.GOLD_INGOT)));
        assertTrue(REPAIR_READS.get() >= 2);
    }

    @Test
    void layerTexturePathsAndDyeFlagsRetainSourceMetadata() {
        var layers = ArmorMaterialData.layers(MATERIAL);
        assertEquals(new ResourceLocation(ID.getNamespace(), "textures/models/armor/fixture_layer_1.png"), layers.get(0).texture(false));
        assertEquals(new ResourceLocation(ID.getNamespace(), "textures/models/armor/fixture_layer_2_cloth.png"), layers.get(1).texture(true));
        assertFalse(layers.get(0).dyeable());
        assertTrue(layers.get(1).dyeable());
        assertSame(layers.get(1).texture(true), layers.get(1).texture(true));
        assertEquals(new ResourceLocation("minecraft", "textures/models/armor/leather_layer_1_overlay.png"),
                ArmorMaterialData.layers(ArmorMaterials.LEATHER).get(1).texture(false));
    }

    @Test
    void nativeItemStackSaveLoadPreservesDamageAndTheRegisteredArmorItem() {
        var item = ITEMS.get(ArmorItem.Type.CHESTPLATE).get();
        var source = new ItemStack(item);
        source.setDamageValue(123);
        var loaded = ItemStack.of(source.save(new CompoundTag()));
        assertSame(item, loaded.getItem());
        assertEquals(123, loaded.getDamageValue());
        assertEquals(400, loaded.getMaxDamage());
    }
}
