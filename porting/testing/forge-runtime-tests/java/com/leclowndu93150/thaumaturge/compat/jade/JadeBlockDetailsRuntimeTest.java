package com.leclowndu93150.thaumaturge.compat.jade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.leclowndu93150.thaumaturge.serialization.TTNbt;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class JadeBlockDetailsRuntimeTest {
    private static final ResourceLocation OPTION = ResourceLocation.fromNamespaceAndPath("thaumaturge", "display.jars");

    @Test
    void roundTripsLocalizedStyledComponentsAndOptionalFlagsThroughNbt() {
        Component amount = Component.translatable("jade.thaumaturge.aspect_amount", Component.literal("Aer"), 7)
                .withStyle(ChatFormatting.AQUA)
                .append(Component.literal("!").withStyle(ChatFormatting.BOLD));
        var original = new JadeBlockDetails(OPTION, List.of(amount), List.of(Component.literal("More")),
                Optional.of(Component.translatable("block.thaumaturge.jar")), true);
        CompoundTag payload = new CompoundTag();
        TTNbt.store(payload, "details", JadeBlockDetails.CODEC, original);
        JadeBlockDetails decoded = TTNbt.read(payload, "details", JadeBlockDetails.CODEC).orElseThrow();
        assertEquals(OPTION, decoded.option());
        assertEquals(Component.Serializer.toJson(amount), Component.Serializer.toJson(decoded.summary().get(0)));
        assertEquals("More", decoded.details().get(0).getString());
        assertEquals(Component.Serializer.toJson(original.title().orElseThrow()),
                Component.Serializer.toJson(decoded.title().orElseThrow()));
        assertTrue(decoded.hideFluid());

        var defaults = new JadeBlockDetails(OPTION, List.of(), List.of(), Optional.empty(), false);
        TTNbt.store(payload, "defaults", JadeBlockDetails.CODEC, defaults);
        assertEquals(defaults, TTNbt.read(payload, "defaults", JadeBlockDetails.CODEC).orElseThrow());
    }

    @Test
    void malformedComponentProducesACodecError() {
        CompoundTag payload = new CompoundTag();
        payload.putString("option", OPTION.toString());
        var summary = new ListTag();
        summary.add(StringTag.valueOf("{"));
        payload.put("summary", summary);
        payload.put("details", new ListTag());
        assertTrue(JadeBlockDetails.CODEC.parse(NbtOps.INSTANCE, payload).error().isPresent());
    }
}
