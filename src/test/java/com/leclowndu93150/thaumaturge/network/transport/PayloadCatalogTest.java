package com.leclowndu93150.thaumaturge.network.transport;

import static org.junit.jupiter.api.Assertions.*;

import com.leclowndu93150.thaumaturge.network.transport.PayloadCatalog.Flow;
import com.leclowndu93150.thaumaturge.serialization.stream.ByteBufCodecs;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.junit.jupiter.api.Test;

class PayloadCatalogTest {
    private record Request(int value) implements ModPayload {
        static final Type<Request> TYPE = new Type<>(new ResourceLocation("thaumaturge", "test_request"));
        static final StreamCodec<ByteBuf, Request> CODEC = ByteBufCodecs.VAR_INT.map(Request::new, Request::value);
        @Override public Type<Request> type() { return TYPE; }
    }

    private record Response(String value) implements ModPayload {
        static final Type<Response> TYPE = new Type<>(new ResourceLocation("thaumaturge", "test_response"));
        static final StreamCodec<ByteBuf, Response> CODEC = ByteBufCodecs.STRING_UTF8.map(Response::new, Response::value);
        @Override public Type<Response> type() { return TYPE; }
    }

    private static final PayloadContext DIRECT_CONTEXT = new PayloadContext() {
        @Override public Player player() { throw new UnsupportedOperationException("This test does not access a player"); }
        @Override public CompletableFuture<Void> enqueueWork(Runnable work) {
            work.run();
            return CompletableFuture.completedFuture(null);
        }
    };

    @Test
    void encodesStableIdentifierThenDataAndDispatchesTheDecodedRequest() {
        PayloadCatalog catalog = new PayloadCatalog();
        AtomicInteger received = new AtomicInteger();
        catalog.register(Request.TYPE, Request.CODEC, Flow.SERVERBOUND,
                (request, context) -> context.enqueueWork(() -> received.set(request.value())));
        RegistryFriendlyByteBuf buffer = buffer();
        try {
            Request request = new Request(300);
            catalog.encode(buffer, request, Flow.SERVERBOUND);
            assertEquals(Request.TYPE.id(), buffer.readResourceLocation());
            assertEquals(300, buffer.readVarInt());
            assertFalse(buffer.isReadable());
            buffer.readerIndex(0);
            ModPayload decoded = catalog.decode(buffer, Flow.SERVERBOUND);
            assertEquals(request, decoded);
            catalog.handle(decoded, Flow.SERVERBOUND, DIRECT_CONTEXT);
            assertEquals(300, received.get());
        } finally { buffer.release(); }
    }

    @Test
    void clientboundMessagesRoundTripWithoutDependingOnRegistrationOrder() {
        PayloadCatalog encoder = new PayloadCatalog();
        PayloadCatalog decoder = new PayloadCatalog();
        encoder.register(Request.TYPE, Request.CODEC, Flow.SERVERBOUND, (value, context) -> {});
        encoder.register(Response.TYPE, Response.CODEC, Flow.CLIENTBOUND, (value, context) -> {});
        decoder.register(Response.TYPE, Response.CODEC, Flow.CLIENTBOUND, (value, context) -> {});
        decoder.register(Request.TYPE, Request.CODEC, Flow.SERVERBOUND, (value, context) -> {});
        RegistryFriendlyByteBuf buffer = buffer();
        try {
            Response response = new Response("火 / aura");
            encoder.encode(buffer, response, Flow.CLIENTBOUND);
            assertEquals(response, decoder.decode(buffer, Flow.CLIENTBOUND));
        } finally { buffer.release(); }
    }

    @Test
    void duplicateIdentifiersAreRejectedWithoutReplacingTheOriginalHandler() {
        PayloadCatalog catalog = new PayloadCatalog();
        AtomicInteger calls = new AtomicInteger();
        catalog.register(Request.TYPE, Request.CODEC, Flow.SERVERBOUND, (value, context) -> calls.incrementAndGet());
        assertThrows(IllegalStateException.class, () -> catalog.register(Request.TYPE, Request.CODEC,
                Flow.CLIENTBOUND, (value, context) -> fail("duplicate replaced original")));
        catalog.handle(new Request(1), Flow.SERVERBOUND, DIRECT_CONTEXT);
        assertEquals(1, calls.get());
    }

    @Test
    void rejectsWrongDirectionDuringEncodeDecodeAndDispatch() {
        PayloadCatalog catalog = new PayloadCatalog();
        catalog.register(Request.TYPE, Request.CODEC, Flow.SERVERBOUND, (value, context) -> fail("wrong direction dispatched"));
        RegistryFriendlyByteBuf buffer = buffer();
        try {
            Request request = new Request(1);
            assertThrows(EncoderException.class, () -> catalog.encode(buffer, request, Flow.CLIENTBOUND));
            assertEquals(0, buffer.writerIndex());
            catalog.encode(buffer, request, Flow.SERVERBOUND);
            assertThrows(DecoderException.class, () -> catalog.decode(buffer, Flow.CLIENTBOUND));
            assertThrows(DecoderException.class, () -> catalog.handle(request, Flow.CLIENTBOUND, DIRECT_CONTEXT));
        } finally { buffer.release(); }
    }

    @Test
    void rejectsUnknownPayloadIdentifiersOnBothSides() {
        PayloadCatalog catalog = new PayloadCatalog();
        RegistryFriendlyByteBuf buffer = buffer();
        try {
            assertThrows(EncoderException.class, () -> catalog.encode(buffer, new Request(1), Flow.SERVERBOUND));
            buffer.writeResourceLocation(Request.TYPE.id());
            assertThrows(DecoderException.class, () -> catalog.decode(buffer, Flow.SERVERBOUND));
            assertThrows(DecoderException.class, () -> catalog.handle(new Request(1), Flow.SERVERBOUND, DIRECT_CONTEXT));
        } finally { buffer.release(); }
    }

    @Test
    void rejectsTrailingBytesAndMalformedFields() {
        PayloadCatalog catalog = new PayloadCatalog();
        catalog.register(Request.TYPE, Request.CODEC, Flow.SERVERBOUND, (value, context) -> {});
        RegistryFriendlyByteBuf buffer = buffer();
        try {
            catalog.encode(buffer, new Request(10), Flow.SERVERBOUND);
            buffer.writeByte(0);
            assertThrows(DecoderException.class, () -> catalog.decode(buffer, Flow.SERVERBOUND));
            buffer.clear();
            buffer.writeResourceLocation(Request.TYPE.id());
            buffer.writeBytes(new byte[] {-128, -128, -128, -128, -128});
            assertThrows(DecoderException.class, () -> catalog.decode(buffer, Flow.SERVERBOUND));
        } finally { buffer.release(); }
    }

    @Test
    void rejectsADecoderReturningAnUnexpectedPayloadType() {
        PayloadCatalog catalog = new PayloadCatalog();
        ModPayload.Type<Response> declared = new ModPayload.Type<>(new ResourceLocation("thaumaturge", "different"));
        catalog.register(declared, Response.CODEC, Flow.CLIENTBOUND, (value, context) -> {});
        RegistryFriendlyByteBuf buffer = buffer();
        try {
            buffer.writeResourceLocation(declared.id());
            ByteBufCodecs.STRING_UTF8.encode(buffer, "message");
            assertThrows(DecoderException.class, () -> catalog.decode(buffer, Flow.CLIENTBOUND));
        } finally { buffer.release(); }
    }

    private static RegistryFriendlyByteBuf buffer() {
        return new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
    }
}
