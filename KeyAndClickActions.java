package com.example.endermanfinder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.inventory.GuiContainer;

import java.util.Random;

public class KeyAndClickActions {

    private final Minecraft mc = Minecraft.getMinecraft();
    private boolean running = false;  // Tracks if actions are currently running
    private boolean paused = false;   // Tracks if actions should be paused
    private final Random random = new Random();

    // This method starts simulating key presses for '4', '5', and right-clicking.
    public void performActions() {
        running = true;
        new Thread(() -> {
            try {
                while (running) {
                    // Check if the player is paused (in inventory or chat)
                    if (isPlayerPaused()) {
                        paused = true;
                        System.out.println("Player paused: inventory or chat open.");
                    } else {
                        paused = false;
                    }

                    // Only execute actions if not paused
                    if (!paused) {
                        System.out.println("New cycle");

                        // Simulate pressing key '4'
                        sendKeyPress(3); // 2 is the hotbar slot for '4'
                        Thread.sleep(getRandomizedDelay(300)); // Delay after switching to slot 4

                        // Simulate right-click
                        sendRightClick();
                        Thread.sleep(getRandomizedDelay(100));

                        // Simulate pressing key '5'
                        sendKeyPress(4); // 3 is the hotbar slot for '5'
                        Thread.sleep(getRandomizedDelay(100));

                        // Start the right-click timer
                        startRightClickTimer();

                        System.out.println("Waiting for next iteration...");
                        Thread.sleep(getRandomizedDelay(6800)); // Healing wand cooldown
                    } else {
                        // Sleep briefly to avoid high CPU usage when paused
                        Thread.sleep(500);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // Stop the actions by setting running to false
    public void stopActions() {
        running = false;
        System.out.println("Key and click actions stopped.");
    }

    // Simulate pressing a hotbar key (Minecraft hotbar slots start from 0)
    private void sendKeyPress(int hotbarSlot) {
        mc.thePlayer.inventory.currentItem = hotbarSlot;  // Change the player's current item
        System.out.println("Simulated key press for slot: " + (hotbarSlot + 1));  // Hotbar slots are 0-indexed, Minecraft's hotbar is 1-9
    }

    // Simulate right-click using KeyBinding
    private void sendRightClick() {
        KeyBinding.onTick(mc.gameSettings.keyBindUseItem.getKeyCode());  // Simulate a right-click (use item)
        System.out.println("Simulated right-click via KeyBinding");
    }

    // Timer to simulate repeated right-clicking
    private void startRightClickTimer() {
        new Thread(() -> {
            try {
                for (int i = 0; i < 7 && running && !paused; i++) {
                    sendRightClick();
                    Thread.sleep(getRandomizedDelay(2100));  // Repeated right-click every 1.1 seconds
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // This method checks if the player is paused (in inventory or chat)
    private boolean isPlayerPaused() {
        return mc.currentScreen instanceof GuiContainer || mc.currentScreen instanceof GuiChat;
    }

    // Helper method to add random delay (+/- 80ms)
    private int getRandomizedDelay(int baseDelay) {
        return baseDelay + random.nextInt(160) - 80;  // Random delay between -80ms and +80ms
    }
}