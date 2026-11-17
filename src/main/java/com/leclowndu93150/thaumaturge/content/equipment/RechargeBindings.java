package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.api.items.ChargeProfile;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.itemdata.ItemDataKey;

public final class RechargeBindings implements RechargeAccess.Bindings {
    @Override
    public ItemDataKey<Integer> charge() {
        return TTDataComponents.CHARGE.get();
    }

    @Override
    public ItemDataKey<ChargeProfile> profile() {
        return TTDataComponents.RECHARGEABLE.get();
    }
}
