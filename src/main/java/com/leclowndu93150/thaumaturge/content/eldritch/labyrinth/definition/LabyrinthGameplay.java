package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition;

import com.leclowndu93150.thaumaturge.registry.TTLootTables;

import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;

public record LabyrinthGameplay(
        ResourceKey<LootTable> keyRoomLoot, boolean glyphHints, boolean tabletResonance, BlockState arrivalPad) {
    public static final Codec<LabyrinthGameplay> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    TTLootTables.KEY_CODEC
                            .fieldOf("key_room_loot")
                            .forGetter(LabyrinthGameplay::keyRoomLoot),
                    Codecs.optionalFieldOf(Codec.BOOL, "glyph_hints", true).forGetter(LabyrinthGameplay::glyphHints),
                    Codecs.optionalFieldOf(Codec.BOOL, "tablet_resonance", true).forGetter(LabyrinthGameplay::tabletResonance),
                    BlockState.CODEC.fieldOf("arrival_pad").forGetter(LabyrinthGameplay::arrivalPad))
            .apply(instance, LabyrinthGameplay::new));
}
