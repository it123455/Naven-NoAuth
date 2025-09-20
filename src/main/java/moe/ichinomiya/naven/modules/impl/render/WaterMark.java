package moe.ichinomiya.naven.modules.impl.render;

import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.impl.EventRender2D;
import moe.ichinomiya.naven.events.impl.EventShader;
import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleInfo;
import moe.ichinomiya.naven.ui.watermark.NavenWatermark;
import moe.ichinomiya.naven.ui.watermark.SilenceFixWaterMark;
import moe.ichinomiya.naven.values.ValueBuilder;
import moe.ichinomiya.naven.values.impl.ModeValue;

@ModuleInfo(name = "WaterMark", description = "The WaterMark", category = Category.RENDER)
public class WaterMark extends Module {
    private moe.ichinomiya.naven.ui.watermark.WaterMark waterMark;

    public ModeValue mode = ValueBuilder.create(this, "Mode").setDefaultModeIndex(0).setModes("Naven", "SilenceFix").setOnUpdate(value -> {
        if (value.getModeValue().isCurrentMode("Naven")) {
            waterMark = new NavenWatermark();
        } else {
            waterMark = new SilenceFixWaterMark();
        }
    }).build().getModeValue();

    @EventTarget
    public void onRender(EventRender2D e) {
        if (waterMark != null) {
            waterMark.render();
        }
    }

    @EventTarget
    public void onRender(EventShader e) {
        if (waterMark != null) {
            waterMark.renderShader();
        }
    }
}
