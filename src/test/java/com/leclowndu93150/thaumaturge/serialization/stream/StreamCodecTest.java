package com.leclowndu93150.thaumaturge.serialization.stream;

import static org.junit.jupiter.api.Assertions.*;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class StreamCodecTest {
    private record Message(int id, String name, Optional<Integer> limit, boolean active, float speed, double distance) {}

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 127, 128, 16383, 16384, Integer.MAX_VALUE, -1, Integer.MIN_VALUE})
    void varIntRoundTripsEntireIntegerRange(int value) {
        assertEquals(value, roundTrip(ByteBufCodecs.VAR_INT, value));
    }

    @Test
    void varIntUsesMinecraftWireFormat() {
        ByteBuf buffer = Unpooled.buffer();
        try {
            ByteBufCodecs.VAR_INT.encode(buffer, 300);
            assertEquals(2, buffer.readableBytes());
            assertEquals(0xAC, buffer.readUnsignedByte());
            assertEquals(0x02, buffer.readUnsignedByte());
        } finally { buffer.release(); }
    }

    @Test
    void rejectsOverlongAndTruncatedVarInts() {
        ByteBuf overlong = Unpooled.wrappedBuffer(new byte[] {-128, -128, -128, -128, -128, 0});
        ByteBuf truncated = Unpooled.wrappedBuffer(new byte[] {-128});
        try {
            assertThrows(DecoderException.class, () -> ByteBufCodecs.VAR_INT.decode(overlong));
            assertThrows(IndexOutOfBoundsException.class, () -> ByteBufCodecs.VAR_INT.decode(truncated));
        } finally { overlong.release(); truncated.release(); }
    }

    @Test
    void sixFieldsKeepTheirOrderAndRoundTrip() {
        StreamCodec<ByteBuf, Message> codec = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Message::id,
                ByteBufCodecs.STRING_UTF8, Message::name,
                ByteBufCodecs.optional(ByteBufCodecs.INT), Message::limit,
                ByteBufCodecs.BOOL, Message::active,
                ByteBufCodecs.FLOAT, Message::speed,
                ByteBufCodecs.DOUBLE, Message::distance, Message::new);
        Message message = new Message(42, "vis", Optional.of(1000), true, 0.5F, 1024.25D);
        assertEquals(message, roundTrip(codec, message));
        ByteBuf buffer = Unpooled.buffer();
        try {
            codec.encode(buffer, message);
            assertEquals(42, buffer.readByte());
            assertEquals("vis", ByteBufCodecs.STRING_UTF8.decode(buffer));
            assertTrue(buffer.readBoolean());
            assertEquals(1000, buffer.readInt());
            assertTrue(buffer.readBoolean());
            assertEquals(0.5F, buffer.readFloat());
            assertEquals(1024.25D, buffer.readDouble());
            assertFalse(buffer.isReadable());
        } finally { buffer.release(); }
    }

    @Test
    void compositeOverloadsEncodeEveryField() {
        assertEquals(3, roundTrip(StreamCodec.composite(ByteBufCodecs.VAR_INT, (Integer value) -> value, value -> value), 3));
        assertEquals(List.of(2, 3), roundTrip(StreamCodec.composite(
                ByteBufCodecs.VAR_INT, (List<Integer> value) -> value.get(0),
                ByteBufCodecs.VAR_INT, value -> value.get(1), List::of), List.of(2, 3)));
        assertEquals(List.of(2, 3, 4), roundTrip(StreamCodec.composite(
                ByteBufCodecs.VAR_INT, (List<Integer> value) -> value.get(0),
                ByteBufCodecs.VAR_INT, value -> value.get(1),
                ByteBufCodecs.VAR_INT, value -> value.get(2), List::of), List.of(2, 3, 4)));
        assertEquals(List.of(2, 3, 4, 5), roundTrip(StreamCodec.composite(
                ByteBufCodecs.VAR_INT, (List<Integer> value) -> value.get(0),
                ByteBufCodecs.VAR_INT, value -> value.get(1),
                ByteBufCodecs.VAR_INT, value -> value.get(2),
                ByteBufCodecs.VAR_INT, value -> value.get(3), List::of), List.of(2, 3, 4, 5)));
        assertEquals(List.of(2, 3, 4, 5, 6), roundTrip(StreamCodec.composite(
                ByteBufCodecs.VAR_INT, (List<Integer> value) -> value.get(0),
                ByteBufCodecs.VAR_INT, value -> value.get(1),
                ByteBufCodecs.VAR_INT, value -> value.get(2),
                ByteBufCodecs.VAR_INT, value -> value.get(3),
                ByteBufCodecs.VAR_INT, value -> value.get(4), List::of), List.of(2, 3, 4, 5, 6)));
    }

    @Test
    void optionalAndMappedValuesRoundTrip() {
        var optional = ByteBufCodecs.optional(ByteBufCodecs.VAR_INT.map(Object::toString, Integer::parseInt));
        assertEquals(Optional.empty(), roundTrip(optional, Optional.empty()));
        assertEquals(Optional.of("25"), roundTrip(optional, Optional.of("25")));
    }

    @Test
    void nestedListsAndOrderedMapsRoundTrip() {
        var lists = ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()).apply(ByteBufCodecs.list());
        assertEquals(List.of(List.of(), List.of(-1, 0, 5)), roundTrip(lists, List.of(List.of(), List.of(-1, 0, 5))));
        StreamCodec<ByteBuf, LinkedHashMap<String, Integer>> maps = ByteBufCodecs.map(
                LinkedHashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.VAR_INT);
        LinkedHashMap<String, Integer> input = new LinkedHashMap<>();
        input.put("aer", 10); input.put("ordo", 3);
        Map<String, Integer> decoded = roundTrip(maps, input);
        assertEquals(input, decoded);
        assertEquals(List.of("aer", "ordo"), List.copyOf(decoded.keySet()));
    }

    @Test
    void listLimitsApplyToEncodingAndDecodingIncludingEmptyList() {
        var codec = ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list(2));
        assertEquals(List.of(), roundTrip(codec, List.of()));
        assertEquals(List.of(1, 2), roundTrip(codec, List.of(1, 2)));
        ByteBuf buffer = Unpooled.buffer();
        try {
            assertThrows(EncoderException.class, () -> codec.encode(buffer, List.of(1, 2, 3)));
            assertEquals(0, buffer.writerIndex());
            ByteBufCodecs.VAR_INT.encode(buffer, 3);
            assertThrows(DecoderException.class, () -> codec.decode(buffer));
            buffer.clear();
            ByteBufCodecs.VAR_INT.encode(buffer, -1);
            assertThrows(DecoderException.class, () -> codec.decode(buffer));
        } finally { buffer.release(); }
    }

    @Test
    void mapLimitsApplyOnBothSides() {
        var codec = ByteBufCodecs.map(LinkedHashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.INT, 1);
        assertEquals(Map.of("aer", 1), roundTrip(codec, new LinkedHashMap<>(Map.of("aer", 1))));
        ByteBuf buffer = Unpooled.buffer();
        try {
            assertThrows(EncoderException.class, () -> codec.encode(buffer, new LinkedHashMap<>(Map.of("aer", 1, "ordo", 2))));
            ByteBufCodecs.VAR_INT.encode(buffer, 2);
            assertThrows(DecoderException.class, () -> codec.decode(buffer));
        } finally { buffer.release(); }
    }

    @Test
    void boundedStringsRoundTripUnicodeAndRejectExcessLength() {
        var codec = ByteBufCodecs.stringUtf8(2);
        assertEquals("火a", roundTrip(codec, "火a"));
        ByteBuf buffer = Unpooled.buffer();
        try {
            assertThrows(EncoderException.class, () -> codec.encode(buffer, "abc"));
            ByteBufCodecs.STRING_UTF8.encode(buffer, "abc");
            assertThrows(DecoderException.class, () -> codec.decode(buffer));
        } finally { buffer.release(); }
    }

    @Test
    void arraysRoundTripAndRejectLengthsBeyondPacket() {
        assertArrayEquals(new byte[0], roundTrip(ByteBufCodecs.BYTE_ARRAY, new byte[0]));
        assertArrayEquals(new byte[] {1, -2, 3}, roundTrip(ByteBufCodecs.BYTE_ARRAY, new byte[] {1, -2, 3}));
        ByteBuf buffer = Unpooled.buffer();
        try {
            ByteBufCodecs.VAR_INT.encode(buffer, 100);
            buffer.writeByte(1);
            assertThrows(DecoderException.class, () -> ByteBufCodecs.BYTE_ARRAY.decode(buffer));
        } finally { buffer.release(); }
    }

    @Test
    void unitConsumesNoBytesAndRejectsWrongConstant() {
        assertEquals("fixed", roundTrip(StreamCodec.unit("fixed"), "fixed"));
        ByteBuf buffer = Unpooled.buffer();
        try {
            assertThrows(EncoderException.class, () -> StreamCodec.<ByteBuf, String>unit("fixed").encode(buffer, "other"));
            assertEquals(0, buffer.writerIndex());
        } finally { buffer.release(); }
    }

    @Test
    void idMapperRejectsUnknownIdsAndUnregisteredValues() {
        var codec = ByteBufCodecs.idMapper(id -> new String[] {"first", "second"}[id],
                value -> List.of("first", "second").indexOf(value));
        assertEquals("second", roundTrip(codec, "second"));
        ByteBuf buffer = Unpooled.buffer();
        try {
            assertThrows(EncoderException.class, () -> codec.encode(buffer, "missing"));
            ByteBufCodecs.VAR_INT.encode(buffer, 2);
            assertThrows(DecoderException.class, () -> codec.decode(buffer));
        } finally { buffer.release(); }
    }

    static <V> V roundTrip(StreamCodec<ByteBuf, V> codec, V value) {
        ByteBuf buffer = Unpooled.buffer();
        try {
            codec.encode(buffer, value);
            V result = codec.decode(buffer);
            assertFalse(buffer.isReadable(), "codec left unread bytes");
            return result;
        } finally { buffer.release(); }
    }
}
