package com.leclowndu93150.thaumaturge.compat.ae2;

import appeng.api.config.Actionable;
import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.StorageHelper;
import appeng.util.Platform;
import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessory;
import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessoryBehavior;
import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessoryContext;
import com.leclowndu93150.thaumaturge.content.world.LoadedChunks;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class GolemWirelessBackpack implements GolemAccessoryBehavior<GolemWirelessBackpack.State> {
    static final String ACCESS_POINT = "WirelessAccessPoint";
    static final String OWNER = "WirelessOwner";
    public static final GolemWirelessBackpack BEHAVIOR = new GolemWirelessBackpack();
    public static final GolemAccessory ACCESSORY = new GolemAccessory(TTIds.rl("wireless_golem_backpack"),
            new GolemAccessory.Group(TTIds.rl("back")), 0, 1, 1, 1, 0, false, Optional.of(BEHAVIOR));
    private static final State UNLINKED = new State(Optional.empty(), Optional.empty(), false);
    private static final Codec<State> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            GlobalPos.CODEC.optionalFieldOf("access_point").forGetter(State::accessPoint),
            UUIDUtil.CODEC.optionalFieldOf("owner").forGetter(State::owner)
    ).apply(builder, (accessPoint, owner) -> new State(accessPoint, owner, false)));

    private GolemWirelessBackpack() {}

    public record State(Optional<GlobalPos> accessPoint, Optional<UUID> owner, boolean connected) {}

    record Link(IGrid grid, IActionSource source) {}

    @Override
    public Codec<State> stateCodec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, State> syncCodec() {
        return StreamCodec.of(
                (buffer, state) -> buffer.writeBoolean(state.connected()),
                buffer -> new State(Optional.empty(), Optional.empty(), buffer.readBoolean()));
    }

    @Override
    public State initialState() {
        return UNLINKED;
    }

    @Override
    public State onAttach(GolemAccessoryContext context, State state, ItemStack stack) {
        var tag = stack.getTag();
        if (tag == null || !tag.hasUUID(OWNER) || !tag.contains(ACCESS_POINT, Tag.TAG_COMPOUND)) return UNLINKED;
        var point = GlobalPos.CODEC.parse(NbtOps.INSTANCE, tag.get(ACCESS_POINT)).result();
        var owner = Optional.of(tag.getUUID(OWNER));
        if (!owner.equals(context.golem().ownerIdentity())) return UNLINKED;
        var linked = new State(point, owner, false);
        return new State(point, owner, link(context.golem(), linked) != null);
    }

    @Override
    public void onRemove(GolemAccessoryContext context, State state, ItemStack stack) {
        if (state.accessPoint().isPresent() && state.owner().isPresent()) {
            var point = state.accessPoint().get();
            GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, point).result().ifPresent(encoded ->
                    stack.getOrCreateTag().put(ACCESS_POINT, encoded));
            stack.getOrCreateTag().putUUID(OWNER, state.owner().get());
        }
    }

    @Override
    public State serverTick(GolemAccessoryContext context, State state) {
        var golem = context.golem();
        if (state.accessPoint().isEmpty() || (golem.asEntity().tickCount + golem.asEntity().getId()) % 20 != 0) {
            return state;
        }
        return new State(state.accessPoint(), state.owner(), link(golem, state) != null);
    }

    static @Nullable Link link(IGolemAPI golem) {
        var state = golem.accessoryState(BEHAVIOR).orElse(UNLINKED);
        return link(golem, state);
    }

    private static @Nullable Link link(IGolemAPI golem, State state) {
        if (!(golem.level() instanceof ServerLevel level) || state.accessPoint().isEmpty()
                || state.owner().isEmpty() || !state.owner().equals(golem.ownerIdentity())) return null;
        var point = state.accessPoint().get();
        if (!level.dimension().equals(point.dimension()) || !LoadedChunks.contains(level, point.pos())
                || !(level.getBlockEntity(point.pos()) instanceof IWirelessAccessPoint access) || !access.isActive()) {
            return null;
        }
        if (golem.asEntity().distanceToSqr(Vec3.atCenterOf(point.pos())) > access.getRange() * access.getRange()) return null;
        ServerPlayer owner = level.getServer().getPlayerList().getPlayer(state.owner().get());
        if (owner == null || !Platform.hasPermissions(access.getLocation(), owner)) return null;
        var grid = access.getGrid();
        return grid == null ? null : new Link(grid, IActionSource.ofPlayer(owner, access));
    }

    public static int gather(IGolemAPI golem, @Nullable UUID sealOwner, ItemStack stack, boolean simulate) {
        if (sealOwner == null || !golem.ownerIdentity().filter(sealOwner::equals).isPresent()) return 0;
        var link = link(golem);
        var key = AEItemKey.of(stack);
        if (link == null || key == null) return 0;
        return (int) StorageHelper.poweredInsert(link.grid().getEnergyService(),
                link.grid().getStorageService().getInventory(), key, stack.getCount(),
                link.source(), Actionable.ofSimulate(simulate));
    }
}
