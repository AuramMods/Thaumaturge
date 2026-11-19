package com.leclowndu93150.thaumaturge.client.extensions;

import com.leclowndu93150.thaumaturge.client.model.AdvancedAlchemicalFurnaceItemSpecialRenderer;
import com.leclowndu93150.thaumaturge.client.model.DeconTableItemSpecialRenderer;
import com.leclowndu93150.thaumaturge.client.model.EldritchObeliskItemSpecialRenderer;
import com.leclowndu93150.thaumaturge.client.model.GolemBuilderItemSpecialRenderer;
import com.leclowndu93150.thaumaturge.client.model.GolemItemSpecialRenderer;
import com.leclowndu93150.thaumaturge.client.model.HungryChestItemSpecialRenderer;
import com.leclowndu93150.thaumaturge.client.model.JarBrainItemSpecialRenderer;
import com.leclowndu93150.thaumaturge.client.model.JarItemSpecialRenderer;
import com.leclowndu93150.thaumaturge.client.model.JarNodeItemSpecialRenderer;
import com.leclowndu93150.thaumaturge.client.model.NitorItemSpecialRenderer;
import com.leclowndu93150.thaumaturge.client.model.NodeStabilizerItemSpecialRenderer;
import com.leclowndu93150.thaumaturge.client.model.ResearchTableItemSpecialRenderer;
import com.leclowndu93150.thaumaturge.client.model.WandItemSpecialRenderer;
import com.leclowndu93150.thaumaturge.content.item.RenderedBlockItem;
import java.util.function.Supplier;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.util.Lazy;

public final class TTItemRenderExtensions {
    public static final IClientItemExtensions JAR = lazy(JarItemSpecialRenderer::new);
    public static final IClientItemExtensions JAR_BRAIN = lazy(JarBrainItemSpecialRenderer::new);
    public static final IClientItemExtensions JAR_NODE = lazy(JarNodeItemSpecialRenderer::new);
    public static final IClientItemExtensions ADVANCED_ALCHEMICAL_FURNACE =
            lazy(AdvancedAlchemicalFurnaceItemSpecialRenderer::new);
    public static final IClientItemExtensions GOLEM_BUILDER = lazy(GolemBuilderItemSpecialRenderer::new);
    public static final IClientItemExtensions GOLEM_PLACER = lazy(GolemItemSpecialRenderer::new);
    public static final IClientItemExtensions WAND = lazy(WandItemSpecialRenderer::new);

    private TTItemRenderExtensions() {}

    public static IClientItemExtensions forBlock(Supplier<RenderedBlockItem.Renderer> renderer) {
        return lazy(() -> switch (renderer.get()) {
            case DECONSTRUCTION_TABLE -> new DeconTableItemSpecialRenderer();
            case RESEARCH_TABLE -> new ResearchTableItemSpecialRenderer();
            case NODE_STABILIZER -> new NodeStabilizerItemSpecialRenderer(false);
            case NODE_STABILIZER_ADVANCED -> new NodeStabilizerItemSpecialRenderer(true);
            case NODE_TRANSDUCER -> new NodeStabilizerItemSpecialRenderer(false, true);
            case HUNGRY_CHEST -> new HungryChestItemSpecialRenderer();
            case ELDRITCH_OBELISK -> new EldritchObeliskItemSpecialRenderer();
            case NITOR -> new NitorItemSpecialRenderer();
        });
    }

    private static IClientItemExtensions lazy(Supplier<BlockEntityWithoutLevelRenderer> factory) {
        return new BewlrExtension(factory);
    }

    private static final class BewlrExtension implements IClientItemExtensions {
        private final Lazy<BlockEntityWithoutLevelRenderer> renderer;

        private BewlrExtension(Supplier<BlockEntityWithoutLevelRenderer> factory) {
            this.renderer = Lazy.of(factory);
        }

        @Override
        public BlockEntityWithoutLevelRenderer getCustomRenderer() {
            return renderer.get();
        }
    }
}
