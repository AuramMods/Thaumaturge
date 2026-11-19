package com.leclowndu93150.thaumaturge.content.entity.loot;

import com.leclowndu93150.thaumaturge.registry.TTLootTypes;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.providers.number.LootNumberProviderType;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

/** Calculates the chance for a loot roll using the attacker's Looting level. */
public final class LootingChanceValue implements NumberProvider {
    public static final MapCodec<LootingChanceValue> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.floatRange(0.0F, 1.0F).fieldOf("unenchanted_chance").forGetter(value -> value.unenchantedChance),
                    Curve.CODEC.fieldOf("enchanted_chance").forGetter(value -> value.enchantedChance))
            .apply(instance, LootingChanceValue::new));

    private final float unenchantedChance;
    private final Curve enchantedChance;

    private LootingChanceValue(float unenchantedChance, Curve enchantedChance) {
        this.unenchantedChance = unenchantedChance;
        this.enchantedChance = enchantedChance;
    }

    public static LootingChanceValue increasing(float chance, float bonus) {
        return linear(chance, chance + bonus, bonus);
    }

    public static LootingChanceValue linear(float unenchantedChance, float firstLevelChance, float perLevelAboveFirst) {
        return new LootingChanceValue(unenchantedChance, new Linear(firstLevelChance, perLevelAboveFirst));
    }

    public static LootingChanceValue byLevel(float unenchantedChance, List<Float> values, float fallback) {
        return new LootingChanceValue(unenchantedChance, new ByLevel(List.copyOf(values), fallback));
    }

    @Override
    public LootNumberProviderType getType() {
        return TTLootTypes.LOOTING_CHANCE.get();
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.KILLER_ENTITY);
    }

    @Override
    public float getFloat(LootContext context) {
        int looting = 0;
        if (context.getParamOrNull(LootContextParams.KILLER_ENTITY) instanceof LivingEntity attacker) {
            looting = EnchantmentHelper.getEnchantmentLevel(Enchantments.MOB_LOOTING, attacker);
        }
        return looting > 0 ? enchantedChance.chance(looting) : unenchantedChance;
    }

    private sealed interface Curve permits Linear, ByLevel {
        Codec<Curve> CODEC = Codec.either(Linear.CODEC.codec(), ByLevel.CODEC.codec()).xmap(
                value -> value.map(linear -> linear, lookup -> lookup),
                curve -> curve instanceof Linear linear ? Either.left(linear) : Either.right((ByLevel) curve));

        float chance(int looting);
    }

    private record Linear(float base, float perLevelAboveFirst) implements Curve {
        private static final MapCodec<Linear> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                        Codec.FLOAT.fieldOf("base").forGetter(Linear::base),
                        Codec.FLOAT.fieldOf("per_level_above_first").forGetter(Linear::perLevelAboveFirst))
                .apply(instance, Linear::new));

        @Override
        public float chance(int looting) {
            return base + perLevelAboveFirst * (looting - 1);
        }
    }

    private record ByLevel(List<Float> values, float fallback) implements Curve {
        private static final MapCodec<ByLevel> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                        Codec.FLOAT.listOf().fieldOf("values").forGetter(ByLevel::values),
                        Codec.FLOAT.fieldOf("fallback").forGetter(ByLevel::fallback))
                .apply(instance, ByLevel::new));

        @Override
        public float chance(int looting) {
            return looting <= values.size() ? values.get(looting - 1) : fallback;
        }
    }
}
