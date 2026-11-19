package com.leclowndu93150.thaumaturge.content.aspect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.mojang.serialization.Lifecycle;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EquipmentAspectsRuntimeTest {
    private static Item multiSlotSword;
    private final Map<TagKey<Item>, List<Holder<Item>>> previousTags = new LinkedHashMap<>();
    private RegistryAccess registries;

    @BeforeAll
    static void registerMultiSlotSword() {
        var registry = (ForgeRegistry<?>) ForgeRegistries.ITEMS;
        var view = (MappedRegistry<?>) BuiltInRegistries.ITEM;
        view.unfreeze();
        registry.unfreeze();
        try {
            multiSlotSword = new MultiSlotSword();
            ForgeRegistries.ITEMS.register(ResourceLocation.fromNamespaceAndPath("port_gear_aspects", "multi_slot_sword"),
                    multiSlotSword);
        } finally {
            registry.freeze();
            view.freeze();
        }
    }

    @BeforeEach
    void bindNativeItemTagsAndAspects() {
        var items = BuiltInRegistries.ITEM;
        items.getTags().forEach(pair -> previousTags.put(pair.getFirst(), pair.getSecond().stream().toList()));
        var tags = new LinkedHashMap<>(previousTags);
        tags.put(ItemTags.SWORDS, holders(Items.WOODEN_SWORD, Items.IRON_SWORD, Items.DIAMOND_SWORD, multiSlotSword));
        tags.put(ItemTags.PICKAXES, holders(Items.WOODEN_PICKAXE, Items.STONE_PICKAXE, Items.IRON_PICKAXE,
                Items.DIAMOND_PICKAXE));
        tags.put(ItemTags.AXES, holders(Items.IRON_AXE));
        tags.put(ItemTags.SHOVELS, holders(Items.STONE_SHOVEL));
        tags.put(ItemTags.HOES, holders(Items.WOODEN_HOE));
        items.bindTags(tags);

        var aspects = new MappedRegistry<IAspect>(IAspect.REGISTRY_KEY, Lifecycle.stable());
        for (var key : List.of(TTAspects.AVERSIO, TTAspects.VOLATUS, TTAspects.PRAEMUNIO, TTAspects.INSTRUMENTUM)) {
            aspects.register(key, mock(IAspect.class), Lifecycle.stable());
        }
        registries = new RegistryAccess.ImmutableRegistryAccess(List.of(aspects.freeze()));
    }

    @AfterEach
    void restoreNativeTags() {
        BuiltInRegistries.ITEM.bindTags(previousTags);
    }

    @Test
    void taggedVanillaSwordsUseTheirNativeDeclaredDamage() {
        assertBonus(Items.WOODEN_SWORD, TTAspects.AVERSIO, 4);
        assertBonus(Items.IRON_SWORD, TTAspects.AVERSIO, 12);
        assertBonus(Items.DIAMOND_SWORD, TTAspects.AVERSIO, 16);
    }

    @Test
    void swordCountsADeclarationOnceAcrossSlotsAndIgnoresStackEvents() {
        var eventModifier = new AttributeModifier("test context damage", 100, AttributeModifier.Operation.ADDITION);
        Consumer<ItemAttributeModifierEvent> listener = event -> {
            if (event.getItemStack().is(multiSlotSword)) event.addModifier(Attributes.ATTACK_DAMAGE, eventModifier);
        };
        var bus = MinecraftForge.EVENT_BUS;
        bus.addListener(listener);
        try {
            assertTrue(new ItemStack(multiSlotSword).getAttributeModifiers(EquipmentSlot.MAINHAND)
                    .containsEntry(Attributes.ATTACK_DAMAGE, eventModifier));
            assertBonus(multiSlotSword, TTAspects.AVERSIO, 20);
        } finally {
            bus.unregister(listener);
        }
    }

    @Test
    void armorAndRangedWeaponsRetainTheirDifferentAspects() {
        assertBonus(Items.IRON_HELMET, TTAspects.PRAEMUNIO, 8);
        assertBonus(Items.IRON_CHESTPLATE, TTAspects.PRAEMUNIO, 24);
        assertBonus(Items.IRON_LEGGINGS, TTAspects.PRAEMUNIO, 20);
        assertBonus(Items.IRON_BOOTS, TTAspects.PRAEMUNIO, 8);
        assertTrue(bonus(Items.IRON_HORSE_ARMOR).isEmpty());
        for (Item bow : List.of(Items.BOW, Items.CROSSBOW)) {
            var bonus = bonus(bow);
            assertEquals(10, amount(bonus, TTAspects.AVERSIO));
            assertEquals(5, amount(bonus, TTAspects.VOLATUS));
            assertEquals(2, bonus.size());
        }
    }

    @Test
    void toolsRespectTheOriginalDurabilityBandsAndUnavailableAspects() {
        assertBonus(Items.WOODEN_PICKAXE, TTAspects.INSTRUMENTUM, 4);
        assertBonus(Items.STONE_PICKAXE, TTAspects.INSTRUMENTUM, 8);
        assertBonus(Items.IRON_PICKAXE, TTAspects.INSTRUMENTUM, 12);
        assertBonus(Items.DIAMOND_PICKAXE, TTAspects.INSTRUMENTUM, 16);
        assertBonus(Items.IRON_AXE, TTAspects.INSTRUMENTUM, 12);
        assertBonus(Items.STONE_SHOVEL, TTAspects.INSTRUMENTUM, 8);
        assertBonus(Items.WOODEN_HOE, TTAspects.INSTRUMENTUM, 4);
        assertBonus(Items.SHEARS, TTAspects.INSTRUMENTUM, 12);
        assertTrue(bonus(Items.BREAD).isEmpty());
        assertTrue(EquipmentAspects.bonusFor(Items.IRON_SWORD, RegistryAccess.EMPTY).isEmpty());
    }

    private static List<Holder<Item>> holders(Item... items) {
        return Arrays.stream(items).map(BuiltInRegistries.ITEM::wrapAsHolder).toList();
    }

    private AspectList bonus(Item item) {
        return EquipmentAspects.bonusFor(item, registries);
    }

    private static int amount(AspectList list, ResourceKey<IAspect> key) {
        return list.entries().stream().filter(entry -> entry.aspect().is(key)).findFirst().orElseThrow().amount();
    }

    private void assertBonus(Item item, ResourceKey<IAspect> key, int expected) {
        var bonus = bonus(item);
        assertEquals(expected, amount(bonus, key));
        assertEquals(1, bonus.size());
    }

    private static final class MultiSlotSword extends Item {
        private static final UUID GLOBAL_ID = UUID.fromString("7e2b4701-35c4-4268-9b44-46dc86e8c35a");
        private static final UUID HAND_ID = UUID.fromString("cd4a1745-a170-404e-8b7e-59fd2c17ce61");
        private final AttributeModifier mainHand = new AttributeModifier(HAND_ID, "main hand", 3,
                AttributeModifier.Operation.ADDITION);
        private final AttributeModifier offHand = new AttributeModifier(HAND_ID, "off hand", 1.5,
                AttributeModifier.Operation.ADDITION);
        private final AttributeModifier multiplier = new AttributeModifier("multiplier", 20,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
        private final AttributeModifier speed = new AttributeModifier("speed", 50, AttributeModifier.Operation.ADDITION);

        MultiSlotSword() {
            super(new Item.Properties());
        }

        @Override
        public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
            var modifiers = ImmutableMultimap.<Attribute, AttributeModifier>builder();
            modifiers.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(GLOBAL_ID, "all slots", 2.5,
                    AttributeModifier.Operation.ADDITION));
            if (slot == EquipmentSlot.MAINHAND) {
                modifiers.put(Attributes.ATTACK_DAMAGE, mainHand);
                modifiers.put(Attributes.ATTACK_DAMAGE, multiplier);
                modifiers.put(Attributes.ATTACK_SPEED, speed);
            } else if (slot == EquipmentSlot.OFFHAND) {
                modifiers.put(Attributes.ATTACK_DAMAGE, offHand);
            }
            return modifiers.build();
        }
    }
}
