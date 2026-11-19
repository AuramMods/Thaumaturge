package com.leclowndu93150.thaumaturge.content.item;

import com.leclowndu93150.thaumaturge.client.extensions.TTItemRenderExtensions;
import java.util.function.Consumer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

/** A plain block item with a custom renderer on the physical client. */
public final class RenderedBlockItem extends BlockItem {
    public enum Renderer {
        DECONSTRUCTION_TABLE,
        RESEARCH_TABLE,
        NODE_STABILIZER,
        NODE_STABILIZER_ADVANCED,
        NODE_TRANSDUCER,
        HUNGRY_CHEST,
        ELDRITCH_OBELISK,
        NITOR
    }

    private final Renderer renderer;

    public RenderedBlockItem(Block block, Properties properties, Renderer renderer) {
        super(block, properties);
        this.renderer = renderer;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        // Forge calls this from the Item constructor, before the renderer field is assigned.
        consumer.accept(TTItemRenderExtensions.forBlock(() -> renderer));
    }
}
