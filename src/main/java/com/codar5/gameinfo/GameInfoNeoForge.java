package com.codar5.gameinfo;

import com.codar5.gameinfo.config.GameInfoConfig;
import com.codar5.gameinfo.hud.BlockInfoHudRenderer;
import com.codar5.gameinfo.hud.EntityHealthHudRenderer;
import com.codar5.gameinfo.hud.HudOverlay;
import com.codar5.gameinfo.network.FurnaceDataPayload;
import com.codar5.gameinfo.network.GameInfoNetwork;
import com.codar5.gameinfo.util.RendererPreferenceWriter;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(GameInfoNeoForge.MOD_ID)
public class GameInfoNeoForge {
    public static final String MOD_ID = "gameinfo";
    public static final GameInfoConfig config = new GameInfoConfig();

    public GameInfoNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        RendererPreferenceWriter.forceOpenGLPreference();

        modEventBus.addListener(this::registerGuiLayers);
        modEventBus.addListener(this::registerPayloads);
    }

    private void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(
                VanillaGuiLayers.CHAT,
                Identifier.fromNamespaceAndPath(MOD_ID, "custom_text"),
                new HudOverlay()
        );
        event.registerAbove(
                VanillaGuiLayers.CHAT,
                Identifier.fromNamespaceAndPath(MOD_ID, "block_info"),
                new BlockInfoHudRenderer()
        );
        event.registerAbove(
                VanillaGuiLayers.CHAT,
                Identifier.fromNamespaceAndPath(MOD_ID, "entity_health"),
                new EntityHealthHudRenderer()
        );
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(
                FurnaceDataPayload.TYPE,
                FurnaceDataPayload.CODEC,
                GameInfoNetwork::handleFurnaceData
        );
    }

    @EventBusSubscriber(modid = MOD_ID)
    public static class ServerTickHandler {
        @SubscribeEvent
        public static void onServerTick(ServerTickEvent.Post event) {
            for (ServerPlayer sp : event.getServer().getPlayerList().getPlayers()) {
                if (!(sp.level() instanceof ServerLevel level)) continue;
                BlockPos center = sp.blockPosition();
                BlockPos min = center.offset(-6, -3, -6);
                BlockPos max = center.offset(6, 3, 6);
                for (BlockPos p : BlockPos.betweenClosed(min, max)) {
                    BlockEntity be = level.getBlockEntity(p);
                    if (be instanceof AbstractFurnaceBlockEntity f) {
                        PacketDistributor.sendToPlayer(sp, new FurnaceDataPayload(
                                p.immutable(),
                                f.getItem(0).copy(),
                                f.getItem(1).copy(),
                                f.getItem(2).copy()
                        ));
                    }
                }
            }
        }
    }
}