package com.leclowndu93150.thaumaturge.itemdata;

import static org.junit.jupiter.api.Assertions.*;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.StringTag;
import org.junit.jupiter.api.Test;

class ItemNbtTest {
    @Test
    void subsetMatchingIsDirectionalAndDamageCanBeIgnoredSeparately() {
        CompoundTag reference = new CompoundTag();
        reference.putInt("Damage", 3);
        CompoundTag candidate = reference.copy();
        CompoundTag display = new CompoundTag();
        display.putString("Name", "named");
        candidate.put("display", display);
        var plain = ItemNbt.fields(reference, new CompoundTag());
        var named = ItemNbt.fields(candidate, new CompoundTag());
        assertTrue(ItemNbt.matches(plain, named, false, true));
        assertFalse(ItemNbt.matches(named, plain, false, true));
        assertFalse(ItemNbt.matches(plain, named, false, false));
        CompoundTag worn = reference.copy();
        worn.putInt("Damage", 9);
        assertFalse(ItemNbt.matches(plain, ItemNbt.fields(worn, new CompoundTag()), false, false));
        assertTrue(ItemNbt.matches(plain, ItemNbt.fields(worn, new CompoundTag()), true, false));
    }

    @Test
    void defaultsParticipateInMatchingAndRemovalsStayAbsent() {
        CompoundTag defaults = new CompoundTag();
        defaults.putInt("thaumaturge:warp", 2);
        var fresh = ItemNbt.fields(null, defaults);
        CompoundTag values = new CompoundTag();
        values.putInt("thaumaturge:warp", 3);
        CompoundTag tag = new CompoundTag();
        tag.put(ItemData.TAG, values);
        assertFalse(ItemNbt.matches(fresh, ItemNbt.fields(tag, defaults), false, true));
        values.putInt("thaumaturge:warp", 2);
        assertTrue(ItemNbt.matches(fresh, ItemNbt.fields(tag, defaults), false, false));
        values.remove("thaumaturge:warp");
        values.putBoolean("!thaumaturge:warp", true);
        var removed = ItemNbt.fields(tag, defaults);
        assertFalse(removed.containsKey("item:thaumaturge:warp"));
        assertFalse(ItemNbt.matches(fresh, removed, false, true));
    }

    @Test
    void customFieldsAreIndependentAndNestedValuesRequireWholeValueEquality() {
        CompoundTag values = new CompoundTag();
        CompoundTag aspects = new CompoundTag();
        aspects.putInt("aer", 1);
        values.put("thaumaturge:aspects", aspects);
        CompoundTag reference = new CompoundTag();
        reference.put(ItemData.TAG, values);
        CompoundTag candidate = reference.copy();
        candidate.getCompound(ItemData.TAG).putInt("thaumaturge:charge", 7);
        assertTrue(ItemNbt.matches(ItemNbt.fields(reference, new CompoundTag()), ItemNbt.fields(candidate, new CompoundTag()), false, true));
        candidate.getCompound(ItemData.TAG).getCompound("thaumaturge:aspects").putInt("ignis", 2);
        assertFalse(ItemNbt.matches(ItemNbt.fields(reference, new CompoundTag()), ItemNbt.fields(candidate, new CompoundTag()), false, true));
    }

    @Test
    void displayFieldsCanMatchIndependentlyAndSnapshotsDoNotAliasTheItem() {
        CompoundTag display = new CompoundTag();
        display.putString("Name", "named");
        CompoundTag reference = new CompoundTag();
        reference.put("display", display);
        var snapshot = ItemNbt.fields(reference, new CompoundTag());
        CompoundTag candidate = reference.copy();
        candidate.getCompound("display").putInt("color", 0xff00ff);
        assertTrue(ItemNbt.matches(snapshot, ItemNbt.fields(candidate, new CompoundTag()), false, true));
        display.putString("Name", "changed");
        assertEquals(StringTag.valueOf("named"), snapshot.get("display:Name"));
        assertThrows(UnsupportedOperationException.class, snapshot::clear);
    }

    @Test
    void explicitCopyPreservesDestinationFieldsAndReplacesWholeSourceValues() {
        CompoundTag source = new CompoundTag();
        CompoundTag values = new CompoundTag();
        CompoundTag contents = new CompoundTag();
        contents.putInt("aer", 4);
        values.put("thaumaturge:essentia_contents", contents);
        values.putBoolean("!thaumaturge:warp", true);
        source.put(ItemData.TAG, values);
        CompoundTag destination = new CompoundTag();
        CompoundTag destinationValues = new CompoundTag();
        destinationValues.putInt("thaumaturge:charge", 5);
        destinationValues.putInt("thaumaturge:warp", 2);
        CompoundTag oldContents = new CompoundTag();
        oldContents.putInt("ignis", 7);
        destinationValues.put("thaumaturge:essentia_contents", oldContents);
        destination.put(ItemData.TAG, destinationValues);
        destination.putString("addon:data", "keep");
        ItemNbt.copyExplicit(source, destination);
        CompoundTag copied = destination.getCompound(ItemData.TAG);
        assertEquals(5, copied.getInt("thaumaturge:charge"));
        assertFalse(copied.contains("thaumaturge:warp"));
        assertTrue(copied.getBoolean("!thaumaturge:warp"));
        assertEquals(contents, copied.getCompound("thaumaturge:essentia_contents"));
        assertFalse(copied.getCompound("thaumaturge:essentia_contents").contains("ignis"));
        assertEquals("keep", destination.getString("addon:data"));
        contents.putInt("aer", 99);
        assertEquals(4, copied.getCompound("thaumaturge:essentia_contents").getInt("aer"));
    }

    @Test
    void explicitValueReplacesAPreviousRemovalAndDisplayDataMergesOneLevel() {
        CompoundTag source = new CompoundTag();
        CompoundTag values = new CompoundTag();
        values.putInt("thaumaturge:warp", 4);
        source.put(ItemData.TAG, values);
        CompoundTag display = new CompoundTag();
        display.putString("Name", "jar");
        source.put("display", display);
        CompoundTag destination = new CompoundTag();
        CompoundTag removed = new CompoundTag();
        removed.putBoolean("!thaumaturge:warp", true);
        destination.put(ItemData.TAG, removed);
        CompoundTag oldDisplay = new CompoundTag();
        oldDisplay.putInt("color", 0x123456);
        destination.put("display", oldDisplay);
        ItemNbt.copyExplicit(source, destination);
        assertFalse(destination.getCompound(ItemData.TAG).contains("!thaumaturge:warp"));
        assertEquals(IntTag.valueOf(4), destination.getCompound(ItemData.TAG).get("thaumaturge:warp"));
        assertEquals("jar", destination.getCompound("display").getString("Name"));
        assertEquals(0x123456, destination.getCompound("display").getInt("color"));
    }

    @Test
    void invalidStateIsReportedBeforeExplicitCopyStarts() {
        CompoundTag source = new CompoundTag();
        source.putString(ItemData.TAG, "invalid");
        CompoundTag destination = new CompoundTag();
        assertThrows(IllegalArgumentException.class, () -> ItemNbt.fields(source, new CompoundTag()));
        assertThrows(IllegalArgumentException.class, () -> ItemNbt.copyExplicit(source, destination));
        assertTrue(destination.isEmpty());
        CompoundTag values = new CompoundTag();
        values.putInt("thaumaturge:warp", 1);
        values.putBoolean("!thaumaturge:warp", true);
        source.put(ItemData.TAG, values);
        assertThrows(IllegalArgumentException.class, () -> ItemNbt.fields(source, new CompoundTag()));
    }
}
