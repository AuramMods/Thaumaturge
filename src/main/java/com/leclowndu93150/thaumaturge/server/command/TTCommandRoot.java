package com.leclowndu93150.thaumaturge.server.command;

import com.leclowndu93150.thaumaturge.TTIds;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class TTCommandRoot {
    public static final String NAME = "thaumaturge";
    public static final String ALIAS = "tt";

    private TTCommandRoot() {}

    public static LiteralArgumentBuilder<CommandSourceStack> root() {
        return Commands.literal(NAME);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRegister(RegisterCommandsEvent event) {
        CommandNode<CommandSourceStack> root = event.getDispatcher().getRoot().getChild(NAME);
        if (root != null) {
            event.getDispatcher().register(Commands.literal(ALIAS).redirect(root));
        }
    }
}
