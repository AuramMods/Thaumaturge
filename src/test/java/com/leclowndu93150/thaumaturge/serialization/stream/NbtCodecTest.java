package com.leclowndu93150.thaumaturge.serialization.stream;

import static org.junit.jupiter.api.Assertions.*;

import com.mojang.serialization.Codec;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

class NbtCodecTest {
    @Test
    void nbtCodecCarriesScalarsAndCollections() {
        assertEquals("aer", StreamCodecTest.roundTrip(ByteBufCodecs.fromCodec(Codec.STRING), "aer"));
        assertEquals(List.of(1, 2, 3), StreamCodecTest.roundTrip(ByteBufCodecs.fromCodec(Codec.INT.listOf()), List.of(1, 2, 3)));
    }

    @Test
    void nbtCodecRejectsInvalidValuesAndMissingPayload() {
        var codec = ByteBufCodecs.fromCodec(Codec.intRange(1, 5));
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            assertThrows(EncoderException.class, () -> codec.encode(buffer, 0));
            buffer.writeNbt(null);
            assertThrows(DecoderException.class, () -> codec.decode(buffer));
            buffer.clear();
            ByteBufCodecs.fromCodec(Codec.STRING).encode(buffer, "wrong type");
            assertThrows(DecoderException.class, () -> codec.decode(buffer));
        } finally { buffer.release(); }
    }

}
