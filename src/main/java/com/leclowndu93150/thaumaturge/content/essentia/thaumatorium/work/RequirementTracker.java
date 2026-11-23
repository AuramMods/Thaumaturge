package com.leclowndu93150.thaumaturge.content.essentia.thaumatorium.work;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.AspectRequirements;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.content.recipe.crucible.CrucibleRecipe;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jspecify.annotations.Nullable;

public final class RequirementTracker {
    private static final Comparator<AspectInstance> NEED_ORDER = Comparator.comparing(
                    (AspectInstance need) -> aspectId(need).getNamespace())
            .thenComparing(need -> aspectId(need).getPath());
    private static final int NOTHING = 0;

    private final List<AspectInstance> needs = new ArrayList<>();
    private AspectList required = AspectList.EMPTY;
    private AspectList received = AspectList.EMPTY;
    private AspectRequirements.@Nullable Resolved alternatives;

    private static ResourceLocation aspectId(AspectInstance instance) {
        return instance.aspect().unwrapKey().map(ResourceKey::location).orElseThrow();
    }

    public void planFor(CrucibleRecipe recipe) {
        planFor(recipe, recipe.recipeRegistryLookup());
    }

    public void planFor(CrucibleRecipe recipe, HolderLookup.@Nullable Provider registries) {
        required = recipe.aspects();
        alternatives = recipe.requirements().hasTags()
                ? recipe.requirements().resolve(Objects.requireNonNull(registries))
                : null;
        needs.clear();
        needs.addAll(required.entries());
        needs.sort(NEED_ORDER);
    }

    public void clearPlan() {
        required = AspectList.EMPTY;
        alternatives = null;
        needs.clear();
    }

    public @Nullable Holder<IAspect> firstUnmet() {
        if (alternatives != null) {
            return pullChoices().stream().findFirst().orElse(null);
        }
        for (AspectInstance need : needs) {
            if (received.amountOf(need.aspect()) < need.amount()) {
                return need.aspect();
            }
        }
        return null;
    }

    public int shortfall(Holder<IAspect> aspect) {
        if (alternatives != null) {
            if (alternatives.hasEmptyDemand() || !alternatives.choices().contains(aspect)) return NOTHING;
            int paid = alternatives.payment(received).totalAmount();
            int remaining = Math.min(alternatives.amount() - paid, Integer.MAX_VALUE - received.amountOf(aspect));
            return remaining <= NOTHING
                    ? NOTHING
                    : alternatives.payment(received.add(aspect, remaining)).totalAmount() - paid;
        }
        return Math.max(NOTHING, required.amountOf(aspect) - received.amountOf(aspect));
    }

    public List<Holder<IAspect>> pullChoices() {
        if (alternatives != null) {
            return alternatives.choices().stream().filter(aspect -> shortfall(aspect) > NOTHING).toList();
        }
        var missing = firstUnmet();
        return missing == null ? List.of() : List.of(missing);
    }

    public int accept(Holder<IAspect> aspect, int offered) {
        int accepted = Math.min(offered, shortfall(aspect));
        if (accepted <= NOTHING) {
            return NOTHING;
        }
        received = received.add(aspect, accepted);
        return accepted;
    }

    public AspectList received() {
        return received;
    }

    public void restoreReceived(AspectList stored) {
        received = stored;
    }

    public void pay(AspectList payment) {
        received = received.subtract(payment);
    }

    public void clearReceived() {
        received = AspectList.EMPTY;
    }
}
