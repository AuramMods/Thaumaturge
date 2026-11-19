package com.leclowndu93150.thaumaturge.content.alchemy;

import com.leclowndu93150.thaumaturge.client.taint.LiquidDeathClientExtensions;
import java.util.function.Consumer;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidType;

public final class LiquidDeathFluidType extends FluidType {
    public LiquidDeathFluidType(Properties properties) {
        super(properties);
    }

    @Override
    public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
        consumer.accept(new LiquidDeathClientExtensions());
    }
}
