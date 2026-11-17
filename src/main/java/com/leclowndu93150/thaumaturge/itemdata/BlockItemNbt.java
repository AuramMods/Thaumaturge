package com.leclowndu93150.thaumaturge.itemdata;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

/** Preserve unconsumed item changes, equivalent to the baseline block entity's component map. */
public final class BlockItemNbt {
    private static final Map<String, String> NATIVE_FIELDS = Map.ofEntries(
            Map.entry("Damage", "damage"), Map.entry("Enchantments", "enchantments"),
            Map.entry("StoredEnchantments", "stored_enchantments"), Map.entry("AttributeModifiers", "attribute_modifiers"),
            Map.entry("Unbreakable", "unbreakable"), Map.entry("CanDestroy", "can_break"),
            Map.entry("CanPlaceOn", "can_place_on"), Map.entry("RepairCost", "repair_cost"),
            Map.entry("CustomModelData", "custom_model_data"), Map.entry("BlockStateTag", "block_state"),
            Map.entry("Trim", "trim"), Map.entry("Potion", "potion_contents"),
            Map.entry("CustomPotionEffects", "potion_contents"), Map.entry("CustomPotionColor", "potion_contents"),
            Map.entry("SkullOwner", "profile"), Map.entry("ChargedProjectiles", "charged_projectiles"),
            Map.entry("EntityTag", "entity_data"), Map.entry("Fireworks", "fireworks"), Map.entry("Explosion", "firework_explosion"));

    private BlockItemNbt() {}

    public static CompoundTag capture(CompoundTag itemTag, Set<ResourceLocation> consumed) {
        if (itemTag == null) return new CompoundTag();
        CompoundTag saved = itemTag.copy();
        // Vanilla always consumes block-entity data; unconsumed removals are not
        // retained by 1.21.1 DataComponentPatch.split().added().
        saved.remove("BlockEntityTag");
        ItemNbt.fields(saved, new CompoundTag());
        if (saved.get(ItemData.TAG) instanceof CompoundTag fields) {
            for (String name : Set.copyOf(fields.getAllKeys())) {
                if (name.startsWith("!")) fields.remove(name);
            }
            if (fields.isEmpty()) saved.remove(ItemData.TAG);
        }
        return select(saved, new ItemDataSelection(Optional.empty(), consumed));
    }

    public static CompoundTag select(CompoundTag source, ItemDataSelection selection) {
        ItemNbt.fields(source, new CompoundTag());
        CompoundTag result = new CompoundTag();
        for (String name : source.getAllKeys()) {
            Tag value = source.get(name);
            if (name.equals(ItemData.TAG) && value instanceof CompoundTag fields) {
                CompoundTag selected = new CompoundTag();
                for (String field : fields.getAllKeys()) {
                    String id = field.startsWith("!") ? field.substring(1) : field;
                    ResourceLocation parsed = ResourceLocation.tryParse(id);
                    if (parsed == null) throw new IllegalArgumentException("Invalid preserved item field " + field);
                    if (selection.includes(parsed)) selected.put(field, fields.get(field).copy());
                }
                if (!selected.isEmpty()) result.put(name, selected);
            } else if (name.equals("display") && value instanceof CompoundTag fields) {
                CompoundTag selected = new CompoundTag();
                for (String field : fields.getAllKeys()) {
                    String id = switch (field) {
                        case "Name" -> "custom_name";
                        case "Lore" -> "lore";
                        case "color" -> "dyed_color";
                        case "MapColor" -> "map_color";
                        default -> "custom_data";
                    };
                    if (selection.includes(vanilla(id))) selected.put(field, fields.get(field).copy());
                }
                if (!selected.isEmpty()) result.put(name, selected);
            } else if (name.equals("HideFlags")) {
                int flags = selectedHideFlags(source.getInt(name), selection);
                if (flags != 0) result.putInt(name, flags);
            } else if (selection.includes(vanilla(NATIVE_FIELDS.getOrDefault(name, "custom_data")))) {
                result.put(name, value.copy());
            }
        }
        return result;
    }

    private static int selectedHideFlags(int flags, ItemDataSelection selection) {
        String[] fields = {"enchantments", "attribute_modifiers", "unbreakable", "can_break", "can_place_on",
                "stored_enchantments", "dyed_color", "trim"};
        int selected = 0;
        for (int i = 0; i < fields.length; i++) {
            boolean include = selection.includes(vanilla(fields[i]));
            if (i == 5) include |= selection.includes(vanilla("potion_contents")) || selection.includes(vanilla("banner_patterns"));
            if (include) selected |= flags & (1 << i);
        }
        if (selection.includes(vanilla("custom_data"))) selected |= flags & ~255;
        return selected;
    }

    private static ResourceLocation vanilla(String id) { return ResourceLocation.withDefaultNamespace(id); }
}
