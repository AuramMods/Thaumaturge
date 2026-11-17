package com.leclowndu93150.thaumaturge.recipe.ingredient;

import com.google.common.base.Suppliers;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

/** Ingredient serializers use CraftingHelper's real registry during Forge's serializer event. */
public final class IngredientRegistration {
    private final String namespace;
    private final Map<ResourceLocation, Entry<?>> entries = new LinkedHashMap<>();

    public IngredientRegistration(String namespace) { this.namespace = namespace; }

    public <I extends Ingredient> Entry<I> register(String name, Supplier<CodecIngredientSerializer<I>> factory) {
        return register(ResourceLocation.fromNamespaceAndPath(namespace, name), factory);
    }

    public <I extends Ingredient> Entry<I> register(ResourceLocation id, Supplier<CodecIngredientSerializer<I>> factory) {
        Entry<I> entry = new Entry<>(id, factory);
        if (entries.putIfAbsent(id, entry) != null) throw new IllegalArgumentException("Duplicate ingredient " + id);
        return entry;
    }

    public void register(IEventBus bus) { bus.addListener(this::onRegister); }

    private void onRegister(RegisterEvent event) {
        if (event.getRegistryKey().equals(ForgeRegistries.Keys.RECIPE_SERIALIZERS)) {
            entries.values().forEach(entry -> CodecIngredientSerializer.register(entry.getId(), entry.get()));
        }
    }

    public static final class Entry<I extends Ingredient> implements Supplier<CodecIngredientSerializer<I>> {
        private final ResourceLocation id;
        private final Supplier<CodecIngredientSerializer<I>> value;

        private Entry(ResourceLocation id, Supplier<CodecIngredientSerializer<I>> factory) {
            this.id = id;
            value = Suppliers.memoize(factory::get);
        }

        public ResourceLocation getId() { return id; }
        @Override public CodecIngredientSerializer<I> get() { return value.get(); }
    }
}
