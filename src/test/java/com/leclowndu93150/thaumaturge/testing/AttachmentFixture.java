package com.leclowndu93150.thaumaturge.testing;

import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import com.leclowndu93150.thaumaturge.state.AttachmentStorage;
import java.util.function.Supplier;
import net.minecraft.core.RegistryAccess;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

public final class AttachmentFixture {
    private static final Capability<AttachmentStorage> STATE = CapabilityManager.get(new CapabilityToken<>() {});

    private AttachmentFixture() {}

    public static AttachmentStorage attach(ICapabilityProvider owner, Supplier<RegistryAccess> registries) {
        var storage = new AttachmentStorage(TTAttachments.ATTACHMENTS, registries, () -> {});
        when(owner.getCapability(STATE)).thenReturn(LazyOptional.of(() -> storage));
        return storage;
    }
}
