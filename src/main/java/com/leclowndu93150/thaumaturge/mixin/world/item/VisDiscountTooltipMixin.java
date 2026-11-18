package com.leclowndu93150.thaumaturge.mixin.world.item;

import com.leclowndu93150.thaumaturge.registry.TTAttributes.VisDiscountAttribute;
import java.util.Map;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(ItemStack.class)
abstract class VisDiscountTooltipMixin {
    @Redirect(method = "getTooltipLines",
            slice = @Slice(
                    from = @At(value = "FIELD", target = "Lnet/minecraft/world/item/ItemStack$TooltipPart;MODIFIERS:Lnet/minecraft/world/item/ItemStack$TooltipPart;"),
                    to = @At(value = "FIELD", target = "Lnet/minecraft/world/item/ItemStack$TooltipPart;UNBREAKABLE:Lnet/minecraft/world/item/ItemStack$TooltipPart;")),
            at = @At(value = "INVOKE", target = "Ljava/util/Map$Entry;getValue()Ljava/lang/Object;", remap = false))
    private Object thaumaturge$showVisDiscountAsPercent(Map.Entry<?, ?> entry) {
        Object value = entry.getValue();
        if (entry.getKey() instanceof VisDiscountAttribute && value instanceof AttributeModifier modifier
                && modifier.getOperation() == AttributeModifier.Operation.ADDITION) {
            // Native tooltips display multiplicative operations in percent. This modifier is only used for display.
            return new AttributeModifier(modifier.getId(), modifier.getName(), modifier.getAmount(),
                    AttributeModifier.Operation.MULTIPLY_BASE);
        }
        return value;
    }
}
