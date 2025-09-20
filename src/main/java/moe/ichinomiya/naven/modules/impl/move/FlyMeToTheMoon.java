package moe.ichinomiya.naven.modules.impl.move;

import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.api.types.EventType;
import moe.ichinomiya.naven.events.impl.EventMotion;
import moe.ichinomiya.naven.events.impl.EventRespawn;
import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleInfo;

@ModuleInfo(name = "FlyMeToTheMoon", description = "Fly me to the moon", category = Category.MOVEMENT)
public class FlyMeToTheMoon extends Module {
    public int ticks = 0;

    @Override
    public void onEnable() {
        if (mc.thePlayer.onGround) {
            mc.thePlayer.jump();
        }
        ticks = 0;
        super.onEnable();
    }

    @EventTarget
    public void onRespawn(EventRespawn e) {
        toggle();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @EventTarget
    public void onMotion(EventMotion e) {
        if (e.getType() == EventType.PRE && ++ticks > 1) {
            e.setX(e.getX() + 1337);
            e.setZ(e.getZ() + 1337);
        }
    }
}
