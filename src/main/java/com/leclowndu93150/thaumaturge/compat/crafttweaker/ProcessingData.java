package com.leclowndu93150.thaumaturge.compat.crafttweaker;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.action.base.IRuntimeAction;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.data.MapData;
import com.blamejared.crafttweaker.api.data.visitor.DataToJsonStringVisitor;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.leclowndu93150.thaumaturge.datamap.DataMapLoader;
import com.leclowndu93150.thaumaturge.datamap.ResourceDataMaps;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.thaumaturge.ProcessingData")
public final class ProcessingData {
    private ProcessingData() {}

    @ZenCodeType.Method
    public static void edit(ResourceLocation registry, ResourceLocation map, MapData definition) {
        var resources = CraftTweakerAPI.getAccessibleElementsProvider().server().resources();
        var loader = ((ResourceDataMaps) resources).thaumaturge$dataMapLoader();
        var json = JsonParser.parseString(definition.accept(DataToJsonStringVisitor.INSTANCE));
        CraftTweakerAPI.apply(new Edit(loader, registry, map, json));
    }

    private record Edit(DataMapLoader loader, ResourceLocation registry, ResourceLocation map, JsonElement json)
            implements IRuntimeAction {
        @Override
        public void apply() {
            loader.addScriptLayer(registry, map, json);
        }

        @Override
        public String describe() {
            return "Editing Thaumaturge data map " + registry + "/" + map;
        }

        @Override
        public String systemName() {
            return "thaumaturge";
        }
    }
}
