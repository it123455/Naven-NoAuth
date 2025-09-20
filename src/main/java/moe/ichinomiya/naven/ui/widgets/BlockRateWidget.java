package moe.ichinomiya.naven.ui.widgets;

import moe.ichinomiya.naven.Naven;
import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.impl.EventMotion;
import moe.ichinomiya.naven.utils.SmoothAnimationTimer;
import moe.ichinomiya.naven.values.impl.BooleanValue;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.item.ItemSword;

public class BlockRateWidget extends DraggableWidget {
    BooleanValue value;

    SmoothAnimationTimer blockRate = new SmoothAnimationTimer(0);
    float textWidth = 0;

    public BlockRateWidget(BooleanValue value) {
        super("BlockRate");
        this.value = value;
    }

    @EventTarget
    public void onUpdate(EventMotion e) {
        blockRate.target = (float) (mc.thePlayer.getBlockRate() * 100f);
    }

    @Override
    public void renderBody() {
        blockRate.update(true);
        String text = "Block Rate: " + Math.round(blockRate.value) + "%";
        textWidth = Naven.getInstance().getFontManager().opensans14.getStringWidth(text);
        Naven.getInstance().getFontManager().opensans14.drawString(text, 3, 4, 0xffffffff);
    }

    @Override
    public float getWidth() {
        return textWidth + 8;
    }

    @Override
    public float getHeight() {
        return 15;
    }

    @Override
    public boolean shouldRender() {
        return value.getCurrentValue() && (mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword || mc.currentScreen instanceof GuiChat);
    }
}
