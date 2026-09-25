package com.codar5.gameinfo.network;

import com.codar5.gameinfo.hud.HudOverlay;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class GameInfoNetwork {

    public static void handleFurnaceData(FurnaceDataPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            HudOverlay.serverFurnaceCache.put(
                    payload.pos(),
                    new ItemStack[]{payload.input(), payload.fuel(), payload.output()}
            );
        });
    }
}