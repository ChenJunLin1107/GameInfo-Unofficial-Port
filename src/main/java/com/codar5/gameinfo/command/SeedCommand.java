/**
 *认准作者Coda_R5，谁敢冒充我我打死他。
 */
package com.codar5.gameinfo.command;

import com.codar5.gameinfo.network.WorldSeedStore;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;

/**
 * 客户端种子指令注册类。
 * 提供 /giseed 指令，用于在纯客户端环境下设置和查看当前服务器的种子。
 */
public class SeedCommand implements ClientCommandRegistrationCallback {

    @Override
    public void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext buildContext) {
        dispatcher.register(ClientCommands.literal("giseed")
                .then(ClientCommands.literal("set")
                        .then(ClientCommands.argument("seed", LongArgumentType.longArg())
                                .executes(context -> {
                                    long seed = LongArgumentType.getLong(context, "seed");
                                    WorldSeedStore.setManual(seed);
                                    feedback("Gameinfo: slime-chunk seed " + seed + " saved for this server");
                                    return 1;
                                })))
                .then(ClientCommands.literal("clear")
                        .executes(context -> {
                            WorldSeedStore.clearManual();
                            feedback("Gameinfo: saved seed cleared for this server");
                            return 1;
                        }))
                .then(ClientCommands.literal("show")
                        .executes(context -> {
                            long seed = WorldSeedStore.resolve();
                            if (seed == 0L) {
                                feedback("Gameinfo: no seed known for this server (overlay stays off)");
                            } else {
                                feedback("Gameinfo: using slime-chunk seed " + seed);
                            }
                            return 1;
                        }))
        );
    }

    /**
     * 发送反馈消息。
     * 由于 26.4-snapshot 移除了 Gui.getChat() 和 displayClientMessage，
     * 这里使用 Player.sendSystemMessage()，这是最底层且最稳定的系统消息发送方式。
     *
     * @param message 反馈文本内容
     */
    private static void feedback(String message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.sendSystemMessage(Component.literal(message));
        }
    }
}
