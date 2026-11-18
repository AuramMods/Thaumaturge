package com.leclowndu93150.thaumaturge.compat;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/** Translate 1.21 namespaced modifier IDs to stable native 1.20.1 UUIDs. */
public final class NamedAttributeModifiers {
    private NamedAttributeModifiers() {}

    public static UUID id(ResourceLocation id) {
        return UUID.nameUUIDFromBytes(id.toString().getBytes(StandardCharsets.UTF_8));
    }

    public static AttributeModifier create(ResourceLocation id, double amount, AttributeModifier.Operation operation) {
        return new AttributeModifier(id(id), id.toString(), amount, operation);
    }

    public static boolean has(AttributeInstance instance, ResourceLocation id) {
        return instance.getModifier(id(id)) != null;
    }

    public static void replacePermanent(AttributeInstance instance, AttributeModifier modifier) {
        AttributeModifier previous = instance.getModifier(modifier.getId());
        if (previous != null
                && previous.getOperation() == modifier.getOperation()
                && Double.compare(previous.getAmount(), modifier.getAmount()) == 0) {
            return;
        }
        instance.removeModifier(modifier.getId());
        instance.addPermanentModifier(modifier);
    }
}
