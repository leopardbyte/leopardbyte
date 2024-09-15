package com.example.endermanfinder;

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
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.util.*;

@Mod(modid = EndermanFinder.MODID, version = EndermanFinder.VERSION, clientSideOnly = true)
public class EndermanFinder {
    public static final String MODID = "endermanfinder";
    public static final String VERSION = "1.3";
    private static final int SEARCH_RADIUS = 150;
    private static final double PATH_STEP = 4;

    private boolean autoWalkEnabled = false;
    private List<BlockPos> currentPath = new ArrayList<>();
    private int currentPathIndex = 0;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        ClientCommandHandler.instance.registerCommand(new CommandBase() {
            @Override
            public String getCommandName() {
                return "eman";
            }

            @Override
            public String getCommandUsage(ICommandSender sender) {
                return "/eman walk";
            }

            @Override
            public void processCommand(ICommandSender sender, String[] args) {
                if (args.length == 1 && args[0].equalsIgnoreCase("walk")) {
                    autoWalkEnabled = !autoWalkEnabled;
                    String status = autoWalkEnabled ? "enabled" : "disabled";
                    sender.addChatMessage(new ChatComponentText("Auto-walk " + status));
                }
            }

            @Override
            public int getRequiredPermissionLevel() {
                return 0;
            }
        });
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) return;

        List<EntityEnderman> endermen = findEndermen(mc);
        if (endermen.isEmpty()) return;

        EntityEnderman closestEnderman = findClosestEnderman(mc, endermen);
        if (closestEnderman == null) return;

        currentPath = findPath(mc, closestEnderman);
        drawPath(currentPath, event.partialTicks);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START && autoWalkEnabled) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.thePlayer != null && !currentPath.isEmpty()) {
                if (currentPathIndex < currentPath.size()) {
                    BlockPos nextPoint = currentPath.get(currentPathIndex);
                    moveTowards(mc, nextPoint);
                } else {
                    currentPathIndex = 0;
                }
            }
        }
    }

    private void moveTowards(Minecraft mc, BlockPos target) {
        double dx = target.getX() + 0.5 - mc.thePlayer.posX;
        double dy = target.getY() + 0.1 - mc.thePlayer.posY;
        double dz = target.getZ() + 0.5 - mc.thePlayer.posZ;
        double distSq = dx * dx + dy * dy + dz * dz;

        if (distSq < 0.1) {
            currentPathIndex++;
            return;
        }

        // Calculate target yaw
        float targetYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));

        // Smooth rotation
        float yawDiff = targetYaw - mc.thePlayer.rotationYaw;
        while (yawDiff > 180) yawDiff -= 360;
        while (yawDiff < -180) yawDiff += 360;
        mc.thePlayer.rotationYaw += yawDiff * 0.2f;

        // Keep pitch level
        mc.thePlayer.rotationPitch = 0;

        // Movement
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
        BlockPos pos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
        return !mc.theWorld.isAirBlock(pos.offset(mc.thePlayer.getHorizontalFacing())) ||
               !mc.theWorld.isAirBlock(pos.offset(mc.thePlayer.getHorizontalFacing()).up());
    }

    private List<EntityEnderman> findEndermen(Minecraft mc) {
        List<EntityEnderman> endermen = new ArrayList<>();
        BlockPos playerPos = mc.thePlayer.getPosition();
        for (Object entity : mc.theWorld.loadedEntityList) {
            if (entity instanceof EntityEnderman) {
                EntityEnderman enderman = (EntityEnderman) entity;
                if (enderman.getDistanceSq(playerPos) <= SEARCH_RADIUS * SEARCH_RADIUS) {
                    endermen.add(enderman);
                }
            }
        }
        return endermen;
    }

    private EntityEnderman findClosestEnderman(Minecraft mc, List<EntityEnderman> endermen) {
        return endermen.stream()
                .min(Comparator.comparingDouble(e -> e.getDistanceSqToEntity(mc.thePlayer)))
                .orElse(null);
    }

    private List<BlockPos> findPath(Minecraft mc, EntityEnderman enderman) {
        BlockPos start = mc.thePlayer.getPosition();
        BlockPos end = enderman.getPosition();
    
        System.out.println("Start: " + start + ", End: " + end); // Debug statement
    
        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Map<BlockPos, Node> allNodes = new HashMap<>();
    
        Node startNode = new Node(start, null, 0, start.distanceSq(end));
        openSet.add(startNode);
        allNodes.put(start, startNode);
    
        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            if (current.pos.distanceSq(end) <= 2) {  // Allow getting close to the enderman
                List<BlockPos> rawPath = reconstructPath(current);
                return smoothPath(mc, rawPath);
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
                    neighbors.add(pos.add(dx, dy, dz));
                }
            }
        }
        return neighbors;
    }

    private boolean isWalkable(Minecraft mc, BlockPos pos) {
        return mc.theWorld.isAirBlock(pos) && 
               mc.theWorld.isAirBlock(pos.up()) && 
               (mc.theWorld.getBlockState(pos.down()).getBlock().isBlockNormalCube() ||
                mc.theWorld.getBlockState(pos.down()).getBlock().isOpaqueCube());
    }


    private void drawPath(List<BlockPos> path, float partialTicks) {
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

        // Draw the path line
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);

        for (BlockPos point : path) {
            worldrenderer.pos(point.getX() + 0.5, point.getY() + 0.1, point.getZ() + 0.5)
                        .color(1.0f, 1.0f, 0.0f, 1.0f).endVertex();
        }

        tessellator.draw();

        // Draw small cubes at each path point
        for (BlockPos point : path) {
            drawCube(new Vec3(point.getX() + 0.5, point.getY() + 0.1, point.getZ() + 0.5), 0.1f, 1.0f, 0.0f, 0.0f, 1.0f);
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
