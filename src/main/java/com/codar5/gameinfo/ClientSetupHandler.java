package com.codar5.gameinfo;

import com.codar5.gameinfo.gui.GameInfoConfigScreen;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@EventBusSubscriber(modid = GameInfoNeoForge.MOD_ID)
public class ClientSetupHandler {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        ModList.get().getModContainerById(GameInfoNeoForge.MOD_ID).ifPresent(container -> {
            container.registerExtensionPoint(
                    IConfigScreenFactory.class,
                    (IConfigScreenFactory) (c, parent) -> new GameInfoConfigScreen(parent)
            );
        });
    }
}