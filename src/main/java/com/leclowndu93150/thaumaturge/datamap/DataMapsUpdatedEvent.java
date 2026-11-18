package com.leclowndu93150.thaumaturge.datamap;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.eventbus.api.Event;

/** Fired on the Forge bus after the complete new set of maps has been installed. */
public final class DataMapsUpdatedEvent extends Event {
    public enum UpdateCause { SERVER_RELOAD, CLIENT_SYNC }
    private final RegistryAccess registries;
    private final Registry<?> registry;
    private final UpdateCause cause;
    public DataMapsUpdatedEvent(RegistryAccess registries, Registry<?> registry, UpdateCause cause) {
        this.registries = registries; this.registry = registry; this.cause = cause;
    }
    public RegistryAccess getRegistries() { return registries; }
    public Registry<?> getRegistry() { return registry; }
    public ResourceKey<? extends Registry<?>> getRegistryKey() { return registry.key(); }
    public UpdateCause getCause() { return cause; }
}
