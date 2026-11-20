package com.leclowndu93150.thaumaturge.compat.dynamictrees;

import com.ferreusveritas.dynamictrees.api.event.AddResourceLoadersEvent;
import com.ferreusveritas.dynamictrees.api.resource.ResourceAccessor;
import com.ferreusveritas.dynamictrees.api.resource.loading.AbstractResourceLoader;
import com.ferreusveritas.dynamictrees.api.resource.loading.preparation.JsonResourcePreparer;
import com.ferreusveritas.dynamictrees.deserialisation.JsonHelper;
import com.ferreusveritas.dynamictrees.event.BiomeSuitabilityEvent;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.util.IgnoreThrowable;
import com.google.gson.JsonElement;
import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;

/** Reads the climate properties that Dynamic Trees 1.4.11 predates. */
public final class DynamicTreesClimate {
    private static volatile Map<ResourceLocation, Preference> preferences = Map.of();

    private DynamicTreesClimate() {}

    public static void register(IEventBus modBus) {
        modBus.addListener(DynamicTreesClimate::addResourceLoader);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, DynamicTreesClimate::applySuitability);
    }

    private static void addResourceLoader(AddResourceLoadersEvent event) {
        event.getResourceManager().addLoader(new ClimateLoader());
    }

    private static void applySuitability(BiomeSuitabilityEvent event) {
        if (event.isHandled()) return;
        var preference = preferences.get(event.getSpecies().getRegistryName());
        if (preference == null) return;

        Climate local = Climate.atTemperature(event.getBiome().getBaseTemperature());
        double scale = DTConfigs.SCALE_BIOME_GROWTH_RATE.get();
        if (scale == 1.0 || preference.climate == Climate.NONE || preference.climate == local) {
            event.setSuitability(1.0F);
            return;
        }
        double factor = preference.climate.multipliers[local.ordinal()];
        double tolerance = preference.tolerance;
        double tolerated = factor * (1.0 - tolerance) + tolerance;
        double scaled = scale <= 0.5 ? scale * 2 * tolerated : ((1.0 - scale) * tolerated + (scale - 0.5)) * 2;
        event.setSuitability((float) Mth.clamp(scaled, 0.0, 1.0));
    }

    private static final class ClimateLoader extends AbstractResourceLoader<JsonElement> {
        private ClimateLoader() {
            super(new JsonResourcePreparer("species"));
        }

        @Override
        public void applyOnLoad(ResourceAccessor<JsonElement> resources, ResourceManager manager) {
            readPreferences(resources);
        }

        @Override
        public void applyOnReload(ResourceAccessor<JsonElement> resources, ResourceManager manager) {
            readPreferences(resources);
        }

        private static void readPreferences(ResourceAccessor<JsonElement> resources) {
            var loaded = new HashMap<ResourceLocation, Preference>();
            for (var resource : resources.getAllResources(id -> id.getNamespace().equals(TTIds.MODID))) {
                var id = resource.getLocation();
                var json = resource.getResource();
                if (!json.isJsonObject()) continue;
                try {
                    JsonHelper.throwIfShouldNotLoad(json.getAsJsonObject());
                } catch (IgnoreThrowable ignored) {
                    continue;
                }
                Preference.CODEC.parse(JsonOps.INSTANCE, json)
                        .resultOrPartial(error -> Thaumaturge.LOGGER.warn("Invalid tree climate for {}: {}", id, error))
                        .ifPresent(preference -> loaded.put(id, preference));
            }
            preferences = Map.copyOf(loaded);
        }
    }

    private record Preference(Climate climate, float tolerance) {
        private static final Codec<Preference> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                StringRepresentable.fromEnum(Climate::values)
                        .optionalFieldOf("preferred_climate", Climate.TEMPERATE).forGetter(Preference::climate),
                Codec.FLOAT.optionalFieldOf("climate_tolerance", 0.2F).forGetter(Preference::tolerance)
        ).apply(instance, Preference::new));
    }

    private enum Climate implements StringRepresentable {
        // Multipliers are indexed by the destination climate in this order.
        NONE(1.0, 1.0, 1.0, 1.0, 1.0),
        TEMPERATE(1.0, 1.0, 0.6, 0.9, 0.4),
        TROPICAL(1.0, 0.7, 1.0, 0.6, 0.1),
        ARID(1.0, 0.6, 0.7, 1.0, 0.3),
        COLD(1.0, 0.9, 0.4, 0.8, 1.0);

        private final double[] multipliers;

        Climate(double... multipliers) {
            this.multipliers = multipliers;
        }

        private static Climate atTemperature(float temperature) {
            if (temperature < 0.2F) return COLD;
            if (temperature >= 1.0F) return ARID;
            return temperature > 0.8F ? TROPICAL : TEMPERATE;
        }

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
