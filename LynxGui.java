package com.example.endermanfinder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatComponentText;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.awt.Color;

public class LynxGui extends GuiScreen {

    private CustomDropdownMenu dropdownMenu;
    private EndermanFinder endermanFinder;
    private static final ResourceLocation BUTTON_TEXTURES = new ResourceLocation("textures/gui/widgets.png");
    private boolean isAutokillerRunning = false;
    private float hue = 0;

    public LynxGui(EndermanFinder endermanFinder) {
        this.endermanFinder = endermanFinder;
    }

    @Override
    public void initGui() {
        this.isAutokillerRunning = endermanFinder.autoWalkEnabled; // Load autokiller state
    
        // Initialize the dropdown menu first
        List<String> targets = Arrays.asList("enderman", "zombie", "spider", "wolf", "blaze", "mooshroom", "creeper", "skeleton", "custom");
        this.dropdownMenu = new CustomDropdownMenu(this.width / 2 - 60, this.height / 2, 100, 20, targets, this.endermanFinder);
    
        // Now set the selected option from EndermanFinder
        this.dropdownMenu.selectedOption = endermanFinder.selectedTarget; // Load the previously selected target
    
        this.buttonList.clear();
        this.buttonList.add(new CustomGuiButton(0, this.width / 2 - 150, this.height / 2 - 50, 80, 20,
                isAutokillerRunning ? I18n.format("Stop Autokiller") : I18n.format("Start Autokiller")));
    
        this.buttonList.add(new CustomGuiButton(1, this.width / 2 - 150, this.height / 2, 80, 20, I18n.format("Select Target")));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        // Handle button click actions
        switch (button.id) {
            case 0:
                startAutokiller((CustomGuiButton) button);
                break;
            case 1:
                // Toggle the dropdown menu visibility
                this.dropdownMenu.setVisible(!this.dropdownMenu.isVisible());
                break;
        }
    }

    private void startAutokiller(CustomGuiButton button) {
        endermanFinder.setAutoWalkEnabled(!endermanFinder.autoWalkEnabled);
        String status = endermanFinder.autoWalkEnabled ? "§aenabled" : "§cdisabled";
        mc.thePlayer.addChatMessage(new ChatComponentText("§6§l[Lynx] §bAuto-walk " + status));
        isAutokillerRunning = endermanFinder.autoWalkEnabled;  // Update the stored state

        if (endermanFinder.autoWalkEnabled) {
            button.displayString = I18n.format("Stop Autokiller");
            button.setButtonColor(0x00FF00); // Green
        } else {
            button.displayString = I18n.format("Start Autokiller");
            button.setButtonColor(0x4169E1); // Royal Blue
            endermanFinder.keyAndClickActions.stopActions();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Draw the GUI background
        this.drawDefaultBackground();
    
        // Calculate rainbow color
        int rainbow = Color.HSBtoRGB(hue, 1.0F, 1.0F);
    
        // Draw title with rainbow color
        this.drawCenteredString(this.fontRendererObj, I18n.format("Lynx"), this.width / 2, 20, rainbow);
    
        // Update hue for next frame
        hue += 0.001F;
        if (hue > 1.0F) {
            hue = 0.0F;
        }
    
        // Draw the dropdown menu
        this.dropdownMenu.drawMenu(mc, mouseX, mouseY);
    
        // Call super to render buttons
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;  // Keeps the game running while GUI is open
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        // Close the GUI when Escape is pressed
        if (keyCode == Keyboard.KEY_ESCAPE) {
            this.mc.displayGuiScreen(null);
        }
        try {
            super.keyTyped(typedChar, keyCode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Custom button class to handle color changes
    public static class CustomGuiButton extends GuiButton {
        private int buttonColor = 0x4169E1; // Default to royal blue

        public CustomGuiButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
            super(buttonId, x, y, widthIn, heightIn, buttonText);
        }

        public void setButtonColor(int color) {
            this.buttonColor = color;
        }

        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY) {
            if (this.visible) {
                FontRenderer fontrenderer = mc.fontRendererObj;
                mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
                GlStateManager.color((buttonColor >> 16 & 255) / 255.0F, (buttonColor >> 8 & 255) / 255.0F, (buttonColor & 255) / 255.0F, this.hovered ? 1.0F : 0.8F);
                this.drawTexturedModalRect(this.xPosition, this.yPosition, 0, 46 + (this.getHoverState(this.hovered) * 20), this.width, this.height);
                this.mouseDragged(mc, mouseX, mouseY);
                int j = 14737632;

                if (packedFGColour != 0) {
                    j = packedFGColour;
                } else if (!this.enabled) {
                    j = 10526880;
                } else if (this.hovered) {
                    j = 16777120;
                }

                this.drawCenteredString(fontrenderer, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, j);
            }
        }
    }

    // Custom dropdown menu class
    public static class CustomDropdownMenu {
        private int x, y, width, height;
        private List<String> options;
        private boolean visible = false;
        private EndermanFinder endermanFinder;
        private String selectedOption = null;

        public CustomDropdownMenu(int x, int y, int width, int height, List<String> options, EndermanFinder endermanFinder) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.options = options;
            this.endermanFinder = endermanFinder;
        }

        public void drawMenu(Minecraft mc, int mouseX, int mouseY) {
            if (this.visible) {
                FontRenderer fontrenderer = mc.fontRendererObj;
                for (int i = 0; i < options.size(); i++) {
                    int optionY = this.y + i * this.height;
                    mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
                    
                    // Set color based on selection
                    if (options.get(i).equals(selectedOption)) {
                        GlStateManager.color(0.0F, 1.0F, 1.0F, 1.0F); // Cyan color for selected option
                    } else {
                        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F); // Default white color
                    }
        
                    mc.ingameGUI.drawTexturedModalRect(this.x, optionY, 0, 46, this.width, this.height);
                    fontrenderer.drawString(options.get(i), this.x + 5, optionY + (this.height - 8) / 2, options.get(i).equals(selectedOption) ? 0x00FFFF : 0xFFFFFF); // Change text color if selected
                }
                // Reset color to avoid affecting other UI elements
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            }
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
        }

        public boolean isVisible() {
            return this.visible;
        }

        public void handleMouseClick(int mouseX, int mouseY) {
            if (this.visible) {
                for (int i = 0; i < options.size(); i++) {
                    int optionY = this.y + i * this.height;
                    if (mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= optionY && mouseY <= optionY + this.height) {
                        String entityType = options.get(i);
                        if (endermanFinder.setTargetEntityType(entityType)) {
                            selectedOption = entityType;  // Set the selected option locally
                            endermanFinder.selectedTarget = entityType;  // Save the target globally in EndermanFinder
                            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("§6§l[Lynx] §bTarget entity set to " + entityType));
                            endermanFinder.updateDesiredHPListForEntity(entityType);
                        } else {
                            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("§6§l[Lynx] §cInvalid entity type!"));
                        }
                        this.setVisible(false);
                        break;
                    }
                }
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.dropdownMenu.handleMouseClick(mouseX, mouseY);
    }
}