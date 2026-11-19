package com.leclowndu93150.thaumaturge.recipe;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonObject;
import com.leclowndu93150.thaumaturge.content.recipe.SimpleRecipeSerializer;
import com.leclowndu93150.thaumaturge.serialization.RegistryContext;
import com.leclowndu93150.thaumaturge.serialization.stream.ByteBufCodecs;
import com.leclowndu93150.thaumaturge.serialization.stream.RegistryFriendlyByteBuf;
import com.leclowndu93150.thaumaturge.serialization.stream.StreamCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.DecoderException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.PacketDecoder;
import net.minecraft.network.PacketEncoder;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.ChatTypeDecoration;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.network.protocol.game.ServerboundKeepAlivePacket;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraftforge.registries.DataPackRegistryEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Native packet codecs, without dispatching a login handler or constructing a client world. */
public class RecipePacketRuntimeTest {
    private static final ResourceLocation TYPE_ID = new ResourceLocation("thaumaturge", "packet_fixture");
    private static final ResourceLocation ID = new ResourceLocation("thaumaturge", "packet_recipe");
    private static final ResourceKey<ChatType> VALUE = ResourceKey.create(Registries.CHAT_TYPE,
            new ResourceLocation("thaumaturge", "value"));
    private static final ResourceKey<ChatType> OTHER = ResourceKey.create(Registries.CHAT_TYPE,
            new ResourceLocation("thaumaturge", "other"));
    private static final ResourceKey<Registry<LinkedEntry>> LINKS =
            ResourceKey.createRegistryKey(new ResourceLocation("thaumaturge", "packet_links"));
    private static final ResourceKey<LinkedEntry> FIRST_LINK =
            ResourceKey.create(LINKS, new ResourceLocation("thaumaturge", "first"));
    private static final ResourceKey<LinkedEntry> LATER_LINK =
            ResourceKey.create(LINKS, new ResourceLocation("thaumaturge", "later"));
    private static final RecipeType<ChatRecipe> TYPE = new RecipeType<>() {};
    private static final MapCodec<ChatRecipe> CODEC = RegistryFixedCodec.create(Registries.CHAT_TYPE)
            .fieldOf("value").xmap(ChatRecipe::new, recipe -> recipe.value);
    private static final StreamCodec<RegistryFriendlyByteBuf, ChatRecipe> STREAM =
            ByteBufCodecs.holderRegistry(Registries.CHAT_TYPE).map(ChatRecipe::new, recipe -> recipe.value);
    private static final SimpleRecipeSerializer<ChatRecipe> SERIALIZER = new SimpleRecipeSerializer<>(CODEC, STREAM);

    private record LinkedEntry(Holder<Item> item, Holder<ChatType> chat, Optional<Holder<LinkedEntry>> next) {
        private static final Codec<LinkedEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                RegistryFixedCodec.create(Registries.ITEM).fieldOf("item").forGetter(LinkedEntry::item),
                RegistryFixedCodec.create(Registries.CHAT_TYPE).fieldOf("chat").forGetter(LinkedEntry::chat),
                RegistryFixedCodec.create(LINKS).optionalFieldOf("next").forGetter(LinkedEntry::next))
                .apply(instance, LinkedEntry::new));
    }

    public static final class ChatRecipe extends AbstractRecipe<SingleRecipeInput> {
        final Holder<ChatType> value;
        ChatRecipe(Holder<ChatType> value) { this.value = value; }
        @Override public boolean matches(SingleRecipeInput input, Level level) { return true; }
        @Override public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) { return ItemStack.EMPTY; }
        @Override public ItemStack getResultItem(HolderLookup.Provider registries) { return ItemStack.EMPTY; }
        @Override public boolean canCraftInDimensions(int width, int height) { return true; }
        @Override public RecipeSerializer<?> getSerializer() { return SERIALIZER; }
        @Override public RecipeType<?> getType() { return TYPE; }
    }

    @BeforeAll
    static void registerActualFixture() throws ReflectiveOperationException {
        ForgeRegistries.RECIPE_TYPES.register(TYPE_ID, TYPE);
        ForgeRegistries.RECIPE_SERIALIZERS.register(TYPE_ID, SERIALIZER);
        // The focused launcher skips Forge's datapack registry event.
        var event = new DataPackRegistryEvent.NewRegistry();
        event.dataPackRegistry(LINKS, LinkedEntry.CODEC, LinkedEntry.CODEC);
        var process = DataPackRegistryEvent.NewRegistry.class.getDeclaredMethod("process");
        process.setAccessible(true);
        process.invoke(event);
    }

    private RegistryAccess.Frozen registries(String prefix, boolean reversed) {
        MappedRegistry<ChatType> registry = new MappedRegistry<>(Registries.CHAT_TYPE, Lifecycle.stable());
        if (reversed) registry.register(OTHER, chat(prefix + "_other"), Lifecycle.stable());
        registry.register(VALUE, chat(prefix + "_value"), Lifecycle.stable());
        if (!reversed) registry.register(OTHER, chat(prefix + "_other"), Lifecycle.stable());
        return new RegistryAccess.ImmutableRegistryAccess(List.of(registry)).freeze();
    }

    private ChatType chat(String key) {
        return new ChatType(ChatTypeDecoration.withSender(key), ChatTypeDecoration.withSender(key));
    }

    private ChatRecipe recipe(RegistryAccess.Frozen lookup) {
        JsonObject json = new JsonObject();
        json.addProperty("value", VALUE.location().toString());
        return RegistryContext.withRegistries(lookup, () -> SERIALIZER.fromJson(ID, json));
    }

    private ClientboundLoginPacket login(RegistryAccess.Frozen lookup) {
        return new ClientboundLoginPacket(1, false, GameType.SURVIVAL, null, Set.of(Level.OVERWORLD), lookup,
                BuiltinDimensionTypes.OVERWORLD, Level.OVERWORLD, 0L, 2, 2, 2,
                false, true, false, false, Optional.empty(), 0);
    }

    private EmbeddedChannel decoder() {
        return decoder(PacketFlow.CLIENTBOUND);
    }

    private EmbeddedChannel decoder(PacketFlow flow) {
        EmbeddedChannel channel = new EmbeddedChannel(new PacketDecoder(flow));
        channel.attr(Connection.ATTRIBUTE_PROTOCOL).set(ConnectionProtocol.PLAY);
        return channel;
    }

    private ByteBuf encode(Packet<?> packet) {
        return encode(packet, PacketFlow.CLIENTBOUND);
    }

    private ByteBuf encode(Packet<?> packet, PacketFlow flow) {
        EmbeddedChannel encoder = new EmbeddedChannel(new PacketEncoder(flow));
        encoder.attr(Connection.ATTRIBUTE_PROTOCOL).set(ConnectionProtocol.PLAY);
        try {
            assertTrue(encoder.writeOutbound(packet));
            return encoder.readOutbound();
        } finally { encoder.finishAndReleaseAll(); }
    }

    private <T extends Packet<?>> T decode(EmbeddedChannel channel, Packet<?> packet, Class<T> type) {
        assertTrue(channel.writeInbound(encode(packet)));
        return type.cast(channel.readInbound());
    }

    @Test
    void nativeLoginLinksRegistryReferencesAndPreservesNumericIds() {
        var chatTypes = registries("registry", true).registryOrThrow(Registries.CHAT_TYPE);
        var links = new MappedRegistry<>(LINKS, Lifecycle.stable());
        var later = links.createRegistrationLookup().getOrThrow(LATER_LINK);
        var item = BuiltInRegistries.ITEM.wrapAsHolder(Items.STONE);
        links.registerMapping(3, FIRST_LINK,
                new LinkedEntry(item, chatTypes.getHolderOrThrow(VALUE), Optional.of(later)), Lifecycle.stable());
        links.registerMapping(7, LATER_LINK,
                new LinkedEntry(item, chatTypes.getHolderOrThrow(OTHER), Optional.empty()), Lifecycle.stable());
        var source = new RegistryAccess.ImmutableRegistryAccess(List.of(links, chatTypes)).freeze();

        var channel = decoder();
        try {
            var received = decode(channel, login(source), ClientboundLoginPacket.class).registryHolder();
            var receivedLinks = received.registryOrThrow(LINKS);
            var first = receivedLinks.get(FIRST_LINK);
            var receivedLater = receivedLinks.getHolderOrThrow(LATER_LINK);
            assertSame(item, first.item());
            assertSame(received.registryOrThrow(Registries.CHAT_TYPE).getHolderOrThrow(VALUE), first.chat());
            assertSame(receivedLater, first.next().orElseThrow());
            assertSame(receivedLater.value(), receivedLinks.byId(7));
            assertSame(first, receivedLinks.byId(3));
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    @Test
    void nativeLoginThenRecipeDecodingWorksBeforeAnyWorldHandlerRuns() {
        var source = registries("login", true);
        EmbeddedChannel channel = decoder();
        try {
            ClientboundLoginPacket login = decode(channel, login(source), ClientboundLoginPacket.class);
            assertNotSame(source, login.registryHolder());
            ClientboundUpdateRecipesPacket packet = decode(channel,
                    new ClientboundUpdateRecipesPacket(List.of(recipe(source))), ClientboundUpdateRecipesPacket.class);
            ChatRecipe value = (ChatRecipe) packet.getRecipes().get(0);
            assertEquals(ID, value.getId());
            assertSame(login.registryHolder().registryOrThrow(Registries.CHAT_TYPE).getHolderOrThrow(VALUE), value.value);
            assertEquals("login_value", value.value.value().chat().translationKey());
            assertSame(BuiltInRegistries.ITEM,
                    ((RegistryAccess) value.recipeRegistryLookup()).registryOrThrow(Registries.ITEM));
            assertNull(RegistryContext.scoped());
        } finally { channel.finishAndReleaseAll(); }
    }

    @Test
    void interleavedConnectionsKeepTheirOwnReceivedHolderIdentitiesAndNumericIds() {
        var first = registries("first", false);
        var second = registries("second", true);
        EmbeddedChannel a = decoder();
        EmbeddedChannel b = decoder();
        try {
            var aLogin = decode(a, login(first), ClientboundLoginPacket.class);
            var bLogin = decode(b, login(second), ClientboundLoginPacket.class);
            assertNotEquals(first.registryOrThrow(Registries.CHAT_TYPE).getId(first.registryOrThrow(Registries.CHAT_TYPE).get(VALUE)),
                    second.registryOrThrow(Registries.CHAT_TYPE).getId(second.registryOrThrow(Registries.CHAT_TYPE).get(VALUE)));
            for (int i = 0; i < 3; i++) {
                ChatRecipe aRecipe = (ChatRecipe) decode(a, new ClientboundUpdateRecipesPacket(List.of(recipe(first))),
                        ClientboundUpdateRecipesPacket.class).getRecipes().get(0);
                ChatRecipe bRecipe = (ChatRecipe) RegistryContext.withRegistries(first, () -> {
                    var packet = decode(b, new ClientboundUpdateRecipesPacket(List.of(recipe(second))),
                            ClientboundUpdateRecipesPacket.class);
                    assertSame(first, RegistryContext.scoped());
                    return packet.getRecipes().get(0);
                });
                assertSame(aLogin.registryHolder().registryOrThrow(Registries.CHAT_TYPE).getHolderOrThrow(VALUE), aRecipe.value);
                assertSame(bLogin.registryHolder().registryOrThrow(Registries.CHAT_TYPE).getHolderOrThrow(VALUE), bRecipe.value);
                assertEquals("first_value", aRecipe.value.value().chat().translationKey());
                assertEquals("second_value", bRecipe.value.value().chat().translationKey());
            }
            assertNull(RegistryContext.scoped());
        } finally { a.finishAndReleaseAll(); b.finishAndReleaseAll(); }
    }

    @Test
    void decodedRecipeCanBeEncodedLaterWithoutALiveWorldOrOuterScope() {
        var source = registries("relay", true);
        EmbeddedChannel a = decoder();
        EmbeddedChannel b = decoder();
        try {
            decode(a, login(source), ClientboundLoginPacket.class);
            var received = decode(a, new ClientboundUpdateRecipesPacket(List.of(recipe(source))), ClientboundUpdateRecipesPacket.class);
            var bLogin = decode(b, login(source), ClientboundLoginPacket.class);
            var relayed = decode(b, received, ClientboundUpdateRecipesPacket.class);
            ChatRecipe result = (ChatRecipe) relayed.getRecipes().get(0);
            assertSame(bLogin.registryHolder().registryOrThrow(Registries.CHAT_TYPE).getHolderOrThrow(VALUE), result.value);
            assertEquals("relay_value", result.value.value().chat().translationKey());
            assertEquals(ID, result.getId());
        } finally { a.finishAndReleaseAll(); b.finishAndReleaseAll(); }
    }

    @Test
    void vanillaRecipesAndKeepAlivePacketsStillWorkWithoutAClientWorld() {
        EmbeddedChannel channel = decoder();
        try {
            assertEquals(735L, decode(channel, new ClientboundKeepAlivePacket(735L), ClientboundKeepAlivePacket.class).getId());
            var vanilla = new ShapelessRecipe(ID, "packet", CraftingBookCategory.MISC, new ItemStack(Items.STONE, 2),
                    NonNullList.of(Ingredient.EMPTY, Ingredient.of(Items.COAL)));
            var packet = decode(channel, new ClientboundUpdateRecipesPacket(List.of(vanilla)), ClientboundUpdateRecipesPacket.class);
            ShapelessRecipe result = (ShapelessRecipe) packet.getRecipes().get(0);
            assertEquals(ID, result.getId());
            assertEquals(2, result.getResultItem(RegistryAccess.EMPTY).getCount());
            assertTrue(result.getIngredients().get(0).test(new ItemStack(Items.COAL)));
            decode(channel, login(registries("after", false)), ClientboundLoginPacket.class);
            assertEquals(736L, decode(channel, new ClientboundKeepAlivePacket(736L), ClientboundKeepAlivePacket.class).getId());
        } finally { channel.finishAndReleaseAll(); }
    }

    @Test
    void serverboundPacketsRetainTheirNativeDecodePath() {
        EmbeddedChannel channel = decoder(PacketFlow.SERVERBOUND);
        try {
            assertTrue(channel.writeInbound(encode(new ServerboundKeepAlivePacket(831L), PacketFlow.SERVERBOUND)));
            ServerboundKeepAlivePacket result = channel.readInbound();
            assertEquals(831L, result.getId());
        } finally { channel.finishAndReleaseAll(); }
    }

    @Test
    void registryDependentRecipeStillFailsWhenNoLoginWasReceived() {
        EmbeddedChannel channel = decoder();
        ByteBuf frame = encode(new ClientboundUpdateRecipesPacket(List.of(recipe(registries("absent", false)))));
        try {
            DecoderException failure = assertThrows(DecoderException.class, () -> channel.writeInbound(frame));
            assertTrue(failure.getMessage().contains("Missing registry"));
            assertNull(channel.readInbound());
            assertNull(RegistryContext.scoped());
        } finally { channel.finishAndReleaseAll(); }
        assertEquals(0, frame.refCnt());
    }

    @Test
    void trailingLoginBytesStillFailNativePacketValidation() {
        var valid = registries("valid", false);
        EmbeddedChannel channel = decoder();
        ByteBuf bad = encode(login(registries("invalid", true)));
        bad.writeByte(0);
        try {
            decode(channel, login(valid), ClientboundLoginPacket.class);
            DecoderException failure = assertThrows(DecoderException.class, () -> channel.writeInbound(bad));
            assertTrue(failure.getMessage().contains("larger than I expected"));
            assertNull(channel.readInbound());
        } finally { channel.finishAndReleaseAll(); }
        assertEquals(0, bad.refCnt());
    }

    @Test
    void unknownNumericRecipeHolderIdsFailWithoutLeakingBuffersOrScopes() {
        var source = registries("bad_id", false);
        EmbeddedChannel channel = decoder();
        ByteBuf bad = encode(new ClientboundUpdateRecipesPacket(List.of(recipe(source))));
        bad.setByte(bad.writerIndex() - 1, 127);
        try {
            decode(channel, login(source), ClientboundLoginPacket.class);
            assertThrows(DecoderException.class, () -> channel.writeInbound(bad));
            assertNull(channel.readInbound());
            assertNull(RegistryContext.scoped());
        } finally { channel.finishAndReleaseAll(); }
        assertEquals(0, bad.refCnt());
    }
}
