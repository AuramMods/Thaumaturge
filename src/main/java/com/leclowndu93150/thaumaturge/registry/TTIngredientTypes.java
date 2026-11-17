package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.spell.item.SpellPartIngredient;
import com.leclowndu93150.thaumaturge.recipe.ingredient.ComponentIngredient;
import com.leclowndu93150.thaumaturge.recipe.ingredient.IngredientRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;

public final class TTIngredientTypes {
    public static final IngredientRegistration INGREDIENT_TYPES = new IngredientRegistration(TTIds.MODID);

    public static final IngredientRegistration.Entry<SpellPartIngredient> SPELL_PART =
            INGREDIENT_TYPES.register("focus_element", () -> SpellPartIngredient.SERIALIZER);
    private static final IngredientRegistration.Entry<ComponentIngredient> COMPONENTS = INGREDIENT_TYPES.register(
            ResourceLocation.fromNamespaceAndPath("neoforge", "components"), () -> ComponentIngredient.SERIALIZER);

    private TTIngredientTypes() {}

    public static void register(IEventBus modBus) {
        INGREDIENT_TYPES.register(modBus);
    }
}
