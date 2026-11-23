package com.leclowndu93150.thaumaturge.compat.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.google.gson.JsonElement;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.thaumaturge.ResearchRequirement")
public final class ResearchRequirement {
    private final ResearchGate gate;

    private ResearchRequirement(ResearchGate gate) {
        this.gate = gate;
    }

    @ZenCodeType.Method
    public static ResearchRequirement entry(ResourceLocation entry) {
        return new ResearchRequirement(new ResearchGate(entry, Optional.empty(), false));
    }

    @ZenCodeType.Method
    public static ResearchRequirement stage(ResourceLocation entry, int stage) {
        if (stage < 0) throw new IllegalArgumentException("Research stage must be nonnegative");
        return new ResearchRequirement(new ResearchGate(entry, Optional.of(stage), false));
    }

    @ZenCodeType.Method
    public ResearchRequirement negated() {
        return new ResearchRequirement(new ResearchGate(gate.entry(), gate.stage(), !gate.negate()));
    }

    JsonElement definition() {
        return Recipes.encode(ResearchGate.CODEC, gate);
    }
}
