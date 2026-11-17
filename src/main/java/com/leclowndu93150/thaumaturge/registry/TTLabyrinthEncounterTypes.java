package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthEncounterType;
import com.leclowndu93150.thaumaturge.content.eldritch.encounter.SingleBossEncounter;
import com.leclowndu93150.thaumaturge.content.eldritch.encounter.SwarmEncounter;
import net.minecraft.core.Registry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public final class TTLabyrinthEncounterTypes {
    public static final DeferredRegister<LabyrinthEncounterType<?>> TYPES =
            DeferredRegister.create(LabyrinthEncounterType.REGISTRY_KEY, TTIds.MODID);
    private static final java.util.function.Supplier<net.minecraftforge.registries.IForgeRegistry<LabyrinthEncounterType<?>>> REGISTRY =
            TYPES.makeRegistry(() -> new net.minecraftforge.registries.RegistryBuilder<LabyrinthEncounterType<?>>().hasTags().disableSync());

    public static final RegistryObject<LabyrinthEncounterType<SingleBossEncounter>>
            SINGLE_BOSS = TYPES.register("single_boss", () -> new LabyrinthEncounterType<>(SingleBossEncounter.CODEC));
    public static final RegistryObject<LabyrinthEncounterType<SwarmEncounter>> SWARM =
            TYPES.register("swarm", () -> new LabyrinthEncounterType<>(SwarmEncounter.CODEC));

    private TTLabyrinthEncounterTypes() {}

    public static Registry<LabyrinthEncounterType<?>> registry() {
        return ForgeRegistration.vanillaRegistry(REGISTRY);
    }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
    }
}
