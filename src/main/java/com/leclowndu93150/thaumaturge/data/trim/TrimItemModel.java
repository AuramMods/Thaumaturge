package com.leclowndu93150.thaumaturge.data.trim;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.armortrim.TrimMaterials;

public record TrimItemModel(String name, float itemModelIndex) {
    public static List<TrimItemModel> withVanillaMaterials(List<TrimItemModel> customModels) {
        var vanilla = new RegistrySetBuilder()
                .add(Registries.TRIM_MATERIAL, TrimMaterials::bootstrap)
                .build(RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
        var vanillaModels = vanilla.lookupOrThrow(Registries.TRIM_MATERIAL).listElements()
                .map(Holder::value)
                .map(material -> new TrimItemModel(material.assetName(), material.itemModelIndex()));
        // Item model predicates match a lower bound, so the largest eligible index must come last.
        return Stream.concat(customModels.stream(), vanillaModels)
                .sorted(Comparator.comparingDouble(TrimItemModel::itemModelIndex))
                .toList();
    }
}
