package com.leclowndu93150.thaumaturge.compat.ae2;

import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.stacks.GenericStack;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCrystalAccess;
import com.leclowndu93150.thaumaturge.api.recipe.ArcaneCraftingTransaction.Inspection;
import com.leclowndu93150.thaumaturge.content.essentia.advancedfurnace.BlockEntityAdvancedAlchemicalFurnace;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

final class ThaumicPatterns {
    static final String KNOWLEDGE = "ThaumaturgeKnowledge";
    static final String CASTER = "Caster";
    static final String GRID = "Grid";
    static final String CRYSTALS = "Crystals";

    private ThaumicPatterns() {}

    static ItemStack distillation(ItemStack input) {
        if (input.isEmpty()) return ItemStack.EMPTY;
        var single = input.copyWithCount(1);
        var result = BlockEntityAdvancedAlchemicalFurnace.distillationOutput(single);
        if (result.isEmpty()) return ItemStack.EMPTY;
        var outputs = new ArrayList<GenericStack>();
        for (var entry : result.entries()) {
            var key = EssentiaKey.of(entry.aspect());
            if (key == null) return ItemStack.EMPTY;
            outputs.add(new GenericStack(key, entry.amount()));
        }
        if (outputs.size() > 27) return ItemStack.EMPTY;
        return PatternDetailsHelper.encodeProcessingPattern(
                new GenericStack[]{GenericStack.fromItemStack(single)}, outputs.toArray(GenericStack[]::new));
    }

    static ItemStack knowledge(ServerPlayer caster, List<ItemStack> grid, Inspection inspection) {
        if (!inspection.successful() || inspection.requirements() == null || inspection.output().isEmpty()) {
            return ItemStack.EMPTY;
        }
        var requirements = inspection.requirements().crystalRequirements().resolve(caster.serverLevel().registryAccess());
        var crystals = AspectList.EMPTY;
        for (var demand : requirements.demands()) {
            if (demand.choices().isEmpty()) return ItemStack.EMPTY;
            crystals = crystals.add(demand.choices().get(0), demand.amount());
        }
        if (crystals.size() > 6) return ItemStack.EMPTY;

        var inputs = new ArrayList<GenericStack>();
        var outputs = new ArrayList<GenericStack>();
        var gridTag = new ListTag();
        var crystalTag = new ListTag();
        for (ItemStack stack : grid) {
            var one = stack.copyWithCount(1);
            gridTag.add(one.save(new CompoundTag()));
            if (!one.isEmpty()) inputs.add(GenericStack.fromItemStack(one));
        }
        if (inputs.isEmpty()) return ItemStack.EMPTY;
        for (var crystal : crystals.entries()) {
            var stack = EssentiaCrystalAccess.create(crystal.aspect(), crystal.amount());
            inputs.add(GenericStack.fromItemStack(stack));
            crystalTag.add(stack.save(new CompoundTag()));
        }
        outputs.add(GenericStack.fromItemStack(inspection.output()));
        for (ItemStack remainder : inspection.remainders()) {
            if (!remainder.isEmpty()) outputs.add(GenericStack.fromItemStack(remainder));
        }

        var pattern = ThaumaturgeAe2.KNOWLEDGE_PATTERN.get().encode(
                inputs.toArray(GenericStack[]::new), outputs.toArray(GenericStack[]::new));
        var knowledge = new CompoundTag();
        knowledge.putUUID(CASTER, caster.getUUID());
        knowledge.put(GRID, gridTag);
        knowledge.put(CRYSTALS, crystalTag);
        pattern.getOrCreateTag().put(KNOWLEDGE, knowledge);
        return pattern;
    }
}
