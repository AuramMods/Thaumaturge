package com.leclowndu93150.thaumaturge.content.essentia.crystalizer;

import com.leclowndu93150.thaumaturge.content.blockentity.RegistryBlockEntity;

import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaPull;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaConduit;
import com.leclowndu93150.thaumaturge.api.items.InvHelper;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.essentia.flow.EssentiaFlowHandler;
import com.leclowndu93150.thaumaturge.content.legacy.LegacyIds;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.serialization.TTNbt;
import com.leclowndu93150.thaumaturge.stage.StageRules;
import com.leclowndu93150.thaumaturge.stage.StageTarget;
import com.leclowndu93150.thaumaturge.stage.OwnedProcessing;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class BlockEntityEssentiaCrystalizer extends RegistryBlockEntity implements IEssentiaConduit, OwnedProcessing {
    public static final int TARGET_PROGRESS = Crystallization.DEFAULT_PROGRESS;

    private static final String ASPECT_KEY = "Aspect";
    private static final String PROGRESS_KEY = "Progress";
    private static final String BATCH_KEY = "Batch";
    private static final String ESSENTIA_KEY = "Essentia";
    private static final String OWNER_KEY = "Owner";
    private static final int STEP_INTERVAL = 5;
    private static final int IDLE_SUCTION = 128;
    private static final int BUSY_SUCTION = 64;
    private static final int ONE_POINT = 1;
    private static final int MAX_VIS_REQUEST = 20;
    private static final int MIN_VIS_REQUEST = 1;
    private static final int REQUEST_DIVISOR = 2;
    private static final float HUNDREDTHS = 100.0F;
    private static final int VIS_PROGRESS_FACTOR = 2;
    private static final int BASE_PROGRESS_STEP = 1;
    private static final int RED_SHIFT = 16;
    private static final int GREEN_SHIFT = 8;
    private static final int CHANNEL_MASK = 0xFF;
    private static final int VENT_COLOR = 0xFFFFFF;
    private static final float VENT_SCALE = 4.0F;
    private static final float COLOR_SCALE = 255.0F;
    private static final float MAX_SPEED = 20.0F;
    private static final float FULL_TURN = 360.0F;
    private static final int WHITE_TINT = 0xFFFFFF;
    private static final int SUCTION_FLOOR = 0;
    private static final int NOTHING_MOVED = 0;
    private static final boolean OUTPUT_ALLOWED = false;
    private static final double DROP_DISTANCE = 0.7;
    private static final double DROP_SPEED = 0.04;
    private static final int VENT_TICKS = 6;
    private static final double VENT_DISTANCE = 0.55;
    private static final double VENT_JITTER = 0.15;
    private static final float COLOR_STEP = 0.05F;
    private static final float TINT_LIFT = 0.25F;
    private static final float SPEED_RISE = 0.1F;
    private static final float SPEED_FALL = 0.2F;
    private static final float FINISH_VOLUME = 0.2F;
    private static final float FINISH_BASE_PITCH = 1.75F;
    private static final float FINISH_PITCH_SPREAD = 0.2F;

    private @Nullable ResourceKey<IAspect> aspect;
    private @Nullable UUID owner;
    private Crystallization batch = Crystallization.DEFAULT;
    private int held;
    private int progress;
    private int ventTicks;
    private float rotation;
    private float rotationSpeed;
    private float crystalRed = 1.0F;
    private float crystalGreen = 1.0F;
    private float crystalBlue = 1.0F;

    public BlockEntityEssentiaCrystalizer(BlockPos pos, BlockState state) {
        super(TTBlockEntities.ESSENTIA_CRYSTALIZER.get(), pos, state);
        exposeConduit(side -> side == null || connectsOn(side) ? this : null);
    }

    public static void serverTick(
            Level level, BlockPos pos, BlockState state, BlockEntityEssentiaCrystalizer crystalizer) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        Direction input = state.getValue(BlockStateProperties.FACING);
        Direction output = input.getOpposite();
        if (crystalizer.ventTicks > 0) {
            crystalizer.ventTicks--;
            crystalizer.emitPuff(server, output);
        }
        boolean stepTick = server.getGameTime() % STEP_INTERVAL == 0;
        if (!stepTick || server.hasNeighborSignal(pos)) {
            return;
        }
        if (crystalizer.aspect != null && crystalizer.held >= crystalizer.batch.essentia()) {
            if (!crystalizer.allows(server, crystalizer.aspect)) return;
            crystalizer.advance(server, output);
            return;
        }
        crystalizer.pullFromInput(server, input);
    }

    public static void clientTick(
            Level level, BlockPos pos, BlockState state, BlockEntityEssentiaCrystalizer crystalizer) {
        crystalizer.fadeTint(level);
        crystalizer.spin(!level.hasNeighborSignal(pos));
    }

    private static float channel(int packed, int shift) {
        return ((packed >> shift) & CHANNEL_MASK) / COLOR_SCALE;
    }

    private static float approach(float current, float target, float step) {
        return current < target ? Math.min(current + step, target) : Math.max(current - step, target);
    }

    private void pullFromInput(ServerLevel server, Direction inputFace) {
        Direction touching = inputFace.getOpposite();
        IEssentiaConduit source = EssentiaFlowHandler.conduit(server, worldPosition.relative(inputFace), touching);
        if (source == null || !source.releasesOn(touching)) {
            return;
        }
        Holder<IAspect> offered = source.heldAspect(touching);
        if (offered == null || source.heldAmount(touching) < ONE_POINT || roomFor(offered, inputFace) == NOTHING_MOVED) {
            return;
        }
        int ownPull = pullStrength(inputFace);
        if (source.pull(touching).strength() >= ownPull || ownPull < source.pullThreshold()) {
            return;
        }
        if (source.extract(offered, ONE_POINT, touching, false) == ONE_POINT) {
            accept(offered, ONE_POINT);
        }
    }

    private void advance(ServerLevel server, Direction output) {
        int needed = batch.progress() - progress;
        int request = Math.min(MAX_VIS_REQUEST, Math.max(MIN_VIS_REQUEST, needed / REQUEST_DIVISOR));
        float drained = AuraHelper.drainVis(server, worldPosition, request / HUNDREDTHS, false);
        int units = Math.min(request, Math.round(drained * HUNDREDTHS));
        int gained = BASE_PROGRESS_STEP + VIS_PROGRESS_FACTOR * Math.max(units, 0);
        if (gained >= needed) {
            finish(server, output);
            return;
        }
        progress += gained;
        setChanged();
    }

    private void finish(ServerLevel server, Direction output) {
        Holder<IAspect> input = heldHolder();
        ItemStack result = input == null ? ItemStack.EMPTY : batch.assemble(input);
        aspect = null;
        batch = Crystallization.DEFAULT;
        held = 0;
        progress = 0;
        if (!result.isEmpty()) {
            ItemStack leftover =
                    InvHelper.insertAt(server, worldPosition.relative(output), output.getOpposite(), result, false);
            if (!leftover.isEmpty()) {
                dropOutward(server, output, leftover);
                ventTicks = VENT_TICKS;
            }
        }
        RandomSource random = server.getRandom();
        server.playSound(
                null,
                worldPosition,
                SoundEvents.LAVA_EXTINGUISH,
                SoundSource.BLOCKS,
                FINISH_VOLUME,
                FINISH_BASE_PITCH + random.nextFloat() * FINISH_PITCH_SPREAD);
        changedForClients();
    }

    private void dropOutward(ServerLevel server, Direction output, ItemStack stack) {
        Vec3 spot = Vec3.atCenterOf(worldPosition)
                .add(
                        output.getStepX() * DROP_DISTANCE,
                        output.getStepY() * DROP_DISTANCE,
                        output.getStepZ() * DROP_DISTANCE);
        ItemEntity item = new ItemEntity(server, spot.x, spot.y, spot.z, stack);
        item.setDeltaMovement(
                output.getStepX() * DROP_SPEED, output.getStepY() * DROP_SPEED, output.getStepZ() * DROP_SPEED);
        server.addFreshEntity(item);
    }

    private void emitPuff(ServerLevel server, Direction output) {
        RandomSource random = server.getRandom();
        Vec3 face = Vec3.atCenterOf(worldPosition)
                .add(
                        output.getStepX() * VENT_DISTANCE,
                        output.getStepY() * VENT_DISTANCE,
                        output.getStepZ() * VENT_DISTANCE);
        double mx = output.getStepX() + jitter(random);
        double my = output.getStepY() + jitter(random);
        double mz = output.getStepZ() + jitter(random);
        Effects.vent(server, face)
                .motion(mx, my, mz)
                .color(VENT_COLOR)
                .scale(VENT_SCALE)
                .send();
    }

    private void fadeTint(Level level) {
        Holder<IAspect> held = aspect == null ? null : Aspects.resolve(level, aspect);
        int packed = held == null ? WHITE_TINT : held.value().color();
        crystalRed = approach(crystalRed, brighten(channel(packed, RED_SHIFT)), COLOR_STEP);
        crystalGreen = approach(crystalGreen, brighten(channel(packed, GREEN_SHIFT)), COLOR_STEP);
        crystalBlue = approach(crystalBlue, brighten(channel(packed, 0)), COLOR_STEP);
    }

    private static float brighten(float channel) {
        return Math.min(1.0F, channel + (1.0F - channel) * TINT_LIFT);
    }

    private void spin(boolean unpowered) {
        boolean working = unpowered && aspect != null;
        rotationSpeed =
                working ? approach(rotationSpeed, MAX_SPEED, SPEED_RISE) : approach(rotationSpeed, 0.0F, SPEED_FALL);
        rotation = (rotation + rotationSpeed) % FULL_TURN;
    }

    private void accept(Holder<IAspect> offered, int count) {
        if (aspect == null) {
            aspect = offered.unwrapKey().orElseThrow();
            batch = Crystallization.of(offered);
            progress = 0;
        }
        held += count;
        changedForClients();
    }

    @Override
    public void setOwner(UUID owner) {
        this.owner = owner;
        setChanged();
    }

    private boolean allows(ServerLevel server, @Nullable ResourceKey<IAspect> input) {
        StageTarget target = input == null
                ? StageTarget.family(StageTarget.ESSENTIA_CRYSTALIZER)
                : StageTarget.recipe(StageTarget.ESSENTIA_CRYSTALIZER, input.location()).withSubject(input);
        return StageRules.allowsOwner(server, target, owner);
    }

    @Override
    public int roomFor(Holder<IAspect> candidate, Direction side) {
        ResourceKey<IAspect> key = candidate.unwrapKey().orElse(null);
        if (key == null || !isInputSide(side) || aspect != null && !aspect.equals(key)
                || level instanceof ServerLevel server && !allows(server, key)) {
            return NOTHING_MOVED;
        }
        Crystallization rule = aspect == null ? Crystallization.of(candidate) : batch;
        return rule.enabled() ? Math.max(NOTHING_MOVED, rule.essentia() - held) : NOTHING_MOVED;
    }

    private static double jitter(RandomSource random) {
        return (random.nextDouble() * 2.0 - 1.0) * VENT_JITTER;
    }

    private void changedForClients() {
        setChanged();
        if (level != null) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public boolean releasesOn(Direction face) {
        return OUTPUT_ALLOWED;
    }

    private Direction inputFace() {
        return getBlockState().getValue(BlockStateProperties.FACING);
    }

    private boolean isInputSide(@Nullable Direction face) {
        return face != null && face == inputFace();
    }

    public @Nullable ResourceKey<IAspect> aspectKey() {
        return aspect;
    }

    public int progress() {
        return progress;
    }

    public float rotation() {
        return rotation;
    }

    public float rotationSpeed() {
        return rotationSpeed;
    }

    public float crystalRed() {
        return crystalRed;
    }

    public float crystalGreen() {
        return crystalGreen;
    }

    public float crystalBlue() {
        return crystalBlue;
    }

    @Override
    public boolean receivesOn(Direction face) {
        return face == inputFace();
    }

    @Override
    public int insert(Holder<IAspect> offered, int count, Direction side, boolean simulate) {
        if (count <= 0) return NOTHING_MOVED;
        int accepted = Math.min(count, roomFor(offered, side));
        if (accepted > 0 && !simulate) accept(offered, accepted);
        return accepted;
    }

    @Override
    public int extract(Holder<IAspect> wanted, int count, Direction side, boolean simulate) {
        return NOTHING_MOVED;
    }

    @Override
    public boolean connectsOn(Direction face) {
        return isInputSide(face);
    }

    private @Nullable Holder<IAspect> heldHolder() {
        return aspect == null ? null : Aspects.resolve(level, aspect);
    }

    private int pullStrength(@Nullable Direction face) {
        int base = aspect == null ? IDLE_SUCTION : BUSY_SUCTION;
        boolean listening = isInputSide(face) && level != null && !level.hasNeighborSignal(worldPosition);
        if (listening && level instanceof ServerLevel server && !allows(server, aspect)) {
            return SUCTION_FLOOR;
        }
        return listening ? base : SUCTION_FLOOR;
    }

    @Override
    public EssentiaPull pull(@Nullable Direction face) {
        int strength = pullStrength(face);
        Holder<IAspect> input = held < batch.essentia() ? heldHolder() : null;
        return input == null ? EssentiaPull.any(strength) : EssentiaPull.of(input, strength);
    }

    @Override
    public int pullThreshold() {
        return SUCTION_FLOOR;
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        aspect = !input.contains("HasAspect") || input.getBoolean("HasAspect")
                ? TTNbt.read(input, ASPECT_KEY, LegacyIds.ASPECT_KEY_CODEC, registries)
                        .orElse(null)
                : null;
        owner = input.hasUUID(OWNER_KEY) ? input.getUUID(OWNER_KEY) : null;
        batch = aspect == null ? Crystallization.DEFAULT
                : TTNbt.read(input, BATCH_KEY, Crystallization.CODEC, registries).orElse(Crystallization.DEFAULT);
        held = aspect == null ? 0
                : input.contains(ESSENTIA_KEY) ? Mth.clamp(input.getInt(ESSENTIA_KEY), 1, batch.essentia()) : 1;
        progress = held < batch.essentia() ? 0 : Mth.clamp(input.getInt(PROGRESS_KEY), 0, batch.progress());
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        if (owner != null) output.putUUID(OWNER_KEY, owner);
        storeBatch(output, registries);
    }

    private void storeBatch(CompoundTag output, HolderLookup.Provider registries) {
        output.putBoolean("HasAspect", aspect != null);
        if (aspect == null) {
            return;
        }
        output.putInt(PROGRESS_KEY, progress);
        TTNbt.store(output, ASPECT_KEY, LegacyIds.ASPECT_KEY_CODEC, registries, aspect);
        if (batch != Crystallization.DEFAULT) {
            TTNbt.store(output, BATCH_KEY, Crystallization.CODEC, registries, batch);
        }
        if (held != ONE_POINT) output.putInt(ESSENTIA_KEY, held);
    }

    @Override
    public int heldAmount(@Nullable Direction face) {
        return held;
    }

    @Override
    public @Nullable Holder<IAspect> heldAspect(@Nullable Direction face) {
        return heldHolder();
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag output = new CompoundTag();
        super.saveAdditional(output, registries);
        storeBatch(output, registries);
        return output;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
