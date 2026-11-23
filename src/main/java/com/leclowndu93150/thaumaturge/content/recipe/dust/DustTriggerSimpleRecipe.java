package com.leclowndu93150.thaumaturge.content.recipe.dust;

import com.leclowndu93150.thaumaturge.itemdata.ItemStackCodecs;

import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.leclowndu93150.thaumaturge.recipe.AbstractRecipe;
import com.leclowndu93150.thaumaturge.serialization.stream.VanillaStreamCodecs;
import com.leclowndu93150.thaumaturge.stage.StageAuthorization;
import com.leclowndu93150.thaumaturge.stage.StageRules;

import com.leclowndu93150.thaumaturge.api.recipe.DustTrigger;
import com.leclowndu93150.thaumaturge.api.recipe.DustTriggerInput;
import com.leclowndu93150.thaumaturge.api.recipe.DustTriggerPlacement;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.content.recipe.SimpleRecipeSerializer;
import com.leclowndu93150.thaumaturge.registry.TTRecipeTypes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.ByteBufCodecs;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public final class DustTriggerSimpleRecipe extends AbstractRecipe<DustTriggerInput> implements DustTrigger {
    public static final MapCodec<DustTriggerSimpleRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                    BuiltInRegistries.BLOCK.byNameCodec().fieldOf("target").forGetter(r -> r.target),
                    ItemStackCodecs.CODEC.fieldOf("result").forGetter(r -> r.result),
                    Codecs.optionalFieldOf(ResearchGate.CODEC, "research").forGetter(r -> r.research))
            .apply(i, DustTriggerSimpleRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DustTriggerSimpleRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.registry(Registries.BLOCK),
                    r -> r.target,
                    VanillaStreamCodecs.ITEM_STACK,
                    r -> r.result,
                    ByteBufCodecs.optional(ResearchGate.STREAM_CODEC),
                    r -> r.research,
                    DustTriggerSimpleRecipe::new);

    public static final RecipeSerializer<DustTriggerSimpleRecipe> SERIALIZER =
            new SimpleRecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private final Block target;
    private final ItemStack result;
    private final Optional<ResearchGate> research;

    public DustTriggerSimpleRecipe(Block target, ItemStack result, Optional<ResearchGate> research) {
        this.target = target;
        this.result = result;
        this.research = research;
    }

    public Block target() {
        return this.target;
    }

    public ItemStack result() {
        return this.result.copy();
    }

    @Override
    public Optional<ResearchGate> researchGate() {
        return this.research;
    }

    @Override
    public boolean matches(DustTriggerInput input, Level level) {
        return input.clicked().getBlock() == this.target;
    }

    @Override
    public ItemStack assemble(DustTriggerInput input, HolderLookup.Provider registries) {
        return this.result.copy();
    }

    @Override
    public void execute(
            DustTriggerInput input,
            Player player,
            @org.jspecify.annotations.Nullable DustTriggerPlacement placement,
            Direction useFace) {
        if (!(input.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        var target = stageTarget(input);
        if (DustTriggerSwapQueue.isBlocked(serverLevel, input.pos()) || !StageRules.check(serverLevel, target, player)) {
            return;
        }
        DustTriggerSwapQueue.enqueue(serverLevel, PendingSwap.dropItem(input.pos(), input.clicked(), this.result, 50)
                .withAuthority(new StageAuthorization(target, player.getUUID())));
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return this.result.copy();
    }

    @Override
    public RecipeSerializer<DustTriggerSimpleRecipe> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public RecipeType<DustTrigger> getType() {
        return TTRecipeTypes.DUST_TRIGGER.get();
    }
}
