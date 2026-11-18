package com.leclowndu93150.thaumaturge.datamap;

/** Reference holders resolve through their actual native owner, rather than a global ID cache. */
public interface HolderDataMapAccess<R> {
    <T> T thaumaturge$data(DataMapType<R, T> type);
}
