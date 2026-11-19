package com.leclowndu93150.thaumaturge.content.entity.loot;

import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import com.leclowndu93150.thaumaturge.registry.TTLootTypes;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

/** Adds drops based on the same equipped Looting level used by the drop chances. */
public final class LootingCountFunction extends LootItemConditionalFunction {
    private final NumberProvider count;

    private LootingCountFunction(LootItemCondition[] conditions, NumberProvider count) {
        super(conditions);
        this.count = count;
    }

    public static Builder<?> lootingMultiplier(NumberProvider count) {
        return simpleBuilder(conditions -> new LootingCountFunction(conditions, count));
    }

    @Override
    public LootItemFunctionType getType() {
        return TTLootTypes.LOOTING_COUNT.get();
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return Sets.union(Set.of(LootContextParams.KILLER_ENTITY), count.getReferencedContextParams());
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        int looting = LootingChanceValue.lootingLevel(context);
        if (looting != 0) {
            stack.grow(Math.round(looting * count.getFloat(context)));
        }
        return stack;
    }

    public static final class Serializer extends LootItemConditionalFunction.Serializer<LootingCountFunction> {
        @Override
        public void serialize(JsonObject json, LootingCountFunction value, JsonSerializationContext context) {
            super.serialize(json, value, context);
            json.add("count", context.serialize(value.count, NumberProvider.class));
        }

        @Override
        public LootingCountFunction deserialize(
                JsonObject json, JsonDeserializationContext context, LootItemCondition[] conditions) {
            NumberProvider count = GsonHelper.getAsObject(json, "count", context, NumberProvider.class);
            if (count == null) throw new JsonSyntaxException("Missing number provider for count");
            return new LootingCountFunction(conditions, count);
        }
    }
}
