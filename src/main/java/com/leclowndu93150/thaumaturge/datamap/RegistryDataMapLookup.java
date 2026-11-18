package com.leclowndu93150.thaumaturge.datamap;

/** Binds the native lookup view to its owning registry during construction. */
public interface RegistryDataMapLookup<R> extends RegistryDataMapAccess<R> {
    void thaumaturge$bindDataMapStorage(RegistryDataMapAccess<R> registry);
}
