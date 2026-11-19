package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.itemdata.ItemStackCodecs;

import com.leclowndu93150.thaumaturge.serialization.stream.VanillaStreamCodecs;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaList;
import com.leclowndu93150.thaumaturge.api.items.ChargeProfile;
import com.leclowndu93150.thaumaturge.api.labyrinth.MazeId;
import com.leclowndu93150.thaumaturge.api.spell.Spell;
import com.leclowndu93150.thaumaturge.api.wands.WandVis;
import com.leclowndu93150.thaumaturge.content.aura.node.NodeData;
import com.leclowndu93150.thaumaturge.content.casters.CasterArea;
import com.leclowndu93150.thaumaturge.content.casters.SocketedFocus;
import com.leclowndu93150.thaumaturge.content.equipment.InfusionEnchantments;
import com.leclowndu93150.thaumaturge.content.essentia.EssentiaContentsComponent;
import com.leclowndu93150.thaumaturge.content.golem.GolemProperties;
import com.leclowndu93150.thaumaturge.content.item.CelestialBody;
import com.leclowndu93150.thaumaturge.content.legacy.LegacyFocusPackage;
import com.leclowndu93150.thaumaturge.content.legacy.LegacyIds;
import com.leclowndu93150.thaumaturge.content.research.link.LinkBinding;
import com.leclowndu93150.thaumaturge.content.research.note.ResearchNoteData;
import com.leclowndu93150.thaumaturge.content.research.share.ShareBinding;
import com.leclowndu93150.thaumaturge.content.wands.WandParts;
import com.mojang.serialization.Codec;
import net.minecraft.core.GlobalPos;
import com.leclowndu93150.thaumaturge.itemdata.ItemDataKey;
import com.leclowndu93150.thaumaturge.itemdata.ItemDataRegistry;
import com.leclowndu93150.thaumaturge.itemdata.ItemData;
import java.util.function.Supplier;
import com.leclowndu93150.thaumaturge.serialization.stream.ByteBufCodecs;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import com.leclowndu93150.thaumaturge.itemdata.ItemContents;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.IEventBus;

public final class TTDataComponents {
    public static final ItemDataRegistry DATA_COMPONENTS = new ItemDataRegistry(TTIds.MODID);

    public static final Supplier<ItemDataKey<AspectList>> ASPECTS =
            DATA_COMPONENTS.registerComponentType(
                    "aspects",
                    builder -> builder.persistent(AspectList.CODEC).networkSynchronized(AspectList.STREAM_CODEC));

    public static final Supplier<ItemDataKey<EssentiaList>> ESSENTIA_CONTENTS =
            DATA_COMPONENTS.registerComponentType(
                    "essentia_contents",
                    builder -> builder.persistent(EssentiaContentsComponent.CODEC)
                            .networkSynchronized(EssentiaContentsComponent.STREAM_CODEC));

    public static final Supplier<ItemDataKey<ResourceKey<IAspect>>> ASPECT_FILTER =
            DATA_COMPONENTS.registerComponentType(
                    "aspect_filter",
                    builder -> builder.persistent(LegacyIds.ASPECT_KEY_CODEC)
                            .networkSynchronized(VanillaStreamCodecs.resourceKey(IAspect.REGISTRY_KEY)));

    public static final Supplier<ItemDataKey<CelestialBody>> CELESTIAL_BODY =
            DATA_COMPONENTS.registerComponentType(
                    "celestial_body",
                    builder -> builder.persistent(CelestialBody.CODEC).networkSynchronized(CelestialBody.STREAM_CODEC));

    public static final Supplier<ItemDataKey<AspectInstance>> CRYSTAL_ASPECT =
            DATA_COMPONENTS.registerComponentType(
                    "crystal_aspect",
                    builder ->
                            builder.persistent(AspectInstance.CODEC).networkSynchronized(AspectInstance.STREAM_CODEC));

    public static final Supplier<ItemDataKey<Spell>> SPELL =
            DATA_COMPONENTS.registerComponentType(
                    "spell", builder -> builder.persistent(Spell.CODEC).networkSynchronized(Spell.STREAM_CODEC));

    public static final Supplier<ItemDataKey<Spell>> LEGACY_FOCUS_PACKAGE =
            DATA_COMPONENTS.registerComponentType(
                    "focus_package",
                    builder -> builder.persistent(LegacyFocusPackage.CODEC).networkSynchronized(Spell.STREAM_CODEC));

    public static final Supplier<ItemDataKey<SocketedFocus>> SOCKETED_FOCUS =
            DATA_COMPONENTS.registerComponentType(
                    "socketed_focus",
                    builder -> builder.persistent(SocketedFocus.CODEC).networkSynchronized(SocketedFocus.STREAM_CODEC));

    public static final Supplier<ItemDataKey<CasterArea>> CASTER_AREA =
            DATA_COMPONENTS.registerComponentType(
                    "caster_area",
                    builder -> builder.persistent(CasterArea.CODEC).networkSynchronized(CasterArea.STREAM_CODEC));

    public static final Supplier<ItemDataKey<MazeId>> LABYRINTH_KEY =
            DATA_COMPONENTS.registerComponentType(
                    "labyrinth_key",
                    builder -> builder.persistent(MazeId.CODEC).networkSynchronized(MazeId.STREAM_CODEC));

    public static final Supplier<ItemDataKey<BlockState>> PICKED_BLOCK =
            DATA_COMPONENTS.registerComponentType(
                    "picked_block",
                    builder -> builder.persistent(BlockState.CODEC)
                            .networkSynchronized(ByteBufCodecs.fromCodec(BlockState.CODEC)));

    public static final Supplier<ItemDataKey<Boolean>> GRAPPLE_LOADED =
            DATA_COMPONENTS.registerComponentType(
                    "grapple_loaded",
                    builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    public static final Supplier<ItemDataKey<Integer>> CHARGE =
            DATA_COMPONENTS.registerComponentType(
                    "charge",
                    builder -> builder.persistent(ExtraCodecs.NON_NEGATIVE_INT)
                            .networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final Supplier<ItemDataKey<ChargeProfile>> RECHARGEABLE =
            DATA_COMPONENTS.registerComponentType(
                    "rechargeable",
                    builder -> builder.persistent(ChargeProfile.CODEC).networkSynchronized(ChargeProfile.STREAM_CODEC));

    public static final Supplier<ItemDataKey<InfusionEnchantments>>
            INFUSION_ENCHANTMENTS = DATA_COMPONENTS.registerComponentType(InfusionEnchantments.ITEM_DATA);

    public static final Supplier<ItemDataKey<Integer>> STACK_WARP =
            DATA_COMPONENTS.registerComponentType(
                    "warp",
                    builder -> builder.persistent(ExtraCodecs.NON_NEGATIVE_INT)
                            .networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final Supplier<ItemDataKey<Integer>> RUNIC_CHARGE =
            DATA_COMPONENTS.registerComponentType(
                    "runic_charge",
                    builder -> builder.persistent(ExtraCodecs.POSITIVE_INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final Supplier<ItemDataKey<Integer>> TOOL_ORIENTATION =
            DATA_COMPONENTS.registerComponentType(
                    "tool_orientation",
                    builder -> builder.persistent(ExtraCodecs.NON_NEGATIVE_INT)
                            .networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final Supplier<ItemDataKey<Integer>> ENERGY =
            DATA_COMPONENTS.registerComponentType(
                    "energy",
                    builder -> builder.persistent(ExtraCodecs.NON_NEGATIVE_INT)
                            .networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final Supplier<ItemDataKey<GolemProperties>> GOLEM_PROPERTIES =
            DATA_COMPONENTS.registerComponentType(
                    "golem_properties",
                    builder -> builder.persistent(GolemProperties.CODEC)
                            .networkSynchronized(GolemProperties.STREAM_CODEC));

    public static final Supplier<ItemDataKey<Integer>> GOLEM_XP =
            DATA_COMPONENTS.registerComponentType(
                    "golem_xp",
                    builder -> builder.persistent(ExtraCodecs.NON_NEGATIVE_INT)
                            .networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final Supplier<ItemDataKey<Integer>> STORED_XP =
            DATA_COMPONENTS.registerComponentType(
                    "stored_xp",
                    builder -> builder.persistent(ExtraCodecs.NON_NEGATIVE_INT)
                            .networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final Supplier<ItemDataKey<Integer>> VERDANT_TYPE =
            DATA_COMPONENTS.registerComponentType(
                    "verdant_type",
                    builder -> builder.persistent(ExtraCodecs.NON_NEGATIVE_INT)
                            .networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final Supplier<ItemDataKey<Integer>> FORTRESS_MASK =
            DATA_COMPONENTS.registerComponentType(
                    "fortress_mask",
                    builder -> builder.persistent(ExtraCodecs.NON_NEGATIVE_INT)
                            .networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final Supplier<ItemDataKey<Unit>> GOGGLES_UPGRADE =
            DATA_COMPONENTS.registerComponentType(
                    "goggles_upgrade",
                    builder -> builder.persistent(Unit.CODEC).networkSynchronized(StreamCodec.unit(Unit.INSTANCE)));

    public static final Supplier<ItemDataKey<ItemStack>> HARNESS_JAR =
            DATA_COMPONENTS.registerComponentType(
                    "harness_jar",
                    builder -> builder.persistent(ItemStackCodecs.CODEC).networkSynchronized(VanillaStreamCodecs.ITEM_STACK));
    public static final Supplier<ItemDataKey<ItemContents>> POUCH_CONTENTS =
            DATA_COMPONENTS.registerComponentType(
                    "pouch_contents",
                    builder -> builder.persistent(ItemContents.CODEC)
                            .networkSynchronized(ItemContents.STREAM_CODEC));

    public static final Supplier<ItemDataKey<GlobalPos>> MIRROR_LINK =
            DATA_COMPONENTS.registerComponentType(
                    "mirror_link",
                    builder -> builder.persistent(GlobalPos.CODEC).networkSynchronized(VanillaStreamCodecs.GLOBAL_POS));

    public static final Supplier<ItemDataKey<GlobalPos>> ARCANE_KEY_LINK =
            DATA_COMPONENTS.registerComponentType(
                    "arcane_key_link",
                    builder -> builder.persistent(GlobalPos.CODEC).networkSynchronized(VanillaStreamCodecs.GLOBAL_POS));

    public static final Supplier<ItemDataKey<Boolean>> NOTE_COMPLETE =
            DATA_COMPONENTS.registerComponentType(
                    "note_complete", builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    public static final Supplier<ItemDataKey<Boolean>> WHIRLWIND_DISABLED =
            DATA_COMPONENTS.registerComponentType(
                    "whirlwind_disabled",
                    builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    public static final Supplier<ItemDataKey<ResearchNoteData>> RESEARCH_NOTE =
            DATA_COMPONENTS.registerComponentType(
                    "research_note",
                    builder -> builder.persistent(ResearchNoteData.CODEC)
                            .networkSynchronized(ResearchNoteData.STREAM_CODEC));

    public static final Supplier<ItemDataKey<NodeData>> NODE_DATA =
            DATA_COMPONENTS.registerComponentType(
                    "node_data",
                    builder -> builder.persistent(NodeData.CODEC).networkSynchronized(NodeData.STREAM_CODEC));

    public static final Supplier<ItemDataKey<WandParts>> WAND_PARTS =
            DATA_COMPONENTS.registerComponentType(
                    "wand_parts",
                    builder -> builder.persistent(WandParts.CODEC).networkSynchronized(WandParts.STREAM_CODEC));

    public static final Supplier<ItemDataKey<WandVis>> WAND_VIS =
            DATA_COMPONENTS.registerComponentType(
                    "wand_vis",
                    builder -> builder.persistent(LegacyIds.WAND_VIS_CODEC).networkSynchronized(WandVis.STREAM_CODEC));

    public static final Supplier<ItemDataKey<ShareBinding>> SHARE_BINDING =
            DATA_COMPONENTS.registerComponentType(
                    "share_binding",
                    builder -> builder.persistent(ShareBinding.CODEC).networkSynchronized(ShareBinding.STREAM_CODEC));

    public static final Supplier<ItemDataKey<LinkBinding>> LINK_BINDING =
            DATA_COMPONENTS.registerComponentType(
                    "link_binding",
                    builder -> builder.persistent(LinkBinding.CODEC).networkSynchronized(LinkBinding.STREAM_CODEC));

    private TTDataComponents() {}

    public static void register(IEventBus modBus) {
        ItemData.registerCatalog(DATA_COMPONENTS);
    }
}
