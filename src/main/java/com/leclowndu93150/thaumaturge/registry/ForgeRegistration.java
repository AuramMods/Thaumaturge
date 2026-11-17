package com.leclowndu93150.thaumaturge.registry;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

/** Small factories over Forge's native deferred registration and registry lifecycle. */
public final class ForgeRegistration {
    private ForgeRegistration() {}

    public static <B extends Block> RegistryObject<B> registerBlock(DeferredRegister<Block> register,
            String name, Function<BlockBehaviour.Properties, B> factory, BlockBehaviour.Properties properties) {
        return register.register(name, () -> factory.apply(properties));
    }

    public static RegistryObject<Block> registerSimpleBlock(DeferredRegister<Block> register,
            String name, BlockBehaviour.Properties properties) {
        return registerBlock(register, name, Block::new, properties);
    }

    public static <I extends Item> RegistryObject<I> registerItem(DeferredRegister<Item> register,
            String name, Function<Item.Properties, I> factory, Item.Properties properties) {
        return register.register(name, () -> factory.apply(properties));
    }

    public static <I extends Item> RegistryObject<I> registerItem(DeferredRegister<Item> register,
            String name, Function<Item.Properties, I> factory) {
        return registerItem(register, name, factory, new Item.Properties());
    }

    public static RegistryObject<Item> registerSimpleItem(DeferredRegister<Item> register, String name) {
        return registerSimpleItem(register, name, new Item.Properties());
    }

    public static RegistryObject<Item> registerSimpleItem(DeferredRegister<Item> register,
            String name, Item.Properties properties) {
        return registerItem(register, name, Item::new, properties);
    }

    public static RegistryObject<BlockItem> registerSimpleBlockItem(DeferredRegister<Item> register,
            RegistryObject<? extends Block> block) {
        return registerItem(register, block.getId().getPath(),
                properties -> new BlockItem(block.get(), properties), new Item.Properties());
    }

    public static <R, V extends R> RegistryObject<V> registerById(DeferredRegister<R> register,
            String name, Function<ResourceLocation, V> factory) {
        // Read the actual namespaced ID from Forge's handle when the factory runs.
        @SuppressWarnings("unchecked")
        RegistryObject<V>[] result = (RegistryObject<V>[]) new RegistryObject<?>[1];
        result[0] = register.register(name, () -> factory.apply(result[0].getId()));
        return result[0];
    }

    @SuppressWarnings("unchecked")
    public static <R> Registry<R> vanillaRegistry(Supplier<IForgeRegistry<R>> registration) {
        IForgeRegistry<R> registry = registration.get();
        return (Registry<R>) Objects.requireNonNull(BuiltInRegistries.REGISTRY.get(registry.getRegistryName()),
                () -> "Registry wrapper is not available for " + registry.getRegistryName());
    }
}
