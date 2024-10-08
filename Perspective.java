package com.example.endermanfinder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class Perspective {

    private static final Minecraft mc = Minecraft.getMinecraft();
    private boolean active = false;
    private Vec3 cameraOffset = new Vec3(0, 2, -4);
    private float cameraYaw, cameraPitch;
    private float smoothness = 0.1f;

    public Perspective() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (Keyboard.isKeyDown(Keyboard.KEY_P)) {
            toggle();
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START && active) {
            EntityPlayerSP player = mc.thePlayer;
            if (player == null) return;

            // Update camera rotation based on mouse movement
            cameraYaw += Mouse.getDX() * 0.15f;
            cameraPitch -= Mouse.getDY() * 0.15f;
            cameraPitch = Math.max(-90, Math.min(90, cameraPitch));

            // Calculate desired camera position
            double dx = -Math.sin(Math.toRadians(cameraYaw)) * Math.cos(Math.toRadians(cameraPitch)) * 4;
            double dy = -Math.sin(Math.toRadians(cameraPitch)) * 4;
            double dz = Math.cos(Math.toRadians(cameraYaw)) * Math.cos(Math.toRadians(cameraPitch)) * 4;
            Vec3 desiredOffset = new Vec3(dx, dy + 2, dz);

            // Smoothly interpolate camera position
            cameraOffset = new Vec3(
                cameraOffset.xCoord + (desiredOffset.xCoord - cameraOffset.xCoord) * smoothness,
                cameraOffset.yCoord + (desiredOffset.yCoord - cameraOffset.yCoord) * smoothness,
                cameraOffset.zCoord + (desiredOffset.zCoord - cameraOffset.zCoord) * smoothness
            );
        }
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (active) {
            EntityPlayerSP player = mc.thePlayer;
            if (player == null) return;

            // Store current camera position and rotation
            double originalX = mc.getRenderManager().viewerPosX;
            double originalY = mc.getRenderManager().viewerPosY;
            double originalZ = mc.getRenderManager().viewerPosZ;
            float originalYaw = player.rotationYaw;
            float originalPitch = player.rotationPitch;

            // Set camera position
            mc.getRenderManager().viewerPosX = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.partialTicks + cameraOffset.xCoord;
            mc.getRenderManager().viewerPosY = player.lastTickPosY + (player.posY - player.lastTickPosY) * event.partialTicks + cameraOffset.yCoord;
            mc.getRenderManager().viewerPosZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.partialTicks + cameraOffset.zCoord;

            // Set camera rotation
            player.rotationYaw = cameraYaw;
            player.rotationPitch = cameraPitch;

            // Reset camera position and rotation after rendering
            mc.getRenderManager().viewerPosX = originalX;
            mc.getRenderManager().viewerPosY = originalY;
            mc.getRenderManager().viewerPosZ = originalZ;
            player.rotationYaw = originalYaw;
            player.rotationPitch = originalPitch;
        }
    }

    private void toggle() {
        active = !active;
        if (active) {
            cameraYaw = mc.thePlayer.rotationYaw;
            cameraPitch = mc.thePlayer.rotationPitch;
        }
    }
}