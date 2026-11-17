package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthMarkerType;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.marker.BlockMarker;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.marker.EntryPortalMarker;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.marker.GlyphMarker;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.marker.GuardianPostMarker;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.marker.KeyReliquaryMarker;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.marker.LandmarkMarker;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.marker.LockMarker;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.marker.LootMarker;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.marker.ReliquaryMarker;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.marker.SpawnerMarker;
import net.minecraft.core.Registry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public final class TTLabyrinthMarkers {
    public static final DeferredRegister<LabyrinthMarkerType<?>> MARKERS =
            DeferredRegister.create(LabyrinthMarkerType.REGISTRY_KEY, TTIds.MODID);
    private static final java.util.function.Supplier<net.minecraftforge.registries.IForgeRegistry<LabyrinthMarkerType<?>>> REGISTRY =
            MARKERS.makeRegistry(() -> new net.minecraftforge.registries.RegistryBuilder<LabyrinthMarkerType<?>>().hasTags().disableSync());

    public static final RegistryObject<LabyrinthMarkerType<LandmarkMarker>> LANDMARK =
            MARKERS.register("landmark", () -> new LabyrinthMarkerType<>(LandmarkMarker.CODEC));
    public static final RegistryObject<LabyrinthMarkerType<LootMarker>> LOOT =
            MARKERS.register("loot", () -> new LabyrinthMarkerType<>(LootMarker.CODEC));
    public static final RegistryObject<LabyrinthMarkerType<BlockMarker>> BLOCK =
            MARKERS.register("block", () -> new LabyrinthMarkerType<>(BlockMarker.CODEC));
    public static final RegistryObject<LabyrinthMarkerType<SpawnerMarker>> SPAWNER =
            MARKERS.register("spawner", () -> new LabyrinthMarkerType<>(SpawnerMarker.CODEC));
    public static final RegistryObject<LabyrinthMarkerType<GlyphMarker>> GLYPH =
            MARKERS.register("wayfinding_glyph", () -> new LabyrinthMarkerType<>(GlyphMarker.CODEC));
    public static final RegistryObject<LabyrinthMarkerType<LockMarker>> LOCK =
            MARKERS.register("lock", () -> new LabyrinthMarkerType<>(LockMarker.CODEC));
    public static final RegistryObject<LabyrinthMarkerType<KeyReliquaryMarker>> KEY_RELIQUARY =
            MARKERS.register("key_reliquary", () -> new LabyrinthMarkerType<>(KeyReliquaryMarker.CODEC));
    public static final RegistryObject<LabyrinthMarkerType<ReliquaryMarker>> RELIQUARY =
            MARKERS.register("reliquary", () -> new LabyrinthMarkerType<>(ReliquaryMarker.CODEC));
    public static final RegistryObject<LabyrinthMarkerType<EntryPortalMarker>> ENTRY_PORTAL =
            MARKERS.register("entry_portal", () -> new LabyrinthMarkerType<>(EntryPortalMarker.CODEC));
    public static final RegistryObject<LabyrinthMarkerType<GuardianPostMarker>> GUARDIAN_POST =
            MARKERS.register("guardian_post", () -> new LabyrinthMarkerType<>(GuardianPostMarker.CODEC));

    private TTLabyrinthMarkers() {}

    public static Registry<LabyrinthMarkerType<?>> registry() {
        return ForgeRegistration.vanillaRegistry(REGISTRY);
    }

    public static void register(IEventBus modBus) {
        MARKERS.register(modBus);
    }
}
