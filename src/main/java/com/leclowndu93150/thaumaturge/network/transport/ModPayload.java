package com.leclowndu93150.thaumaturge.network.transport;

import java.util.Objects;
import net.minecraft.resources.ResourceLocation;

/** Identified mod messages carried by the Forge SimpleChannel transport. */
public interface ModPayload {
    Type<? extends ModPayload> type();

    record Type<T extends ModPayload>(ResourceLocation id) {
        public Type {
            Objects.requireNonNull(id);
        }
    }
}
