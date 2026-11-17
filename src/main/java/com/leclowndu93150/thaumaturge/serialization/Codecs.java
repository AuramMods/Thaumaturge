package com.leclowndu93150.thaumaturge.serialization;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.List;
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
