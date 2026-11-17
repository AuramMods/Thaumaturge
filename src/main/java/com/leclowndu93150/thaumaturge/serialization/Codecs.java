package com.leclowndu93150.thaumaturge.serialization;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.BaseMapCodec;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * 1.21.1 codec operations implemented on Forge's DFU 6 API.
 * Portions adapted from Mojang DataFixerUpper 8.0.16, Copyright Microsoft,
 * under the MIT license in META-INF/licenses/thaumaturge-dfu-compat.txt.
 */
public final class Codecs {
    private Codecs() {}

    public static <A> A getOrThrow(DataResult<A> result) {
        return result.result().orElseThrow(() -> new IllegalStateException(result.error().orElseThrow().message()));
    }

    public static <K, V> Codec<Map<K, V>> strictUnboundedMap(Codec<K> keys, Codec<V> values) {
        return new StrictMapCodec<>(keys, values);
    }

    private record StrictMapCodec<K, V>(Codec<K> keyCodec, Codec<V> elementCodec)
            implements Codec<Map<K, V>>, BaseMapCodec<K, V> {
        @Override public <T> DataResult<Map<K, V>> decode(DynamicOps<T> ops, MapLike<T> input) {
            ImmutableMap.Builder<K, V> values = ImmutableMap.builder();
            for (Pair<T, T> entry : input.entries().toList()) {
                DataResult<K> key = keyCodec.parse(ops, entry.getFirst());
                DataResult<V> value = elementCodec.parse(ops, entry.getSecond());
                DataResult<Pair<K, V>> pair = key.apply2stable(Pair::of, value);
                if (pair.error().isPresent()) {
                    String message = pair.error().orElseThrow().message();
                    return DataResult.error(() -> key.result().map(k -> "Map entry '" + k + "' : " + message).orElse(message));
                }
                if (pair.result().isEmpty()) return DataResult.error(() -> "Empty or invalid map contents are not allowed");
                Pair<K, V> decoded = pair.result().orElseThrow();
                values.put(decoded.getFirst(), decoded.getSecond());
            }
            return DataResult.success(values.build());
        }

        @Override public <T> DataResult<Pair<Map<K, V>, T>> decode(DynamicOps<T> ops, T input) {
            return ops.getMap(input).setLifecycle(Lifecycle.stable()).flatMap(map -> decode(ops, map))
                    .map(value -> Pair.of(value, input));
        }

        @Override public <T> DataResult<T> encode(Map<K, V> input, DynamicOps<T> ops, T prefix) {
            return encode(input, ops, ops.mapBuilder()).build(prefix);
        }
    }

    public static <A> Codec<A> validate(Codec<A> codec, Function<A, DataResult<A>> validator) {
        return codec.flatXmap(validator, validator);
    }

    public static <A> MapCodec<A> validate(MapCodec<A> codec, Function<A, DataResult<A>> validator) {
        return codec.flatXmap(validator, validator);
    }

    public static <A> Codec<List<A>> list(Codec<A> element, int min, int max) {
        return new BoundedListCodec<>(element, min, max);
    }

    public static <A> Codec<List<A>> sizeLimitedList(Codec<A> element, int max) {
        return list(element, 0, max);
    }

    public static <A> Codec<A> lazyInitialized(Supplier<Codec<A>> factory) {
        return recursive(factory.toString(), self -> factory.get());
    }

    public static <A> Codec<A> recursive(String name, Function<Codec<A>, Codec<A>> factory) {
        return new RecursiveCodec<>(name, factory);
    }

    public static <A> Codec<A> withAlternative(Codec<A> primary, Codec<? extends A> alternative) {
        return withAlternative(primary, alternative, value -> value);
    }

    public static <A, B> Codec<A> withAlternative(Codec<A> primary, Codec<B> alternative, Function<B, A> convert) {
        return new AlternativeCodec<>(primary, alternative, convert);
    }

    public static Codec<String> string(int min, int max) {
        return validate(Codec.STRING, value -> {
            if (value.length() < min) return DataResult.error(() -> "String \"" + value + "\" is too short: "
                    + value.length() + ", expected range [" + min + "-" + max + "]");
            if (value.length() > max) return DataResult.error(() -> "String \"" + value + "\" is too long: "
                    + value.length() + ", expected range [" + min + "-" + max + "]");
            return DataResult.success(value);
        });
    }

    public static <A> MapCodec<Optional<A>> optionalFieldOf(Codec<A> codec, String name) {
        return new OptionalField<>(name, codec, false);
    }

    public static <A> MapCodec<A> optionalFieldOf(Codec<A> codec, String name, A fallback) {
        return optionalFieldOf(codec, name).xmap(value -> value.orElse(fallback),
                value -> Objects.equals(value, fallback) ? Optional.empty() : Optional.of(value));
    }

    public static <A> MapCodec<Optional<A>> lenientOptionalFieldOf(Codec<A> codec, String name) {
        return new OptionalField<>(name, codec, true);
    }

    public static <A> MapCodec<A> lenientOptionalFieldOf(Codec<A> codec, String name, A fallback) {
        return lenientOptionalFieldOf(codec, name).xmap(value -> value.orElse(fallback),
                value -> Objects.equals(value, fallback) ? Optional.empty() : Optional.of(value));
    }

    public static <K, V> Codec<V> dispatch(Codec<K> keys, Function<? super V, ? extends K> type,
                                          Function<? super K, ? extends MapCodec<? extends V>> formats) {
        return dispatch(keys, "type", type, formats);
    }

    public static <K, V> Codec<V> dispatch(Codec<K> keys, String name, Function<? super V, ? extends K> type,
                                          Function<? super K, ? extends MapCodec<? extends V>> formats) {
        return keys.dispatch(name, type, key -> formats.apply(key).codec());
    }

    public static <K, V> Codec<V> partialDispatch(Codec<K> keys, String name,
            Function<? super V, ? extends DataResult<? extends K>> type,
            Function<? super K, ? extends DataResult<? extends MapCodec<? extends V>>> formats) {
        return keys.partialDispatch(name, type, key -> formats.apply(key).map(MapCodec::codec));
    }

    private static final class OptionalField<A> extends MapCodec<Optional<A>> {
        private final String name;
        private final Codec<A> element;
        private final boolean lenient;

        private OptionalField(String name, Codec<A> element, boolean lenient) {
            this.name = name;
            this.element = element;
            this.lenient = lenient;
        }

        @Override public <T> DataResult<Optional<A>> decode(DynamicOps<T> ops, MapLike<T> input) {
            T raw = input.get(name);
            if (raw == null) return DataResult.success(Optional.empty());
            DataResult<A> parsed = element.parse(ops, raw);
            if (lenient && parsed.error().isPresent()) return DataResult.success(Optional.empty());
            // DFU 6 exposes partial values through this logging callback. The
            // original error remains in the mapped result returned below.
            Optional<A> partial = parsed.resultOrPartial(error -> {});
            return parsed.map(Optional::of).setPartial(partial);
        }

        @Override public <T> RecordBuilder<T> encode(Optional<A> input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            return input.isPresent() ? prefix.add(name, element.encodeStart(ops, input.get())) : prefix;
        }

        @Override public <T> Stream<T> keys(DynamicOps<T> ops) { return Stream.of(ops.createString(name)); }

        @Override public boolean equals(Object other) {
            return other instanceof OptionalField<?> field && name.equals(field.name)
                    && element.equals(field.element) && lenient == field.lenient;
        }
        @Override public int hashCode() { return Objects.hash(name, element, lenient); }
        @Override public String toString() { return "OptionalFieldCodec[" + name + ": " + element + ']'; }
    }

    private static final class RecursiveCodec<A> implements Codec<A> {
        private final String name;
        private final Supplier<Codec<A>> delegate;

        private RecursiveCodec(String name, Function<Codec<A>, Codec<A>> factory) {
            this.name = name;
            delegate = Suppliers.memoize(() -> factory.apply(this));
        }

        @Override public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input) {
            return delegate.get().decode(ops, input);
        }
        @Override public <T> DataResult<T> encode(A input, DynamicOps<T> ops, T prefix) {
            return delegate.get().encode(input, ops, prefix);
        }
        @Override public String toString() { return "RecursiveCodec[" + name + ']'; }
    }

    private record AlternativeCodec<A, B>(Codec<A> primary, Codec<B> alternative, Function<B, A> convert)
            implements Codec<A> {
        @Override public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input) {
            DataResult<Pair<A, T>> first = primary.decode(ops, input);
            if (first.result().isPresent()) return first;
            DataResult<Pair<B, T>> second = alternative.decode(ops, input);
            if (second.result().isPresent()) return second.map(pair -> pair.mapFirst(convert));
            List<String> errors = new ArrayList<>(2);
            if (first.resultOrPartial(errors::add).isPresent()) return first;
            if (second.resultOrPartial(errors::add).isPresent()) return second.map(pair -> pair.mapFirst(convert));
            return DataResult.error(() -> "Failed to parse either. First: " + errors.get(0) + "; Second: " + errors.get(1));
        }

        @Override public <T> DataResult<T> encode(A input, DynamicOps<T> ops, T prefix) {
            return primary.encode(input, ops, prefix);
        }
    }

    private record BoundedListCodec<A>(Codec<A> element, int min, int max) implements Codec<List<A>> {
        private <T> DataResult<T> sizeError(int size, boolean shortList) {
            return DataResult.error(() -> "List is too " + (shortList ? "short" : "long") + ": " + size
                    + ", expected range [" + min + "-" + max + "]");
        }

        @Override public <T> DataResult<T> encode(List<A> input, DynamicOps<T> ops, T prefix) {
            if (input.size() < min) return sizeError(input.size(), true);
            if (input.size() > max) return sizeError(input.size(), false);
            var builder = ops.listBuilder();
            for (A value : input) builder.add(element.encodeStart(ops, value));
            return builder.build(prefix);
        }

        @Override public <T> DataResult<Pair<List<A>, T>> decode(DynamicOps<T> ops, T input) {
            return ops.getList(input).setLifecycle(Lifecycle.stable()).flatMap(values -> {
                class State {
                    final List<A> accepted = new ArrayList<>();
                    final Stream.Builder<T> rejected = Stream.builder();
                    DataResult<Unit> status = DataResult.success(Unit.INSTANCE, Lifecycle.stable());
                    int count;

                    void accept(T value) {
                        count++;
                        if (accepted.size() >= max) { rejected.add(value); return; }
                        DataResult<Pair<A, T>> decoded = element.decode(ops, value);
                        decoded.resultOrPartial(error -> rejected.add(value))
                                .ifPresent(pair -> accepted.add(pair.getFirst()));
                        status = status.apply2stable((ignored, entry) -> ignored, decoded);
                    }
                }
                State state = new State();
                values.accept(state::accept);
                if (state.accepted.size() < min) return sizeError(state.accepted.size(), true);
                Pair<List<A>, T> partial = Pair.of(List.copyOf(state.accepted), ops.createList(state.rejected.build()));
                if (state.count > max) state.status = sizeError(state.count, false);
                return state.status.map(ignored -> partial).setPartial(partial);
            });
        }

        @Override public String toString() { return "ListCodec[" + element + ']'; }
    }
}
