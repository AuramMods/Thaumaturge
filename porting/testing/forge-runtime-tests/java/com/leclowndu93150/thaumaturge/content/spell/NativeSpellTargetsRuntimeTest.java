package com.leclowndu93150.thaumaturge.content.spell;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.damagesource.TTDamageSources;
import com.leclowndu93150.thaumaturge.content.entity.EntityIgnition;
import com.leclowndu93150.thaumaturge.registry.TTEntityTags;
import com.mojang.serialization.Lifecycle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.tags.TagLoader;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class NativeSpellTargetsRuntimeTest {
    private static final Set<ResourceLocation> MOD_UNDEAD = Set.of(
            id("thaumaturge:eldritch_guardian"), id("thaumaturge:inhabited_zombie"),
            id("thaumaturge:brainy_zombie"), id("thaumaturge:giant_brainy_zombie"),
            id("thaumaturge:brainy_drowned"), id("thaumaturge:brainy_husk"));

    @Test
    void defaultUndeadTagLoadsWithVanillaAndCanBeReplaced(@TempDir Path override) throws Exception {
        var expected = new HashSet<>(MOD_UNDEAD);
        Set.of("skeleton", "stray", "wither_skeleton", "skeleton_horse", "zombie_horse", "zombie",
                "zombie_villager", "zombified_piglin", "zoglin", "drowned", "husk", "wither",
                "phantom").stream().map(ResourceLocation::withDefaultNamespace).forEach(expected::add);
        assertEquals(expected, loadUndead(List.of()));

        Path tags = override.resolve("data/minecraft/tags/entity_types");
        Files.createDirectories(tags);
        Files.writeString(tags.resolve("skeletons.json"), """
                {"replace": true, "values": ["minecraft:cow"]}
                """);
        Set.of("skeleton", "stray", "wither_skeleton", "skeleton_horse").stream()
                .map(ResourceLocation::withDefaultNamespace).forEach(expected::remove);
        expected.add(id("minecraft:cow"));
        assertEquals(expected, loadUndead(List.of(new PathPackResources("override", override, false))));

        Files.writeString(tags.resolve("undead.json"), """
                {"replace": true, "values": ["minecraft:cow"]}
                """);
        assertEquals(Set.of(id("minecraft:cow")), loadUndead(List.of(
                new PathPackResources("override", override, false))));
    }

    @Test
    void damageSourceUsesTheLevelsRegisteredTypeAndDirectAttacker() {
        var key = ResourceKey.create(Registries.DAMAGE_TYPE, id("port_spell_test:custom_damage"));
        var damageTypes = new MappedRegistry<DamageType>(Registries.DAMAGE_TYPE, Lifecycle.stable());
        var holder = damageTypes.register(key, new DamageType("port_spell", 0.75F), Lifecycle.stable());
        var registries = new RegistryAccess.ImmutableRegistryAccess(List.of(damageTypes.freeze()));
        var level = mock(ServerLevel.class);
        when(level.registryAccess()).thenReturn(registries);
        var caster = mock(LivingEntity.class);

        var source = TTDamageSources.source(level, key, caster);
        assertSame(holder, source.typeHolder());
        assertSame(caster, source.getEntity());
        assertSame(caster, source.getDirectEntity());
        assertFalse(source.isIndirect());
        assertEquals(0.75F, source.getFoodExhaustion());

        var ownerless = TTDamageSources.source(level, key, null);
        assertSame(holder, ownerless.typeHolder());
        assertNull(ownerless.getEntity());
        assertNull(ownerless.getDirectEntity());
    }

    @Test
    void ignitionPreservesFractionalSecondsAndExistingLongerFire() {
        Entity target = mock(Entity.class, CALLS_REAL_METHODS);
        EntityIgnition.igniteForSeconds(target, 1.47F);
        assertEquals(29, target.getRemainingFireTicks());
        EntityIgnition.igniteForSeconds(target, 1.1F);
        assertEquals(29, target.getRemainingFireTicks());
        EntityIgnition.igniteForSeconds(target, 2.25F);
        assertEquals(45, target.getRemainingFireTicks());
    }

    @Test
    void ignitionAppliesNativeFireProtectionAfterConvertingToTicks() {
        LivingEntity target = mock(LivingEntity.class, CALLS_REAL_METHODS);
        var boots = new ItemStack(Items.DIAMOND_BOOTS);
        boots.enchant(Enchantments.FIRE_PROTECTION, 2);
        when(target.getItemBySlot(any())).thenAnswer(call ->
                call.getArgument(0) == EquipmentSlot.FEET ? boots : ItemStack.EMPTY);
        EntityIgnition.igniteForSeconds(target, 2.25F);
        assertEquals(32, target.getRemainingFireTicks());
    }

    private static Set<ResourceLocation> loadUndead(List<PackResources> higherPriority) {
        var root = Path.of(System.getProperty("thaumaturge.testResourceRoot"));
        var packs = new ArrayList<PackResources>();
        packs.add(ServerPacksSource.createVanillaPackSource());
        packs.add(new PathPackResources("thaumaturge", root, false));
        packs.addAll(higherPriority);

        var loader = new TagLoader<ResourceLocation>(key ->
                BuiltInRegistries.ENTITY_TYPE.containsKey(key) || MOD_UNDEAD.contains(key)
                        ? Optional.of(key) : Optional.empty(), "tags/entity_types");
        try (var resources = new MultiPackResourceManager(PackType.SERVER_DATA, packs)) {
            return Set.copyOf(loader.loadAndBuild(resources).get(TTEntityTags.UNDEAD.location()));
        }
    }

    private static ResourceLocation id(String name) {
        return ResourceLocation.tryParse(name);
    }
}
