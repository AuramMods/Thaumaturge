package com.leclowndu93150.thaumaturge.data.tag;

import com.leclowndu93150.thaumaturge.serialization.EffectCodecs;
import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.registry.TTEffectTags;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.common.data.ExistingFileHelper;

public final class TTMobEffectTagsProvider extends TagsProvider<MobEffect> {
    public TTMobEffectTagsProvider(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> lookupProvider,
            ExistingFileHelper existingFileHelper) {
        super(output, Registries.MOB_EFFECT, lookupProvider, TTIds.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookupProvider) {
        tag(TTEffectTags.MANA_BEAN_EFFECTS)
                .add(key(EffectCodecs.holder(MobEffects.MOVEMENT_SPEED)))
                .add(key(EffectCodecs.holder(MobEffects.MOVEMENT_SLOWDOWN)))
                .add(key(EffectCodecs.holder(MobEffects.DIG_SPEED)))
                .add(key(EffectCodecs.holder(MobEffects.DIG_SLOWDOWN)))
                .add(key(EffectCodecs.holder(MobEffects.DAMAGE_BOOST)))
                .add(key(EffectCodecs.holder(MobEffects.HEAL)))
                .add(key(EffectCodecs.holder(MobEffects.HARM)))
                .add(key(EffectCodecs.holder(MobEffects.JUMP)))
                .add(key(EffectCodecs.holder(MobEffects.CONFUSION)))
                .add(key(EffectCodecs.holder(MobEffects.REGENERATION)))
                .add(key(EffectCodecs.holder(MobEffects.DAMAGE_RESISTANCE)))
                .add(key(EffectCodecs.holder(MobEffects.FIRE_RESISTANCE)))
                .add(key(EffectCodecs.holder(MobEffects.WATER_BREATHING)))
                .add(key(EffectCodecs.holder(MobEffects.INVISIBILITY)))
                .add(key(EffectCodecs.holder(MobEffects.BLINDNESS)))
                .add(key(EffectCodecs.holder(MobEffects.NIGHT_VISION)))
                .add(key(EffectCodecs.holder(MobEffects.HUNGER)))
                .add(key(EffectCodecs.holder(MobEffects.WEAKNESS)))
                .add(key(EffectCodecs.holder(MobEffects.POISON)))
                .add(key(EffectCodecs.holder(MobEffects.WITHER)))
                .add(key(EffectCodecs.holder(MobEffects.HEALTH_BOOST)))
                .add(key(EffectCodecs.holder(MobEffects.ABSORPTION)))
                .add(key(EffectCodecs.holder(MobEffects.SATURATION)));
    }

    private static ResourceKey<MobEffect> key(Holder<MobEffect> effect) {
        return effect.unwrapKey().orElseThrow();
    }
}
