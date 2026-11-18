package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.particle.AirGustParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.BlockRunesParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.BoltParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.BoreDebrisParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.BoreSparkleParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.BubbleParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.BurstParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.CrackShardParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.CurlyWispParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.EarthPebbleParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.EssentiaDropParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.FireMoteParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.FlameFanParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.FluxGooDropletParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.FluxSwirlParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.FrostFlakeParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.GolemEmoteParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.InfusionCrumbsParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.LightningFlashParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.NitorCoreParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.RiftShardParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.ScanGlyphParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.ShieldSparkParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.SlashParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.SlimyBubbleParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.SmokeSpiralParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.SparkParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.SparkleParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.StabilizerRuneParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.TTColorParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.TaintFumeParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.TaintSwarmParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.VentParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.VisSparkleParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.WardFlashParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.WispFlameParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.WispParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.WispyMoteParticleOptions;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public final class TTParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, TTIds.MODID);

    public static final RegistryObject<TTParticleType<SparkleParticleOptions>> SPARKLE =
            register("sparkle", SparkleParticleOptions.CODEC, SparkleParticleOptions.STREAM_CODEC);

    public static final RegistryObject<TTParticleType<WispyMoteParticleOptions>> WISPY_MOTE =
            register("wispy_mote", WispyMoteParticleOptions.CODEC, WispyMoteParticleOptions.STREAM_CODEC);

    public static final RegistryObject<TTParticleType<CurlyWispParticleOptions>> CURLY_WISP =
            register("curly_wisp", CurlyWispParticleOptions.CODEC, CurlyWispParticleOptions.STREAM_CODEC);

    public static final RegistryObject<TTParticleType<WispFlameParticleOptions>> WISP_FLAME =
            register("wisp_flame", WispFlameParticleOptions.CODEC, WispFlameParticleOptions.STREAM_CODEC);

    public static final RegistryObject<TTParticleType<TaintFumeParticleOptions>> TAINT_FUME =
            register("taint_fume", TaintFumeParticleOptions.CODEC, TaintFumeParticleOptions.STREAM_CODEC);

    public static final RegistryObject<TTParticleType<TaintSwarmParticleOptions>> TAINT_SWARM =
            register("taint_swarm", TaintSwarmParticleOptions.CODEC, TaintSwarmParticleOptions.STREAM_CODEC);

    public static final RegistryObject<TTParticleType<LightningFlashParticleOptions>> LIGHTNING_FLASH =
            register(
                    "lightning_flash", LightningFlashParticleOptions.CODEC, LightningFlashParticleOptions.STREAM_CODEC);

    public static final RegistryObject<TTParticleType<StabilizerRuneParticleOptions>> STABILIZER_RUNE =
            register(
                    "stabilizer_rune", StabilizerRuneParticleOptions.CODEC, StabilizerRuneParticleOptions.STREAM_CODEC);

    public static final RegistryObject<TTParticleType<BubbleParticleOptions>> BUBBLE =
            register("bubble", BubbleParticleOptions.CODEC, BubbleParticleOptions.STREAM_CODEC);

    public static final RegistryObject<TTParticleType<SlimyBubbleParticleOptions>> SLIMY_BUBBLE =
            register("slimy_bubble", SlimyBubbleParticleOptions.CODEC, SlimyBubbleParticleOptions.STREAM_CODEC);

    public static final RegistryObject<TTParticleType<SparkParticleOptions>> SPARK =
            register("spark", SparkParticleOptions.CODEC, SparkParticleOptions.STREAM_CODEC);

    public static final RegistryObject<TTParticleType<BurstParticleOptions>> BURST =
            register("burst", BurstParticleOptions.CODEC, BurstParticleOptions.STREAM_CODEC);

    public static final RegistryObject<TTParticleType<ScanGlyphParticleOptions>> SCAN_GLYPH =
            register("scan_glyph", ScanGlyphParticleOptions.CODEC, ScanGlyphParticleOptions.STREAM_CODEC);

    public static final RegistryObject<TTParticleType<SlashParticleOptions>> SLASH =
            register("slash", SlashParticleOptions.CODEC, SlashParticleOptions.STREAM_CODEC);

    public static final RegistryObject<TTColorParticleType> PUFF = color("puff");
    public static final RegistryObject<TTColorParticleType> FLASH = color("flash");
    public static final RegistryObject<TTColorParticleType> FOCUS_CLOUD = color("focus_cloud");
    public static final RegistryObject<TTColorParticleType> BLOCK_MIST = color("block_mist");
    public static final RegistryObject<TTColorParticleType> MIST_FLAT = color("mist_flat");
    public static final RegistryObject<TTColorParticleType> GOO_DRIP = color("goo_drip");
    public static final RegistryObject<TTColorParticleType> LEAF_MOTE = color("leaf_mote");

    public static final RegistryObject<SimpleParticleType> LEVITATOR_MIST = simple("levitator_mist");
    public static final RegistryObject<SimpleParticleType> GOLEM_TRAIL = simple("golem_trail");
    public static final RegistryObject<SimpleParticleType> POLLUTION_FUME = simple("pollution_fume");
    public static final RegistryObject<SimpleParticleType> PECH_CURSE = simple("pech_curse");
    public static final RegistryObject<SimpleParticleType> CRIMSON_SMOKE = simple("crimson_smoke");

    public static final RegistryObject<TTParticleType<FireMoteParticleOptions>> FIRE_MOTE =
            register("fire_mote", FireMoteParticleOptions.CODEC, FireMoteParticleOptions.STREAM_CODEC);

    public static final RegistryObject<TTParticleType<NitorCoreParticleOptions>> NITOR_CORE =
            register("nitor_core", NitorCoreParticleOptions.CODEC, NitorCoreParticleOptions.STREAM_CODEC);

    public static final RegistryObject<TTParticleType<VentParticleOptions>> VENT =
            register("vent", VentParticleOptions.CODEC, VentParticleOptions.STREAM_CODEC);

    public static final RegistryObject<TTParticleType<BlockRunesParticleOptions>> BLOCK_RUNES =
            register("block_runes", BlockRunesParticleOptions.CODEC, BlockRunesParticleOptions.STREAM_CODEC);

    public static final RegistryObject<TTParticleType<SmokeSpiralParticleOptions>> SMOKE_SPIRAL =
            register("smoke_spiral", SmokeSpiralParticleOptions.CODEC, SmokeSpiralParticleOptions.STREAM_CODEC);

    public static final RegistryObject<TTParticleType<VisSparkleParticleOptions>> VIS_SPARKLE =
            register("vis_sparkle", VisSparkleParticleOptions.CODEC, VisSparkleParticleOptions.STREAM_CODEC);

    public static final RegistryObject<TTParticleType<WispParticleOptions>> WISP =
            register("wisp", WispParticleOptions.CODEC, WispParticleOptions.STREAM_CODEC);

    public static final RegistryObject<TTParticleType<BoltParticleOptions>> BOLT =
            register("bolt", BoltParticleOptions.CODEC, BoltParticleOptions.STREAM_CODEC);

    public static final RegistryObject<TTParticleType<BoreDebrisParticleOptions>> BORE_DEBRIS =
            register("bore_debris", BoreDebrisParticleOptions.CODEC, BoreDebrisParticleOptions.STREAM_CODEC);

    public static final RegistryObject<TTParticleType<InfusionCrumbsParticleOptions>> INFUSION_CRUMBS =
            register(
                    "infusion_crumbs", InfusionCrumbsParticleOptions.CODEC, InfusionCrumbsParticleOptions.STREAM_CODEC);

    public static final RegistryObject<TTParticleType<BoreSparkleParticleOptions>> BORE_SPARKLE =
            register("bore_sparkle", BoreSparkleParticleOptions.CODEC, BoreSparkleParticleOptions.STREAM_CODEC);

    public static final RegistryObject<TTParticleType<EssentiaDropParticleOptions>> ESSENTIA_DROP =
            register("essentia_drop", EssentiaDropParticleOptions.CODEC, EssentiaDropParticleOptions.STREAM_CODEC);

    public static final RegistryObject<TTParticleType<FluxGooDropletParticleOptions>> FLUX_GOO_DROPLET =
            register(
                    "flux_goo_droplet",
                    FluxGooDropletParticleOptions.CODEC,
                    FluxGooDropletParticleOptions.STREAM_CODEC);

    public static final RegistryObject<TTParticleType<ShieldSparkParticleOptions>> SHIELD_SPARK =
            register("shield_spark", ShieldSparkParticleOptions.CODEC, ShieldSparkParticleOptions.STREAM_CODEC);

    public static final RegistryObject<TTParticleType<WardFlashParticleOptions>> WARD_FLASH =
            register("ward_flash", WardFlashParticleOptions.CODEC, WardFlashParticleOptions.STREAM_CODEC);

    public static final RegistryObject<TTParticleType<FlameFanParticleOptions>> FLAME_FAN =
            register("flame_fan", FlameFanParticleOptions.CODEC, FlameFanParticleOptions.STREAM_CODEC);

    public static final RegistryObject<TTParticleType<CrackShardParticleOptions>> CRACK_SHARD =
            register("crack_shard", CrackShardParticleOptions.CODEC, CrackShardParticleOptions.STREAM_CODEC);

    public static final RegistryObject<TTParticleType<AirGustParticleOptions>> AIR_GUST =
            register("air_gust", AirGustParticleOptions.CODEC, AirGustParticleOptions.STREAM_CODEC);

    public static final RegistryObject<TTParticleType<EarthPebbleParticleOptions>> EARTH_PEBBLE =
            register("earth_pebble", EarthPebbleParticleOptions.CODEC, EarthPebbleParticleOptions.STREAM_CODEC);

    public static final RegistryObject<TTParticleType<FrostFlakeParticleOptions>> FROST_FLAKE =
            register("frost_flake", FrostFlakeParticleOptions.CODEC, FrostFlakeParticleOptions.STREAM_CODEC);

    public static final RegistryObject<TTParticleType<FluxSwirlParticleOptions>> FLUX_SWIRL =
            register("flux_swirl", FluxSwirlParticleOptions.CODEC, FluxSwirlParticleOptions.STREAM_CODEC);

    public static final RegistryObject<TTParticleType<RiftShardParticleOptions>> RIFT_SHARD =
            register("rift_shard", RiftShardParticleOptions.CODEC, RiftShardParticleOptions.STREAM_CODEC);

    public static final RegistryObject<TTParticleType<GolemEmoteParticleOptions>> GOLEM_EMOTE =
            register("golem_emote", GolemEmoteParticleOptions.CODEC, GolemEmoteParticleOptions.STREAM_CODEC);

    public static final RegistryObject<SimpleParticleType> CURSE_SMOKE = simple("curse_smoke");
    public static final RegistryObject<SimpleParticleType> PRIMAL_FLARE = simple("primal_flare");
    public static final RegistryObject<SimpleParticleType> HEAL_FLASH = simple("heal_flash");

    public static final RegistryObject<SimpleParticleType> TAINT_SPLOSION = simple("taint_splosion");

    private TTParticles() {}

    private static <T extends TTParticleOptions> RegistryObject<TTParticleType<T>> register(
            String name, MapCodec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
        return PARTICLES.register(name, () -> new TTParticleType<>(codec, streamCodec));
    }

    private static RegistryObject<TTColorParticleType> color(String name) {
        return PARTICLES.register(name, TTColorParticleType::new);
    }

    private static RegistryObject<SimpleParticleType> simple(String name) {
        return PARTICLES.register(name, () -> new SimpleParticleType(false));
    }

    public static TTColorParticleOptions colorOf(RegistryObject<TTColorParticleType> type, int color) {
        return new TTColorParticleOptions(type.get(), color);
    }

    public static TTColorParticleOptions colorOf(
            RegistryObject<TTColorParticleType> type, float r, float g, float b) {
        return TTColorParticleOptions.create(type.get(), r, g, b);
    }

    public static void register(IEventBus modBus) {
        PARTICLES.register(modBus);
    }
}
