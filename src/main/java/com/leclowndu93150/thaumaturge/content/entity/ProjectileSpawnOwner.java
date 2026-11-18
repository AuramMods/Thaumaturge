package com.leclowndu93150.thaumaturge.content.entity;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;

/** Preserves the vanilla projectile owner field when Forge sends a custom spawn packet. */
public final class ProjectileSpawnOwner {
    private static final int NO_OWNER = 0;

    private ProjectileSpawnOwner() {}

    public static void write(FriendlyByteBuf buffer, Projectile projectile) {
        Entity owner = projectile.getOwner();
        buffer.writeVarInt(owner == null ? NO_OWNER : owner.getId());
    }

    public static void read(FriendlyByteBuf buffer, Projectile projectile) {
        int id = buffer.readVarInt();
        if (id != NO_OWNER) {
            Entity owner = projectile.level().getEntity(id);
            if (owner != null) {
                projectile.setOwner(owner);
            }
        }
    }
}
