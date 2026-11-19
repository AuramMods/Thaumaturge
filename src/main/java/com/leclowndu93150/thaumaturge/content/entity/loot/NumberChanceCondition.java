package com.leclowndu93150.thaumaturge.content.entity.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import com.leclowndu93150.thaumaturge.registry.TTLootTypes;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

/** A random chance evaluated from a number provider for each loot roll. */
public record NumberChanceCondition(NumberProvider chance) implements LootItemCondition {
    public static LootItemCondition.Builder randomChance(NumberProvider chance) {
        return () -> new NumberChanceCondition(chance);
    }

    @Override
    public LootItemConditionType getType() {
        return TTLootTypes.NUMBER_CHANCE.get();
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return chance.getReferencedContextParams();
    }

    @Override
    public boolean test(LootContext context) {
        float probability = chance.getFloat(context);
        return context.getRandom().nextFloat() < probability;
    }

    public static final class Serializer implements net.minecraft.world.level.storage.loot.Serializer<NumberChanceCondition> {
        @Override
        public void serialize(JsonObject json, NumberChanceCondition value, JsonSerializationContext context) {
            json.add("chance", context.serialize(value.chance(), NumberProvider.class));
        }

        @Override
        public NumberChanceCondition deserialize(JsonObject json, JsonDeserializationContext context) {
            NumberProvider chance = GsonHelper.getAsObject(json, "chance", context, NumberProvider.class);
            if (chance == null) {
                throw new JsonSyntaxException("Missing number provider for chance");
            }
            return new NumberChanceCondition(chance);
        }
    }
}
