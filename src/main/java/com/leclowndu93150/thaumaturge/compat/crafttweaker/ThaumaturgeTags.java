package com.leclowndu93150.thaumaturge.compat.crafttweaker;

import com.blamejared.crafttweaker.api.CraftTweakerConstants;
import com.blamejared.crafttweaker.api.natives.NativeTypeInfo;
import com.blamejared.crafttweaker.api.plugin.CraftTweakerPlugin;
import com.blamejared.crafttweaker.api.plugin.ICraftTweakerPlugin;
import com.blamejared.crafttweaker.api.plugin.IJavaNativeIntegrationRegistrationHandler;
import com.blamejared.crafttweaker.api.plugin.ITaggableElementRegistrationHandler;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.golems.GolemTrait;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemAddon;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemArm;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemHead;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemLeg;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemMaterial;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealType;
import com.leclowndu93150.thaumaturge.api.recipe.Blueprint;
import com.leclowndu93150.thaumaturge.api.research.IResearchCategory;
import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanEntry;
import com.leclowndu93150.thaumaturge.api.spell.affinity.AspectAffinity;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPart;
import com.leclowndu93150.thaumaturge.api.wands.WandCap;
import com.leclowndu93150.thaumaturge.api.wands.WandRod;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.LabyrinthDefinition;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.RoomType;
import com.leclowndu93150.thaumaturge.content.eldritch.site.ObeliskSite;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

@CraftTweakerPlugin("thaumaturge:tags")
public final class ThaumaturgeTags implements ICraftTweakerPlugin {
    private static final List<TagType<?>> TYPES = List.of(
            new TagType<>(IAspect.REGISTRY_KEY, IAspect.class, "Aspect", AspectElements.class),
            new TagType<>(Blueprint.REGISTRY_KEY, Blueprint.class, "Blueprint", BlueprintElements.class),
            new TagType<>(GolemMaterial.REGISTRY_KEY, GolemMaterial.class, "GolemMaterial", GolemMaterialElements.class),
            new TagType<>(GolemHead.REGISTRY_KEY, GolemHead.class, "GolemHead", GolemHeadElements.class),
            new TagType<>(GolemArm.REGISTRY_KEY, GolemArm.class, "GolemArm", GolemArmElements.class),
            new TagType<>(GolemLeg.REGISTRY_KEY, GolemLeg.class, "GolemLeg", GolemLegElements.class),
            new TagType<>(GolemAddon.REGISTRY_KEY, GolemAddon.class, "GolemAddon", GolemAddonElements.class),
            new TagType<>(GolemTrait.REGISTRY_KEY, GolemTrait.class, "GolemTrait", GolemTraitElements.class),
            new TagType<>(SealType.REGISTRY_KEY, SealType.class, "SealType", SealTypeElements.class),
            new TagType<>(SpellPart.REGISTRY_KEY, SpellPart.class, "SpellPart", SpellPartElements.class),
            new TagType<>(AspectAffinity.REGISTRY_KEY, AspectAffinity.class, "SpellAffinity", SpellAffinityElements.class),
            new TagType<>(IResearchEntry.REGISTRY_KEY, IResearchEntry.class, "ResearchEntry", ResearchEntryElements.class),
            new TagType<>(IResearchCategory.REGISTRY_KEY, IResearchCategory.class, "ResearchCategory", ResearchCategoryElements.class),
            new TagType<>(ScanEntry.REGISTRY_KEY, ScanEntry.class, "ScanEntry", ScanEntryElements.class),
            new TagType<>(WandCap.REGISTRY_KEY, WandCap.class, "WandCap", WandCapElements.class),
            new TagType<>(WandRod.REGISTRY_KEY, WandRod.class, "WandRod", WandRodElements.class),
            new TagType<>(LabyrinthDefinition.REGISTRY_KEY, LabyrinthDefinition.class, "Labyrinth", LabyrinthElements.class),
            new TagType<>(RoomType.REGISTRY_KEY, RoomType.class, "LabyrinthRoom", LabyrinthRoomElements.class),
            new TagType<>(ObeliskSite.REGISTRY_KEY, ObeliskSite.class, "ObeliskSite", ObeliskSiteElements.class));

    @Override
    public void registerTaggableElements(ITaggableElementRegistrationHandler handler) {
        TYPES.forEach(type -> type.registerTag(handler));
    }

    @Override
    public void manageJavaNativeIntegration(IJavaNativeIntegrationRegistrationHandler handler) {
        for (TagType<?> type : TYPES) {
            handler.registerNativeType(CraftTweakerConstants.DEFAULT_LOADER_NAME, type.expansion(),
                    new NativeTypeInfo("mods.thaumaturge.tags." + type.name(), type.element()));
        }
    }

    // CraftTweaker uses the expansion class as a unique registration key.
    public static final class AspectElements {}
    public static final class BlueprintElements {}
    public static final class GolemMaterialElements {}
    public static final class GolemHeadElements {}
    public static final class GolemArmElements {}
    public static final class GolemLegElements {}
    public static final class GolemAddonElements {}
    public static final class GolemTraitElements {}
    public static final class SealTypeElements {}
    public static final class SpellPartElements {}
    public static final class SpellAffinityElements {}
    public static final class ResearchEntryElements {}
    public static final class ResearchCategoryElements {}
    public static final class ScanEntryElements {}
    public static final class WandCapElements {}
    public static final class WandRodElements {}
    public static final class LabyrinthElements {}
    public static final class LabyrinthRoomElements {}
    public static final class ObeliskSiteElements {}

    private record TagType<T>(ResourceKey<Registry<T>> key, Class<T> element, String name, Class<?> expansion) {
        void registerTag(ITaggableElementRegistrationHandler handler) {
            handler.registerTaggableElement(key, element);
        }
    }
}
