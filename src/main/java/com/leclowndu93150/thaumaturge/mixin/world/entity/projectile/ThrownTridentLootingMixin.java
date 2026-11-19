package com.leclowndu93150.thaumaturge.mixin.world.entity.projectile;

import com.leclowndu93150.thaumaturge.content.entity.ProjectileLooting;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ThrownTrident.class)
abstract class ThrownTridentLootingMixin implements ProjectileLooting.ThrownWeapon {
    @Shadow
    private ItemStack tridentItem;

    @Override
    public int thaumaturge$weaponLooting() {
        return ProjectileLooting.levelOn(tridentItem);
    }
}
