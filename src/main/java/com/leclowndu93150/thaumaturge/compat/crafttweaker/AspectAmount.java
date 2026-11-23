package com.leclowndu93150.thaumaturge.compat.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.tag.type.KnownTag;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.thaumaturge.AspectAmount")
public final class AspectAmount {
    private final ResourceLocation aspect;
    private final int amount;
    private final boolean tag;

    @ZenCodeType.Constructor
    public AspectAmount(ResourceLocation aspect, int amount) {
        this(aspect, amount, false);
    }

    private AspectAmount(ResourceLocation aspect, int amount, boolean tag) {
        if (amount < 1) throw new IllegalArgumentException("Aspect amount must be positive");
        this.aspect = aspect;
        this.amount = amount;
        this.tag = tag;
    }

    @ZenCodeType.Method
    public static AspectAmount tag(KnownTag<IAspect> tag, int amount) {
        return new AspectAmount(tag.id(), amount, true);
    }

    static JsonArray definitions(AspectAmount[] amounts) {
        var result = new JsonArray();
        for (AspectAmount amount : amounts) {
            var entry = new JsonObject();
            entry.addProperty(amount.tag ? "tag" : "aspect", amount.aspect.toString());
            entry.addProperty("amount", amount.amount);
            result.add(entry);
        }
        return result;
    }
}
