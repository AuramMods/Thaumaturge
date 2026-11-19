package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.world.objects.ConfigNodeSpawnFilter;
import com.leclowndu93150.thaumaturge.content.world.objects.ConfigRarityFilter;
import com.leclowndu93150.thaumaturge.content.world.objects.MagicalCaveFloorPlacement;
import com.leclowndu93150.thaumaturge.content.world.objects.SurfaceBiomeSearch;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public final class TTPlacementModifiers {
    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIERS =
            DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, TTIds.MODID);

    public static final RegistryObject<PlacementModifierType<ConfigRarityFilter>>
            CRIMSON_PORTAL_RARITY =
                    PLACEMENT_MODIFIERS.register("crimson_portal_rarity", () -> ConfigRarityFilter.CODEC::codec);
    public static final RegistryObject<PlacementModifierType<ConfigNodeSpawnFilter>>
            NODE_SPAWN_CHANCE =
                    PLACEMENT_MODIFIERS.register("node_spawn_chance", () -> ConfigNodeSpawnFilter.CODEC::codec);
    public static final RegistryObject<PlacementModifierType<MagicalCaveFloorPlacement>>
            MAGICAL_CAVE_FLOOR =
                    PLACEMENT_MODIFIERS.register("magical_cave_floor", () -> MagicalCaveFloorPlacement.CODEC::codec);
    public static final RegistryObject<PlacementModifierType<SurfaceBiomeSearch>>
            SURFACE_BIOME_SEARCH =
                    PLACEMENT_MODIFIERS.register("surface_biome_search", () -> SurfaceBiomeSearch.CODEC::codec);

    private TTPlacementModifiers() {}

    public static void register(IEventBus bus) {
        PLACEMENT_MODIFIERS.register(bus);
    }
}
