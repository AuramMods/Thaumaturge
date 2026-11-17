package com.leclowndu93150.thaumaturge.api.spell.affinity;

import com.leclowndu93150.thaumaturge.serialization.Codecs;
import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.api.spell.SpellRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

/**
 * What one aspect's energy does when a spell channels it. The entry id equals the aspect id, so
 * {@code data/thaumaturge/thaumaturge/spell_affinity/ignis.json} belongs to {@code thaumaturge:ignis}.
 *
 * <p>Aspect-driven parts (strike, burst, imbue) run the matching action list. An aspect with no
 * affinity cannot be picked for them; an addon gives its aspects spell reactions with JSON alone.
 *
 * @param fx         the {@code SpellFxStyle} id used for impacts
 * @param complexity the complexity added to a node whose aspect the player picked
 * @param vis        the vis per cast added to a node whose aspect the player picked
 * @param research   the research needed before the aspect can be channeled, on top of discovering it
 * @param sound      the sound played at the caster when a spell channeling this aspect is cast
 * @param entity     the actions run on entity targets by strikes and bursts
 * @param block      the actions run on block targets by strikes and bursts
 * @param imbue      the actions run by imbue effects
 * @since 1.0.0
 */
public record AspectAffinity(
        ResourceLocation fx,
        int complexity,
        float vis,
        Optional<ResearchGate> research,
        Optional<Holder<SoundEvent>> sound,
        List<SpellAction> entity,
        List<SpellAction> block,
        List<SpellAction> imbue) {
    /** The datapack registry key of affinities. */
    public static final ResourceKey<Registry<AspectAffinity>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(TTIds.rl("spell_affinity"));

    /** Disk and network codec. */
    public static final Codec<AspectAffinity> CODEC = RecordCodecBuilder.create(i -> i.group(
                    Codecs.optionalFieldOf(ResourceLocation.CODEC, "fx", TTIds.rl("sparkle"))
                            .forGetter(AspectAffinity::fx),
                    Codecs.optionalFieldOf(Codec.INT, "complexity", 0).forGetter(AspectAffinity::complexity),
                    Codecs.optionalFieldOf(Codec.FLOAT, "vis", 0.0F).forGetter(AspectAffinity::vis),
                    Codecs.optionalFieldOf(ResearchGate.CODEC, "research").forGetter(AspectAffinity::research),
                    Codecs.optionalFieldOf(SoundEvent.CODEC, "sound").forGetter(AspectAffinity::sound),
                    Codecs.optionalFieldOf(SpellRegistries.ACTION_CODEC
                            .listOf(), "entity", List.of())
                            .forGetter(AspectAffinity::entity),
                    Codecs.optionalFieldOf(SpellRegistries.ACTION_CODEC
                            .listOf(), "block", List.of())
                            .forGetter(AspectAffinity::block),
                    Codecs.optionalFieldOf(SpellRegistries.ACTION_CODEC
                            .listOf(), "imbue", List.of())
                            .forGetter(AspectAffinity::imbue))
            .apply(i, AspectAffinity::new));

    /**
     * Canonicalizes the lists into immutable copies.
     */
    public AspectAffinity {
        entity = List.copyOf(entity);
        block = List.copyOf(block);
        imbue = List.copyOf(imbue);
    }
}
