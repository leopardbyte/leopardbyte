package com.example.endermanfinder;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.List;

public class EntityDebugger {

    private long lastDebugTime = 0;
    private static final long DEBUG_INTERVAL = 10000; // 10 seconds in milliseconds
    private static final int SCAN_RADIUS = 10;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.theWorld != null && mc.thePlayer != null) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastDebugTime > DEBUG_INTERVAL) {
                    debugScanEntities(mc);
                    lastDebugTime = currentTime;
                }
            }
        }
    }

    private void debugScanEntities(Minecraft mc) {
        List<Entity> nearbyEntities = mc.theWorld.getEntitiesWithinAABBExcludingEntity(
            mc.thePlayer,
            mc.thePlayer.getEntityBoundingBox().expand(SCAN_RADIUS, SCAN_RADIUS, SCAN_RADIUS)
        );

        if (!nearbyEntities.isEmpty()) {
            mc.thePlayer.addChatMessage(new ChatComponentText("§6§l[Lynx] §bEntities found within " + SCAN_RADIUS + " blocks:"));
            for (Entity entity : nearbyEntities) {
                String entityName = entity.getName();
                String entityType = entity.getClass().getSimpleName();
                mc.thePlayer.addChatMessage(new ChatComponentText("§6§l[Lynx] §b- " + entityName + " (" + entityType + ")"));
            }
        } else {
            mc.thePlayer.addChatMessage(new ChatComponentText("§6§l[Lynx] §bNo entities found within " + SCAN_RADIUS + " blocks."));
        }
    }
}