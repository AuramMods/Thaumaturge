package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.state.AttachmentData;
import com.leclowndu93150.thaumaturge.state.AttachmentRegistry;
import com.leclowndu93150.thaumaturge.state.AttachmentKey;
import com.leclowndu93150.thaumaturge.content.aura.AuraData;
import com.leclowndu93150.thaumaturge.content.aura.pressure.FluxPressureState;
import com.leclowndu93150.thaumaturge.content.casters.BlockWorkQueues;
import com.leclowndu93150.thaumaturge.content.eldritch.encounter.LabyrinthBinding;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.world.MazeStamp;
import com.leclowndu93150.thaumaturge.content.eldritch.portal.TransitState;
import com.leclowndu93150.thaumaturge.content.entity.trait.MobTraitRuntime;
import com.leclowndu93150.thaumaturge.content.entity.trait.MobTraitState;
import com.leclowndu93150.thaumaturge.content.equipment.DestructiveSwingMemory;
import com.leclowndu93150.thaumaturge.content.equipment.runic.RunicShieldState;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealWorldIndex;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealsChunkData;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskBoard;
import com.leclowndu93150.thaumaturge.content.recipe.dust.DustTriggerSwapQueue;
import com.leclowndu93150.thaumaturge.content.research.PlayerKnowledge;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPoolData;
import com.leclowndu93150.thaumaturge.content.spell.casting.ChannelSummaryCache;
import com.leclowndu93150.thaumaturge.content.spell.engine.CastBudgets;
import com.leclowndu93150.thaumaturge.content.spell.engine.DelayedSpells;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBloomIndex;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintColumns;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintPressure;
import com.leclowndu93150.thaumaturge.content.taint.flux.PhysicalFluxSamples;
import com.leclowndu93150.thaumaturge.content.warding.ArcaneLockChunkData;
import com.leclowndu93150.thaumaturge.content.warding.WardChunkData;
import com.leclowndu93150.thaumaturge.content.warp.WarpData;
import com.mojang.serialization.Codec;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import com.leclowndu93150.thaumaturge.serialization.stream.ByteBufCodecs;
import net.minecraftforge.eventbus.api.IEventBus;

public final class TTAttachments {
    public static final AttachmentRegistry ATTACHMENTS = new AttachmentRegistry(TTIds.MODID);

    public static final AttachmentKey<PlayerKnowledge> KNOWLEDGE = register(
            "knowledge",
            () -> AttachmentKey.builder(PlayerKnowledge::new)
                    .serialize(PlayerKnowledge.CODEC.codec())
                    .syncToOwner(PlayerKnowledge.STREAM_CODEC)
                    .copyOnDeath());

    public static final AttachmentKey<AuraData> AURA = register(
            "aura",
            () -> AttachmentKey.builder(AuraData::new)
                    .serialize(AuraData.CODEC.codec()));

    public static final AttachmentKey<AspectPoolData> ASPECT_POOL = register(
            "aspect_pool",
            () -> AttachmentKey.builder(AspectPoolData::new)
                    .serialize(AspectPoolData.CODEC.codec())
                    .syncToOwner(AspectPoolData.STREAM_CODEC)
                    .copyOnDeath());

    public static final AttachmentKey<WarpData> WARP = register(
            "warp",
            () -> AttachmentKey.builder(WarpData::new)
                    .serialize(WarpData.CODEC.codec())
                    .syncToOwner(WarpData.STREAM_CODEC)
                    .copyOnDeath());

    public static final AttachmentKey<DustTriggerSwapQueue> DUST_TRIGGER_QUEUE =
            register(
                    "dust_trigger_queue",
                    () -> AttachmentKey.builder(DustTriggerSwapQueue::new)
                            .serialize(DustTriggerSwapQueue.CODEC.codec()));

    public static final AttachmentKey<MazeStamp> LABYRINTH_STAMP = register(
            "labyrinth_stamp",
            () -> AttachmentKey.builder(() -> MazeStamp.NONE)
                    .serialize(MazeStamp.CODEC.codec(), MazeStamp::present));

    public static final AttachmentKey<BlockPos> OBELISK_SITE_MEMBER = register(
            "obelisk_site_member",
            () -> AttachmentKey.builder(() -> BlockPos.ZERO)
                    .serialize(BlockPos.CODEC.fieldOf("altar").codec()));

    public static final AttachmentKey<LabyrinthBinding> LABYRINTH_BINDING =
            register(
                    "labyrinth_binding",
                    () -> AttachmentKey.builder(() -> LabyrinthBinding.NONE)
                            .serialize(LabyrinthBinding.CODEC.codec(), LabyrinthBinding::bound));

    public static final AttachmentKey<TransitState> LABYRINTH_TRANSIT = register(
            "labyrinth_transit", () -> AttachmentKey.builder(TransitState::new));

    public static final AttachmentKey<Integer> GRAPPLE_ID =
            register("grapple_id", () -> AttachmentKey.builder(() -> -1));

    public static final AttachmentKey<DestructiveSwingMemory> DESTRUCTIVE_SWING =
            register(
                    "destructive_swing",
                    () -> AttachmentKey.builder(DestructiveSwingMemory::new));

    public static final AttachmentKey<Boolean> DONATOR_CAPE = register(
            "donator_cape",
            () -> AttachmentKey.builder(() -> false)
                    .serialize(Codec.BOOL)
                    .sync(ByteBufCodecs.BOOL)
                    .copyOnDeath());

    public static final AttachmentKey<Boolean> DONATOR_WELCOMED = register(
            "donator_welcomed",
            () -> AttachmentKey.builder(() -> false)
                    .serialize(Codec.BOOL)
                    .copyOnDeath());

    public static final AttachmentKey<Long> CASTER_COOLDOWN =
            register("caster_cooldown", () -> AttachmentKey.builder(() -> 0L));

    public static final AttachmentKey<Long> CLOUD_JUMP_TIME =
            register("cloud_jump_time", () -> AttachmentKey.builder(() -> 0L));

    public static final AttachmentKey<RunicShieldState> RUNIC_SHIELD = register(
            "runic_shield", () -> AttachmentKey.builder(RunicShieldState::new));

    public static final AttachmentKey<CastBudgets> SPELL_BUDGETS = register(
            "spell_budgets", () -> AttachmentKey.builder(CastBudgets::new));

    public static final AttachmentKey<DelayedSpells> DELAYED_SPELLS = register(
            "delayed_spells", () -> AttachmentKey.builder(DelayedSpells::new));

    public static final AttachmentKey<BlockWorkQueues> BLOCK_WORK_QUEUES = register(
            "block_work_queues",
            () -> AttachmentKey.builder(BlockWorkQueues::new));

    public static final AttachmentKey<SealsChunkData> SEALS = register(
            "seals",
            () -> AttachmentKey.builder(SealsChunkData::new)
                    .serialize(SealsChunkData.CODEC.codec()));

    public static final AttachmentKey<SealWorldIndex> SEAL_INDEX = register(
            "seal_index", () -> AttachmentKey.builder(SealWorldIndex::new));

    public static final AttachmentKey<WardChunkData> WARDS = register(
            "wards",
            () -> AttachmentKey.builder(WardChunkData::new)
                    .serialize(WardChunkData.CODEC.codec()));

    public static final AttachmentKey<ArcaneLockChunkData> ARCANE_LOCKS = register(
            "arcane_locks",
            () -> AttachmentKey.builder(ArcaneLockChunkData::new)
                    .serialize(ArcaneLockChunkData.CODEC.codec()));

    public static final AttachmentKey<TaskBoard> GOLEM_TASKS =
            register("golem_tasks", () -> AttachmentKey.builder(TaskBoard::new));

    public static final AttachmentKey<Set<BlockPos>> EAR_INDEX = register(
            "ear_index",
            () -> AttachmentKey.<Set<BlockPos>>builder(() -> ConcurrentHashMap.newKeySet()));

    public static final AttachmentKey<MobTraitState> MOB_TRAITS = register(
            "mob_traits",
            () -> AttachmentKey.builder(() -> MobTraitState.EMPTY)
                    .serialize(MobTraitState.CODEC.codec(), state -> !state.isEmpty())
                    .sync(MobTraitState.STREAM_CODEC)
                    .copyOnDeath());

    public static final AttachmentKey<MobTraitRuntime> MOB_TRAIT_RUNTIME = register(
            "mob_trait_runtime",
            () -> AttachmentKey.builder(MobTraitRuntime::new));

    public static final AttachmentKey<Boolean> CHAMPION_ROLLED = register(
            "champion_rolled",
            () -> AttachmentKey.builder(() -> false)
                    .serialize(Codec.BOOL)
                    .copyOnDeath());

    public static final AttachmentKey<ChannelSummaryCache> CHANNEL_SUMMARY =
            register(
                    "channel_summary",
                    () -> AttachmentKey.builder(ChannelSummaryCache::new));

    public static final AttachmentKey<Long> WAYFINDING_PULSE =
            register("wayfinding_pulse", () -> AttachmentKey.builder(() -> 0L));

    public static final AttachmentKey<FluxPressureState> FLUX_PRESSURE = register(
            "flux_pressure",
            () -> AttachmentKey.builder(FluxPressureState::new));

    public static final AttachmentKey<Boolean> HOVERING = register(
            "hovering",
            () -> AttachmentKey.builder(() -> false)
                    .serialize(Codec.BOOL, hovering -> hovering)
                    .sync(ByteBufCodecs.BOOL));
    public static final AttachmentKey<Integer> HOVER_CHARGE = register(
            "hover_charge",
            () -> AttachmentKey.builder(() -> 0)
                    .serialize(Codec.INT, charge -> charge > 0));

    public static final AttachmentKey<TaintPressure> TAINT_PRESSURE = register(
            "taint_pressure",
            () -> AttachmentKey.builder(TaintPressure::new)
                    .serialize(TaintPressure.CODEC.codec()));
    public static final AttachmentKey<TaintColumns> TAINT_COLUMNS = register(
            "taint_columns",
            () -> AttachmentKey.builder(TaintColumns::new)
                    .serialize(TaintColumns.CODEC.codec()));
    public static final AttachmentKey<TaintBloomIndex> TAINT_BLOOMS = register(
            "taint_blooms", () -> AttachmentKey.builder(TaintBloomIndex::new));
    public static final AttachmentKey<PhysicalFluxSamples> PHYSICAL_FLUX_SAMPLES =
            register(
                    "physical_flux_samples",
                    () -> AttachmentKey.builder(PhysicalFluxSamples::new));
    public static final AttachmentKey<Boolean> TAINT_LEGACY_IMPORTED = register(
            "taint_legacy_imported",
            () -> AttachmentKey.builder(() -> false).serialize(Codec.BOOL));
    public static final AttachmentKey<Boolean> LEGACY_TAINT_SPIDERLING = register(
            "legacy_taint_spiderling",
            () -> AttachmentKey.builder(() -> false).serialize(Codec.BOOL));

    private TTAttachments() {}

    private static <T> AttachmentKey<T> register(
            String name, Supplier<AttachmentKey.Builder<T>> supplier) {
        return ATTACHMENTS.register(name, supplier.get());
    }

    public static void register(IEventBus modBus) {
        AttachmentData.register(modBus, ATTACHMENTS);
    }

    public static final AttachmentKey<Long> SLOT_SCAN_TIME =
            register("slot_scan_time", () -> AttachmentKey.builder(() -> 0L));
}
