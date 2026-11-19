package com.leclowndu93150.thaumaturge.content.taint.entity.ability;

import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraits;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTMobTraits;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

final class TaintedHarvest {
    private static final float SOUND_VOLUME = 1.0F;
    private static final float SOUND_PITCH = 1.0F;

    private TaintedHarvest() {}

    static @Nullable InteractionResult interact(Player player, InteractionHand hand, ItemStack held, LivingEntity mob) {
        if (TaintedShearing.canShear(held)
                && mob instanceof Sheep sheep
                && MobTraits.has(sheep, TTMobTraits.key(TTMobTraits.TAINT_SHEARING))) {
            return TaintedShearing.shear(player, hand, held, sheep);
        }
        if (held.is(Items.BUCKET) && !mob.isBaby() && MobTraits.has(mob, TTMobTraits.key(TTMobTraits.FLUX_MILK))) {
            return milk(player, hand, held);
        }
        return null;
    }

    private static InteractionResult milk(Player player, InteractionHand hand, ItemStack bucket) {
        player.playSound(SoundEvents.COW_MILK, SOUND_VOLUME, SOUND_PITCH);
        player.setItemInHand(
                hand, ItemUtils.createFilledResult(bucket, player, new ItemStack(TTItems.BUCKET_FLUX_GOO.get())));
        return InteractionResult.SUCCESS;
    }
}
