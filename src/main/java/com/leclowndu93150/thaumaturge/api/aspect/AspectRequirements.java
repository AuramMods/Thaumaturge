package com.leclowndu93150.thaumaturge.api.aspect;

import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.leclowndu93150.thaumaturge.serialization.stream.ByteBufCodecs;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import com.leclowndu93150.thaumaturge.serialization.stream.VanillaStreamCodecs;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.IntUnaryOperator;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.tags.TagKey;
import org.jspecify.annotations.Nullable;

/** Recipe costs. A tag can be paid with any combination of its current members. */
public final class AspectRequirements {
    private static final Codec<Either<AspectInstance, Tagged>> ENTRY_CODEC = Encoded.CODEC.comapFlatMap(
            Encoded::decode, entry -> entry.map(
                    exact -> new Encoded(Optional.of(exact.aspect()), Optional.empty(), exact.amount()),
                    tag -> new Encoded(Optional.empty(), Optional.of(tag.key()), tag.amount())));
    private static final StreamCodec<RegistryFriendlyByteBuf, Either<AspectInstance, Tagged>> ENTRY_STREAM =
            StreamCodec.of((buffer, value) -> value.ifLeft(exact -> {
                buffer.writeBoolean(false);
                AspectInstance.STREAM_CODEC.encode(buffer, exact);
            }).ifRight(tag -> {
                buffer.writeBoolean(true);
                Tagged.STREAM_CODEC.encode(buffer, tag);
            }), buffer -> buffer.readBoolean()
                    ? Either.right(Tagged.STREAM_CODEC.decode(buffer))
                    : Either.left(AspectInstance.STREAM_CODEC.decode(buffer)));

    public static final Codec<AspectRequirements> CODEC = ENTRY_CODEC.listOf().comapFlatMap(entries -> {
        try {
            return DataResult.success(new AspectRequirements(entries));
        } catch (IllegalArgumentException ex) {
            return DataResult.error(ex::getMessage);
        }
    }, requirements -> requirements.entries);
    public static final Codec<AspectRequirements> NON_EMPTY_CODEC = Codecs.validate(CODEC, requirements ->
            requirements.totalAmount == 0
                    ? DataResult.error(() -> "Aspect requirements must not be empty")
                    : DataResult.success(requirements));
    public static final StreamCodec<RegistryFriendlyByteBuf, AspectRequirements> STREAM_CODEC =
            ENTRY_STREAM.apply(ByteBufCodecs.list()).map(AspectRequirements::new, requirements -> requirements.entries);

    private final List<Either<AspectInstance, Tagged>> entries;
    private final AspectList fixed;
    private final List<Tagged> tagged;
    private final int totalAmount;

    private AspectRequirements(List<Either<AspectInstance, Tagged>> entries) {
        long total = 0;
        AspectList exact = AspectList.EMPTY;
        List<Tagged> tags = new ArrayList<>();
        for (var entry : entries) {
            total += entry.map(AspectInstance::amount, Tagged::amount);
            if (total > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("Aspect requirements are too large");
            }
            if (entry.left().isPresent()) exact = exact.add(entry.left().orElseThrow());
            else tags.add(entry.right().orElseThrow());
        }
        this.fixed = exact;
        this.tagged = List.copyOf(tags);
        List<Either<AspectInstance, Tagged>> normalized = new ArrayList<>(fixed.size() + tagged.size());
        for (AspectInstance amount : fixed.entries()) normalized.add(Either.left(amount));
        for (Tagged amount : tagged) normalized.add(Either.right(amount));
        this.entries = List.copyOf(normalized);
        this.totalAmount = (int) total;
    }

    public static AspectRequirements fixed(AspectList aspects) {
        return new AspectRequirements(aspects.entries().stream().<Either<AspectInstance, Tagged>>map(Either::left)
                .toList());
    }

    public AspectList fixed() {
        return fixed;
    }

    public List<Tagged> tagged() {
        return tagged;
    }

    public int totalAmount() {
        return totalAmount;
    }

    public boolean hasTags() {
        return !tagged.isEmpty();
    }

    public AspectRequirements mapAmounts(IntUnaryOperator transform) {
        List<Either<AspectInstance, Tagged>> mapped = new ArrayList<>(entries.size());
        for (var entry : entries) {
            int amount = transform.applyAsInt(entry.map(AspectInstance::amount, Tagged::amount));
            if (amount > 0) {
                mapped.add(entry.mapBoth(exact -> new AspectInstance(exact.aspect(), amount),
                        tag -> new Tagged(tag.key(), amount)));
            }
        }
        return new AspectRequirements(mapped);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof AspectRequirements requirements
                && fixed.equals(requirements.fixed) && tagged.equals(requirements.tagged);
    }

    @Override
    public int hashCode() {
        return 31 * fixed.hashCode() + tagged.hashCode();
    }

    public Resolved resolve(HolderLookup.@Nullable Provider registries) {
        List<Demand> demands = new ArrayList<>(entries.size());
        for (var entry : entries) {
            int amount = entry.map(AspectInstance::amount, Tagged::amount);
            var choices = entry.<Stream<Holder<IAspect>>>map(
                    exact -> Stream.of(exact.aspect()),
                    tag -> registries == null ? Stream.empty()
                            : registries.lookup(IAspect.REGISTRY_KEY).flatMap(lookup -> lookup.get(tag.key()))
                            .stream().flatMap(set -> set.stream()));
            demands.add(new Demand(choices
                    .sorted(Comparator.comparing(holder -> holder.unwrapKey().orElseThrow().location()))
                    .toList(), amount, entry.right().map(Tagged::key)));
        }
        return new Resolved(List.copyOf(demands));
    }

    private record Encoded(Optional<Holder<IAspect>> aspect, Optional<TagKey<IAspect>> tag, int amount) {
        private static final Codec<Encoded> CODEC = RecordCodecBuilder.create(i -> i.group(
                        Codecs.optionalFieldOf(RegistryFixedCodec.create(IAspect.REGISTRY_KEY), "aspect")
                                .forGetter(Encoded::aspect),
                        Codecs.optionalFieldOf(TagKey.codec(IAspect.REGISTRY_KEY), "tag").forGetter(Encoded::tag),
                        Codecs.optionalFieldOf(Codec.intRange(1, Integer.MAX_VALUE), "amount", 1)
                                .forGetter(Encoded::amount))
                .apply(i, Encoded::new));

        private DataResult<Either<AspectInstance, Tagged>> decode() {
            if (aspect.isPresent() == tag.isPresent()) {
                return DataResult.error(() -> "An aspect cost must specify exactly one of 'aspect' or 'tag'");
            }
            return DataResult.success(aspect.<Either<AspectInstance, Tagged>>map(
                    value -> Either.left(new AspectInstance(value, amount)))
                    .orElseGet(() -> Either.right(new Tagged(tag.orElseThrow(), amount))));
        }
    }

    public record Tagged(TagKey<IAspect> key, int amount) {
        private static final StreamCodec<RegistryFriendlyByteBuf, Tagged> STREAM_CODEC =
                StreamCodec.composite(
                        VanillaStreamCodecs.RESOURCE_LOCATION.map(id -> TagKey.create(IAspect.REGISTRY_KEY, id),
                                TagKey::location),
                        Tagged::key,
                        ByteBufCodecs.VAR_INT,
                        Tagged::amount,
                        Tagged::new);

        public Tagged {
            if (amount < 1) throw new IllegalArgumentException("Aspect tag amount must be positive");
        }
    }

    public record Demand(List<Holder<IAspect>> choices, int amount, Optional<TagKey<IAspect>> tag) {}

    public record Resolved(List<Demand> demands) {
        public int amount() {
            return demands.stream().mapToInt(Demand::amount).reduce(0, Math::addExact);
        }

        public List<Holder<IAspect>> choices() {
            return demands.stream().flatMap(demand -> demand.choices().stream()).distinct().toList();
        }

        public boolean hasEmptyDemand() {
            return demands.stream().anyMatch(demand -> demand.choices().isEmpty());
        }

        public Allocation allocate(AspectList available) {
            return AspectAllocation.allocate(demands, choices(), available);
        }

        public AspectList payment(AspectList available) {
            return allocate(available).payment();
        }
    }

    public record Allocation(AspectList payment, List<Integer> paidPerDemand) {}
}
