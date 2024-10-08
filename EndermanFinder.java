package com.example.endermanfinder;

import com.example.endermanfinder.EntityDebugger;
import com.example.endermanfinder.KeybindHandler;

//import com.example.endermanfinder.KeybindHandler;


import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.lwjgl.input.Keyboard;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.Minecraft;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockCarpet;
import net.minecraft.util.MathHelper;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraft.util.EnumFacing;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.GuiChat;
import net.minecraftforge.client.event.GuiOpenEvent;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.entity.monster.EntityZombie; // Example for a zombie
import net.minecraft.entity.passive.EntityWolf; // Example for a wolf
import net.minecraft.entity.passive.EntityMooshroom; // Example for a mooshroom
import net.minecraft.entity.monster.EntitySpider; // Example for a spider
import net.minecraft.entity.monster.EntityBlaze; // Example for a blaze
import net.minecraft.entity.monster.EntityCreeper; // Example for a creeper
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityEndermite;
import java.util.HashMap; // For HashMap
import java.util.Map; // For Map
import java.util.PriorityQueue;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.common.MinecraftForge;
import net.minecraft.client.entity.EntityOtherPlayerMP;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/*   TODO:

    Target Zombie= 2000, 45000  Boss hp: lvlI: 500 lvlII: 20k lvlIII: 400k lvlIV: 1.5m lvlV: 10M (minibosse: 24k,90k,480k,600k,2.4m)
                   if graveyard then 100hp&120hp

    Target wolf: 6000, 7000, 31150, Boss hp: lvlI: 2000, lvlII: 40k, lvlIII: 750k, lvlIV: 2m (minibosse: 45k, 125k, 480k)
    - add /lynx target zombie auto asign hp values and clear hp list before asining values.

    Target spider: 

    - auto SLAYER! focus boss until death maybe slayer name possible "Apfelmilchshake" "playername" or by hp and then prioritizing this entity until its no longer in the entity list

    -cant walk backwards anymore need to fix... ASAP!!

    -add failsafes when no target is found DUE TO 24.09 !!!!

    -fix target reached what to do next to do !!
     loactions: "Dragon's nest = 13000", "The End = 9000", "Zealot Bruiser Hideout= 65000 & 260000", "Howling Cave = 6000 & 31150 & 7000 & 120000 & 2000000(Boss we need to prio until entity gone!)"
  
    -store every 5 seconds current position up to 5 times and then when no target found walk randomlly to each of the 5 points or if new target spawns switch to target.


    bruiser hideout block blocks: 
            return pos.getX() == -521 && pos.getY() == 38 && pos.getZ() == -267;
            return pos.getX() == -520 && pos.getY() == 39 && pos.getZ() == -267;
            return pos.getX() == -521 && pos.getY() == 40 && pos.getZ() == -268;
            return pos.getX() == -519 && pos.getY() == 38 && pos.getZ() == -258;
            return pos.getX() == -520 && pos.getY() == 38 && pos.getZ() == -257;
            return pos.getX() == -520 && pos.getY() == 38 && pos.getZ() == -256;
            return pos.getX() == -520 && pos.getY() == 38 && pos.getZ() == -255;
            return pos.getX() == -517 && pos.getY() == 38 && pos.getZ() == -254;

 */



import java.util.*;

@Mod(modid = EndermanFinder.MODID, version = EndermanFinder.VERSION, clientSideOnly = true)
public class EndermanFinder {
    public static final String MODID = "OptiFine_1.8.9_HD_U_L5";
    public static final String VERSION = "1.8.9";

    private static final String ALLOWED_UDID = "381fd2fc-533b-436c-b836-bd60fee6ae44";  
    private boolean isPlayerAuthorized = false;

    private boolean isDebugEnabled = false;
    private EntityDebugger entityDebugger = new EntityDebugger();

    private static final int SEARCH_RADIUS = 120;
    private boolean lagEnabled = false;

    private List<EntityLivingBase> targetEndermen = new ArrayList<>();
    private Map<EntityLivingBase, Long> cooldownMap = new HashMap<>();
    private static final long COOLDOWN_DURATION = 4000;

    private long lastAttackTime = 0;
    private long lastWaitTime = 0;
    private static final float MOUSE_SENSITIVITY = 0.04f;
    private static final float SMOOTHING_FACTOR = 0.2f;

    private float currentYaw = 0;
    private float currentPitch = 0;

    private static final double MAX_MOUSE_MOVE_DISTANCE = 3;
    private int mouseMoveCounter = 0;

    private boolean hasJoinMessageBeenSent = false;
    private boolean isInventoryOpen = false;
    private boolean isChatOpen = false;
    public static boolean actionsRunning = false;

    // Store failed pathfinding attempts
    private Map<EntityLivingBase, Integer> failedPathAttempts = new HashMap<>();
    private Map<EntityLivingBase, Long> targetCooldowns = new HashMap<>();
    private static final int MAX_FAILED_ATTEMPTS = 3;  // Adjust based on how many failures are allowed
    private static final long TARGET_COOLDOWN = 1000; // 10 seconds in milliseconds

    private static final Set<String> DEFAULT_ENTITY_NAMES = new HashSet<>(Arrays.asList(
    "goblin", "ice walker", "crystal sentry", "firemage", "treasuer hunter"
    ));


    private static final File CUSTOM_ENTITY_FILE = new File(System.getenv("APPDATA") + "\\.minecraft\\Lynx\\custom_entities.txt");
    private static final Set<String> CUSTOM_ENTITY_NAMES = new HashSet<>(DEFAULT_ENTITY_NAMES);

    private void loadCustomEntityNames() {
        // Ensure the folder exists
        File dir = CUSTOM_ENTITY_FILE.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();  // Create the directory if it doesn't exist
        }
    
        // Check if the file exists, and if not, create it with default entity names
        if (!CUSTOM_ENTITY_FILE.exists()) {
            try {
                CUSTOM_ENTITY_FILE.createNewFile();  // Create the file
                saveCustomEntityNames();  // Save default names to the new file
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;  // Defaults have been saved, nothing else to load
        }
    
        // If the file exists, load custom names from it
        try (BufferedReader reader = new BufferedReader(new FileReader(CUSTOM_ENTITY_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                CUSTOM_ENTITY_NAMES.add(line.trim().toLowerCase());  // Load custom names into the set
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // Method to save custom entity names to the file
    private void saveCustomEntityNames() {
        try (FileWriter writer = new FileWriter(CUSTOM_ENTITY_FILE)) {
            // Write both the default and custom names to the file
            for (String name : CUSTOM_ENTITY_NAMES) {
                writer.write(name + System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private static final long ATTACK_COOLDOWN = 130; // 500ms cooldown between attacks // 1000ms cooldown between sneaks
    private long lastAttackTime1 = 0;
    private static final long SNEAK_DURATION = 130; // 100ms sneak duration
    private long sneakStartTime = 0;
    private boolean isSneaking = false;
    private boolean isTargetingEnderman = false;
    private static final float MAX_ROTATION_SPEED = 14.0f;

    private Set<BlockPos> blockedCache = new HashSet<>();
    private long lastCacheClear = 0;
    private static final long CACHE_CLEAR_INTERVAL = 700; // 30 seconds

    public static String targetEntityType = "enderman"; // changed from private
    

    private static final Map<String, List<Float>> ENTITY_HP_RANGES = new HashMap<String, List<Float>>() {{
        put("enderman", Arrays.asList(14000f, 180000f)); // Example range for Enderman
        put("zombie", Arrays.asList(1100f, 5000000f));
        put("spider", Arrays.asList(400f, 4000000f));
        put("wolf", Arrays.asList(1f, 2000000f));
        put("blaze", Arrays.asList(2000f, 10000f));
        put("mooshroom", Arrays.asList(1f, 2000000f));
        put("creeper", Arrays.asList(1f, 3000000f));
        put("custom", Arrays.asList(0.1f, 100000000f));
        
    }};
    

    Minecraft mc = Minecraft.getMinecraft();

    private List<BlockPos> blockedPositions = new ArrayList<>();
    public static KeyAndClickActions keyAndClickActions = new KeyAndClickActions();
    public static boolean keyAndClickActionsEnabled = false;
    public static boolean autoWalkEnabled = false;
    public String selectedTarget = "enderman";

    public static void setAutoWalkEnabled(boolean enabled) {
        autoWalkEnabled = enabled;
    }
    private List<BlockPos> currentPath = new ArrayList<>();
    private int currentPathIndex = 0;
    private static List<Float> desiredEndermanHPs = new ArrayList<Float>() {{
        add(4500f);
    }};

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        KeybindHandler.init();
        loadCustomEntityNames();
        MinecraftForge.EVENT_BUS.register(this);
        ClientCommandHandler.instance.registerCommand(new CommandBase() {
            @Override

            
    
            public String getCommandName() {
                return "lynx";
            }
            
            @Override
            public String getCommandUsage(ICommandSender sender) {
                return "§6§l/lynx walk | /lynx hp <add/remove/list> <value> | /lynx target <mob> | /lynx lag <on/off> (only turn on if u have high ping >120ms) | /lynx block add";
            }
            
            @Override
            public void processCommand(ICommandSender sender, String[] args) {
                if (!isPlayerAuthorized) {
                    IChatComponent message = new ChatComponentText("§6§l[Lynx] §cYou are not authorized to use this command. | ");
                    IChatComponent link = new ChatComponentText("§bClick here to join our Discord for more information");
                    ChatStyle clickableStyle = new ChatStyle();
                    clickableStyle.setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/hBcNz6db"));
                    link.setChatStyle(clickableStyle);
                    message.appendSibling(link);
                    sender.addChatMessage(message);
                    return;
                }
            
                // Handle lag mode
                if (args.length == 2 && args[0].equalsIgnoreCase("lag")) {
                    if (args[1].equalsIgnoreCase("on")) {
                        lagEnabled = true;
                        sender.addChatMessage(new ChatComponentText("§6§l[Lynx] §bLag mode §aenabled"));
                    } else if (args[1].equalsIgnoreCase("off")) {
                        lagEnabled = false;
                        sender.addChatMessage(new ChatComponentText("§6§l[Lynx] §bLag mode §4disabled"));
                    } else {
                        sender.addChatMessage(new ChatComponentText("§6§l[Lynx] §cInvalid lag command. Use 'on' or 'off'"));
                    }
                    return;
                }
                // Handle custom entities
                if (args.length == 2 && args[0].equalsIgnoreCase("custom")) {
                    String customEntityName = args[1].trim().toLowerCase();
                    if (!CUSTOM_ENTITY_NAMES.contains(customEntityName)) {
                        CUSTOM_ENTITY_NAMES.add(customEntityName);
                        saveCustomEntityNames();  // Save the updated list
                        sender.addChatMessage(new ChatComponentText("§6§l[Lynx] §bAdded custom entity: " + customEntityName));
                    } else {
                        sender.addChatMessage(new ChatComponentText("§6§l[Lynx] §cEntity already exists: " + customEntityName));
                    }
                    return;
                }

                if (args.length == 2 && args[0].equalsIgnoreCase("debug")) {
                    if (args[1].equalsIgnoreCase("on")) {
                        enableEntityDebugger(sender);  // Enable the debugger
                    } else if (args[1].equalsIgnoreCase("off")) {
                        disableEntityDebugger(sender);  // Disable the debugger
                    } else {
                        sender.addChatMessage(new ChatComponentText("§6§l[Lynx] §cInvalid debug command. Use 'on' or 'off'"));
                    }
                    return;
                }

            
                // Handle target entity change
                if (args.length == 2 && args[0].equalsIgnoreCase("target")) {
                    String entityType = args[1].toLowerCase();
                    if (setTargetEntityType(entityType)) {
                        sender.addChatMessage(new ChatComponentText("§6§l[Lynx] §bTarget entity set to " + entityType));
                        
                        // Clear and update HP list based on the entity type
                        updateDesiredHPListForEntity(entityType);
                        
                        sender.addChatMessage(new ChatComponentText("§6§l[Lynx] §bHP range for " + entityType + " set."));
                    } else {
                        sender.addChatMessage(new ChatComponentText("§6§l[Lynx] §cInvalid entity type! Available: §fenderman, §3zombie, §0spider, §8wolf, §eblaze, §4mooshroom, §2creeper, §bcustom ->if not vanilla mob choose this"));
                    }
                    return;
                }
            
                // Handle block command
                if (args.length == 2 && args[0].equalsIgnoreCase("block") && args[1].equalsIgnoreCase("add")) {
                    addBlockedPosition(mc);
                    sender.addChatMessage(new ChatComponentText("§6§l[Lynx] §bAdded blocked position"));
                    return;
                }
            
                // Toggle auto-walk or key actions
                if (args.length == 1) {
                    if (args[0].equalsIgnoreCase("slayer")) {
                        autoWalkEnabled = !autoWalkEnabled;
                        String status = autoWalkEnabled ? "§aenabled" : "§cdisabled";
                        sender.addChatMessage(new ChatComponentText("§6§l[Lynx] §bAuto-walk " + status));
                        if (!autoWalkEnabled) {
                            keyAndClickActions.stopActions();
                            actionsRunning = false;
                        }
                    } else if (args[0].equalsIgnoreCase("actions")) {
                        keyAndClickActionsEnabled = !keyAndClickActionsEnabled;
                        String status = keyAndClickActionsEnabled ? "§aenabled" : "§cdisabled";
                        sender.addChatMessage(new ChatComponentText("§6§l[Lynx] §bKey and click actions " + status));
                        if (!keyAndClickActionsEnabled) {
                            keyAndClickActions.stopActions();
                            actionsRunning = false;
                        }
                    } else {
                        sender.addChatMessage(new ChatComponentText("§6§l[Lynx] §cInvalid command. Use '/lynx slayer' to toggle auto-walk/kill or '/lynx actions' to toggle ranged BOL+WAND."));
                    }
                } 
            
                // HP list management
                else if (args.length >= 2 && args[0].equalsIgnoreCase("hp")) {
                    if (args[1].equalsIgnoreCase("add") && args.length == 3) {
                        try {
                            float newHP = Float.parseFloat(args[2]);
                            desiredEndermanHPs.add(newHP);
                            sender.addChatMessage(new ChatComponentText("§6§l[Lynx] §bAdded " + newHP + " to target HP list"));
                        } catch (NumberFormatException e) {
                            sender.addChatMessage(new ChatComponentText("§6§l[Lynx] §cInvalid HP value. Please enter a number."));
                        }
                    } else if (args[1].equalsIgnoreCase("remove") && args.length == 3) {
                        try {
                            float hpToRemove = Float.parseFloat(args[2]);
                            if (desiredEndermanHPs.remove(Float.valueOf(hpToRemove))) {
                                sender.addChatMessage(new ChatComponentText("§6§l[Lynx] §bRemoved " + hpToRemove + " from target HP list"));
                            } else {
                                sender.addChatMessage(new ChatComponentText("§6§l[Lynx] §cHP value " + hpToRemove + " not found in the list"));
                            }
                        } catch (NumberFormatException e) {
                            sender.addChatMessage(new ChatComponentText("§6§l[Lynx] §cInvalid HP value. Please enter a number."));
                        }
                    } else if (args[1].equalsIgnoreCase("list")) {
                        if (desiredEndermanHPs.isEmpty()) {
                            sender.addChatMessage(new ChatComponentText("§6§l[Lynx] §bNo target mob HP values set"));
                        } else {
                            sender.addChatMessage(new ChatComponentText("§6§l[Lynx] §bTarget mob HP values:"));
                            for (Float hp : desiredEndermanHPs) {
                                sender.addChatMessage(new ChatComponentText("§b- " + hp));
                            }
                        }
                    } else {
                        sender.addChatMessage(new ChatComponentText("§6§l[Lynx] §cInvalid HP command. Usage: /lynx hp <add/remove/list> <value>"));
                    }
                } else {
                    sender.addChatMessage(new ChatComponentText("§6§l[Lynx] §cInvalid command. Usage: " + getCommandUsage(sender)));
                }
            }

            @Override
            public int getRequiredPermissionLevel() {
                return 0;
            }
        });
    }

    private void enableEntityDebugger(ICommandSender sender) {
        if (!isDebugEnabled) {
            MinecraftForge.EVENT_BUS.register(entityDebugger);  // Register the debugger to the event bus
            isDebugEnabled = true;
            sender.addChatMessage(new ChatComponentText("§6§l[Lynx] §bEntity debugger §aenabled"));
        } else {
            sender.addChatMessage(new ChatComponentText("§6§l[Lynx] §cEntity debugger is already enabled"));
        }
    }

    private void disableEntityDebugger(ICommandSender sender) {
        if (isDebugEnabled) {
            MinecraftForge.EVENT_BUS.unregister(entityDebugger);  // Unregister the debugger from the event bus
            isDebugEnabled = false;
            sender.addChatMessage(new ChatComponentText("§6§l[Lynx] §bEntity debugger §4disabled"));
        } else {
            sender.addChatMessage(new ChatComponentText("§6§l[Lynx] §cEntity debugger is already disabled"));
        }
    }


    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        EntityPlayer player = event.player;
        
        // Add a short delay before sending the message
        Minecraft.getMinecraft().addScheduledTask(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            String udid = getPlayerUDID(player);
        
            if (ALLOWED_UDID.equals(udid)) {
                IChatComponent message = new ChatComponentText("§6§l[Lynx] §bYou are allowed to use the Zealot mod | ");
                IChatComponent enjoyText = new ChatComponentText("§a§1Enjoy.");
                message.appendSibling(enjoyText);
                player.addChatMessage(message);
                isPlayerAuthorized = true;
            } else {
                IChatComponent message = new ChatComponentText("§6§l[Lynx] §cYou are not allowed to use the Zealoft modaaaaaa. | ");
                
                IChatComponent link = new ChatComponentText("§bClick here to join our Discord");
                ChatStyle clickableStyle = new ChatStyle();
                clickableStyle.setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/hBcNz6db"));
                link.setChatStyle(clickableStyle);
                
                message.appendSibling(link);
                
                player.addChatMessage(message);
                isPlayerAuthorized = false;
                disableModFunctionality();
            }
        });
    }

    private String getPlayerUDID(EntityPlayer player) {
        // Get the player's UUID
        String playerUUID = player.getUniqueID().toString();
        
        // You can add additional processing here if needed
        // For example, you might want to hash the UUID or combine it with other data
        
        return playerUUID;
    }

    private void disableModFunctionality() {
        // Disable any active features of the mod
        autoWalkEnabled = false;
        currentPath.clear();
        currentPathIndex = 0;
        // Add any other necessary disabling logic here
    }

    private EntityLivingBase findNextTargetEnderman(Minecraft mc, EntityLivingBase currentTarget, List<EntityLivingBase> entities) {
        List<EntityLivingBase> remainingEntities = new ArrayList<>(entities);
        remainingEntities.remove(currentTarget);
        return findClosestEnderman(mc, remainingEntities);
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) return;
    
        List<EntityLivingBase> entities = findEndermen(mc);
        if (entities.isEmpty()) return;
    
        // Filter out targets on cooldown
        long currentTime = System.currentTimeMillis();
        entities.removeIf(entity -> targetCooldowns.containsKey(entity) &&
            currentTime - targetCooldowns.get(entity) < TARGET_COOLDOWN);
    
        if (entities.isEmpty()) return;
    
        EntityLivingBase closestEnderman = findClosestEnderman(mc, entities);
        if (closestEnderman == null) return;
    
        // Find path from player to closest Enderman
        List<BlockPos> currentPath = findPath(mc, closestEnderman);
        if (currentPath.isEmpty()) {
            handleFailedPathfinding(closestEnderman);  // Handle failure case
        } else {
            resetFailedPathfinding(closestEnderman);  // Reset failure counter on success
            drawPath(currentPath, event.partialTicks, 0.0f, 1.0f, 1.0f, 1.0f); // Cyan color
    
            // Find the next target Enderman
            EntityLivingBase nextTarget = findNextTargetEnderman(mc, closestEnderman, entities);
            if (nextTarget != null) {
                List<BlockPos> nextPath = findPath(mc, closestEnderman.getPosition(), nextTarget.getPosition());
                if (!nextPath.isEmpty()) {
                    drawPath(nextPath, event.partialTicks, 0.0f, 0.5f, 0.0f, 1.0f); // Dark green color
                }
            }
        }
    }

    private void handleFailedPathfinding(EntityLivingBase target) {
        int attempts = failedPathAttempts.getOrDefault(target, 0) + 1;
        failedPathAttempts.put(target, attempts);
    
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            // Start cooldown for the target
            targetEndermen.remove(target); //exp
            targetCooldowns.put(target, System.currentTimeMillis());
            failedPathAttempts.remove(target); // Reset attempts after cooldown starts

        }
    }
    
    private void resetFailedPathfinding(EntityLivingBase target) {
        failedPathAttempts.remove(target); // Reset the failure count if the pathfinding was successful
    }

    private boolean isPlayerPaused() {
        return isInventoryOpen || isChatOpen;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            Minecraft mc = Minecraft.getMinecraft();
            
            isInventoryOpen = mc.currentScreen instanceof GuiContainer;
            isChatOpen = mc.currentScreen instanceof GuiChat;

            if (mc.currentScreen != null) {
                return;
            }
    
            if (mc.theWorld != null && mc.thePlayer != null) {
                if (!hasJoinMessageBeenSent) {
                    sendJoinMessage(mc.thePlayer);
                    hasJoinMessageBeenSent = true;
                }
    
                if (autoWalkEnabled && !isPlayerPaused()) {
                    if (keyAndClickActionsEnabled && !actionsRunning) {
                        keyAndClickActions.performActions();
                        actionsRunning = true;
                    } else if (!keyAndClickActionsEnabled && actionsRunning) {
                        keyAndClickActions.stopActions();
                        actionsRunning = false;
                    }
    
                    isTargetingEnderman = false;
    
                    updateTargetEndermen(mc);
                    if (!targetEndermen.isEmpty()) {
                        EntityLivingBase closestEnderman = findClosestEnderman(mc, targetEndermen);
                        if (closestEnderman != null) {
                            if (isEndermanInLineOfSight(mc, closestEnderman) && isWithinRange(mc.thePlayer, closestEnderman, MAX_MOUSE_MOVE_DISTANCE)) {
                                if (!lagEnabled || !isTargetingEnderman) {
                                    moveMouseTowardsEnderman(mc, closestEnderman);
                                    if (lagEnabled) {
                                        isTargetingEnderman = true;
                                    }
                                }
                            } 
    
                            if (!isTargetingEnderman) {
                                currentPath = findPath(mc, closestEnderman);
                                if (!currentPath.isEmpty()) {
                                    if (currentPathIndex < currentPath.size()) {
                                        BlockPos nextPoint = currentPath.get(currentPathIndex);
                                        
                                        if (isPlayerAtBlock(mc.thePlayer, nextPoint)) {
                                            currentPathIndex++;
                                        } else {
                                            moveTowards(mc, nextPoint);
                                        }
                                    } else {
                                        currentPathIndex = 0;
                                    }
                                }
                            }
                        }
                    } 
    
                    if (!isTargetingEnderman && autoWalkEnabled) {
                        KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), false);
                    }
    
                    long currentTime = System.currentTimeMillis();
                    if (isSneaking && currentTime - sneakStartTime > SNEAK_DURATION) {
                        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
                        isSneaking = false;
                    }
                } else {
                    if (actionsRunning) {
                        keyAndClickActions.stopActions();
                        actionsRunning = false;
                    }
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), false);
                }
            }
        }
    }

    public void updateDesiredHPListForEntity(String entityType) {
        // Clear the current desired HP list
        desiredEndermanHPs.clear();
    
        // Check if the entity type exists in the HP range map
        if (ENTITY_HP_RANGES.containsKey(entityType)) {
            // Add the HP range for the given entity type
            desiredEndermanHPs.addAll(ENTITY_HP_RANGES.get(entityType));
        } else {
            // If the entity type is not found, you can add a default or log an error
            System.out.println("Unknown entity type: " + entityType);
        }
    }

    
    
    private boolean isPlayerAtBlock(EntityPlayer player, BlockPos pos) {
        return Math.floor(player.posX) == pos.getX() &&
               Math.floor(player.posY) == pos.getY() &&
               Math.floor(player.posZ) == pos.getZ();
    }

    @SubscribeEvent
    public void onGuiClosed(GuiOpenEvent event) {
        if (event.gui == null && isPlayerPaused()) {
            // Reset auto-walk state
            currentPathIndex = 0;
            currentPath.clear();
        }
    }
    
    private void sendJoinMessage(EntityPlayer player) {
        String udid = getPlayerUDID(player);
    
        if (ALLOWED_UDID.equals(udid)) {
            IChatComponent prefix = new ChatComponentText("§6§l[Lynx] ");
            IChatComponent message = new ChatComponentText("§bYou are allowed to use the Zealot mod | ");
            IChatComponent enjoyText = new ChatComponentText("§a§1Enjoy.");
            prefix.appendSibling(message).appendSibling(enjoyText);
            player.addChatMessage(prefix);
            isPlayerAuthorized = true;
        } else {
            IChatComponent message = new ChatComponentText("§6§l[Lynx] §cYou are not allowed to use the Zealot mod | ");
            
            IChatComponent link = new ChatComponentText("§bClick here to get your own license");
            ChatStyle clickableStyle = new ChatStyle();
            clickableStyle.setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/hBcNz6db"));
            link.setChatStyle(clickableStyle);
            
            message.appendSibling(link);
            
            player.addChatMessage(message);
            isPlayerAuthorized = false;
            disableModFunctionality();
        }
    }

    private boolean isWithinRange(EntityPlayerSP player, EntityLivingBase entity, double maxDistance) {
    double dx = player.posX - entity.posX;
    double dy = player.posY - entity.posY;
    double dz = player.posZ - entity.posZ;
    double distanceSq = dx * dx + dy * dy + dz * dz;
    return distanceSq <= maxDistance * maxDistance;
}

    private boolean isEndermanInLineOfSight(Minecraft mc, EntityLivingBase  entity) {
        return mc.thePlayer.canEntityBeSeen(entity);
    }
    
    private void moveMouseTowardsEnderman(Minecraft mc, EntityLivingBase entity) {
        isTargetingEnderman = true;
    
        double dx = entity.posX - mc.thePlayer.posX;
        double dy = (entity.posY + entity.getEyeHeight() - 0.6) - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        double dz = entity.posZ - mc.thePlayer.posZ;
    
        double distance = Math.sqrt(dx * dx + dz * dz);
        float targetYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float targetPitch = (float) -Math.toDegrees(Math.atan2(dy, distance));
    
        float yawDifference = targetYaw - mc.thePlayer.rotationYaw;
        float pitchDifference = targetPitch - mc.thePlayer.rotationPitch;
    
        while (yawDifference > 180) yawDifference -= 360;
        while (yawDifference < -180) yawDifference += 360;
    
        float smoothYaw = clamp(yawDifference, -MAX_ROTATION_SPEED, MAX_ROTATION_SPEED);
        float smoothPitch = clamp(pitchDifference, -MAX_ROTATION_SPEED, MAX_ROTATION_SPEED);
    
        mc.thePlayer.rotationYaw += smoothYaw;
        mc.thePlayer.rotationPitch = clamp(mc.thePlayer.rotationPitch + smoothPitch, -90, 90);
    
        mc.thePlayer.rotationYaw = mc.thePlayer.rotationYaw % 360;
        if (mc.thePlayer.rotationYaw < 0) mc.thePlayer.rotationYaw += 360;
    
        long currentTime = System.currentTimeMillis();
    
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), true);
    
        if (isLookingAtEntity(mc, entity)) {
            if (currentTime - lastAttackTime > ATTACK_COOLDOWN) {
                KeyBinding.onTick(mc.gameSettings.keyBindAttack.getKeyCode());
                lastAttackTime = currentTime;
                
                // Implement 10% chance to sneak
                if (Math.random() < 0.1 && !isSneaking) {
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
                    isSneaking = true;
                    sneakStartTime = currentTime;
                }
            }
        }
    }
    
    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private boolean isLookingAtEntity(Minecraft mc, EntityLivingBase entity) {
        Vec3 look = mc.thePlayer.getLook(1.0F);
        Vec3 playerPos = mc.thePlayer.getPositionEyes(1.0F);
        Vec3 entityPos = new Vec3(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ);
        double d = playerPos.distanceTo(entityPos);
        Vec3 vec = playerPos.addVector(look.xCoord * d, look.yCoord * d, look.zCoord * d);
        return entity.getEntityBoundingBox().expand(0.1, 0.1, 0.1).isVecInside(vec);
    }

    private float smoothAngle(float current, float target) {
        float difference = MathHelper.wrapAngleTo180_float(target - current);
        return current + difference * SMOOTHING_FACTOR;
    }

    private void updateTargetEndermen(Minecraft mc) {
        List<EntityLivingBase> allEntities = findEndermen(mc);
        targetEndermen.clear();
        long currentTime = System.currentTimeMillis();
    
        for (EntityLivingBase entity : allEntities) {
            if (!cooldownMap.containsKey(entity) || currentTime - cooldownMap.get(entity) > COOLDOWN_DURATION) {
                targetEndermen.add(entity);
            }
        }
    }

    private void attackEnderman(Minecraft mc, EntityLivingBase entity) {
        // Attack the enderman
        long currentTime = System.currentTimeMillis();
        lastAttackTime = currentTime;
    
        // Add the enderman to the cooldown map
        cooldownMap.put(entity, currentTime);
    }

    private void moveTowards(Minecraft mc, BlockPos target) {
        double dx = target.getX() + 0.5 - mc.thePlayer.posX;
        double dy = target.getY() + 0.1 - mc.thePlayer.posY;
        double dz = target.getZ() + 0.5 - mc.thePlayer.posZ;
        double distSq = dx * dx + dy * dy + dz * dz;
    
        // If close to the target, move to the next point
        if (distSq < 2.5) {
            currentPathIndex++;
            return;
        }
    
        // Only adjust yaw if not targeting an Enderman
        if (!isTargetingEnderman) {
            // Calculate target yaw for larger steps
            float targetYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
    
            // Smooth rotation, larger yaw adjustments
            float yawDiff = targetYaw - mc.thePlayer.rotationYaw;
            while (yawDiff > 180) yawDiff -= 360;
            while (yawDiff < -180) yawDiff += 360;
            mc.thePlayer.rotationYaw += yawDiff * 0.4f;
    
            // Smooth pitch adjustment
            float targetPitch = 0;
            float pitchDiff = targetPitch - mc.thePlayer.rotationPitch;
            mc.thePlayer.rotationPitch += pitchDiff * 0.3f;
        }
    
        // Larger movement step
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), true);
    
        // Jump if needed
        if (isJumpNeeded(mc)) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), true);
        } else {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), false);
        }
    }

    private List<BlockPos> smoothPath(Minecraft mc, List<BlockPos> path) {
        if (path.size() < 3) return path;
    
        List<BlockPos> smoothed = new ArrayList<>();
        smoothed.add(path.get(0));
    
        for (int i = 1; i < path.size() - 1; i++) {
            BlockPos prev = path.get(i - 1);
            BlockPos current = path.get(i);
            BlockPos next = path.get(i + 1);
    
            if (canMoveDiagonally(mc, prev, next)) {
                // Skip the current point if we can move diagonally
                continue;
            }
    
            smoothed.add(current);
        }
    
        smoothed.add(path.get(path.size() - 1));
        return smoothed;
    }
    
    private boolean canMoveDiagonally(Minecraft mc, BlockPos start, BlockPos end) {
        BlockPos diff = end.subtract(start);
        BlockPos middle = start.add(diff.getX(), 0, diff.getZ());
        return isWalkable(mc, middle) && isWalkable(mc, middle.up());
    }

    private boolean isJumpNeeded(Minecraft mc) {
        EntityPlayerSP player = mc.thePlayer;
        BlockPos pos = new BlockPos(player.posX, player.posY, player.posZ);
        BlockPos frontPos = pos.offset(player.getHorizontalFacing());
        
        // Check if there's a block in front of the player
        if (!mc.theWorld.isAirBlock(frontPos)) {
            // Check if the block above the front block is air
            if (mc.theWorld.isAirBlock(frontPos.up())) {
                // Check if the player is on the ground
                if (player.onGround) {
                    // Check if the player is moving forward
                    double forwardSpeed = player.movementInput.moveForward;
                    if (forwardSpeed > 0) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }

    public boolean setTargetEntityType(String entityType) {
        System.out.println("Current target: " + targetEntityType);
        targetEntityType = entityType;
        System.out.println("Updated target: " + targetEntityType);
        System.out.println("Target entity type set to: " + targetEntityType); // Debug statement
        return true;
    }
    

    private List<EntityLivingBase> findEndermen(Minecraft mc) {
        List<EntityLivingBase> reachable = new ArrayList<>();
        BlockPos playerPos = mc.thePlayer.getPosition();
        double searchRadius = SEARCH_RADIUS;
        
        // Priority queue to find the closest entities
        PriorityQueue<EntityLivingBase> entityQueue = new PriorityQueue<>(Comparator.comparingDouble(e -> e.getDistanceSq(playerPos)));
    
        // Iterate through loaded entities and filter by the selected entity type
        for (Object entity : mc.theWorld.loadedEntityList) {
            if (entity instanceof EntityLivingBase) {
                EntityLivingBase livingEntity = (EntityLivingBase) entity;
                if (isTargetEntityType(livingEntity)) {
                    double distanceSq = livingEntity.getDistanceSq(playerPos);
                    if (distanceSq <= searchRadius * searchRadius) {
                        // Ensure the entity is on a similar Y level (within 35 blocks) and has desired attributes if needed
                        if (Math.abs(livingEntity.posY - mc.thePlayer.posY) < 10) {
                            if (hasDesiredMaxHealth(livingEntity)) {
                                entityQueue.add(livingEntity);
                            }
                        }
                    }
                }
            }
        }
    
        // Limit to the 15 closest entities
        List<EntityLivingBase> closestEntities = new ArrayList<>();
        while (!entityQueue.isEmpty() && closestEntities.size() < 35) {
            closestEntities.add(entityQueue.poll());
        }
    
        reachable.addAll(closestEntities);
        return reachable;
    }

    public boolean isTargetEntityType(EntityLivingBase entity) {// Debug statement
        switch (targetEntityType) {
            case "enderman":
                return entity instanceof EntityEnderman;
            case "zombie":
                return entity instanceof EntityZombie;
            case "spider":
                return entity instanceof EntitySpider;
            case "wolf":
                return entity instanceof EntityWolf;
            case "blaze":
                return entity instanceof EntityBlaze;
            case "mooshroom":
                return entity instanceof EntityMooshroom;
            case "creeper":
                return entity instanceof EntityCreeper;
            case "skeleton":
                return entity instanceof EntitySkeleton;
            case "endermite":
                return entity instanceof EntityEndermite;
            case "custom":
                if (entity instanceof EntityOtherPlayerMP) {
                    String name = entity.getName();
                    return name != null && CUSTOM_ENTITY_NAMES.contains(name.trim().toLowerCase());
                }
                return false;
            default:
                return false;
        }
    }
    

    // firemage entity 2m hp is EntityOtherplayerMP
    // magma cube 400k hp EntityMagmaCube
    //  entitySlime 150hp
    //entity skeleton 250hp
    



    private boolean hasDesiredMaxHealth(EntityLivingBase entity) {
        float currentHealth = entity.getHealth();
        
        if (desiredEndermanHPs.size() >= 2) {
            float minHP = desiredEndermanHPs.get(0);
            float maxHP = desiredEndermanHPs.get(1);
            
            // Check if the current health falls within the range
            return currentHealth >= minHP && currentHealth <= maxHP;
        }
        
        return false;
    }
    
    
    

    private EntityLivingBase findClosestEnderman(Minecraft mc, List<EntityLivingBase> entities) {
        return entities.stream()
                .min(Comparator.comparingDouble(e -> e.getDistanceSqToEntity(mc.thePlayer)))
                .orElse(null);
    }

    private List<BlockPos> findPath(Minecraft mc, BlockPos start, BlockPos end) {
    
        // Preliminary reachability check
        if (isInSealedArea(mc, end)) {
            return new ArrayList<>(); // Return empty path if unreachable
        }
    
        final int MAX_PATH_LENGTH = 100; // Maximum path length
    
        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Map<BlockPos, Node> allNodes = new HashMap<>();
    
        Node startNode = new Node(start, null, 0, start.distanceSq(end));
        openSet.add(startNode);
        allNodes.put(start, startNode);
    
        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
    
            // Check if the path is too long
            if (current.g > MAX_PATH_LENGTH) {
                continue; // Skip processing this node, it's part of an overly long path
            }
    
            if (current.pos.distanceSq(end) <= 1) {  // Allow getting close to the target
                List<BlockPos> rawPath = reconstructPath(current);
                if (rawPath.size() <= MAX_PATH_LENGTH) {
                    return smoothPath(mc, rawPath);
                } else {
                    return new ArrayList<>(); // Path is too long
                }
            }
    
            for (BlockPos neighbor : getNeighbors(current.pos)) {
                if (!isWalkable(mc, neighbor)) continue;
    
                double newG = current.g + current.pos.distanceSq(neighbor);
                Node neighborNode = allNodes.get(neighbor);
    
                if (neighborNode == null) {
                    neighborNode = new Node(neighbor, current, newG, heuristic(neighbor, end));
                    allNodes.put(neighbor, neighborNode);
                    openSet.add(neighborNode);
                } else if (newG < neighborNode.g) {
                    neighborNode.parent = current;
                    neighborNode.g = newG;
                    neighborNode.f = newG + heuristic(neighbor, end);
                    openSet.remove(neighborNode);
                    openSet.add(neighborNode);
                }
            }
        }
    
        return new ArrayList<>(); // No path found
    }
    
    private boolean isInSealedArea(Minecraft mc, BlockPos pos) {
        // Check if the position is surrounded by solid blocks
        for (EnumFacing facing : EnumFacing.values()) {
            BlockPos neighbor = pos.offset(facing);
            if (mc.theWorld.isAirBlock(neighbor)) {
                return false;
            }
        }
    
        // Check if there's a path from the player to this position
        EntityPlayerSP player = mc.thePlayer;
        BlockPos playerPos = new BlockPos(player.posX, player.posY, player.posZ);
        
        // Use a simplified BFS to check if there's a path
        Queue<BlockPos> queue = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();
        queue.offer(playerPos);
        visited.add(playerPos);
    
        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            if (current.equals(pos)) {
                return false; // Found a path, not sealed
            }
    
            for (EnumFacing facing : EnumFacing.values()) {
                BlockPos next = current.offset(facing);
                if (!visited.contains(next) && isWalkable(mc, next)) {
                    queue.offer(next);
                    visited.add(next);
                }
            }
        }
    
        return true; // No path found, considered sealed
    }
    
    // Overload for finding path from player to Enderman
    private List<BlockPos> findPath(Minecraft mc, EntityLivingBase entity) {
        return findPath(mc, mc.thePlayer.getPosition(), entity.getPosition());
    }

    private double heuristic(BlockPos a, BlockPos b) {
        return Math.sqrt(a.distanceSq(b));
    }

    private List<BlockPos> reconstructPath(Node endNode) {
        List<BlockPos> path = new ArrayList<>();
        Node current = endNode;
        while (current != null) {
            path.add(current.pos);
            current = current.parent;
        }
        Collections.reverse(path);
        return path;
    }

    private List<BlockPos> getNeighbors(BlockPos pos) {
        List<BlockPos> neighbors = new ArrayList<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    BlockPos neighbor = pos.add(dx, dy, dz);
                    
                    // Add valid neighbor blocks to the path
                    neighbors.add(neighbor);
                }
            }
        }
    
        // Handle dropping down up to 8 blocks
        for (int drop = 1; drop <= 8; drop++) {
            BlockPos dropPos = pos.down(drop);
            if (isWalkable(mc, dropPos)) {
                neighbors.add(dropPos);
            } else {
                break; // Stop checking further down if we hit an unwalkable block
            }
        }
        
        return neighbors;
    }

    private boolean isWalkable(Minecraft mc, BlockPos pos) {
        // Block specific positions for pathfinding
        if (isBlockedPosition(pos)) {
            return false;
        }
    
        Block blockBelow = mc.theWorld.getBlockState(pos.down()).getBlock();
        Block block = mc.theWorld.getBlockState(pos).getBlock();
        
        // Check if the block is a slab, stair, or carpet
        boolean isSlabOrStair = blockBelow instanceof BlockSlab || blockBelow instanceof BlockStairs;
        boolean isCarpet = block instanceof BlockCarpet;
    
        // Check if the block below is solid, or it's a slab/stair
        return (mc.theWorld.isAirBlock(pos) || isCarpet) && 
               mc.theWorld.isAirBlock(pos.up()) &&
               (isSlabOrStair || blockBelow.isBlockNormalCube() || blockBelow.isOpaqueCube() || isCarpet);
    }

    public void addBlockedPosition(Minecraft mc) {
        // Get the player's current position
        BlockPos currentPos = mc.thePlayer.getPosition();
    
        // Add the block at the player's position and surrounding blocks to the blocked list
        blockedPositions.add(currentPos);
        blockedPositions.add(currentPos.up());
        blockedPositions.add(currentPos.down());
        blockedPositions.add(currentPos.north());
        blockedPositions.add(currentPos.south());
        blockedPositions.add(currentPos.east());
        blockedPositions.add(currentPos.west());
    
        // Send a confirmation message to the player
        mc.thePlayer.addChatMessage(new ChatComponentText("Added blocked positions at: " + currentPos));
    }
    

    private boolean isBlockedPosition(BlockPos pos) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCacheClear > CACHE_CLEAR_INTERVAL) {
            blockedCache.clear();
            lastCacheClear = currentTime;
        }
    
        if (blockedCache.contains(pos)) {
            return true;
        }
    
        for (BlockPos blocked : blockedPositions) {
            if (pos.equals(blocked)) {
                blockedCache.add(pos);
                return true;
            }
        }
    
        return false;
    }


    private void drawPath(List<BlockPos> path, float partialTicks, float r, float g, float b, float a) {
        if (path.isEmpty()) return;
    
        Minecraft mc = Minecraft.getMinecraft();
        double playerX = mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * partialTicks;
        double playerY = mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * partialTicks;
        double playerZ = mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * partialTicks;
    
        GlStateManager.pushMatrix();
        GlStateManager.translate(-playerX, -playerY, -playerZ);
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
    
        for (BlockPos point : path) {
            worldrenderer.pos(point.getX() + 0.5, point.getY() + 0.1, point.getZ() + 0.5)
                        .color(r, g, b, a)
                        .endVertex();
        }
    
        tessellator.draw();
    
        // Draw small cubes at each path point
        for (BlockPos point : path) {
            drawCube(new Vec3(point.getX() + 0.5, point.getY() + 0.1, point.getZ() + 0.5), 0.1f, r, g, b, a);
        }
    
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    private void drawSphere(Vec3 center, float radius, float r, float g, float b, float a) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);

        for (int i = 0; i <= 18; i++) {
            double lat = Math.PI * (double) (i - 9) / 18.0;
            for (int j = 0; j <= 18; j++) {
                double lon = 2 * Math.PI * (double) j / 18.0;
                double x = Math.cos(lat) * Math.cos(lon);
                double y = Math.sin(lat);
                double z = Math.cos(lat) * Math.sin(lon);
                worldrenderer.pos(center.xCoord + x * radius, center.yCoord + y * radius, center.zCoord + z * radius).color(r, g, b, a).endVertex();
            }
        }

        tessellator.draw();
    }

    private void drawCube(Vec3 center, float size, float r, float g, float b, float a) {
        float halfSize = size / 2;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
    
        // Bottom face
        worldrenderer.pos(center.xCoord - halfSize, center.yCoord - halfSize, center.zCoord - halfSize).color(r, g, b, a).endVertex();
        worldrenderer.pos(center.xCoord + halfSize, center.yCoord - halfSize, center.zCoord - halfSize).color(r, g, b, a).endVertex();
        worldrenderer.pos(center.xCoord + halfSize, center.yCoord - halfSize, center.zCoord + halfSize).color(r, g, b, a).endVertex();
        worldrenderer.pos(center.xCoord - halfSize, center.yCoord - halfSize, center.zCoord + halfSize).color(r, g, b, a).endVertex();
    
        // Top face
        worldrenderer.pos(center.xCoord - halfSize, center.yCoord + halfSize, center.zCoord - halfSize).color(r, g, b, a).endVertex();
        worldrenderer.pos(center.xCoord + halfSize, center.yCoord + halfSize, center.zCoord - halfSize).color(r, g, b, a).endVertex();
        worldrenderer.pos(center.xCoord + halfSize, center.yCoord + halfSize, center.zCoord + halfSize).color(r, g, b, a).endVertex();
        worldrenderer.pos(center.xCoord - halfSize, center.yCoord + halfSize, center.zCoord + halfSize).color(r, g, b, a).endVertex();
    
        // Front face
        worldrenderer.pos(center.xCoord - halfSize, center.yCoord - halfSize, center.zCoord + halfSize).color(r, g, b, a).endVertex();
        worldrenderer.pos(center.xCoord + halfSize, center.yCoord - halfSize, center.zCoord + halfSize).color(r, g, b, a).endVertex();
        worldrenderer.pos(center.xCoord + halfSize, center.yCoord + halfSize, center.zCoord + halfSize).color(r, g, b, a).endVertex();
        worldrenderer.pos(center.xCoord - halfSize, center.yCoord + halfSize, center.zCoord + halfSize).color(r, g, b, a).endVertex();
    
        // Back face
        worldrenderer.pos(center.xCoord - halfSize, center.yCoord - halfSize, center.zCoord - halfSize).color(r, g, b, a).endVertex();
        worldrenderer.pos(center.xCoord + halfSize, center.yCoord - halfSize, center.zCoord - halfSize).color(r, g, b, a).endVertex();
        worldrenderer.pos(center.xCoord + halfSize, center.yCoord + halfSize, center.zCoord - halfSize).color(r, g, b, a).endVertex();
        worldrenderer.pos(center.xCoord - halfSize, center.yCoord + halfSize, center.zCoord - halfSize).color(r, g, b, a).endVertex();
    
        // Left face
        worldrenderer.pos(center.xCoord - halfSize, center.yCoord - halfSize, center.zCoord - halfSize).color(r, g, b, a).endVertex();
        worldrenderer.pos(center.xCoord - halfSize, center.yCoord - halfSize, center.zCoord + halfSize).color(r, g, b, a).endVertex();
        worldrenderer.pos(center.xCoord - halfSize, center.yCoord + halfSize, center.zCoord + halfSize).color(r, g, b, a).endVertex();
        worldrenderer.pos(center.xCoord - halfSize, center.yCoord + halfSize, center.zCoord - halfSize).color(r, g, b, a).endVertex();
    
        // Right face
        worldrenderer.pos(center.xCoord + halfSize, center.yCoord - halfSize, center.zCoord - halfSize).color(r, g, b, a).endVertex();
        worldrenderer.pos(center.xCoord + halfSize, center.yCoord - halfSize, center.zCoord + halfSize).color(r, g, b, a).endVertex();
        worldrenderer.pos(center.xCoord + halfSize, center.yCoord + halfSize, center.zCoord + halfSize).color(r, g, b, a).endVertex();
        worldrenderer.pos(center.xCoord + halfSize, center.yCoord + halfSize, center.zCoord - halfSize).color(r, g, b, a).endVertex();
    
        tessellator.draw();
    }

    private void drawBoundingBox(AxisAlignedBB bb, float red, float green, float blue, float alpha) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        worldrenderer.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
        worldrenderer.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
        worldrenderer.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        worldrenderer.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();

        worldrenderer.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
        worldrenderer.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
        worldrenderer.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        worldrenderer.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();

        worldrenderer.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
        worldrenderer.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
        worldrenderer.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
        worldrenderer.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();

        worldrenderer.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        worldrenderer.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        worldrenderer.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        worldrenderer.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();

        tessellator.draw();
    }


    private static class Node implements Comparable<Node> {
        BlockPos pos;
        Node parent;
        double g, f;

        Node(BlockPos pos, Node parent, double g, double h) {
            this.pos = pos;
            this.parent = parent;
            this.g = g;
            this.f = g + h;
        }

        @Override
        public int compareTo(Node other) {
            return Double.compare(this.f, other.f);
        }
    }
}


    