package com.leclowndu93150.thaumaturge.content.spell.manipulator;

import com.leclowndu93150.thaumaturge.content.spell.SpellStages;
import com.leclowndu93150.thaumaturge.stage.StageAuthorization;
import com.leclowndu93150.thaumaturge.stage.StageRules;
import com.leclowndu93150.thaumaturge.stage.StageTarget;

import com.leclowndu93150.thaumaturge.itemdata.VanillaItemData;

import com.leclowndu93150.thaumaturge.itemdata.ItemData;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.spell.FocusTier;
import com.leclowndu93150.thaumaturge.api.spell.Spell;
import com.leclowndu93150.thaumaturge.api.spell.SpellSummary;
import com.leclowndu93150.thaumaturge.api.spell.Spells;
import com.leclowndu93150.thaumaturge.api.spell.event.SpellInscribeEvent;
import com.leclowndu93150.thaumaturge.content.aura.AuraVisDrain;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.content.effect.EffectDispatch;
import com.leclowndu93150.thaumaturge.content.particle.ShieldSparkParticleOptions;
import com.leclowndu93150.thaumaturge.content.spell.item.FocusItems;
import com.leclowndu93150.thaumaturge.content.taint.item.EssentiaCrystalFactory;
import com.leclowndu93150.thaumaturge.content.world.LoadedChunks;
import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import com.leclowndu93150.thaumaturge.serialization.TTNbt;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import com.leclowndu93150.thaumaturge.compat.Colors.ARGB32;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.items.ItemStackHandler;
import org.jspecify.annotations.Nullable;

public final class BlockEntityFocalManipulator extends AbstractSyncedBlockEntity implements MenuProvider {
    private static final int RENDER_BOUNDS_HEIGHT = 2;
    public static final int SLOT_FOCUS = 0;
    public static final int MAX_NAME_LENGTH = 50;
    private static final int VIS_PER_COMPLEXITY = 10;
    private static final int VIS_PER_BUDGET_DIVISOR = 5;
    private static final int CHARGER_REACH = 1;
    private static final int SPARKLE_SPREAD = 3;
    private static final Component TITLE = Component.translatable("block.thaumaturge.focal_manipulator");

    private final Drawer drawer = new Drawer();
    private Spell draft = Spell.empty();
    private String name = "";
    private float vis;
    private float visTotal;
    private AspectList crystals = AspectList.EMPTY;
    private int ticks;
    private int interval = InscriptionRules.DEFAULT_INTERVAL;
    private float drainPerCycle = InscriptionRules.DEFAULT_DRAIN;
    private @Nullable StageAuthorization authority;

    public BlockEntityFocalManipulator(BlockPos pos, BlockState state) {
        super(TTBlockEntities.FOCAL_MANIPULATOR.get(), pos, state);
    }

    @Override
    public AABB getRenderBoundingBox() {
        var pos = getBlockPos();
        return new AABB(
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                pos.getX() + 1.0,
                pos.getY() + RENDER_BOUNDS_HEIGHT,
                pos.getZ() + 1.0);
    }

    public ItemStackHandler items() {
        return drawer;
    }

    public ItemStack focusStack() {
        return drawer.getStackInSlot(SLOT_FOCUS).copy();
    }

    public Spell draft() {
        return draft;
    }

    public String name() {
        return name;
    }

    public float vis() {
        return vis;
    }

    public float progress() {
        return visTotal <= 0.0F ? 0.0F : 1.0F - vis / visTotal;
    }

    public boolean inscribing() {
        return vis > 0.0F;
    }

    public AspectList crystals() {
        return crystals;
    }

    public void acceptDraft(Spell spell, String newName) {
        if (inscribing()) {
            return;
        }
        draft = spell;
        name = SharedConstants.filterText(newName).strip();
        setChangedAndSync();
    }

    public boolean startInscribing(Player player) {
        ItemStack focus = focusStack();
        Optional<FocusTier> tier = Spells.tierOf(focus);
        if (inscribing() || tier.isEmpty() || !(level instanceof ServerLevel server) || player.level() != server) {
            return false;
        }
        var rules = InscriptionRules.of(focus);
        if (!rules.enabled()) {
            player.displayClientMessage(Component.translatable("message.thaumaturge.focus.inscription_disabled"), true);
            return false;
        }
        var target = SpellStages.target(server, StageTarget.FOCUS_INSCRIPTION, focus, draft);
        if (!StageRules.check(server, target, player)) return false;
        SpellSummary summary = rules.adjusted(Spells.analyze(draft, tier.get(), server.registryAccess(), player));
        AspectList cost = FocusItems.aspects(summary, server.registryAccess());
        if (!InscriptionCheck.of(player, true, false, summary, cost).ready()) {
            return false;
        }
        boolean creative = player.getAbilities().instabuild;
        if (MinecraftForge.EVENT_BUS.post(new SpellInscribeEvent(player, worldPosition, focus, draft, summary))) {
            return false;
        }
        if (!StageRules.check(server, target, player)) return false;
        if (!creative) {
            player.giveExperienceLevels(-summary.xp());
            for (AspectInstance instance : cost.entries()) {
                take(player, EssentiaCrystalFactory.of(instance.aspect(), instance.amount()));
            }
        }
        crystals = cost;
        ticks = 0;
        interval = rules.interval();
        drainPerCycle = rules.drain();
        visTotal = rules.vis()
                * (summary.complexity() * VIS_PER_COMPLEXITY + (float) tier.get().complexity() / VIS_PER_BUDGET_DIVISOR);
        vis = visTotal;
        authority = StageAuthorization.capture(server, target, player);
        setChangedAndSync();
        level.playSound(null, worldPosition, TTSounds.CRAFTSTART.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        return true;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityFocalManipulator table) {
        if (level instanceof ServerLevel server) {
            table.tickServer(server);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, BlockEntityFocalManipulator table) {
        if (table.inscribing()) {
            table.shimmer(level);
        }
    }

    private void tickServer(ServerLevel server) {
        if (!inscribing() || !mayContinue(server)) {
            return;
        }
        ticks = advanceWorkPulse(ticks, interval);
        setChanged();
        if (ticks != 0) {
            return;
        }
        if (!FocusItems.isFocus(focusStack())) {
            abort(server);
            return;
        }
        float drained = drain(server, Math.min(drainPerCycle, vis));
        if (drained > 0.0F) {
            RandomSource random = server.getRandom();
            Vec3 from = new Vec3(
                    worldPosition.getX() + random.nextInt(SPARKLE_SPREAD) - random.nextInt(SPARKLE_SPREAD),
                    worldPosition.getY() + random.nextInt(SPARKLE_SPREAD),
                    worldPosition.getZ() + random.nextInt(SPARKLE_SPREAD) - random.nextInt(SPARKLE_SPREAD));
            EffectDispatch.spawnVisSparkle(server, from, Vec3.atBottomCenterOf(worldPosition.above()));
            vis -= drained;
            setChangedAndSync();
        }
        if (vis <= 0.0F) {
            finish(server);
        }
    }

    static int advanceWorkPulse(int pulse) {
        return advanceWorkPulse(pulse, InscriptionRules.DEFAULT_INTERVAL);
    }

    private static int advanceWorkPulse(int pulse, int interval) {
        return pulse + 1 >= interval ? 0 : pulse + 1;
    }

    private boolean mayContinue(ServerLevel level) {
        if (!StageRules.of(level).hasRules(StageTarget.FOCUS_INSCRIPTION)) return true;
        return authority != null ? authority.allows(level)
                : StageRules.allows(level, SpellStages.target(level, StageTarget.FOCUS_INSCRIPTION, focusStack(), draft),
                        null);
    }

    private float drain(ServerLevel server, float amount) {
        if (!server.getBlockState(worldPosition.above()).is(TTBlocks.ARCANE_WORKBENCH_CHARGER.get())) {
            return AuraHelper.drainVis(server, worldPosition, amount, false);
        }
        BlockPos home = LevelSpaces.worldBlock(server, worldPosition);
        ChunkPos center = new ChunkPos(home);
        List<BlockPos> anchors = new ArrayList<>((CHARGER_REACH * 2 + 1) * (CHARGER_REACH * 2 + 1));
        for (int dx = -CHARGER_REACH; dx <= CHARGER_REACH; dx++) {
            for (int dz = -CHARGER_REACH; dz <= CHARGER_REACH; dz++) {
                ChunkPos chunk = new ChunkPos(center.x + dx, center.z + dz);
                BlockPos anchor = chunk.getMiddleBlockPosition(home.getY());
                if (LoadedChunks.contains(server, anchor)) {
                    anchors.add(anchor);
                }
            }
        }
        return (float) AuraVisDrain.acrossAnchors(
                amount, anchors, (anchor, request) -> AuraHelper.drainVis(server, anchor, request, false));
    }

    private void finish(ServerLevel server) {
        ItemStack focus = focusStack();
        vis = 0.0F;
        visTotal = 0.0F;
        crystals = AspectList.EMPTY;
        authority = null;
        if (FocusItems.isFocus(focus)) {
            Spells.setSpell(focus, draft);
            if (name.isBlank()) {
                ItemData.remove(focus, VanillaItemData.CUSTOM_NAME);
            } else {
                ItemData.set(focus, VanillaItemData.CUSTOM_NAME, Component.literal(name));
            }
            drawer.place(focus);
            server.playSound(null, worldPosition, TTSounds.WAND.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        setChangedAndSync();
    }

    private void abort(ServerLevel server) {
        vis = 0.0F;
        visTotal = 0.0F;
        crystals = AspectList.EMPTY;
        authority = null;
        server.playSound(null, worldPosition, TTSounds.WANDFAIL.get(), SoundSource.BLOCKS, 0.33F, 1.0F);
        setChangedAndSync();
    }

    private void shimmer(Level clientLevel) {
        RandomSource random = clientLevel.getRandom();
        ShieldSparkParticleOptions spark = new ShieldSparkParticleOptions(
                ARGB32.color(
                        (int) ((1.0F) * 255.0F),
                        (int) ((0.5F + random.nextFloat() * 0.4F) * 255.0F),
                        (int) ((1.0F - random.nextFloat() * 0.4F) * 255.0F),
                        (int) ((1.0F - random.nextFloat() * 0.4F) * 255.0F)),
                0.8F,
                0.3F + random.nextFloat() * 0.3F,
                6 + random.nextInt(5),
                0,
                true);
        clientLevel.addParticle(
                spark,
                worldPosition.getX() + 0.5 + (random.nextFloat() - random.nextFloat()) * 0.3F,
                worldPosition.getY() + 1.4 + (random.nextFloat() - random.nextFloat()) * 0.3F,
                worldPosition.getZ() + 0.5 + (random.nextFloat() - random.nextFloat()) * 0.3F,
                0.0,
                0.0,
                0.0);
    }

    private void loadFocusDesign() {
        ItemStack focus = focusStack();
        Spell spell = Spells.spellOf(focus);
        if (spell != null) {
            draft = spell;
            name = ItemData.has(focus, VanillaItemData.CUSTOM_NAME) ? focus.getHoverName().getString() : "";
        }
    }

    private static void take(Player player, ItemStack wanted) {
        int remaining = wanted.getCount();
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize() && remaining > 0; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (ItemStack.isSameItemSameTags(stack, wanted)) {
                int taken = Math.min(remaining, stack.getCount());
                stack.shrink(taken);
                remaining -= taken;
            }
        }
    }

    public void dropContents() {
        BlockPos pos = worldPosition;
        ItemStack focus = focusStack();
        if (level != null && !level.isClientSide() && !focus.isEmpty()) {
            drawer.setStackInSlot(SLOT_FOCUS, ItemStack.EMPTY);
            Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, focus);
        }
    }

    @Override
    public Component getDisplayName() {
        return TITLE;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new MenuFocalManipulator(containerId, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        writeClientData(output, registries);
        output.putInt("Ticks", ticks);
        output.putInt("Interval", interval);
        output.putFloat("Drain", drainPerCycle);
        if (authority != null) TTNbt.store(output, "Authority", StageAuthorization.CODEC, registries, authority);
    }

    @Override
    protected void writeClientData(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        output.put("inventory", drawer.serializeNBT());
        TTNbt.store(output, "Draft", Spell.CODEC, registries, draft);
        output.putString("Name", name);
        output.putFloat("Vis", vis);
        output.putFloat("VisTotal", visTotal);
        TTNbt.store(output, "Crystals", AspectList.CODEC, registries, crystals);
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        drawer.deserializeNBT(input.getCompound("inventory"));
        draft = TTNbt.read(input, "Draft", Spell.CODEC, registries).orElse(Spell.empty());
        name = (input.contains("Name") ? input.getString("Name") : "");
        vis = (input.contains("Vis") ? input.getFloat("Vis") : 0.0F);
        visTotal = (input.contains("VisTotal") ? input.getFloat("VisTotal") : 0.0F);
        crystals = TTNbt.read(input, "Crystals", AspectList.CODEC, registries).orElse(AspectList.EMPTY);
        interval = input.contains("Interval") ? Math.max(1, input.getInt("Interval")) : InscriptionRules.DEFAULT_INTERVAL;
        drainPerCycle = input.getFloat("Drain") > 0.0F ? input.getFloat("Drain") : InscriptionRules.DEFAULT_DRAIN;
        ticks = Math.max(0, Math.min(interval - 1, input.getInt("Ticks")));
        authority = TTNbt.read(input, "Authority", StageAuthorization.CODEC, registries).orElse(null);
    }

    private final class Drawer extends ItemStackHandler {
        private ItemStack previous = ItemStack.EMPTY;

        @Override
        protected void onLoad() {
            previous = getStackInSlot(SLOT_FOCUS).copy();
        }

        Drawer() {
            super(NonNullList.withSize(1, ItemStack.EMPTY));
        }

        void place(ItemStack stack) {
            setStackInSlot(SLOT_FOCUS, stack);
        }

        @Override
        protected void onContentsChanged(int index) {
            ItemStack previousContents = previous;
            previous = getStackInSlot(SLOT_FOCUS).copy();
            if (level != null && !level.isClientSide()) {
                if (inscribing() && !ItemStack.isSameItemSameTags(previousContents, focusStack())) {
                    vis = 0.0F;
                    visTotal = 0.0F;
                    crystals = AspectList.EMPTY;
                    authority = null;
                }
                if (!inscribing()) {
                    loadFocusDesign();
                }
            }
            setChangedAndSync();
        }

        @Override
        public boolean isItemValid(int index, ItemStack resource) {
            return Spells.tierOf(resource).isPresent();
        }
    }
}
