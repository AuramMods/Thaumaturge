package com.leclowndu93150.thaumaturge.data.loot;

import com.leclowndu93150.thaumaturge.data.lang.LoreBookTextEn;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetNbtFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

public final class LoreBookLoot {
    private static final String TITLE = "A Message to the World";
    private static final String AUTHOR = "A Thaumaturge";

    private LoreBookLoot() {}

    public static LootTable.Builder table() {
        var pages = new ListTag();
        for (int page = 1; page <= LoreBookTextEn.pageCount(); page++) {
            var content = Component.translatable(LoreBookTextEn.pageKey(page));
            pages.add(StringTag.valueOf(Component.Serializer.toJson(content)));
        }
        var book = new CompoundTag();
        book.putString("title", TITLE);
        book.putString("author", AUTHOR);
        book.put("pages", pages);
        return LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(Items.WRITTEN_BOOK).apply(SetNbtFunction.setTag(book))));
    }
}
