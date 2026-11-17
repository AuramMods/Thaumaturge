package com.leclowndu93150.thaumaturge.serialization.stream;

import io.netty.handler.codec.EncoderException;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/** Typed binary serializers for the Forge 1.20.1 transport. */
public interface StreamCodec<B, V> {
    V decode(B buffer);

    void encode(B buffer, V value);

    static <B, V> StreamCodec<B, V> of(BiConsumer<B, V> encoder, Function<B, V> decoder) {
        return new StreamCodec<>() {
            @Override
            public V decode(B buffer) { return decoder.apply(buffer); }

            @Override
            public void encode(B buffer, V value) { encoder.accept(buffer, value); }
        };
    }

    static <B, V> StreamCodec<B, V> unit(V constant) {
        return of((buffer, value) -> {
            if (!Objects.equals(value, constant)) {
                throw new EncoderException("Expected constant " + constant + ", got " + value);
            }
        }, buffer -> constant);
    }

    /** Stateless wire data with a fresh value on decode, for per-resource recipe identities. */
    static <B, V> StreamCodec<B, V> unitFactory(java.util.function.Supplier<V> factory) {
        return of((buffer, value) -> {}, buffer -> factory.get());
    }

    default <O> StreamCodec<B, O> map(Function<? super V, ? extends O> decode,
            Function<? super O, ? extends V> encode) {
        return of((buffer, value) -> encode(buffer, encode.apply(value)),
                buffer -> decode.apply(decode(buffer)));
    }

    default <O> StreamCodec<B, O> apply(Function<StreamCodec<B, V>, StreamCodec<B, O>> operation) {
        return operation.apply(this);
    }

    static <B, V, T1> StreamCodec<B, V> composite(
            StreamCodec<? super B, T1> codec1, Function<V, T1> field1,
            Function<T1, V> constructor) {
        return of((buffer, value) -> {
            codec1.encode(buffer, field1.apply(value));
        }, buffer -> constructor.apply(codec1.decode(buffer)));
    }

    static <B, V, T1, T2> StreamCodec<B, V> composite(
            StreamCodec<? super B, T1> codec1, Function<V, T1> field1,
            StreamCodec<? super B, T2> codec2, Function<V, T2> field2,
            BiFunction<T1, T2, V> constructor) {
        return of((buffer, value) -> {
            codec1.encode(buffer, field1.apply(value));
            codec2.encode(buffer, field2.apply(value));
        }, buffer -> constructor.apply(codec1.decode(buffer), codec2.decode(buffer)));
    }

    static <B, V, T1, T2, T3> StreamCodec<B, V> composite(
            StreamCodec<? super B, T1> codec1, Function<V, T1> field1,
            StreamCodec<? super B, T2> codec2, Function<V, T2> field2,
            StreamCodec<? super B, T3> codec3, Function<V, T3> field3,
            Function3<T1, T2, T3, V> constructor) {
        return of((buffer, value) -> {
            codec1.encode(buffer, field1.apply(value));
            codec2.encode(buffer, field2.apply(value));
            codec3.encode(buffer, field3.apply(value));
        }, buffer -> constructor.apply(codec1.decode(buffer), codec2.decode(buffer), codec3.decode(buffer)));
    }

    static <B, V, T1, T2, T3, T4> StreamCodec<B, V> composite(
            StreamCodec<? super B, T1> codec1, Function<V, T1> field1,
            StreamCodec<? super B, T2> codec2, Function<V, T2> field2,
            StreamCodec<? super B, T3> codec3, Function<V, T3> field3,
            StreamCodec<? super B, T4> codec4, Function<V, T4> field4,
            Function4<T1, T2, T3, T4, V> constructor) {
        return of((buffer, value) -> {
            codec1.encode(buffer, field1.apply(value));
            codec2.encode(buffer, field2.apply(value));
            codec3.encode(buffer, field3.apply(value));
            codec4.encode(buffer, field4.apply(value));
        }, buffer -> constructor.apply(codec1.decode(buffer), codec2.decode(buffer), codec3.decode(buffer), codec4.decode(buffer)));
    }

    static <B, V, T1, T2, T3, T4, T5> StreamCodec<B, V> composite(
            StreamCodec<? super B, T1> codec1, Function<V, T1> field1,
            StreamCodec<? super B, T2> codec2, Function<V, T2> field2,
            StreamCodec<? super B, T3> codec3, Function<V, T3> field3,
            StreamCodec<? super B, T4> codec4, Function<V, T4> field4,
            StreamCodec<? super B, T5> codec5, Function<V, T5> field5,
            Function5<T1, T2, T3, T4, T5, V> constructor) {
        return of((buffer, value) -> {
            codec1.encode(buffer, field1.apply(value));
            codec2.encode(buffer, field2.apply(value));
            codec3.encode(buffer, field3.apply(value));
            codec4.encode(buffer, field4.apply(value));
            codec5.encode(buffer, field5.apply(value));
        }, buffer -> constructor.apply(codec1.decode(buffer), codec2.decode(buffer), codec3.decode(buffer), codec4.decode(buffer), codec5.decode(buffer)));
    }

    static <B, V, T1, T2, T3, T4, T5, T6> StreamCodec<B, V> composite(
            StreamCodec<? super B, T1> codec1, Function<V, T1> field1,
            StreamCodec<? super B, T2> codec2, Function<V, T2> field2,
            StreamCodec<? super B, T3> codec3, Function<V, T3> field3,
            StreamCodec<? super B, T4> codec4, Function<V, T4> field4,
            StreamCodec<? super B, T5> codec5, Function<V, T5> field5,
            StreamCodec<? super B, T6> codec6, Function<V, T6> field6,
            Function6<T1, T2, T3, T4, T5, T6, V> constructor) {
        return of((buffer, value) -> {
            codec1.encode(buffer, field1.apply(value));
            codec2.encode(buffer, field2.apply(value));
            codec3.encode(buffer, field3.apply(value));
            codec4.encode(buffer, field4.apply(value));
            codec5.encode(buffer, field5.apply(value));
            codec6.encode(buffer, field6.apply(value));
        }, buffer -> constructor.apply(codec1.decode(buffer), codec2.decode(buffer), codec3.decode(buffer), codec4.decode(buffer), codec5.decode(buffer), codec6.decode(buffer)));
    }

    @FunctionalInterface
    interface Function3<T1, T2, T3, R> {
        R apply(T1 value1, T2 value2, T3 value3);
    }

    @FunctionalInterface
    interface Function4<T1, T2, T3, T4, R> {
        R apply(T1 value1, T2 value2, T3 value3, T4 value4);
    }

    @FunctionalInterface
    interface Function5<T1, T2, T3, T4, T5, R> {
        R apply(T1 value1, T2 value2, T3 value3, T4 value4, T5 value5);
    }

    @FunctionalInterface
    interface Function6<T1, T2, T3, T4, T5, T6, R> {
        R apply(T1 value1, T2 value2, T3 value3, T4 value4, T5 value5, T6 value6);
    }
}
