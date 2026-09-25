package com.codar5.gameinfo.keybind;

import com.codar5.gameinfo.GameInfoNeoForge;
import com.codar5.gameinfo.gui.GameInfoConfigScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@EventBusSubscriber(modid = GameInfoNeoForge.MOD_ID, value = Dist.CLIENT)
public class KeybindHandler {

    private static final int SDL_SCANCODE_U = 24;

    public static KeyMapping openGuiKeyBinding;

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        openGuiKeyBinding = new KeyMapping(
                "key.gameinfo.open_gui",
                SDL_SCANCODE_U,
                KeyMapping.Category.register(Identifier.fromNamespaceAndPath(GameInfoNeoForge.MOD_ID, "keybindings"))
        );
        event.register(openGuiKeyBinding);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (openGuiKeyBinding == null) return;
        while (openGuiKeyBinding.consumeClick()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.gui.screen() == null) {
                mc.gui.setScreen(new GameInfoConfigScreen(null));
            }
        }
    }
}