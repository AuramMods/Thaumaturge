package com.leclowndu93150.thaumaturge.compat.curio;

import com.leclowndu93150.thaumaturge.api.items.GogglesAccess;
import com.leclowndu93150.thaumaturge.api.items.IVisDiscountGear;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import com.leclowndu93150.thaumaturge.compat.NamedAttributeModifiers;
import com.leclowndu93150.thaumaturge.content.equipment.bauble.AmuletVisItem;
import com.leclowndu93150.thaumaturge.content.equipment.bauble.VerdantCharmItem;
import com.leclowndu93150.thaumaturge.content.equipment.bauble.VoidseerCharmItem;
import com.leclowndu93150.thaumaturge.registry.TTAttributes;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.items.IItemHandlerModifiable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.event.CurioAttributeModifierEvent;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

public final class ThaumaturgeCuriosCompat {

    private ThaumaturgeCuriosCompat() {}

    public static void init(IEventBus modBus) {
        GogglesAccess.bindCurios(ThaumaturgeCuriosCompat::anyCurioMatches);
        modBus.addListener(ThaumaturgeCuriosCompat::onCommonSetup);
        MinecraftForge.EVENT_BUS.addListener(ThaumaturgeCuriosCompat::onItemAttributeModifier);
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ThaumaturgeCuriosCompat::registerCurios);
    }

    private static void registerCurios() {
        register(new ICurioItem() {},
                TTItems.GOGGLES_REVEALING.get(),
                TTItems.AMULET_MUNDANE.get(),
                TTItems.RING_MUNDANE.get(),
                TTItems.GIRDLE_MUNDANE.get(),
                TTItems.RING_APPRENTICE.get(),
                TTItems.AMULET_FANCY.get(),
                TTItems.RING_FANCY.get(),
                TTItems.GIRDLE_FANCY.get(),
                TTItems.CHARM_UNDYING.get(),
                TTItems.CLOUD_RING.get(),
                TTItems.CURIOSITY_BAND.get(),
                TTItems.FOCUS_POUCH.get());
        CuriosApi.registerCurio(TTItems.THAUMOSTATIC_HARNESS.get(), new HoverHarnessCurio());
        register(tickingCurio(),
                TTItems.AMULET_VIS.get(), TTItems.AMULET_VIS_CRAFTED.get(),
                TTItems.VERDANT_CHARM.get(), TTItems.VOIDSEER_CHARM.get());
    }

    private static void register(ICurioItem behavior, Item... items) {
        for (Item item : items) {
            CuriosApi.registerCurio(item, behavior);
        }
    }

    private static ICurioItem tickingCurio() {
        return new ICurioItem() {
            @Override
            public void curioTick(SlotContext slotContext, ItemStack stack) {
                LivingEntity wearer = slotContext.entity();
                if (stack.getItem() instanceof AmuletVisItem amulet) {
                    amulet.wornTick(stack, wearer);
                } else if (stack.getItem() instanceof VerdantCharmItem charm) {
                    charm.wornTick(stack, wearer);
                } else if (stack.getItem() instanceof VoidseerCharmItem charm) {
                    charm.wornTick(stack, wearer);
                }
            }

            @Override
            public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
                if (stack.getItem() instanceof VoidseerCharmItem && !newStack.is(stack.getItem())) {
                    VoidseerCharmItem.clearDiscount(slotContext.entity());
                }
            }
        };
    }

    public static List<ItemStack> equippedCurios(LivingEntity entity) {
        Optional<ICuriosItemHandler> invOpt = CuriosApi.getCuriosInventory(entity).resolve();
        if (invOpt.isEmpty()) {
            return List.of();
        }
        IItemHandlerModifiable equipped = invOpt.get().getEquippedCurios();
        List<ItemStack> stacks = new ArrayList<>(equipped.getSlots());
        for (int slot = 0; slot < equipped.getSlots(); slot++) {
            ItemStack stack = equipped.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                stacks.add(stack);
            }
        }
        return stacks;
    }

    public static boolean anyCurioMatches(LivingEntity entity, Predicate<ItemStack> predicate) {
        return findSlot(entity, predicate).isPresent();
    }

    public static ItemStack findCurio(LivingEntity entity, Predicate<ItemStack> predicate) {
        return findSlot(entity, predicate).map(SlotResult::stack).orElse(ItemStack.EMPTY);
    }

    private static Optional<SlotResult> findSlot(LivingEntity entity, Predicate<ItemStack> predicate) {
        return CuriosApi.getCuriosInventory(entity).resolve().flatMap(inv -> inv.findFirstCurio(predicate));
    }

    public static boolean isCurioEquipped(LivingEntity entity, Item item) {
        return anyCurioMatches(entity, stack -> stack.is(item));
    }

    public static ItemStack extractCurio(LivingEntity entity, Item item) {
        Optional<ICuriosItemHandler> invOpt = CuriosApi.getCuriosInventory(entity).resolve();
        if (invOpt.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ICuriosItemHandler inv = invOpt.get();
        Optional<SlotResult> slotOpt = inv.findFirstCurio(stack -> stack.is(item));
        if (slotOpt.isEmpty()) {
            return ItemStack.EMPTY;
        }
        int index = slotIndex(inv, slotOpt.get());
        if (index < 0) {
            return ItemStack.EMPTY;
        }
        ItemStack removed = inv.getEquippedCurios().getStackInSlot(index).copy();
        inv.getEquippedCurios().setStackInSlot(index, ItemStack.EMPTY);
        return removed;
    }

    public static boolean rechargeFirstCurio(Player player) {
        Optional<ICuriosItemHandler> invOpt = CuriosApi.getCuriosInventory(player).resolve();
        if (invOpt.isEmpty()) {
            return false;
        }
        IItemHandlerModifiable equipped = invOpt.get().getEquippedCurios();
        for (int slot = 0; slot < equipped.getSlots(); slot++) {
            if (RechargeAccess.drawFromAura(
                            player.level(), equipped.getStackInSlot(slot), player.blockPosition(), player, 1)
                    > 0) {
                return true;
            }
        }
        return false;
    }

    public static List<CurioPouchRef> equippedPouches(LivingEntity entity, Predicate<ItemStack> predicate) {
        Optional<ICuriosItemHandler> invOpt = CuriosApi.getCuriosInventory(entity).resolve();
        if (invOpt.isEmpty()) {
            return List.of();
        }
        IItemHandlerModifiable equipped = invOpt.get().getEquippedCurios();
        List<CurioPouchRef> refs = new ArrayList<>();
        for (int slot = 0; slot < equipped.getSlots(); slot++) {
            ItemStack stack = equipped.getStackInSlot(slot);
            if (!stack.isEmpty() && predicate.test(stack)) {
                refs.add(new CurioPouchRef(equipped, slot));
            }
        }
        return refs;
    }

    public record CurioPouchRef(IItemHandlerModifiable handler, int slot) {
        public ItemStack stack() {
            return handler.getStackInSlot(slot);
        }
    }

    private static int slotIndex(ICuriosItemHandler inv, SlotResult result) {
        IItemHandlerModifiable equipped = inv.getEquippedCurios();
        for (int slot = 0; slot < equipped.getSlots(); slot++) {
            if (equipped.getStackInSlot(slot) == result.stack()) {
                return slot;
            }
        }
        return -1;
    }

    private static void onItemAttributeModifier(CurioAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof IVisDiscountGear gear) {
            float contribution = (float) gear.getVisDiscount(stack) / 100;
            if (contribution != 0) {
                event.addModifier(
                        TTAttributes.VIS_DISCOUNT.get(),
                        NamedAttributeModifiers.create(
                                BuiltInRegistries.ITEM.getKey(stack.getItem()),
                                contribution,
                                AttributeModifier.Operation.ADDITION));
            }
        }
    }
}
