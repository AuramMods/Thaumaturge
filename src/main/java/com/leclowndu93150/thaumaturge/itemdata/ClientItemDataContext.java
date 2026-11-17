package com.leclowndu93150.thaumaturge.itemdata;

import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
final class ClientItemDataContext {
    private ClientItemDataContext() {}

    static HolderLookup.Provider registries() {
        Minecraft client = Minecraft.getInstance();
        if (client == null) return null;
        var connection = client.getConnection();
        return connection == null ? null : connection.registryAccess();
    }
}
