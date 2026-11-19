package com.leclowndu93150.thaumaturge.client.tooltip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.items.ILabel;
import com.mojang.serialization.Lifecycle;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@SuppressWarnings("removal")
class AspectContainerTooltipRuntimeTest {
    private static final ResourceLocation LABEL_ID = ResourceLocation.fromNamespaceAndPath("port_tooltip", "label");
    private static final ResourceKey<IAspect> FILTER = ResourceKey.create(IAspect.REGISTRY_KEY, LABEL_ID);
    private static Item label;

    @BeforeAll
    static void registerLabel() {
        var forge = (ForgeRegistry<?>) ForgeRegistries.ITEMS;
        var vanilla = (MappedRegistry<?>) BuiltInRegistries.ITEM;
        forge.unfreeze();
        vanilla.unfreeze();
        try {
            label = new Label();
            ForgeRegistries.ITEMS.register(LABEL_ID, label);
        } finally {
            forge.freeze();
            vanilla.freeze();
        }
    }

    @Test
    void nativeTooltipUsesTheEventPlayerWorldOrTheActiveClientWorld() {
        Minecraft client = mock(Minecraft.class);
        client.screen = mock(Screen.class);
        ClientLevel clientLevel = mock(ClientLevel.class);
        RegistryAccess clientRegistries = registries(0x123456);
        when(clientLevel.registryAccess()).thenReturn(clientRegistries);
        client.level = clientLevel;
        Player player = mock(Player.class);
        Level eventLevel = mock(Level.class);
        RegistryAccess eventRegistries = registries(0xABCDEF);
        when(player.level()).thenReturn(eventLevel);
        when(eventLevel.registryAccess()).thenReturn(eventRegistries);
        when(eventLevel.enabledFeatures()).thenReturn(FeatureFlags.DEFAULT_FLAGS);

        MinecraftForge.EVENT_BUS.register(AspectContainerTooltipHandler.class);
        try (var minecraft = mockStatic(Minecraft.class)) {
            minecraft.when(Minecraft::getInstance).thenReturn(client);
            ItemStack stack = new ItemStack(label);
            assertAspect(stack.getTooltipLines(player, TooltipFlag.Default.NORMAL), 0xABCDEF);
            assertAspect(stack.getTooltipLines(null, TooltipFlag.Default.NORMAL), 0x123456);

            when(eventLevel.registryAccess()).thenReturn(RegistryAccess.EMPTY);
            assertEquals(1, stack.getTooltipLines(player, TooltipFlag.Default.NORMAL).size());
            client.level = null;
            assertEquals(1, stack.getTooltipLines(null, TooltipFlag.Default.NORMAL).size());
            client.level = clientLevel;
            client.screen = null;
            assertEquals(1, stack.getTooltipLines(null, TooltipFlag.Default.NORMAL).size());
        } finally {
            MinecraftForge.EVENT_BUS.unregister(AspectContainerTooltipHandler.class);
        }
    }

    private static RegistryAccess registries(int color) {
        MappedRegistry<IAspect> registry = new MappedRegistry<>(IAspect.REGISTRY_KEY, Lifecycle.stable());
        IAspect aspect = mock(IAspect.class);
        when(aspect.tag()).thenReturn("fixture");
        when(aspect.color()).thenReturn(color);
        Registry.register(registry, FILTER, aspect);
        return new RegistryAccess.ImmutableRegistryAccess(List.of(registry.freeze()));
    }

    private static void assertAspect(List<Component> lines, int color) {
        assertEquals(3, lines.size());
        assertEquals("tooltip.thaumaturge.aspects.header", ((TranslatableContents) lines.get(1).getContents()).getKey());
        assertEquals(color, lines.get(2).getStyle().getColor().getValue());
    }

    private static final class Label extends Item implements ILabel {
        private Label() {
            super(new Properties());
        }

        @Override
        public ResourceKey<IAspect> getFilteredAspect(ItemStack stack) {
            return FILTER;
        }
    }
}
