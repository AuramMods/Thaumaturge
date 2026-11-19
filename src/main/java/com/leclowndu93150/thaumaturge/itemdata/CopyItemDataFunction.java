package com.leclowndu93150.thaumaturge.itemdata;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/** Forge 1.20.1 loot function for the baseline's copy_components block-entity transfers. */
public final class CopyItemDataFunction extends LootItemConditionalFunction {
    private final ItemDataSelection selection;

    public CopyItemDataFunction(LootItemCondition[] conditions, ItemDataSelection selection) {
        super(conditions);
        this.selection = selection;
    }

    @Override public LootItemFunctionType getType() { return ItemDataLoot.COPY_ITEM_DATA.get(); }
    @Override public Set<LootContextParam<?>> getReferencedContextParams() { return Set.of(LootContextParams.BLOCK_ENTITY); }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        var blockEntity = context.getParamOrNull(LootContextParams.BLOCK_ENTITY);
        if (blockEntity != null && !stack.isEmpty()) {
            ItemDataContext.withRegistries(context.getLevel().registryAccess(), () -> {
                BlockItemDataBridge.copyToItem(blockEntity, stack, selection);
                return null;
            });
        }
        return stack;
    }

    public static Builder copyBlockEntity() { return new Builder(); }

    public static final class Builder extends LootItemConditionalFunction.Builder<Builder> {
        private Set<ResourceLocation> include;
        private final Set<ResourceLocation> exclude = new LinkedHashSet<>();

        public Builder include(ItemDataKey<?> key) {
            if (include == null) include = new LinkedHashSet<>();
            include.add(key.id());
            return this;
        }

        public Builder exclude(ItemDataKey<?> key) { exclude.add(key.id()); return this; }
        @Override protected Builder getThis() { return this; }
        @Override public LootItemFunction build() {
            return new CopyItemDataFunction(getConditions(), new ItemDataSelection(Optional.ofNullable(include), exclude));
        }
    }

    public static final class Serializer extends LootItemConditionalFunction.Serializer<CopyItemDataFunction> {
        @Override
        public void serialize(JsonObject json, CopyItemDataFunction function, JsonSerializationContext context) {
            super.serialize(json, function, context);
            function.selection.serialize(json);
        }

        @Override
        public CopyItemDataFunction deserialize(JsonObject json, JsonDeserializationContext context, LootItemCondition[] conditions) {
            return new CopyItemDataFunction(conditions, ItemDataSelection.deserialize(json));
        }
    }
}
