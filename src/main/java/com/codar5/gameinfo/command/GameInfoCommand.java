package com.codar5.gameinfo.command;

import com.codar5.gameinfo.GameInfoNeoForge;
import com.codar5.gameinfo.util.HexUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = GameInfoNeoForge.MOD_ID)
public class GameInfoCommand {

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("gameinfo");

        addBoolean(root, "fps", v -> GameInfoNeoForge.config.showFPS = v);
        addBoolean(root, "time", v -> GameInfoNeoForge.config.showTimeAndDays = v);
        addBoolean(root, "coords", v -> GameInfoNeoForge.config.showCoordinates = v);
        addBoolean(root, "nether", v -> GameInfoNeoForge.config.showNetherCoordinates = v);
        addBoolean(root, "biome", v -> GameInfoNeoForge.config.showBiome = v);
        addBoolean(root, "remark", v -> GameInfoNeoForge.config.remark = v);
        addBoolean(root, "enable", v -> GameInfoNeoForge.config.enabled = v);

        addInteger(root, "xpos", v -> GameInfoNeoForge.config.xPos = v);
        addInteger(root, "ypos", v -> GameInfoNeoForge.config.yPos = v);

        addColor(root, "color");

        dispatcher.register(root);
    }

    private static void addBoolean(LiteralArgumentBuilder<CommandSourceStack> root, String name, Consumer<Boolean> setter) {
        root.then(Commands.literal(name)
                .then(Commands.argument("state", BoolArgumentType.bool())
                        .executes(ctx -> {
                            setter.accept(BoolArgumentType.getBool(ctx, "state"));
                            GameInfoNeoForge.config.saveConfig();
                            return 1;
                        })));
    }

    private static void addInteger(LiteralArgumentBuilder<CommandSourceStack> root, String name, IntConsumer setter) {
        root.then(Commands.literal(name)
                .then(Commands.argument("position", IntegerArgumentType.integer())
                        .executes(ctx -> {
                            setter.accept(IntegerArgumentType.getInteger(ctx, "position"));
                            GameInfoNeoForge.config.saveConfig();
                            return 1;
                        })));
    }

    private static void addColor(LiteralArgumentBuilder<CommandSourceStack> root, String name) {
        root.then(Commands.literal(name)
                .then(Commands.argument("color", StringArgumentType.string())
                        .executes(ctx -> {
                            String color = StringArgumentType.getString(ctx, "color");
                            GameInfoNeoForge.config.color = HexUtil.toDecimal("0xFF" + color);
                            GameInfoNeoForge.config.saveConfig();
                            return 1;
                        })));
    }
}