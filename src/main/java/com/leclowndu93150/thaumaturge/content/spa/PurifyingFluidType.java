package com.leclowndu93150.thaumaturge.content.spa;

import com.leclowndu93150.thaumaturge.client.taint.PurifyingClientExtensions;
import java.util.function.Consumer;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidType;

public final class PurifyingFluidType extends FluidType {
    public PurifyingFluidType(Properties properties) {
        super(properties);
    }

    @Override
    public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
        consumer.accept(new PurifyingClientExtensions());
    }
}
