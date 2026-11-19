package com.leclowndu93150.thaumaturge.data.worldgen.pech;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.content.pech.PechTradeTable;
import com.leclowndu93150.thaumaturge.content.pech.PechTradeTable.PechTrade;
import com.leclowndu93150.thaumaturge.itemdata.ItemData;
import com.leclowndu93150.thaumaturge.itemdata.ItemDataContext;
import com.leclowndu93150.thaumaturge.itemdata.PotionData;
import com.leclowndu93150.thaumaturge.itemdata.VanillaItemData;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;

public final class PechTradeBootstrap {
    private PechTradeBootstrap() {}

    public static HolderLookup.Provider withTrades(HolderLookup.Provider registries) {
        // Crystal stacks encode their aspect into NBT as soon as they are created.
        var aspects = registries.lookupOrThrow(IAspect.REGISTRY_KEY);
        var tables = ItemDataContext.withRegistries(registries, () -> new RegistrySetBuilder()
                .add(PechTradeTable.REGISTRY_KEY, ctx -> bootstrap(ctx, aspects))
                .build(RegistryAccess.EMPTY));
        return new HolderLookup.Provider() {
            @Override
            public <T> Optional<HolderLookup.RegistryLookup<T>> lookup(
                    ResourceKey<? extends Registry<? extends T>> key) {
                return tables.lookup(key).or(() -> registries.lookup(key));
            }
        };
    }

    private static void bootstrap(BootstapContext<PechTradeTable> ctx, HolderGetter<IAspect> aspects) {
        List<PechTrade> forager = new ArrayList<>();
        add(forager, 1, TTItems.CLUSTER_IRON.get());
        add(forager, 1, TTItems.CLUSTER_GOLD.get());
        add(forager, 1, TTItems.CLUSTER_CINNABAR.get());
        add(forager, 1, TTItems.CLUSTER_QUARTZ.get());
        add(forager, 1, TTItems.CLUSTER_COPPER.get());
        add(forager, 2, Items.BLAZE_ROD);
        add(forager, 2, TTBlocks.SAPLING_GREATWOOD.get());
        add(forager, 2, Items.DRAGON_BREATH);
        add(forager, 2, Items.COMPASS);
        add(forager, 3, Items.EXPERIENCE_BOTTLE);
        add(forager, 3, Items.EXPERIENCE_BOTTLE);
        add(forager, 3, Items.GOLDEN_APPLE);
        add(forager, 4, TTItems.THAUMIUM_PICKAXE.get());
        add(forager, 4, TTItems.THAUMIUM_AXE.get());
        add(forager, 4, TTItems.THAUMIUM_HOE.get());
        add(forager, 4, Items.SPECTRAL_ARROW);
        add(forager, 5, Items.ENCHANTED_GOLDEN_APPLE);
        add(forager, 5, TTBlocks.SAPLING_SILVERWOOD.get());
        add(forager, 5, Items.TOTEM_OF_UNDYING);
        add(forager, 5, TTItems.CURIO_KNOWLEDGE.get());
        register(ctx, "forager", forager);

        List<PechTrade> mage = new ArrayList<>();
        mage.add(crystal(aspects, 1, TTAspects.AER));
        mage.add(crystal(aspects, 1, TTAspects.TERRA));
        mage.add(crystal(aspects, 1, TTAspects.IGNIS));
        mage.add(crystal(aspects, 1, TTAspects.AQUA));
        mage.add(crystal(aspects, 1, TTAspects.ORDO));
        mage.add(crystal(aspects, 1, TTAspects.PERDITIO));
        mage.add(potion(2, Potions.REGENERATION));
        mage.add(potion(2, Potions.HEALING));
        mage.add(crystal(aspects, 2, TTAspects.VITIUM));
        add(mage, 3, Items.EXPERIENCE_BOTTLE);
        add(mage, 3, Items.EXPERIENCE_BOTTLE);
        mage.add(crystal(aspects, 3, TTAspects.AURAM));
        add(mage, 3, Items.GOLDEN_APPLE);
        add(mage, 4, TTItems.CLOTH_BOOTS.get());
        add(mage, 4, TTItems.CLOTH_CHEST.get());
        add(mage, 4, TTItems.CLOTH_LEGS.get());
        add(mage, 5, Items.ENCHANTED_GOLDEN_APPLE);
        add(mage, 5, TTItems.PECH_WAND.get());
        add(mage, 5, TTItems.CURIO_KNOWLEDGE.get());
        add(mage, 5, Items.TOTEM_OF_UNDYING);
        register(ctx, "mage", mage);

        List<PechTrade> stalker = new ArrayList<>();
        for (DyeColor dye : DyeColor.values()) {
            if (dye != DyeColor.WHITE) {
                add(stalker, 1, TTBlocks.CANDLES.get(dye).get());
            }
        }
        add(stalker, 2, Items.GHAST_TEAR);
        add(stalker, 2, Items.COMPASS);
        stalker.add(book(2, Enchantments.POWER_ARROWS));
        add(stalker, 3, Items.EXPERIENCE_BOTTLE);
        add(stalker, 3, Items.EXPERIENCE_BOTTLE);
        add(stalker, 3, Items.GOLDEN_APPLE);
        add(stalker, 4, TTItems.ELDRITCH_EYE.get());
        add(stalker, 4, Items.ENCHANTED_GOLDEN_APPLE);
        add(stalker, 4, Items.SPECTRAL_ARROW);
        stalker.add(book(5, Enchantments.FLAMING_ARROWS));
        stalker.add(book(5, Enchantments.INFINITY_ARROWS));
        add(stalker, 5, TTItems.CURIO_KNOWLEDGE.get());
        register(ctx, "stalker", stalker);
    }

    private static void register(BootstapContext<PechTradeTable> ctx, String name, List<PechTrade> trades) {
        ctx.register(ResourceKey.create(PechTradeTable.REGISTRY_KEY, TTIds.rl(name)), new PechTradeTable(trades));
    }

    private static void add(List<PechTrade> trades, int tier, ItemLike item) {
        trades.add(new PechTrade(tier, new ItemStack(item)));
    }

    private static PechTrade crystal(HolderGetter<IAspect> aspects, int tier, ResourceKey<IAspect> aspect) {
        ItemStack stack = new ItemStack(TTItems.ESSENTIA_CRYSTAL.get());
        ItemData.set(stack, TTDataComponents.CRYSTAL_ASPECT.get(), new AspectInstance(aspects.getOrThrow(aspect), 1));
        return new PechTrade(tier, stack);
    }

    private static PechTrade potion(int tier, Potion potion) {
        ItemStack stack = new ItemStack(Items.POTION);
        ItemData.set(stack, VanillaItemData.POTION_CONTENTS, new PotionData(potion));
        return new PechTrade(tier, stack);
    }

    private static PechTrade book(int tier, Enchantment enchantment) {
        return new PechTrade(tier, EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, 1)));
    }
}
