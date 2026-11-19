package com.leclowndu93150.thaumaturge.datamap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.leclowndu93150.thaumaturge.api.aspect.AspectDataMaps;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipFile;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.junit.jupiter.api.Test;

class BaseAspectTagsRuntimeTest {
    private static final Set<String> GENERATED_TAGS = Set.of(
            "#c:ores/quartz", "#c:raw_materials/cinnabar", "#c:ingots/brass", "#c:ingots/thaumium");

    @Test
    void shippedSelectorsLoadAgainstNativeForgeAndVanillaTags() throws Exception {
        var resources = Path.of(System.getProperty("thaumaturge.testResourceRoot"));
        var items = new MappedRegistry<Item>(Registries.ITEM, Lifecycle.stable());
        BuiltInRegistries.ITEM.entrySet().forEach(entry ->
                items.register(entry.getKey(), entry.getValue(), Lifecycle.stable()));
        items.freeze();
        var loader = new TagLoader<Holder<Item>>(
                id -> items.getHolder(ResourceKey.create(Registries.ITEM, id)), "tags/items");

        try (var packs = new MultiPackResourceManager(PackType.SERVER_DATA, List.of(
                packContaining("vanilla-fixture", "data/minecraft/tags/items/iron_ores.json"),
                packContaining("forge-fixture", "data/forge/tags/items/glass.json"),
                new PathPackResources("thaumaturge", resources, false)))) {
            var bindings = new LinkedHashMap<TagKey<Item>, List<Holder<Item>>>();
            loader.loadAndBuild(packs).forEach((id, holders) ->
                    bindings.put(TagKey.create(Registries.ITEM, id), List.copyOf(holders)));
            items.bindTags(bindings);

            var source = packs.getResourceOrThrow(id("thaumaturge:data_maps/item/base_aspects.json"));
            JsonObject data;
            try (var reader = source.openAsReader()) {
                data = JsonParser.parseReader(reader).getAsJsonObject();
            }
            var values = data.getAsJsonObject("values");
            for (String key : values.keySet()) {
                if (key.startsWith("#c:") && !GENERATED_TAGS.contains(key)) {
                    var tag = TagKey.create(Registries.ITEM, id(key.substring(1)));
                    assertTrue(bindings.containsKey(tag), key);
                    if (!key.equals("#c:nuggets/copper") && !key.equals("#c:rods/breeze")) {
                        assertFalse(bindings.get(tag).isEmpty(), key);
                    }
                }
            }
            assertTrue(items.getHolderOrThrow(Items.TINTED_GLASS.builtInRegistryHolder().key())
                    .is(TagKey.create(Registries.ITEM, id("c:glass_blocks"))));
            assertTrue(items.getHolderOrThrow(Items.CHEST.builtInRegistryHolder().key())
                    .is(TagKey.create(Registries.ITEM, id("c:chests/wooden"))));

            var aspects = new MappedRegistry<IAspect>(IAspect.REGISTRY_KEY, Lifecycle.stable());
            var aspectIds = new LinkedHashSet<ResourceLocation>();
            for (var value : values.asMap().values()) {
                var array = value.isJsonArray() ? value.getAsJsonArray()
                        : value.getAsJsonObject().getAsJsonArray("forge:value");
                array.forEach(entry -> aspectIds.add(id(entry.getAsJsonObject().get("aspect").getAsString())));
            }
            for (var aspectId : aspectIds) {
                aspects.register(ResourceKey.create(IAspect.REGISTRY_KEY, aspectId), mock(IAspect.class), Lifecycle.stable());
            }
            var registries = new RegistryAccess.ImmutableRegistryAccess(List.of(aspects.freeze(), items)).freeze();
            var type = AspectDataMaps.BASE_ASPECTS;
            var file = DataMapFile.read(type, RegistryOps.create(JsonOps.INSTANCE, registries), data, ICondition.IContext.EMPTY);
            var errors = new ArrayList<String>();
            var parts = DataMapAssembler.build(items, type, List.of(file), errors::add);
            // The focused runtime has no Thaumaturge item registration or generated tags.
            assertTrue(errors.stream().allMatch(error -> error.startsWith("Unknown data map object thaumaturge:")),
                    errors::toString);
            assertEquals(5, parts.get(Items.TINTED_GLASS.builtInRegistryHolder().key()).amountOf(TTAspects.VITREUS, registries));
            assertEquals(10, parts.get(Items.REDSTONE.builtInRegistryHolder().key()).amountOf(TTAspects.POTENTIA, registries));
            assertEquals(5, parts.get(Items.SCUTE.builtInRegistryHolder().key()).amountOf(TTAspects.BESTIA, registries));
        }
    }

    private static ResourceLocation id(String value) {
        return ResourceLocation.tryParse(value);
    }

    private static FilePackResources packContaining(String id, String resource) throws IOException {
        for (String entry : System.getProperty("legacyClassPath").split(File.pathSeparator)) {
            Path path = Path.of(entry);
            if (!entry.endsWith(".jar") || !Files.isRegularFile(path)) {
                continue;
            }
            try (var archive = new ZipFile(path.toFile())) {
                if (archive.getEntry(resource) != null) {
                    return new FilePackResources(id, path.toFile(), false);
                }
            }
        }
        throw new IOException("No native pack on the Forge classpath contains " + resource);
    }
}
