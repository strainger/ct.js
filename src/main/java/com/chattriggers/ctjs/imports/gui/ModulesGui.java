package com.chattriggers.ctjs.imports.gui;

import com.chattriggers.ctjs.imports.Module;
import com.chattriggers.ctjs.imports.ModuleMetadata;
import com.chattriggers.ctjs.libs.ChatLib;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class ModulesGui extends GuiScreen {
    private FontRenderer ren = Minecraft.getMinecraft().fontRendererObj;
    private ArrayList<Module> modules;

    private int scrolled;
    private int maxScroll;

    private ScaledResolution res;

    public ModulesGui(ArrayList<Module> modules) {
        this.modules = modules;

        this.scrolled = 0;

        this.res = new ScaledResolution(Minecraft.getMinecraft());
        this.maxScroll = this.modules.size() * 110 + 10 - this.res.getScaledHeight();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        this.res = new ScaledResolution(Minecraft.getMinecraft());
        this.maxScroll = this.modules.size() * 110 + 10 - this.res.getScaledHeight();

        drawBackground(0);

        // draw scroll bar
        int scrollHeight = res.getScaledHeight() - this.maxScroll;
        if (scrollHeight < 20) scrollHeight = 20;
        if (scrollHeight < res.getScaledHeight()) {
            int scrollY = (int) map(this.scrolled, 0, this.maxScroll, 10, res.getScaledHeight() - scrollHeight - 10);
            rectangle(
                    this.res.getScaledWidth() - 5,
                    scrollY,
                    5,
                    scrollHeight,
                    0xa0000000
            );
        }

        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            drawModule(module, module.getMetadata(), i);
        }
    }

    private float map(float number, float in_min, float in_max, float out_min, float out_max) {
        return (number - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);

            if (isHovered(module, mouseX, mouseY, i)) {

            }
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        int i = Mouse.getEventDWheel();
        if (i == 0) return;

        if (i > 1) i = 1;
        if (i < -1) i = -1;

        if (isShiftKeyDown()) i *= 10;
        else i *= 20;

        this.scrolled -= i;
        if (this.scrolled > this.maxScroll)
            this.scrolled = this.maxScroll;
        if (this.scrolled < 0)
            this.scrolled = 0;
    }

    private void drawModule(Module module, ModuleMetadata moduleMetadata, int i) {
        int x = 20;
        int y = getModuleY(i);
        int width = this.res.getScaledWidth() - 40;
        int height = 105;

        // background
        rectangle(x, y, width, height, 0x80000000);

        // name
        String name = (moduleMetadata.getName() == null) ? module.getName() : moduleMetadata.getName();
        ren.drawStringWithShadow(
                ChatLib.addColor(name),
                x + 2,
                y + 2,
                0xffffffff
        );

        // version
        if (moduleMetadata.getVersion() != null) {
            String version = ChatFormatting.GRAY  + "v" + moduleMetadata.getVersion();
            ren.drawStringWithShadow(
                    version,
                    x + width - ren.getStringWidth(version) - 2,
                    y + 2,
                    0xffffffff
            );
        }

        // line break
        rectangle(x + 2, y+12, width - 4, 2, 0xa0000000);

        // description
        String description = (moduleMetadata.getDescription() == null) ? "No description provided" : moduleMetadata.getDescription();
        ArrayList<String> descriptionLines = lineWrap(new ArrayList<>(Arrays.asList(description.split("\n"))), width - 5);
        for (int j = 0; j < descriptionLines.size(); j++) {
            ren.drawStringWithShadow(
                    ChatLib.addColor(descriptionLines.get(j)),
                    x + 2,
                    y + 20 + j * 10,
                    0xffffffff
            );
        }

        // directory
        ren.drawStringWithShadow(
                ChatFormatting.DARK_GRAY + "/mods/ChatTriggers/modules/" + module.getName() + "/",
                x + 2,
                y + height - 12,
                0xffffffff
        );
    }

    private ArrayList<String> lineWrap(ArrayList<String> lines, int width) {
        int lineWrapIterator = 0;
        Boolean lineWrapContinue = true;
        Boolean addExtra = false;

        while (lineWrapContinue) {
            String line = lines.get(lineWrapIterator);
            if (ren.getStringWidth(line) > width) {
                String[] lineParts = line.split(" ");
                StringBuilder lineBefore = new StringBuilder();
                StringBuilder lineAfter = new StringBuilder();

                Boolean fillBefore = true;
                for (String linePart : lineParts) {
                    if (fillBefore) {
                        if (ren.getStringWidth(lineBefore.toString() + linePart) < width)
                            lineBefore.append(linePart).append(" ");
                        else
                            fillBefore = false;
                    }

                    if (!fillBefore) {
                        lineAfter.append(" ").append(linePart);
                    }
                }

                lines.set(lineWrapIterator, lineBefore.toString());
                if (lines.size() < 6) lines.add(lineWrapIterator+1, lineAfter.toString());
                else addExtra = true;
            }

            lineWrapIterator++;
            if (lineWrapIterator >= lines.size()) {
                lineWrapContinue = false;
            }
        }

        if (addExtra) lines.add("...");

        return lines;
    }

    private int getModuleY(int i) {
        return i * 110 + 10 - scrolled;
    }

    private Boolean isHovered(Module module, int mouseX, int mouseY, int i) {
        return mouseX > 10 && mouseX < ren.getStringWidth(module.getName()) + 20
                && mouseY > getModuleY(i) && mouseY < getModuleY(i) + 10;
    }

    private void rectangle(int x, int y, int width, int height, int color) {
        drawRect(x, y, x+width, y+height, color);
    }

    private float easeOut(float from, float to ) {
        return from + (to - from) / 5f;
    }

    private void openModule(Module theModule) {
        Minecraft.getMinecraft().displayGuiScreen(new ModuleGui(theModule));
    }
}
