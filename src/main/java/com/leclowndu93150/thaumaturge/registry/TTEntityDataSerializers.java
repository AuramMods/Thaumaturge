package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.golem.GolemProperties;
import com.leclowndu93150.thaumaturge.content.golem.accessory.GolemAccessoryStates;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class TTEntityDataSerializers {
    public static final DeferredRegister<EntityDataSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS, TTIds.MODID);

    public static final RegistryObject<EntityDataSerializer<GolemProperties>>
            GOLEM_PROPERTIES = SERIALIZERS.register(
                    "golem_properties", () -> EntityDataSerializer.simple(
                            GolemProperties.STREAM_CODEC::encode, GolemProperties.STREAM_CODEC::decode));

    public static final RegistryObject<EntityDataSerializer<GolemAccessoryStates>>
            GOLEM_ACCESSORY_STATES = SERIALIZERS.register(
                    "golem_accessory_states",
                    () -> GolemAccessoryStates.ENTITY_DATA_SERIALIZER);

    private TTEntityDataSerializers() {}

    public static void register(IEventBus modBus) {
        SERIALIZERS.register(modBus);
    }
}
