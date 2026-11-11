/**
 *认准作者Coda_R5，谁敢冒充我我打死他。
 */
package com.codar5.gameinfo.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ChunkLoader implements CommandRegistrationCallback {
    private static final Map<String, Set<ChunkPos>> loadedChunks = new HashMap<>();

    @Override
    public void register(CommandDispatcher<CommandSourceStack> commandDispatcher, CommandBuildContext commandRegistryAccess, Commands.CommandSelection registrationEnvironment) {
        commandDispatcher.register(Commands.literal("chunk")
                .then(Commands.literal("load").executes(this::loadChunk))
                .then(Commands.literal("unload").executes(this::unloadChunk))
                .then(Commands.literal("list").executes(this::listChunks))
                .then(Commands.literal("check")
                        .then(Commands.argument("x", StringArgumentType.word())
                                .then(Commands.argument("z", StringArgumentType.word())
                                        .executes(this::checkChunk))))
        );
    }

    private int loadChunk(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        Level world = source.getLevel();
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        
        BlockPos pos = player.blockPosition();
        ChunkPos chunkPos = ChunkPos.containing(pos);
        String worldKey = getWorldKey(world);

        loadedChunks.computeIfAbsent(worldKey, k -> new HashSet<>()).add(chunkPos);
        world.getChunkSource().updateChunkForced(chunkPos, true);

        source.sendSuccess(() -> Component.literal(String.format("§a已加载区块 [%d, %d] 在世界 %s", chunkPos.x(), chunkPos.z(), world.dimension().identifier())), true);
        return 1;
    }

    private int unloadChunk(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        Level world = source.getLevel();
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;

        ChunkPos chunkPos = ChunkPos.containing(player.blockPosition());
        String worldKey = getWorldKey(world);

        Set<ChunkPos> chunks = loadedChunks.get(worldKey);
        if (chunks != null && chunks.remove(chunkPos)) {
            world.getChunkSource().updateChunkForced(chunkPos, false);
            source.sendSuccess(() -> Component.literal(String.format("§c已取消加载区块 [%d, %d] 在世界 %s", chunkPos.x(), chunkPos.z(), world.dimension().identifier())), true);
            if (chunks.isEmpty()) {
                loadedChunks.remove(worldKey);
            }
        } else {
            source.sendSuccess(() -> Component.literal(String.format("§e区块 [%d, %d] 未被此mod加载", chunkPos.x(), chunkPos.z())), false);
        }
        return 1;
    }

    private int listChunks(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        Level world = source.getLevel();
        String worldKey = getWorldKey(world);

        Set<ChunkPos> chunks = loadedChunks.get(worldKey);
        if (chunks == null || chunks.isEmpty()) {
            source.sendSuccess(() -> Component.literal(String.format("§e当前世界 %s 没有被此mod加载的区块", world.dimension().identifier())), false);
        } else {
            source.sendSuccess(() -> Component.literal(String.format("§a当前世界 %s 中被此mod加载的区块:", world.dimension().identifier())), false);
            for (ChunkPos chunk : chunks) {
                source.sendSuccess(() -> Component.literal(String.format("§f - [%d, %d]", chunk.x(), chunk.z())), false);
            }
        }
        return 1;
    }

    private int checkChunk(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Level world = source.getLevel();
        
        try {
            int x = Integer.parseInt(StringArgumentType.getString(context, "x"));
            int z = Integer.parseInt(StringArgumentType.getString(context, "z"));
            
            BlockPos blockPos = new BlockPos(x, 0, z);
            ChunkPos chunkPos = ChunkPos.containing(blockPos);
            String worldKey = getWorldKey(world);

            Set<ChunkPos> chunks = loadedChunks.get(worldKey);
            boolean isThisMod = chunks != null && chunks.contains(chunkPos);
            boolean isLoaded = world.getChunkSource().hasChunk(chunkPos.x(), chunkPos.z());

            source.sendSuccess(() -> Component.literal(String.format("§f区块 [%d, %d] 状态检查: 是否加载: %b, 是否被此mod设置为永久加载: %b", chunkPos.x(), chunkPos.z(), isLoaded, isThisMod)), false);
            
            if (isLoaded) {
                source.sendSuccess(() -> Component.literal(String.format("§a区块 [%d, %d] 已加载", chunkPos.x(), chunkPos.z())), false);
            } else {
                source.sendSuccess(() -> Component.literal(String.format("§c区块 [%d, %d] 未加载", chunkPos.x(), chunkPos.z())), false);
            }
        } catch (NumberFormatException e) {
            source.sendFailure(Component.literal("§c请输入有效的整数坐标!"));
            return 0;
        }
        return 1;
    }

    private String getWorldKey(Level world) {
        return world.dimension().identifier().toString();
    }
}
