package com.example.alertmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import org.lwjgl.opengl.GL11;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Mod(modid = AlertMod.MODID, version = AlertMod.VERSION)
public class AlertMod {

    public static final String MODID = "alertmod";
    public static final String VERSION = "1.1";

    @Mod.Instance(MODID)
    public static AlertMod instance;

    private boolean alerted = false;
    private long lastSoundPlayTime = 0;
    private static final long SOUND_DELAY_MS = 1000;

    private Configuration config;
    private List<String> targetPlayers;
    private List<EntityPlayer> detectedPlayers = new ArrayList<EntityPlayer>();
    private boolean soundEnabled = true;
    private boolean showDistances = true;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        File configFile = new File(event.getModConfigurationDirectory(), "AlertMod.cfg");
        config = new Configuration(configFile);
        loadConfig();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        ClientCommandHandler.instance.registerCommand(new AlertModCommand());
    }

    private void loadConfig() {
        config.load();
        String[] defaultPlayers = {};
        String[] configPlayers = config.getStringList("targetPlayers", Configuration.CATEGORY_GENERAL, defaultPlayers,
                "List of players to monitor");
        targetPlayers = new ArrayList<String>(Arrays.asList(configPlayers));
        
        if (config.hasChanged()) {
            config.save();
        }
    }

    public void addUsername(String username) {
        if (!targetPlayers.contains(username)) {
            targetPlayers.add(username);
            saveConfig();
        }
    }

    public void removeUsername(String username) {
        if (targetPlayers.remove(username)) {
            saveConfig();
        }
    }

    private void saveConfig() {
        config.get(Configuration.CATEGORY_GENERAL, "targetPlayers", new String[0])
              .set(targetPlayers.toArray(new String[targetPlayers.size()]));
        config.save();
    }

    public List<String> getTargetPlayers() {
        return new ArrayList<String>(targetPlayers);
    }

    public void toggleSound() {
        soundEnabled = !soundEnabled;
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public void toggleShowDistances() {
        showDistances = !showDistances;
    }

    public boolean isShowDistancesEnabled() {
        return showDistances;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (Minecraft.getMinecraft().theWorld == null) {
            return;
        }

        NetHandlerPlayClient netHandler = Minecraft.getMinecraft().thePlayer.sendQueue;
        Collection<NetworkPlayerInfo> playerInfoList = netHandler.getPlayerInfoMap();

        detectedPlayers.clear();
        boolean foundTargetPlayer = false;

        for (NetworkPlayerInfo playerInfo : playerInfoList) {
            String playerName = playerInfo.getGameProfile().getName();

            if (targetPlayers.contains(playerName)) {
                foundTargetPlayer = true;
                EntityPlayer player = Minecraft.getMinecraft().theWorld.getPlayerEntityByName(playerName);
                if (player != null) {
                    detectedPlayers.add(player);
                }
                if (!alerted) {
                    alerted = true;
                }
            }
        }

        if (alerted && foundTargetPlayer && soundEnabled) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastSoundPlayTime >= SOUND_DELAY_MS) {
                Minecraft.getMinecraft().thePlayer.playSound("dig.glass", 1.0F, 1.0F);
                lastSoundPlayTime = currentTime;
            }
        } else {
            alerted = false;
        }
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        for (EntityPlayer player : detectedPlayers) {
            renderPlayerESP(player, event.partialTicks);
        }
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT || !showDistances) {
            return;
        }

        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
        int y = 5;
        for (EntityPlayer player : detectedPlayers) {
            double distance = Minecraft.getMinecraft().thePlayer.getDistanceToEntity(player);
            String text = String.format("%s: %.1f blocks", player.getName(), distance);
            fontRenderer.drawStringWithShadow(text, 5, y, 0x00FFFF); // Cyan color
            y += fontRenderer.FONT_HEIGHT + 2;
        }
    }

    private void renderPlayerESP(EntityPlayer player, float partialTicks) {
        Minecraft mc = Minecraft.getMinecraft();
        
        double renderPosX = mc.getRenderManager().viewerPosX;
        double renderPosY = mc.getRenderManager().viewerPosY;
        double renderPosZ = mc.getRenderManager().viewerPosZ;

        double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks - renderPosX;
        double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks - renderPosY;
        double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks - renderPosZ;

        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glLineWidth(2.0F);
        GL11.glColor4f(0.0F, 1.0F, 1.0F, 1.0F);  // Cyan color

        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3d(x - 0.5, y, z - 0.5);
        GL11.glVertex3d(x - 0.5, y + 2, z - 0.5);
        GL11.glVertex3d(x + 0.5, y, z - 0.5);
        GL11.glVertex3d(x + 0.5, y + 2, z - 0.5);
        GL11.glVertex3d(x + 0.5, y, z + 0.5);
        GL11.glVertex3d(x + 0.5, y + 2, z + 0.5);
        GL11.glVertex3d(x - 0.5, y, z + 0.5);
        GL11.glVertex3d(x - 0.5, y + 2, z + 0.5);
        GL11.glEnd();

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3d(x - 0.5, y, z - 0.5);
        GL11.glVertex3d(x - 0.5, y, z + 0.5);
        GL11.glVertex3d(x + 0.5, y, z + 0.5);
        GL11.glVertex3d(x + 0.5, y, z - 0.5);
        GL11.glVertex3d(x - 0.5, y + 2, z - 0.5);
        GL11.glVertex3d(x + 0.5, y + 2, z - 0.5);
        GL11.glVertex3d(x + 0.5, y + 2, z + 0.5);
        GL11.glVertex3d(x - 0.5, y + 2, z + 0.5);
        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glPopMatrix();
    }
}

class AlertModCommand extends CommandBase {
    @Override
    public String getCommandName() {
        return "alertmod";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/alertmod <add|remove|list|find|sound|show> [username]";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.addChatMessage(new ChatComponentText("Usage: " + getCommandUsage(sender)));
            return;
        }

        if ("add".equalsIgnoreCase(args[0])) {
            if (args.length < 2) {
                sender.addChatMessage(new ChatComponentText("Please specify a username to add."));
            } else {
                AlertMod.instance.addUsername(args[1]);
                sender.addChatMessage(new ChatComponentText("Added username: " + args[1]));
            }
        } else if ("remove".equalsIgnoreCase(args[0])) {
            if (args.length < 2) {
                sender.addChatMessage(new ChatComponentText("Please specify a username to remove."));
            } else {
                AlertMod.instance.removeUsername(args[1]);
                sender.addChatMessage(new ChatComponentText("Removed username: " + args[1]));
            }
        } else if ("list".equalsIgnoreCase(args[0])) {
            List<String> players = AlertMod.instance.getTargetPlayers();
            if (players.isEmpty()) {
                sender.addChatMessage(new ChatComponentText("No usernames in the list."));
            } else {
                sender.addChatMessage(new ChatComponentText("Current usernames:"));
                for (String player : players) {
                    sender.addChatMessage(new ChatComponentText("- " + player));
                }
            }
        } else if ("find".equalsIgnoreCase(args[0])) {
            if (args.length < 2) {
                sender.addChatMessage(new ChatComponentText("Please specify a username to find."));
            } else {
                EntityPlayer player = Minecraft.getMinecraft().theWorld.getPlayerEntityByName(args[1]);
                if (player != null) {
                    Vec3 pos = player.getPositionVector();
                    sender.addChatMessage(new ChatComponentText(String.format("%s is at X: %.2f, Y: %.2f, Z: %.2f", 
                        args[1], pos.xCoord, pos.yCoord, pos.zCoord)));
                } else {
                    sender.addChatMessage(new ChatComponentText("Player not found: " + args[1]));
                }
            }
        } else if ("sound".equalsIgnoreCase(args[0])) {
            AlertMod.instance.toggleSound();
            sender.addChatMessage(new ChatComponentText("Sound alert " + 
                (AlertMod.instance.isSoundEnabled() ? "enabled" : "disabled")));
        } else if ("show".equalsIgnoreCase(args[0])) {
            AlertMod.instance.toggleShowDistances();
            sender.addChatMessage(new ChatComponentText("Distance display " + 
                (AlertMod.instance.isShowDistancesEnabled() ? "enabled" : "disabled")));
        } else {
            sender.addChatMessage(new ChatComponentText("Unknown subcommand. Use 'add', 'remove', 'list', 'find', 'sound', or 'show'."));
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}