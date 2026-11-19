package com.leclowndu93150.thaumaturge.content.wands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.wands.WandCap;
import com.mojang.serialization.Lifecycle;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class NativeTooltipRuntimeTest {
    private static Item charm;
    private static Item cap;

    @BeforeAll
    static void registerItems() {
        var forge = (ForgeRegistry<?>) ForgeRegistries.ITEMS;
        var vanilla = (MappedRegistry<?>) BuiltInRegistries.ITEM;
        forge.unfreeze();
        vanilla.unfreeze();
        try {
            charm = new ItemPrimalCharm(new Item.Properties());
            cap = new ItemWandCap(new Item.Properties(), () ->
                    new WandCap(1.0F, Map.of(TTAspects.AER, 0.8F), 1, id("texture")));
            ForgeRegistries.ITEMS.register(id("charm"), charm);
            ForgeRegistries.ITEMS.register(id("cap"), cap);
        } finally {
            forge.freeze();
            vanilla.freeze();
        }
    }

    @Test
    void nativeTooltipProvidesLevelTimeAndAllowsPlayerlessItemLists() {
        var level = mock(Level.class);
        when(level.getGameTime()).thenReturn(237L, 599L);
        var player = playerViewing(level);
        assertEquals(TextColor.fromLegacyFormat(ChatFormatting.GOLD),
                line(charm, player, "tooltip.thaumaturge.primal_charm.2").getStyle().getColor());
        line(charm, player, "tooltip.thaumaturge.primal_charm.0");
        line(charm, null, "tooltip.thaumaturge.primal_charm.0");
    }

    @Test
    void nativeCapTooltipUsesEachPlayersWorldLookupAndGrayWithoutOne() {
        assertCapDiscount(playerWithAspectColor("c"), ChatFormatting.RED);
        assertCapDiscount(playerWithAspectColor("9"), ChatFormatting.BLUE);
        assertCapDiscount(null, ChatFormatting.GRAY);
    }

    private static void assertCapDiscount(@Nullable Player player, ChatFormatting color) {
        var line = line(cap, player, "tooltip.thaumaturge.wand.discount.aspects");
        var content = assertInstanceOf(TranslatableContents.class, line.getContents());
        var amounts = assertInstanceOf(Component.class, content.getArgs()[0]);
        assertEquals(1, amounts.getSiblings().size());
        var entry = amounts.getSiblings().get(0);
        var discount = assertInstanceOf(TranslatableContents.class, entry.getContents());
        assertEquals("tooltip.thaumaturge.wand.discount.aspect", discount.getKey());
        assertEquals(20, discount.getArgs()[1]);
        var aspect = assertInstanceOf(Component.class, discount.getArgs()[0]);
        assertEquals(TextColor.fromLegacyFormat(color), aspect.getStyle().getColor());
        assertEquals("aspect.thaumaturge.aer", assertInstanceOf(TranslatableContents.class, aspect.getContents()).getKey());
    }

    private static Player playerWithAspectColor(String code) {
        var aspects = new MappedRegistry<IAspect>(IAspect.REGISTRY_KEY, Lifecycle.stable());
        var aer = mock(IAspect.class);
        when(aer.chatColor()).thenReturn(Optional.of(code));
        aspects.register(TTAspects.AER, aer, Lifecycle.stable());
        var level = mock(Level.class);
        when(level.registryAccess()).thenReturn(new RegistryAccess.ImmutableRegistryAccess(List.of(aspects)).freeze());
        return playerViewing(level);
    }

    private static Player playerViewing(Level level) {
        when(level.enabledFeatures()).thenReturn(FeatureFlags.DEFAULT_FLAGS);
        var player = mock(Player.class);
        when(player.level()).thenReturn(level);
        return player;
    }

    private static Component line(Item item, @Nullable Player player, String key) {
        return new ItemStack(item).getTooltipLines(player, TooltipFlag.NORMAL).stream()
                .filter(component -> component.getContents() instanceof TranslatableContents text
                        && text.getKey().equals(key))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing tooltip line " + key));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("port_tooltip", path);
    }
}
