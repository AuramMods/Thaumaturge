package com.leclowndu93150.thaumaturge.datamap;

import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;

/** Mod-bus registration dispatched once during native common setup. */
public final class RegisterDataMapTypesEvent extends Event implements IModBusEvent {
    private final DataMapCatalog catalog;
    public RegisterDataMapTypesEvent(DataMapCatalog catalog) { this.catalog = catalog; }
    public void register(DataMapType<?, ?> type) { catalog.register(type); }
}
