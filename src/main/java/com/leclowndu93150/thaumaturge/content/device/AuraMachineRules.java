package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.content.device.fluxscrubber.BlockEntityFluxScrubber;
import com.leclowndu93150.thaumaturge.datamap.DataMapType;
import com.leclowndu93150.thaumaturge.datamap.DataMaps;
import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.leclowndu93150.thaumaturge.stage.StageRules;
import com.leclowndu93150.thaumaturge.stage.StageTarget;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

public final class AuraMachineRules {
    private AuraMachineRules() {}

    public static final DataMapType<Block, Condenser> CONDENSER = map("condenser", Condenser.CODEC);
    public static final DataMapType<Block, VisGenerator> VIS_GENERATOR = map("vis_generator", VisGenerator.CODEC);
    public static final DataMapType<Block, FluxScrubber> FLUX_SCRUBBER = map("flux_scrubber", FluxScrubber.CODEC);

    private static <T> DataMapType<Block, T> map(String path, Codec<T> codec) {
        return DataMapType.builder(TTIds.rl(path), Registries.BLOCK, codec).build();
    }

    public static <T> T of(Block block, DataMapType<Block, T> map, T fallback) {
        T configured = DataMaps.get(block.builtInRegistryHolder(), map);
        return configured == null ? fallback : configured;
    }

    @SafeVarargs
    public static boolean allows(ServerLevel level, ResourceLocation family, Block block, @Nullable UUID owner,
            ResourceKey<IAspect>... aspects) {
        if (!StageRules.of(level).hasRules(family)) {
            return true;
        }
        var key = block.builtInRegistryHolder().key();
        var target = StageTarget.recipe(family, key.location()).withSubject(key);
        for (var aspect : aspects) {
            target = target.withSubject(aspect);
        }
        return StageRules.allowsOwner(level, target, owner);
    }

    public record Condenser(boolean enabled, Optional<Integer> essentia, Optional<Integer> interval,
                            float flux, int vitium) {
        public static final Condenser DEFAULT = new Condenser(true, Optional.empty(), Optional.empty(), 1.0F, 1);
        private static final Codec<Condenser> CODEC = RecordCodecBuilder.create(i -> i.group(
                        Codecs.optionalFieldOf(Codec.BOOL, "enabled", true).forGetter(Condenser::enabled),
                        Codecs.optionalFieldOf(Codec.intRange(0, 100), "essentia").forGetter(Condenser::essentia),
                        Codecs.optionalFieldOf(Codec.intRange(1, 1200), "interval").forGetter(Condenser::interval),
                        Codecs.optionalFieldOf(Codec.floatRange(0.01F, 100.0F), "flux", 1.0F).forGetter(Condenser::flux),
                        Codecs.optionalFieldOf(Codec.intRange(1, 100), "vitium", 1).forGetter(Condenser::vitium))
                .apply(i, Condenser::new));
    }

    public record VisGenerator(boolean enabled, float vis, int energy, int maxPush) {
        public static final VisGenerator DEFAULT = new VisGenerator(true, 1.0F, 1000, 20);
        private static final Codec<VisGenerator> CODEC = RecordCodecBuilder.create(i -> i.group(
                        Codecs.optionalFieldOf(Codec.BOOL, "enabled", true).forGetter(VisGenerator::enabled),
                        Codecs.optionalFieldOf(Codec.floatRange(0.01F, 100.0F), "vis", 1.0F).forGetter(VisGenerator::vis),
                        Codecs.optionalFieldOf(Codec.intRange(1, 1000), "energy", 1000).forGetter(VisGenerator::energy),
                        Codecs.optionalFieldOf(Codec.intRange(1, 1000), "max_push", 20).forGetter(VisGenerator::maxPush))
                .apply(i, VisGenerator::new));
    }

    public record FluxScrubber(boolean enabled, int power, int removal, Optional<Integer> perRoll,
                              Optional<Float> chance, Optional<Integer> yield) {
        public static final FluxScrubber DEFAULT =
                new FluxScrubber(true, BlockEntityFluxScrubber.WORK_POWER, 1,
                        Optional.empty(), Optional.empty(), Optional.empty());
        private static final Codec<FluxScrubber> CODEC = RecordCodecBuilder.create(i -> i.group(
                        Codecs.optionalFieldOf(Codec.BOOL, "enabled", true).forGetter(FluxScrubber::enabled),
                        Codecs.optionalFieldOf(Codec.intRange(1, 100), "power", BlockEntityFluxScrubber.WORK_POWER)
                                .forGetter(FluxScrubber::power),
                        Codecs.optionalFieldOf(Codec.intRange(1, 8), "removal", 1).forGetter(FluxScrubber::removal),
                        Codecs.optionalFieldOf(Codec.intRange(1, 1000), "charges_per_roll").forGetter(FluxScrubber::perRoll),
                        Codecs.optionalFieldOf(Codec.floatRange(0.0F, 1.0F), "chance").forGetter(FluxScrubber::chance),
                        Codecs.optionalFieldOf(Codec.intRange(0, 64), "essentia").forGetter(FluxScrubber::yield))
                .apply(i, FluxScrubber::new));
    }
}
