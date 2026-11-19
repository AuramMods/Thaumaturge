package com.leclowndu93150.thaumaturge.content.world.mound;

import com.leclowndu93150.thaumaturge.registry.TTStructures;
import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.structure.SinglePieceStructure;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

public class MoundStructure extends SinglePieceStructure {
    public static final Codec<MoundStructure> CODEC = simpleCodec(MoundStructure::new);

    public MoundStructure(Structure.StructureSettings settings) {
        super(MoundPiece::new, MoundLayout.SIZE_X, MoundLayout.SIZE_Z, settings);
    }

    @Override
    public StructureType<?> type() {
        return TTStructures.MOUND.get();
    }
}
