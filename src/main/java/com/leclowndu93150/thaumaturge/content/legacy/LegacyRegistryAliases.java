package com.leclowndu93150.thaumaturge.content.legacy;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.mixin.registry.ForgeRegistryAliasAccessor;
import java.util.Set;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;

public final class LegacyRegistryAliases {
    private static final ResourceLocation OLD_BORE = TTIds.rl("turret_bore");
    private static final ResourceLocation ARCANE_BORE = TTIds.rl("arcane_bore");

    private LegacyRegistryAliases() {}

    public static void register(IEventBus modBus) {
        modBus.addListener(EventPriority.LOWEST, LegacyRegistryAliases::onRegister);
    }

    private static void onRegister(RegisterEvent event) {
        if (event.getForgeRegistry() instanceof ForgeRegistry<?> registry) {
            // Install native aliases while Forge can still include them in its saved registry snapshot.
            Set<ResourceLocation> keys = registry.getKeys();
            for (ResourceLocation current : keys) {
                if (TTIds.MODID.equals(current.getNamespace())) {
                    ResourceLocation legacy = ResourceLocation.fromNamespaceAndPath(LegacyIds.LEGACY_NAMESPACE, current.getPath());
                    addAlias(registry, keys, legacy, current);
                }
            }
            if (Registries.ITEM.equals(registry.getRegistryKey()) && keys.contains(ARCANE_BORE)) {
                addAlias(registry, keys, OLD_BORE, ARCANE_BORE);
            }
        }
    }

    private static void addAlias(
            ForgeRegistry<?> registry, Set<ResourceLocation> keys, ResourceLocation legacy, ResourceLocation current) {
        if (!keys.contains(legacy) && !((ForgeRegistryAliasAccessor) registry).thaumaturge$aliases().containsKey(legacy)) {
            registry.addAlias(legacy, current);
        }
    }

    /** Candidate for a vanilla registry lookup. The registry checks both keys against its own entries. */
    public static ResourceLocation candidate(ResourceKey<? extends Registry<?>> registry, ResourceLocation id) {
        if (id != null && LegacyIds.LEGACY_NAMESPACE.equals(id.getNamespace())) {
            return TTIds.rl(id.getPath());
        }
        return OLD_BORE.equals(id) && Registries.ITEM.equals(registry) ? ARCANE_BORE : id;
    }
}
