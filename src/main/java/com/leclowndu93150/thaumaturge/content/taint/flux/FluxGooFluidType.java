package com.leclowndu93150.thaumaturge.content.taint.flux;

import com.leclowndu93150.thaumaturge.client.taint.FluxGooClientExtensions;
import java.util.function.Consumer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidType;

public final class FluxGooFluidType extends FluidType {
    private static final double DRAG = 0.0001D;

    public FluxGooFluidType(Properties properties) {
        super(properties);
    }

    @Override
    public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
        consumer.accept(new FluxGooClientExtensions());
    }

    @Override
    public double motionScale(Entity entity) {
        return DRAG;
    }
}
