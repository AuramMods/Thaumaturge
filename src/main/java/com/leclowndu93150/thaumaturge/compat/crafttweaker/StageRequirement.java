package com.leclowndu93150.thaumaturge.compat.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.leclowndu93150.thaumaturge.stage.StageRules;
import java.util.List;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.thaumaturge.StageRequirement")
public final class StageRequirement {
    private final StageRules.Requirement requirement;

    private StageRequirement(boolean all, String[] stages) {
        this.requirement = new StageRules.Requirement(all, List.of(stages));
    }

    @ZenCodeType.Method
    public static StageRequirement allOf(String[] stages) {
        return new StageRequirement(true, stages);
    }

    @ZenCodeType.Method
    public static StageRequirement anyOf(String[] stages) {
        return new StageRequirement(false, stages);
    }

    StageRules.Requirement requirement() {
        return requirement;
    }
}
