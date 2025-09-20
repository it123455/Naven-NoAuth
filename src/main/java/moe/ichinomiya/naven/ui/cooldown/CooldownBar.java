package moe.ichinomiya.naven.ui.cooldown;

import lombok.Data;
import moe.ichinomiya.naven.Naven;
import moe.ichinomiya.naven.utils.RenderUtils;
import moe.ichinomiya.naven.utils.SmoothAnimationTimer;
import moe.ichinomiya.naven.utils.StencilUtils;
import moe.ichinomiya.naven.utils.font.GlyphPageFontRenderer;

import java.awt.*;

@Data
public class CooldownBar {
    private final static int mainColor = new Color(150, 45, 45, 255).getRGB();
    private final SmoothAnimationTimer animation = new SmoothAnimationTimer(0, 0.2f);
    private final SmoothAnimationTimer yAnimation = new SmoothAnimationTimer(0, 0.2f);
    private final SmoothAnimationTimer startAnimation = new SmoothAnimationTimer(0, 0.3f);

    private long time, createTime;
    private String title;

    public CooldownBar(long time, String title) {
        this.time = time;
        this.createTime = System.currentTimeMillis();
        this.title = title;
    }

    public float getState() {
        return (System.currentTimeMillis() - createTime) / (float) time;
    }

    public void render() {
        float state = getState();
        if (state > 0.99) {
            startAnimation.target = 0;
        } else {
            startAnimation.target = 60;
        }

        startAnimation.update(true);
        animation.target = state;
        animation.update(true);

        StencilUtils.write(false);
        float radius = startAnimation.value;
        if (radius > 5) {
            RenderUtils.circle(50, 9, radius, 0xFFFFFFFF);
        }
        StencilUtils.erase(true);
        GlyphPageFontRenderer font = Naven.getInstance().getFontManager().siyuan16;
        font.drawStringWithShadow(title, 50 - font.getStringWidth(title) / 2f, 0, 0xFFFFFFFF);
        RenderUtils.drawBoundRoundedRect(0, 13, 100, 5, 2, 0x80000000);
        RenderUtils.drawBoundRoundedRect(0, 13, (float) (100 * Math.max(0.05, (1 - animation.value))), 5, 2, mainColor);
        StencilUtils.dispose();
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - createTime > time && yAnimation.isAnimationDone(true) && startAnimation.isAnimationDone(true);
    }
}
