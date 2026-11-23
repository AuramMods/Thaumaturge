package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.entity.EntityFluxRift;
import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.serialization.TTNbt;
import com.leclowndu93150.thaumaturge.stage.OwnedProcessing;
import com.leclowndu93150.thaumaturge.stage.StageTarget;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.ItemStackHandler;
import org.jspecify.annotations.Nullable;

public final class BlockEntityVoidSiphon extends AbstractSyncedBlockEntity implements OwnedProcessing {
    public static final int PROGRESS_REQUIRED = 2000;
    private static final int STREAK_INTERVAL = 40;
    private static final int STREAK_VARIANTS = 8;
    private static final float STREAK_SCALE = 0.12F;
    private static final double RIFT_RANGE = 8.0;

    private final ItemStackHandler output = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int index, ItemStack resource) {
            return ItemStack.isSameItemSameTags(resource, rules().output());
        }

        @Override
        protected void onContentsChanged(int index) {
            super.onContentsChanged(index);
            setChanged();
        }
    };

    private int counter;
    private int progress;
    private @Nullable UUID owner;
    private @Nullable VoidSiphonRules paidBatch;

    public BlockEntityVoidSiphon(BlockPos pos, BlockState state) {
        super(TTBlockEntities.VOID_SIPHON.get(), pos, state);
        exposeCapability(ForgeCapabilities.ITEM_HANDLER, side -> output());
    }

    public ItemStackHandler output() {
        return output;
    }

    public int progress() {
        return progress;
    }

    public int progressRequired() {
        return rules().progress();
    }

    @Override
    public void setOwner(UUID owner) {
        this.owner = owner;
        setChanged();
    }

    private VoidSiphonRules rules() {
        return paidBatch == null ? AuraMachineRules.of(getBlockState().getBlock(), VoidSiphonRules.DATA_MAP,
                VoidSiphonRules.DEFAULT) : paidBatch;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityVoidSiphon siphon) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        var rules = siphon.rules();
        if (++siphon.counter % rules.interval() != 0 || !rules.enabled()) {
            return;
        }
        ItemStack result = rules.output();
        if (!AuraMachineRules.allows(server, StageTarget.VOID_SIPHON, state.getBlock(), siphon.owner,
                TTEntities.FLUX_RIFT.get().builtInRegistryHolder().key(), result.getItemHolder().unwrapKey().orElseThrow())) {
            return;
        }
        if (state.getValue(BlockStateProperties.ENABLED)
                && siphon.progress < rules.progress()
                && siphon.hasOutputRoom(result)) {
            List<EntityFluxRift> drained = siphon.getValidRifts(server, pos);
            for (EntityFluxRift rift : drained) {
                double d = Math.sqrt(rift.currentSize());
                siphon.progress += (int) d;
                rift.adjustStability((float) (-d * rules.stabilityScale() / 15.0));
                if (rules.shrinkChance() > 0 && level.getRandom().nextInt(rules.shrinkChance()) == 0) {
                    rift.resize(rift.currentSize() - 1);
                }
            }
            if (!drained.isEmpty()) {
                siphon.paidBatch = rules;
                siphon.setChanged();
            }
            if (siphon.counter % STREAK_INTERVAL == 0) {
                Vec3 top = LevelSpaces.toWorld(server, new Vec3(pos.getX() + 0.5, pos.getY() + 1.01, pos.getZ() + 0.5));
                drained.forEach(rift -> showStreak(server, rift, top));
            }
        }
        siphon.produce(rules.progress(), result);
    }

    private void produce(int required, ItemStack result) {
        while (progress >= required && hasOutputRoom(result)) {
            progress -= required;
            int count = output.getStackInSlot(0).getCount() + result.getCount();
            output.setStackInSlot(0, result.copyWithCount(count));
        }
        if (progress == 0 && paidBatch != null) {
            paidBatch = null;
            setChanged();
        }
    }

    private boolean hasOutputRoom(ItemStack result) {
        ItemStack stack = output.getStackInSlot(0);
        return stack.isEmpty() || ItemStack.isSameItemSameTags(stack, result)
                && stack.getCount() + result.getCount() <= stack.getMaxStackSize();
    }

    private static void showStreak(ServerLevel level, EntityFluxRift rift, Vec3 target) {
        RandomSource random = level.getRandom();
        Vec3 from = rift.position();
        if (!rift.outline.isEmpty()) {
            from = from.add(rift.outline.get(random.nextInt(rift.outline.size())));
        }
        Effects.voidStream(level, from)
                .to(target)
                .seed(random.nextInt(STREAK_VARIANTS))
                .scale(STREAK_SCALE)
                .send();
    }

    private List<EntityFluxRift> getValidRifts(Level level, BlockPos pos) {
        List<EntityFluxRift> found = new ArrayList<>();
        AABB box = LevelSpaces.worldBox(level, pos, new AABB(pos).inflate(RIFT_RANGE));
        for (EntityFluxRift rift : level.getEntitiesOfClass(EntityFluxRift.class, box)) {
            if (rift.isRemoved() || rift.currentSize() < 2) {
                continue;
            }
            Vec3 from = LevelSpaces.toWorld(level, new Vec3(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5));
            Vec3 to = rift.position();
            from = from.add(to.subtract(from).normalize());
            HitResult hit = level.clip(new ClipContext(
                    from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null));
            if (hit.getType() == HitResult.Type.MISS) {
                found.add(rift);
            }
        }
        return found;
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        progress = Math.max(0, input.getInt("progress"));
        owner = input.hasUUID("Owner") ? input.getUUID("Owner") : null;
        paidBatch = progress == 0 ? null : TTNbt.read(input, "PaidBatch", VoidSiphonRules.CODEC, registries)
                .orElse(VoidSiphonRules.DEFAULT);
        if (input.contains("Output")) {
            output.deserializeNBT(input.getCompound("Output"));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag output_, HolderLookup.Provider registries) {
        super.saveAdditional(output_, registries);
        output_.putInt("progress", progress);
        output_.put("Output", output.serializeNBT());
        if (owner != null) output_.putUUID("Owner", owner);
        if (paidBatch != null) TTNbt.store(output_, "PaidBatch", VoidSiphonRules.CODEC, registries, paidBatch);
    }

    @Override
    protected void writeClientData(CompoundTag output, HolderLookup.Provider registries) {
        super.writeClientData(output, registries);
        output.remove("Owner");
    }
}
