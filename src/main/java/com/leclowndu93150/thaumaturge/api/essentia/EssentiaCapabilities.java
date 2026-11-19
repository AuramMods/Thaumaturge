package com.leclowndu93150.thaumaturge.api.essentia;

import com.leclowndu93150.thaumaturge.content.blockentity.BlockCapabilityViews;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jspecify.annotations.Nullable;

/** Native Forge capabilities for essentia block and item views. */
public final class EssentiaCapabilities {
    /** @deprecated Use {@link #CONDUIT} through {@link EssentiaAccess#conduit}. */
    @Deprecated(since = "1.1.0", forRemoval = true)
    @SuppressWarnings("removal")
    public static final Capability<IEssentiaTransport> TRANSPORT = CapabilityManager.get(new CapabilityToken<>() {});

    /** Sided block capability, with null indicating the whole device. */
    public static final Capability<IEssentiaConduit> CONDUIT = CapabilityManager.get(new CapabilityToken<>() {});

    /** Simulation-aware storage, exposed only on eligible explicit faces. */
    public static final Capability<IEssentiaStorage> STORAGE = CapabilityManager.get(new CapabilityToken<>() {});

    /** A view of the essentia a stack carries. */
    public static final Capability<IItemEssentia> CONTAINER = CapabilityManager.get(new CapabilityToken<>() {});

    /** A fill and drain snapshot for an eligible item stack. */
    public static final Capability<IEssentiaItemStorage> ITEM_STORAGE = CapabilityManager.get(new CapabilityToken<>() {});

    /** Sided synthetic aspect queries, such as filter or routing intents. */
    public static final Capability<IAspectQuery> ASPECT_QUERY = CapabilityManager.get(new CapabilityToken<>() {});

    private EssentiaCapabilities() {}

    /** Expose a conduit and its forwarding legacy view on the same faces. */
    @SuppressWarnings("removal")
    public static void exposeConduit(
            BlockCapabilityViews views, Function<@Nullable Direction, @Nullable IEssentiaConduit> conduit) {
        views.expose(CONDUIT, conduit);
        views.expose(TRANSPORT, side -> ConduitAsTransport.view(conduit.apply(side)));
    }

    /** Bind item views without resolving them during the native stack constructor. */
    public static ICapabilityProvider item(
            Supplier<? extends IItemEssentia> contents, @Nullable Supplier<? extends IEssentiaItemStorage> storage) {
        return new ICapabilityProvider() {
            private final LazyOptional<IItemEssentia> container = LazyOptional.of(contents::get);

            @Override
            public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
                if (side == null) {
                    if (capability == CONTAINER) {
                        return container.cast();
                    }
                    if (capability == ITEM_STORAGE && storage != null) {
                        // Copy the stack at lookup time, after Forge has attached the provider.
                        IEssentiaItemStorage snapshot = storage.get();
                        return LazyOptional.of(() -> snapshot).cast();
                    }
                }
                return LazyOptional.empty();
            }
        };
    }
}
