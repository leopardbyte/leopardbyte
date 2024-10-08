package com.example.endermanfinder;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;
import net.minecraft.client.Minecraft;

public class KeybindHandler {

    private static EndermanFinder endermanFinder = new EndermanFinder();

    public static void init() {
        // Register the keybinding event
        MinecraftForge.EVENT_BUS.register(new KeybindHandler());
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (Keyboard.isKeyDown(Keyboard.KEY_COMMA)) {
            Minecraft.getMinecraft().displayGuiScreen(new LynxGui(endermanFinder));
        }
    }
}
