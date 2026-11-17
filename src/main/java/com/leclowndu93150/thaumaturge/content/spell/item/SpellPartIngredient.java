package com.leclowndu93150.thaumaturge.content.spell.item;

import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.leclowndu93150.thaumaturge.api.spell.FocusTier;
import com.leclowndu93150.thaumaturge.api.spell.Spell;
import com.leclowndu93150.thaumaturge.api.spell.SpellNode;
import com.leclowndu93150.thaumaturge.api.spell.Spells;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPart;
import com.leclowndu93150.thaumaturge.recipe.ingredient.CodecIngredientSerializer;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;

public final class SpellPartIngredient extends AbstractIngredient {
    private final Set<ResourceKey<SpellPart>> parts;
    public static final MapCodec<SpellPartIngredient> CODEC =
            RecordCodecBuilder.mapCodec(i -> i.group(Codecs.validate(Codecs.withAlternative(
                                    ResourceKey.codec(SpellPart.REGISTRY_KEY).listOf(),
                                    ResourceKey.codec(SpellPart.REGISTRY_KEY),
                                    List::of), list -> list.isEmpty()
                                    ? DataResult.error(() -> "Spell part ingredient needs at least one part")
                                    : DataResult.success(list))
                            .fieldOf("parts")
                            .forGetter(ingredient -> List.copyOf(ingredient.parts())))
                    .apply(i, list -> new SpellPartIngredient(Set.copyOf(list))));

    public static final CodecIngredientSerializer<SpellPartIngredient> SERIALIZER = new CodecIngredientSerializer<>(CODEC);

    public SpellPartIngredient(Set<ResourceKey<SpellPart>> parts) {
        this.parts = Set.copyOf(parts);
    }

    public Set<ResourceKey<SpellPart>> parts() { return parts; }

    public static Ingredient of(List<ResourceKey<SpellPart>> parts) {
        return new SpellPartIngredient(Set.copyOf(parts));
    }

    @Override
    public boolean test(ItemStack stack) {
        Spell spell = Spells.spellOf(stack);
        if (spell == null) {
            return false;
        }
        Set<ResourceKey<SpellPart>> present = new HashSet<>();
        spell.root().forEach(node -> present.add(node.part()));
        return present.containsAll(parts);
    }

    @Override
    public ItemStack[] getItems() {
        return BuiltInRegistries.ITEM
                .holders()
                .filter(holder -> holder.getData(FocusTier.DATA_MAP) != null)
                .map(holder -> new ItemStack(holder.value()))
                .toArray(ItemStack[]::new);
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IIngredientSerializer<? extends Ingredient> getSerializer() {
        return SERIALIZER;
    }

    @Override public boolean isEmpty() { return false; }
    @Override public JsonElement toJson() { return SERIALIZER.toJson(this); }
    @Override public boolean equals(Object other) { return other instanceof SpellPartIngredient ingredient && parts.equals(ingredient.parts); }
    @Override public int hashCode() { return parts.hashCode(); }
    @Override public String toString() { return "SpellPartIngredient[parts=" + parts + ']'; }

    public List<ItemStack> displayStacks() {
        List<ResourceKey<SpellPart>> ordered = new ArrayList<>(parts);
        ordered.sort((a, b) -> a.location().compareTo(b.location()));
        SpellNode chain = null;
        for (int index = ordered.size() - 1; index >= 0; index--) {
            SpellNode node = SpellNode.of(ordered.get(index));
            chain = chain == null ? node : node.then(chain);
        }
        Spell sample = Spell.empty().withRoot(SpellNode.of(Spell.ORIGIN).then(chain));
        return Arrays.stream(getItems())
                .map(stack -> {
                    Spells.setSpell(stack, sample);
                    return stack;
                })
                .toList();
    }
}
