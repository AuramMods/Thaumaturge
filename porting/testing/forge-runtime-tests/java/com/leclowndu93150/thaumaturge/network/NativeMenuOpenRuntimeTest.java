package com.leclowndu93150.thaumaturge.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.network.NetworkConstants;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class NativeMenuOpenRuntimeTest {
    private static final Component TITLE = Component.translatable("gui.thaumaturge.menu_test");
    private static MenuType<WireMenu> type;

    @BeforeAll
    static void registerNativeMenu() {
        NetworkConstants.init();
        var forge = (ForgeRegistry<?>) ForgeRegistries.MENU_TYPES;
        var vanilla = (MappedRegistry<?>) BuiltInRegistries.MENU;
        forge.unfreeze();
        vanilla.unfreeze();
        try {
            type = IForgeMenuType.create((id, inventory, buf) -> new WireMenu(id, inventory, buf == null || !buf.isReadable()
                    ? null : new Reference(buf.readBlockPos(), buf.readEnum(Direction.class), buf.readVarInt(),
                            buf.readBoolean())));
            ForgeRegistries.MENU_TYPES.register(new ResourceLocation("port_menu", "wire"), type);
        } finally {
            forge.freeze();
            vanilla.freeze();
        }
    }

    @Test
    void nativePacketAndFactoryRetainMenuWindowAndExtraFields() throws Exception {
        var expected = new Reference(new BlockPos(-127, 87, 625), Direction.WEST, 761, false);
        getOpenedMenu(buf -> {
            buf.writeBlockPos(expected.pos);
            buf.writeEnum(expected.face);
            buf.writeVarInt(expected.entity);
            buf.writeBoolean(expected.mainHand);
        }, expected);
    }

    @Test
    void nativeOpenPreservesAnEmptyMenuPayload() throws Exception {
        getOpenedMenu(null, null);
    }

    private static void getOpenedMenu(Consumer<FriendlyByteBuf> writer, Reference expected) throws Exception {
        var player = mock(ServerPlayer.class);
        when(player.level()).thenReturn(mock(ServerLevel.class));
        var serverInventory = new Inventory(player);
        when(player.getInventory()).thenReturn(serverInventory);
        player.containerCounter = 42;
        doAnswer(call -> {
            player.containerCounter++;
            return null;
        }).when(player).nextContainerCounter();
        var connection = mock(Connection.class);
        var listener = mock(ServerGamePacketListenerImpl.class);
        var connectionField = ServerGamePacketListenerImpl.class.getField("connection");
        connectionField.setAccessible(true);
        connectionField.set(listener, connection);
        player.connection = listener;
        var provider = new SimpleMenuProvider(
                (id, inventory, owner) -> new WireMenu(id, inventory, expected), TITLE);

        if (writer == null) {
            NetworkHooks.openScreen(player, provider);
        } else {
            NetworkHooks.openScreen(player, provider, writer);
        }
        var server = assertInstanceOf(WireMenu.class, player.containerMenu);
        assertEquals(43, server.containerId);
        assertSame(serverInventory, server.inventory);
        verify(player).doCloseContainer();
        verify(player).initMenu(server);

        var packet = ArgumentCaptor.forClass(Packet.class);
        verify(connection).send(packet.capture());
        var nativePayload = assertInstanceOf(ClientboundCustomPayloadPacket.class, packet.getValue());
        assertEquals(new ResourceLocation("fml", "play"), nativePayload.getName());
        var encoded = nativePayload.getInternalData();
        try {
            encoded.readVarInt(); // Forge channel message discriminator.
            var opened = PlayMessages.OpenContainer.decode(encoded);
            var bytes = opened.getAdditionalData();
            try {
                assertSame(type, opened.getType());
                assertEquals(TITLE, opened.getName());
                assertEquals(server.containerId, opened.getWindowId());
                var clientInventory = new Inventory(mock(Player.class));
                var client = assertInstanceOf(WireMenu.class,
                        opened.getType().create(opened.getWindowId(), clientInventory, bytes));
                assertSame(clientInventory, client.inventory);
                assertEquals(server.containerId, client.containerId);
                assertEquals(expected, client.reference);
                assertEquals(0, bytes.readableBytes());
                assertEquals(0, encoded.readableBytes());
            } finally {
                bytes.release();
            }
        } finally {
            encoded.release();
        }
    }

    private record Reference(BlockPos pos, Direction face, int entity, boolean mainHand) {}

    private static final class WireMenu extends AbstractContainerMenu {
        final Inventory inventory;
        final Reference reference;

        WireMenu(int id, Inventory inventory, Reference reference) {
            super(type, id);
            this.inventory = inventory;
            this.reference = reference;
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }

        @Override
        public ItemStack quickMoveStack(Player player, int slot) {
            return ItemStack.EMPTY;
        }
    }
}
