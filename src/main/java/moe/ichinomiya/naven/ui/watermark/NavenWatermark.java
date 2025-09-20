package moe.ichinomiya.naven.ui.watermark;

import moe.ichinomiya.naven.Naven;
import moe.ichinomiya.naven.Version;
import moe.ichinomiya.naven.utils.RenderUtils;
import moe.ichinomiya.naven.utils.StencilUtils;
import moe.ichinomiya.naven.utils.font.FontManager;
import net.minecraft.client.Minecraft;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NavenWatermark extends WaterMark {
    private final static SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
    private final static String version = Version.getVersion();

    public final static int headerColor = new Color(150, 45, 45, 255).getRGB();
    public final static int bodyColor = new Color(0, 0, 0, 190).getRGB();

    int width;

    public void render() {
        if (Minecraft.getMinecraft().gameSettings.hideGUI || Minecraft.getMinecraft().gameSettings.showDebugInfo) {
            return;
        }

        Naven naven = Naven.getInstance();

        FontManager fontManager = naven.getFontManager();
        String text = Naven.CLIENT_DISPLAY_NAME + " " + version + " | User" + " | " + Minecraft.getDebugFPS() + " FPS | " + format.format(new Date());
        width = fontManager.opensans15.getStringWidth(text) + 10;

        StencilUtils.write(false);
        RenderUtils.drawBoundRoundedRect(5, 5, width, 18, 5f, 0xFFFFFFFF);
        StencilUtils.erase(true);

        RenderUtils.drawRectBound(5, 5, width, 3, headerColor);
        RenderUtils.drawRectBound(5, 8, width, 15, bodyColor);

        fontManager.opensans15.drawString(text, 10, 10, 0xFFFFFFFF);
        StencilUtils.dispose();
    }

    public void renderShader() {
        if (Minecraft.getMinecraft().gameSettings.hideGUI || Minecraft.getMinecraft().gameSettings.showDebugInfo) {
            return;
        }

        RenderUtils.drawBoundRoundedRect(5, 5, width, 18, 5f, 0xFFFFFFFF);
    }
}
