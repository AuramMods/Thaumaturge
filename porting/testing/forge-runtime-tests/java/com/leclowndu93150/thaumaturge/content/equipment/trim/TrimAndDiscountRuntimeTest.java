package com.leclowndu93150.thaumaturge.content.equipment.trim;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.leclowndu93150.thaumaturge.api.items.IVisDiscountGear;
import com.leclowndu93150.thaumaturge.api.items.trim.TrimMaterialBonus;
import com.leclowndu93150.thaumaturge.compat.NamedAttributeModifiers;
import com.leclowndu93150.thaumaturge.datamap.DataMaps;
import com.leclowndu93150.thaumaturge.itemdata.ArmorTrimData;
import com.leclowndu93150.thaumaturge.itemdata.ItemData;
import com.leclowndu93150.thaumaturge.itemdata.ItemDataContext;
import com.leclowndu93150.thaumaturge.itemdata.ItemStackCodecs;
import com.leclowndu93150.thaumaturge.itemdata.VanillaItemData;
import com.leclowndu93150.thaumaturge.registry.TTAttributes;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import io.netty.buffer.Unpooled;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimPattern;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class TrimAndDiscountRuntimeTest {
    private static final ResourceLocation FIXTURE_ID = id("discount_helmet");
    private static final ResourceKey<TrimMaterial> MATERIAL = ResourceKey.create(Registries.TRIM_MATERIAL, id("thaumium"));
    private static final ResourceKey<TrimPattern> PATTERN = ResourceKey.create(Registries.TRIM_PATTERN, id("example"));
    private static IEventBus modBus;
    private static Item helmet;

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("port_trim", path);
    }

    @BeforeAll
    static void registerThroughNativeForge() throws Exception {
        modBus = BusBuilder.builder().build();
        TTAttributes.register(modBus);
        var registry = (ForgeRegistry<?>) ForgeRegistries.ATTRIBUTES;
        var ctor = RegisterEvent.class.getDeclaredConstructor(ResourceKey.class, ForgeRegistry.class, Registry.class);
        ctor.setAccessible(true);
        boolean locked = registry.isLocked();
        registry.unfreeze();
        try {
            modBus.post(ctor.newInstance(ForgeRegistries.Keys.ATTRIBUTES, registry, BuiltInRegistries.ATTRIBUTE));
        } finally {
            if (locked) registry.freeze();
        }

        var items = (ForgeRegistry<?>) ForgeRegistries.ITEMS;
        var view = (MappedRegistry<?>) BuiltInRegistries.ITEM;
        items.unfreeze();
        view.unfreeze();
        try {
            helmet = new DiscountHelmet();
            ForgeRegistries.ITEMS.register(FIXTURE_ID, helmet);
        } finally {
            items.freeze();
            view.freeze();
        }
    }

    private static final class DiscountHelmet extends ArmorItem implements IVisDiscountGear {
        DiscountHelmet() {
            super(ArmorMaterials.IRON, Type.HELMET, new Properties());
        }

        @Override public int getVisDiscount(ItemStack stack) {
            return 5;
        }
    }

    private RegistryAccess.Frozen owner(String asset, TrimMaterialBonus bonus) {
        var materials = new MappedRegistry<TrimMaterial>(Registries.TRIM_MATERIAL, Lifecycle.stable());
        materials.register(MATERIAL, TrimMaterial.create(asset, Items.IRON_INGOT, 0.2F, Component.literal(asset), Map.of()), Lifecycle.stable());
        var patterns = new MappedRegistry<TrimPattern>(Registries.TRIM_PATTERN, Lifecycle.stable());
        patterns.register(PATTERN, new TrimPattern(id("example"), Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE.builtInRegistryHolder(),
                Component.literal("Example")), Lifecycle.stable());
        materials.freeze();
        patterns.freeze();
        DataMaps.replace(materials, Map.of(TrimMaterialBonus.DATA_MAP, Map.of(MATERIAL, bonus)));
        return new RegistryAccess.ImmutableRegistryAccess(List.of(materials, patterns)).freeze();
    }

    private ArmorTrimData trim(RegistryAccess owner, boolean show) {
        return new ArmorTrimData(owner.registryOrThrow(Registries.TRIM_MATERIAL).getHolderOrThrow(MATERIAL),
                owner.registryOrThrow(Registries.TRIM_PATTERN).getHolderOrThrow(PATTERN), show);
    }

    @Test
    void nativeTrimAndCodecRoundTripShareTheSameStorageAndResolveAcrossWorlds() {
        var old = owner("old", new TrimMaterialBonus(7, 3));
        var next = owner("next", new TrimMaterialBonus(1, 2));
        var stack = new ItemStack(Items.IRON_CHESTPLATE);
        var upgrades = ItemStack.TooltipPart.UPGRADES.getMask();
        var unrelated = ItemStack.TooltipPart.DYE.getMask();
        var nativeTrim = trim(old, true);
        Map<TagKey<Item>, List<Holder<Item>>> tags = new LinkedHashMap<>();
        BuiltInRegistries.ITEM.getTags().forEach(pair -> tags.put(pair.getFirst(), pair.getSecond().stream().toList()));
        var updatedTags = new LinkedHashMap<>(tags);
        updatedTags.put(ItemTags.TRIMMABLE_ARMOR, List.of(stack.getItemHolder()));
        BuiltInRegistries.ITEM.bindTags(updatedTags);
        try {
            assertTrue(ArmorTrim.setTrim(old, stack, new ArmorTrim(nativeTrim.material(), nativeTrim.pattern())));
            assertEquals(nativeTrim, ItemDataContext.withRegistries(old, () -> ItemData.get(stack, VanillaItemData.TRIM)));
            stack.hideTooltipPart(ItemStack.TooltipPart.DYE);
            ItemDataContext.withRegistries(old, () -> ItemData.set(stack, VanillaItemData.TRIM, trim(old, false)));
            assertEquals(unrelated | upgrades, stack.getTag().getInt("HideFlags"));
            assertSame(nativeTrim.material(), ArmorTrim.getTrim(old, stack).orElseThrow().material());
            assertEquals(new TrimMaterialBonus(7, 3), ItemDataContext.withRegistries(old, () -> TrimBonuses.of(stack)));

            var encoded = ItemStackCodecs.CODEC.encodeStart(RegistryOps.create(JsonOps.INSTANCE, old), stack)
                    .result().orElseThrow().getAsJsonObject();
            assertFalse(encoded.get("forge_data").getAsString().contains("Trim:"));
            assertFalse(encoded.getAsJsonObject("components").getAsJsonObject("minecraft:trim").get("show_in_tooltip").getAsBoolean());
            var read = ItemStackCodecs.CODEC.parse(RegistryOps.create(JsonOps.INSTANCE, next), encoded).result().orElseThrow();
            assertEquals(trim(next, false), ItemDataContext.withRegistries(next, () -> ItemData.get(read, VanillaItemData.TRIM)));
            assertEquals(new TrimMaterialBonus(1, 2), ItemDataContext.withRegistries(next, () -> TrimBonuses.of(read)));
            ItemDataContext.withRegistries(next, () -> ItemData.remove(read, VanillaItemData.TRIM));
            assertFalse(read.getTag().contains("Trim"));
            assertEquals(unrelated, read.getTag().getInt("HideFlags"));
        } finally {
            BuiltInRegistries.ITEM.bindTags(tags);
        }
    }

    @Test
    void olderPortCarrierTrimStillLoadsAndDuplicateSourcesFail() {
        var owner = owner("legacy", new TrimMaterialBonus(3, 4));
        var ops = RegistryOps.create(JsonOps.INSTANCE, owner);
        var encoded = new JsonObject();
        encoded.addProperty("id", "minecraft:iron_chestplate");
        encoded.addProperty("forge_data",
                "{tag:{Trim:{material:\"port_trim:thaumium\",pattern:\"port_trim:example\"},HideFlags:128}}");
        var read = ItemStackCodecs.CODEC.parse(ops, encoded).result().orElseThrow();
        assertEquals(trim(owner, false), ItemDataContext.withRegistries(owner, () -> ItemData.get(read, VanillaItemData.TRIM)));
        encoded.add("components", JsonParser.parseString(
                "{\"minecraft:trim\":{\"material\":\"port_trim:thaumium\",\"pattern\":\"port_trim:example\"}}"));
        assertTrue(ItemStackCodecs.CODEC.parse(ops, encoded).error().orElseThrow().message().contains("both"));
    }

    @Test
    void actualEquipmentEventsUseCorrectSlotAndNativeTooltipShowsPercent() {
        var attribute = TTAttributes.VIS_DISCOUNT.get();
        assertEquals(ResourceLocation.fromNamespaceAndPath("thaumaturge", "vis_discount"), ForgeRegistries.ATTRIBUTES.getKey(attribute));
        assertTrue(attribute.isClientSyncable());
        assertEquals(1.0, attribute.sanitizeValue(1.5));

        Map<EntityType<? extends LivingEntity>, AttributeSupplier.Builder> builders = new LinkedHashMap<>();
        modBus.post(new EntityAttributeModificationEvent(builders));
        var player = builders.get(EntityType.PLAYER).build();
        assertTrue(player.hasAttribute(attribute));

        var owner = owner("wearable", new TrimMaterialBonus(7, 3));
        var stack = new ItemStack(helmet);
        Consumer<ItemAttributeModifierEvent> handler = TTAttributes::onItemAttributeModifier;
        MinecraftForge.EVENT_BUS.addListener(handler);
        try {
            ItemDataContext.withRegistries(owner, () -> {
                ItemData.set(stack, VanillaItemData.TRIM, trim(owner, true));
                assertTrue(stack.getAttributeModifiers(EquipmentSlot.MAINHAND).get(attribute).isEmpty());
                var bonuses = stack.getAttributeModifiers(EquipmentSlot.HEAD).get(attribute);
                assertEquals(2, bonuses.size());
                assertTrue(bonuses.stream().allMatch(m -> m.getOperation() == AttributeModifier.Operation.ADDITION));
                assertEquals(0.12, bonuses.stream().mapToDouble(AttributeModifier::getAmount).sum(), 1e-7);
                var lines = stack.getTooltipLines(null, TooltipFlag.Default.NORMAL);
                var percentages = lines.stream().map(Component::getContents)
                        .filter(TranslatableContents.class::isInstance).map(TranslatableContents.class::cast)
                        .filter(line -> line.getKey().equals("attribute.modifier.plus.1"))
                        .map(line -> line.getArgs()[0]).toList();
                assertTrue(percentages.containsAll(List.of("5", "7")), percentages.toString());
                return null;
            });
        } finally {
            MinecraftForge.EVENT_BUS.unregister(handler);
        }
    }

    @Test
    void travellerModifiersReachNativeJumpAndStepCalculationsAndPacket() throws Exception {
        var jumpAttribute = TTAttributes.JUMP_STRENGTH.get();
        assertEquals(ResourceLocation.withDefaultNamespace("generic.jump_strength"), ForgeRegistries.ATTRIBUTES.getKey(jumpAttribute));
        assertNotSame(Attributes.JUMP_STRENGTH, jumpAttribute);
        assertTrue(jumpAttribute.isClientSyncable());
        assertEquals((double) 0.42F, jumpAttribute.getDefaultValue());
        assertEquals(32.0, jumpAttribute.sanitizeValue(64.0));
        assertEquals(0.0, jumpAttribute.sanitizeValue(-1.0));

        assertEquals(0.42F * ProbePlayer.BLOCK_JUMP_FACTOR, new ProbePlayer().jumpPower());
        Map<EntityType<? extends LivingEntity>, AttributeSupplier.Builder> builders = new LinkedHashMap<>();
        modBus.post(new EntityAttributeModificationEvent(builders));
        var merged = new AttributeSupplier.Builder(DefaultAttributes.getSupplier(EntityType.PLAYER));
        merged.combine(builders.get(EntityType.PLAYER));

        var field = ForgeHooks.class.getDeclaredField("FORGE_ATTRIBUTES");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        var definitions = (Map<EntityType<? extends LivingEntity>, AttributeSupplier>) field.get(null);
        var original = definitions.put(EntityType.PLAYER, merged.build());
        try {
            var player = new ProbePlayer();
            var jump = player.getAttribute(jumpAttribute);
            var step = player.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get());
            assertNotNull(jump);
            assertNotNull(step);
            assertEquals(0.6F, player.getStepHeight());
            var jumpBonus = NamedAttributeModifiers.create(ResourceLocation.fromNamespaceAndPath("thaumaturge", "traveller_jump"),
                    0.275F, AttributeModifier.Operation.ADDITION);
            var stepBonus = NamedAttributeModifiers.create(ResourceLocation.fromNamespaceAndPath("thaumaturge", "traveller_step"),
                    0.5F, AttributeModifier.Operation.ADDITION);
            jump.addTransientModifier(jumpBonus);
            step.addTransientModifier(stepBonus);
            assertTrue(player.addEffect(new MobEffectInstance(MobEffects.JUMP, 80, 1)));
            assertEquals((0.42F + 0.275F) * ProbePlayer.BLOCK_JUMP_FACTOR + 0.2F, player.jumpPower(), 1.0e-6F);
            assertEquals(1.1F, player.getStepHeight());

            var packet = new ClientboundUpdateAttributesPacket(player.getId(), List.of(jump, step));
            var wire = new FriendlyByteBuf(Unpooled.buffer());
            try {
                packet.write(wire);
                var read = new ClientboundUpdateAttributesPacket(wire);
                assertFalse(wire.isReadable());
                var snapshot = read.getValues().stream().filter(value -> value.getAttribute() == jumpAttribute).findFirst().orElseThrow();
                assertEquals(jump.getBaseValue(), snapshot.getBase());
                assertEquals(jumpBonus.getId(), snapshot.getModifiers().iterator().next().getId());
            } finally {
                wire.release();
            }

            jump.removeModifier(jumpBonus.getId());
            step.removeModifier(stepBonus.getId());
            assertEquals(0.42F * ProbePlayer.BLOCK_JUMP_FACTOR + 0.2F, player.jumpPower(), 1.0e-6F);
            assertEquals(0.6F, player.getStepHeight());
            step.setBaseValue(0.5F - 0.6F);
            assertEquals(0.5F, player.getStepHeight());
        } finally {
            if (original == null) definitions.remove(EntityType.PLAYER);
            else definitions.put(EntityType.PLAYER, original);
        }
    }

    private static final class ProbePlayer extends Player {
        static final float BLOCK_JUMP_FACTOR = 0.8F;

        ProbePlayer() {
            super(mock(Level.class), BlockPos.ZERO, 0.0F, new GameProfile(UUID.randomUUID(), "PortMovement"));
        }

        @Override protected float getBlockJumpFactor() {
            return BLOCK_JUMP_FACTOR;
        }

        @Override public boolean isSpectator() { return false; }
        @Override public boolean isCreative() { return false; }

        float jumpPower() {
            return getJumpPower();
        }
    }
}
