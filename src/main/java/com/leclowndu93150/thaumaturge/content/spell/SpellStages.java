package com.leclowndu93150.thaumaturge.content.spell;

import com.leclowndu93150.thaumaturge.api.spell.Spell;
import com.leclowndu93150.thaumaturge.api.spell.Spells;
import com.leclowndu93150.thaumaturge.api.spell.affinity.AspectAffinity;
import com.leclowndu93150.thaumaturge.stage.StageRules;
import com.leclowndu93150.thaumaturge.stage.StageTarget;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class SpellStages {
    private SpellStages() {}

    public static StageTarget target(ServerLevel level, ResourceLocation family, ItemStack focus, Spell spell) {
        var item = focus.getItem().builtInRegistryHolder().key();
        StageTarget target = focus.isEmpty() ? StageTarget.family(family)
                : StageTarget.recipe(family, item.location()).withSubject(item);
        var registries = level.registryAccess();
        for (var node : spell.nodes()) {
            target = target.withSubject(node.part());
            var aspect = Spells.part(registries, node.part()).flatMap(part -> part.aspect().resolve(node.aspect(), registries));
            if (aspect.isPresent()) {
                var aspectKey = aspect.orElseThrow();
                target = target.withSubject(aspectKey);
                if (Spells.affinity(registries, aspectKey).isPresent()) {
                    target = target.withSubject(ResourceKey.create(AspectAffinity.REGISTRY_KEY, aspectKey.location()));
                }
            }
        }
        return target;
    }

    public static boolean checkCast(LivingEntity caster, ItemStack focus, Spell spell) {
        return !(caster instanceof Player player) || !(caster.level() instanceof ServerLevel level)
                || !StageRules.of(level).hasRules(StageTarget.SPELL_CAST)
                || StageRules.check(level, target(level, StageTarget.SPELL_CAST, focus, spell), player);
    }
}
