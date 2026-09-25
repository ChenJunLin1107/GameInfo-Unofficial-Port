package com.codar5.gameinfo.command;

import com.codar5.gameinfo.GameInfoNeoForge;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = GameInfoNeoForge.MOD_ID)
public class ChunkLoader {

    private static final Map<String, Set<ChunkPos>> loadedChunks = new HashMap<>();

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("chunk")
                .then(Commands.literal("load")
                        .executes(ChunkLoader::loadChunk))
                .then(Commands.literal("unload")
                        .executes(ChunkLoader::unloadChunk))
                .then(Commands.literal("list")
                        .executes(ChunkLoader::listChunks))
                .then(Commands.literal("check")
                        .then(Commands.argument("x", IntegerArgumentType.integer())
                                .then(Commands.argument("z", IntegerArgumentType.integer())
                                        .executes(ChunkLoader::checkChunk)))));
    }

    private static int loadChunk(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        Level world = source.getLevel();
        BlockPos pos = source.getPlayer().blockPosition();
        ChunkPos chunkPos = ChunkPos.containing(pos);
        String worldKey = getWorldKey(world);

        loadedChunks.computeIfAbsent(worldKey, k -> new HashSet<>()).add(chunkPos);

        world.getChunkSource().updateChunkForced(chunkPos, true);

        source.sendSuccess(() -> Component.literal(
                        String.format("§a已加载区块 [%d, %d] 在世界 %s",
                                chunkPos.x(), chunkPos.z(), world.dimension().identifier())),
                true);

        return 1;
    }

    private static int unloadChunk(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        Level world = source.getLevel();
        ServerPlayer player = source.getPlayer();

        ChunkPos chunkPos = ChunkPos.containing(player.blockPosition());
        String worldKey = getWorldKey(world);

        Set<ChunkPos> chunks = loadedChunks.get(worldKey);
        if (chunks != null && chunks.remove(chunkPos)) {
            world.getChunkSource().updateChunkForced(chunkPos, false);
            source.sendSuccess(() -> Component.literal(
                            String.format("§c已取消加载区块 [%d, %d] 在世界 %s",
                                    chunkPos.x(), chunkPos.z(), world.dimension().identifier())),
                    true);

            if (chunks.isEmpty()) {
                loadedChunks.remove(worldKey);
            }
        } else {
            source.sendSuccess(() -> Component.literal(
                            String.format("§e区块 [%d, %d] 未被此mod加载",
                                    chunkPos.x(), chunkPos.z())),
                    false);
        }

        return 1;
    }

    private static int listChunks(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        Level world = source.getLevel();
        String worldKey = getWorldKey(world);

        Set<ChunkPos> chunks = loadedChunks.get(worldKey);
        if (chunks == null || chunks.isEmpty()) {
            source.sendSuccess(() -> Component.literal(
                            String.format("§e当前世界 %s 没有被此mod加载的区块",
                                    world.dimension().identifier())),
                    false);
        } else {
            source.sendSuccess(() -> Component.literal(
                            String.format("§a世界 %s 中被此mod加载的区块:",
                                    world.dimension().identifier())),
                    false);

            for (ChunkPos chunk : chunks) {
                source.sendSuccess(() -> Component.literal(
                                String.format("§f  - [%d, %d]", chunk.x(), chunk.z())),
                        false);
            }
        }

        return 1;
    }

    private static int checkChunk(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Level world = source.getLevel();
        int x = IntegerArgumentType.getInteger(context, "x");
        int z = IntegerArgumentType.getInteger(context, "z");
        BlockPos blockPos = new BlockPos(x, 0, z);
        ChunkPos chunkPos = ChunkPos.containing(blockPos);
        String worldKey = getWorldKey(world);

        Set<ChunkPos> chunks = loadedChunks.get(worldKey);
        boolean isThisMod = chunks != null && chunks.contains(chunkPos);
        boolean isLoaded = world.getChunkSource().hasChunk(chunkPos.x(), chunkPos.z());

        source.sendSuccess(() -> Component.literal(
                        String.format("§f区块 [%d, %d] 状态检查: 是否加载: %b, 是否被此mod设置为永久加载: %b", chunkPos.x(), chunkPos.z(), isLoaded, isThisMod)),
                false);

        int loadedChunkCount = world.getChunkSource().getLoadedChunksCount();

        if (isLoaded) {
            source.sendSuccess(() -> Component.literal(
                            String.format("§a区块 [%d, %d] 已加载，当前已加载区块数量为：%d", chunkPos.x(), chunkPos.z(), loadedChunkCount)),
                    false);
        } else {
            source.sendSuccess(() -> Component.literal(
                            String.format("§c区块 [%d, %d] 未加载", chunkPos.x(), chunkPos.z())),
                    false);
        }

        return 1;
    }

    private static String getWorldKey(Level world) {
        return world.dimension().identifier().toString();
    }
}