package com.leclowndu93150.thaumaturge.content.pech;

import com.leclowndu93150.thaumaturge.content.entity.EntityPech;
import com.leclowndu93150.thaumaturge.content.world.PlayerReach;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import com.leclowndu93150.thaumaturge.stage.StageRules;
import com.leclowndu93150.thaumaturge.stage.StageTarget;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class PechBarter {
    private static final int PERCENT = 100;
    private static final int SOUR_BASE_PERCENT = 1;
    private static final int SOUR_BUDGET_DIVISOR = 2;
    private static final float HAGGLE_UP_CHANCE = 0.2F;
    private static final float HAGGLE_DOWN_CHANCE = 0.4F;
    private static final int HAGGLE_STEPS = 3;
    private static final int MAX_TIER = 5;
    private static final int HOARD_TIER = 1;
    private static final float HOARD_PAYOUT_CHANCE = 0.5F;
    private static final int RISKY_TIER = 4;
    private static final float RISKY_MISS_CHANCE = 0.5F;
    private static final int HOARD_TAKE = 1;
    static final double REACH_BUFFER = 4.0;

    private PechBarter() {}

    public static List<ItemStack> haggle(
            EntityPech pech, ItemStack offered, RandomSource random, HolderLookup.Provider registries) {
        if (pech.level() instanceof ServerLevel level && !StageRules.allows(level, target(pech, offered), null)) {
            return List.of();
        }
        return rollPayout(pech, offered, random, registries);
    }

    public static Optional<List<ItemStack>> tryHaggle(EntityPech pech, Player actor, ItemStack offered) {
        if (!(pech.level() instanceof ServerLevel level) || actor.level() != level
                || !pech.isAlive() || !pech.isDomesticated() || !pech.isPrizedItem(offered)
                || !PlayerReach.canInteractWithEntity(actor, pech, REACH_BUFFER)
                || !StageRules.check(level, target(pech, offered), actor)) {
            return Optional.empty();
        }
        return Optional.of(rollPayout(pech, offered, pech.getRandom(), level.registryAccess()));
    }

    private static StageTarget target(EntityPech pech, ItemStack offered) {
        var table = PechTrades.tableKey(pech.variant());
        var target = StageTarget.recipe(StageTarget.PECH_TRADE, table.location())
                .withSubject(table).withSubject(pech.getType().builtInRegistryHolder().key());
        return offered.getItemHolder().unwrapKey().map(target::withSubject).orElse(target);
    }

    private static List<ItemStack> rollPayout(
            EntityPech pech, ItemStack offered, RandomSource random, HolderLookup.Provider registries) {
        int budget = pech.getValue(offered);
        maybeTurnSour(pech, budget, random);
        budget += swing(random);
        List<PechTrades.Entry> trades = PechTrades.tradesFor(pech.variant(), registries);
        List<ItemStack> payout = new ArrayList<>();
        while (budget > 0) {
            int tier = roundSize(budget, random);
            budget -= tier;
            ItemStack paid = payRound(pech, tier, trades, random);
            if (!paid.isEmpty()) {
                payout.add(paid);
            }
        }
        return payout;
    }

    private static void maybeTurnSour(EntityPech pech, int budget, RandomSource random) {
        int sourPercent = budget / SOUR_BUDGET_DIVISOR + SOUR_BASE_PERCENT;
        if (random.nextInt(PERCENT) >= sourPercent) {
            return;
        }
        pech.setDomesticated(false);
        pech.level()
                .playSound(
                        null,
                        pech.getX(),
                        pech.getY(),
                        pech.getZ(),
                        TTSounds.PECH_TRADE.get(),
                        pech.getSoundSource(),
                        pech.getSoundVolume(),
                        pech.getVoicePitch());
    }

    static int swing(RandomSource random) {
        float mood = random.nextFloat();
        if (mood < HAGGLE_UP_CHANCE) {
            return random.nextInt(HAGGLE_STEPS);
        }
        if (mood < HAGGLE_UP_CHANCE + HAGGLE_DOWN_CHANCE) {
            return -random.nextInt(HAGGLE_STEPS);
        }
        return 0;
    }

    static int roundSize(int budget, RandomSource random) {
        int minimumTier = (budget + 1) / 2;
        int gamble = 1 + random.nextInt(budget);
        return Math.min(MAX_TIER, Math.max(minimumTier, gamble));
    }

    private static ItemStack payRound(EntityPech pech, int tier, List<PechTrades.Entry> trades, RandomSource random) {
        if (tier == HOARD_TIER && random.nextFloat() < HOARD_PAYOUT_CHANCE) {
            ItemStack fromHoard = takeFromHoard(pech.loot, random);
            if (!fromHoard.isEmpty()) {
                return fromHoard;
            }
        }
        if (tier >= RISKY_TIER && random.nextFloat() < RISKY_MISS_CHANCE) {
            return ItemStack.EMPTY;
        }
        return pickOfTier(trades, tier, random);
    }

    private static ItemStack takeFromHoard(List<ItemStack> hoard, RandomSource random) {
        List<ItemStack> stocked =
                hoard.stream().filter(stack -> !stack.isEmpty()).toList();
        if (stocked.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return stocked.get(random.nextInt(stocked.size())).split(HOARD_TAKE);
    }

    private static ItemStack pickOfTier(List<PechTrades.Entry> trades, int tier, RandomSource random) {
        List<PechTrades.Entry> matching =
                trades.stream().filter(entry -> entry.tier() == tier).toList();
        if (matching.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return matching.get(random.nextInt(matching.size())).stack().copy();
    }
}
