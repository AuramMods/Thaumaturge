package com.leclowndu93150.thaumaturge.compat.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.thaumaturge.AspectAmount")
public final class AspectAmount {
    private final ResourceLocation aspect;
    private final int amount;

    @ZenCodeType.Constructor
    public AspectAmount(ResourceLocation aspect, int amount) {
        if (amount < 1) throw new IllegalArgumentException("Aspect amount must be positive");
        this.aspect = aspect;
        this.amount = amount;
    }

    static JsonArray definitions(AspectAmount[] amounts) {
        var result = new JsonArray();
        for (AspectAmount amount : amounts) {
            var entry = new JsonObject();
            entry.addProperty("aspect", amount.aspect.toString());
            entry.addProperty("amount", amount.amount);
            result.add(entry);
        }
        return result;
    }
}
