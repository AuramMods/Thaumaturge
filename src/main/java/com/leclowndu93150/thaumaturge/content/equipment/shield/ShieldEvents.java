package com.leclowndu93150.thaumaturge.content.equipment.shield;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.registry.TTDamageTypeTags;
import com.leclowndu93150.thaumaturge.registry.TTItemTags;
import com.leclowndu93150.thaumaturge.serialization.EffectCodecs;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent.ImpactResult;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = TTIds.MODID)
public final class ShieldEvents {
    private static final int SAP_TICKS = 100;
    private static final int SAP_AMPLIFIER = 0;

    private ShieldEvents() {}

    public static boolean wards(LivingEntity blocker, DamageSource source) {
        return source.is(TTDamageTypeTags.WARDED_BY_SHIELDS)
                && ShieldReflection.blockingItem(blocker).is(TTItemTags.SHIELDS_WARDING);
    }

    public static void sapAttacker(LivingEntity blocker, DamageSource source) {
        if (ShieldReflection.blockingItem(blocker).is(TTItemTags.SHIELDS_SAPPING)
                && !source.is(DamageTypeTags.IS_PROJECTILE)
                && source.getDirectEntity() instanceof LivingEntity attacker) {
            attacker.addEffect(EffectCodecs.instance(MobEffects.WEAKNESS, SAP_TICKS, SAP_AMPLIFIER), blocker);
        }
    }

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (event.getImpactResult() != ImpactResult.DEFAULT
                || !(event.getRayTraceResult() instanceof EntityHitResult hit)
                || !(hit.getEntity() instanceof LivingEntity blocker)) {
            return;
        }
        ItemStack shield = ShieldReflection.blockingItem(blocker);
        Projectile projectile = event.getProjectile();
        if (!shield.is(TTItemTags.SHIELDS_REFLECTING) || !ShieldReflection.facing(blocker, projectile.position())) {
            return;
        }
        event.setImpactResult(ImpactResult.SKIP_ENTITY);
        if (projectile instanceof MirrorShieldArrow arrow) arrow.thaumaturge$markMirrorReflection(blocker.getId());
        ShieldReflection.reflect(projectile, blocker, shield);
    }
}
