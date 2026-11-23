package com.leclowndu93150.thaumaturge.content.recipe.dust;

import com.leclowndu93150.thaumaturge.itemdata.ItemStackCodecs;

import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.leclowndu93150.thaumaturge.api.recipe.Blueprint;
import com.leclowndu93150.thaumaturge.api.recipe.BlueprintPart;
import com.leclowndu93150.thaumaturge.api.recipe.BlueprintTarget;
import com.leclowndu93150.thaumaturge.api.recipe.DustTrigger;
import com.leclowndu93150.thaumaturge.api.recipe.DustTriggerInput;
import com.leclowndu93150.thaumaturge.api.recipe.DustTriggerPlacement;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.content.recipe.SimpleRecipeSerializer;
import com.leclowndu93150.thaumaturge.recipe.AbstractRecipe;
import com.leclowndu93150.thaumaturge.registry.TTRecipeTypes;
import com.leclowndu93150.thaumaturge.serialization.stream.ByteBufCodecs;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import com.leclowndu93150.thaumaturge.serialization.stream.VanillaStreamCodecs;
import com.leclowndu93150.thaumaturge.stage.StageAuthorization;
import com.leclowndu93150.thaumaturge.stage.StageRules;
import com.leclowndu93150.thaumaturge.stage.StageTarget;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jspecify.annotations.Nullable;

public final class DustTriggerMultiblockRecipe extends AbstractRecipe<DustTriggerInput> implements DustTrigger {

    public static final MapCodec<DustTriggerMultiblockRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                    ResourceLocation.CODEC.fieldOf("blueprint").forGetter(r -> r.blueprintId),
                    ItemStackCodecs.CODEC.fieldOf("result").forGetter(r -> r.result),
                    Codecs.optionalFieldOf(ResearchGate.CODEC, "research").forGetter(r -> r.research))
            .apply(i, DustTriggerMultiblockRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DustTriggerMultiblockRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    VanillaStreamCodecs.RESOURCE_LOCATION,
                    r -> r.blueprintId,
                    VanillaStreamCodecs.ITEM_STACK,
                    r -> r.result,
                    ByteBufCodecs.optional(ResearchGate.STREAM_CODEC),
                    r -> r.research,
                    DustTriggerMultiblockRecipe::new);

    public static final RecipeSerializer<DustTriggerMultiblockRecipe> SERIALIZER =
            new SimpleRecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private final ResourceLocation blueprintId;
    private final ItemStack result;
    private final Optional<ResearchGate> research;

    public DustTriggerMultiblockRecipe(
            ResourceLocation blueprintId, ItemStack result, Optional<ResearchGate> research) {
        this.blueprintId = blueprintId;
        this.result = result;
        this.research = research;
    }

    public ResourceLocation blueprintId() {
        return this.blueprintId;
    }

    @Override
    public StageTarget stageTarget(DustTriggerInput input) {
        return DustTrigger.super.stageTarget(input).withSubject(ResourceKey.create(Blueprint.REGISTRY_KEY, blueprintId));
    }

    public ItemStack result() {
        return this.result.copy();
    }

    @Override
    public Optional<ResearchGate> researchGate() {
        return this.research;
    }

    @Override
    public boolean isMultiblock() {
        return true;
    }

    @Override
    public boolean matches(DustTriggerInput input, Level level) {
        return findPlacement(input) != null;
    }

    @Override
    public @Nullable DustTriggerPlacement findPlacement(DustTriggerInput input) {
        Blueprint blueprint = lookupBlueprint(input.level());
        if (blueprint == null) {
            return null;
        }
        return MultiblockMatcher.find(input.level(), input.pos(), blueprint);
    }

    @Override
    public ItemStack assemble(DustTriggerInput input, HolderLookup.Provider registries) {
        return this.result.copy();
    }

    @Override
    public List<BlockPos> sparkle(Level level, Player player, BlockPos pos, DustTriggerPlacement placement) {
        Blueprint blueprint = lookupBlueprint(level);
        if (blueprint == null || placement == null || placement.facing() == null) {
            return List.of(pos);
        }
        BlockPos origin = pos.offset(placement.xOffset(), placement.yOffset(), placement.zOffset());
        return new RotatedBlueprint(blueprint, MultiblockMatcher.rotationsFor(placement.facing()))
                .cells().stream().map(cell -> origin.offset(cell.offset())).toList();
    }

    @Override
    public void execute(
            DustTriggerInput input, Player player, @Nullable DustTriggerPlacement placement, Direction useFace) {
        tryExecute(input, player, placement, useFace);
    }

    @Override
    public boolean tryExecute(
            DustTriggerInput input, Player player, @Nullable DustTriggerPlacement placement, Direction useFace) {
        Level level = input.level();
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        if (placement == null || placement.facing() == null) {
            return false;
        }
        Blueprint blueprint = lookupBlueprint(level);
        if (blueprint == null) {
            return false;
        }
        var target = stageTarget(input);
        if (!StageRules.check(serverLevel, target, player)) {
            return false;
        }
        var authority = StageAuthorization.capture(serverLevel, target, player);
        BlockPos origin = input.pos().offset(placement.xOffset(), placement.yOffset(), placement.zOffset());
        List<BlueprintCell> cells =
                new RotatedBlueprint(blueprint, MultiblockMatcher.rotationsFor(placement.facing())).cells();
        if (cells.stream().anyMatch(cell -> DustTriggerSwapQueue.isBlocked(level, origin.offset(cell.offset())))) {
            return false;
        }
        for (BlueprintCell cell : cells) {
            BlockPos cellPos = origin.offset(cell.offset());
            enqueueForPart(
                    serverLevel,
                    cellPos,
                    level.getBlockState(cellPos),
                    cell.part(),
                    placement.facing(),
                    useFace,
                    player,
                    authority,
                    cell.part().priority());
        }
        return true;
    }

    private static void enqueueForPart(
            ServerLevel level,
            BlockPos pos,
            BlockState original,
            BlueprintPart part,
            Direction placementFacing,
            Direction useFace,
            Player player,
            StageAuthorization authority,
            int delay) {
        BlueprintTarget target = part.target();
        if (target instanceof BlueprintTarget.Keep) {
            return;
        }
        if (target instanceof BlueprintTarget.Air) {
            DustTriggerSwapQueue.enqueue(level, PendingSwap.clearOnly(pos, original, delay).withAuthority(authority));
            return;
        }
        if (target instanceof BlueprintTarget.BlockTarget blockTarget) {
            BlockState placed = blockTarget.block().defaultBlockState();
            Direction facing;
            if (blockTarget.applyPlayerFacing()) {
                Direction side2 = useFace.getAxis().isHorizontal()
                        ? useFace
                        : player.getDirection().getOpposite();
                facing = side2;
            } else if (blockTarget.opposite()) {
                facing = placementFacing.getOpposite();
            } else {
                facing = placementFacing;
            }
            if (placed.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                placed = placed.setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
            } else if (placed.hasProperty(BlockStateProperties.FACING)) {
                placed = placed.setValue(BlockStateProperties.FACING, facing);
            }
            DustTriggerSwapQueue.enqueue(level, PendingSwap.placeBlock(pos, original, placed, delay)
                    .withAuthority(authority));
            return;
        }
        if (target instanceof BlueprintTarget.StateTarget stateTarget) {
            DustTriggerSwapQueue.enqueue(level, PendingSwap.placeBlock(pos, original, stateTarget.state(), delay)
                    .withAuthority(authority));
            return;
        }
        if (target instanceof BlueprintTarget.StackTarget stackTarget) {
            DustTriggerSwapQueue.enqueue(level, PendingSwap.dropItem(pos, original, stackTarget.stack(), delay)
                    .withAuthority(authority));
        }
    }

    private @Nullable Blueprint lookupBlueprint(Level level) {
        ResourceKey<Blueprint> key = ResourceKey.create(Blueprint.REGISTRY_KEY, this.blueprintId);
        HolderLookup.RegistryLookup<Blueprint> registry =
                level.registryAccess().lookup(Blueprint.REGISTRY_KEY).orElse(null);
        if (registry == null) {
            return null;
        }
        Holder<Blueprint> holder = registry.get(key).orElse(null);
        return holder == null ? null : holder.value();
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String getGroup() {
        return "";
    }

    @Override
    public RecipeSerializer<DustTriggerMultiblockRecipe> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public RecipeType<DustTrigger> getType() {
        return TTRecipeTypes.DUST_TRIGGER.get();
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result.copy();
    }
}
