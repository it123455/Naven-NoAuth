package moe.ichinomiya.naven.events.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import moe.ichinomiya.naven.events.api.events.Event;
import moe.ichinomiya.naven.events.api.events.callables.EventCancellable;
import moe.ichinomiya.naven.utils.MoveUtils;
import net.minecraft.client.Minecraft;

@AllArgsConstructor
@Getter
@Setter
public class EventStrafe implements Event {
    private float forward;
    private float strafe;
    private float friction;
    private float yaw;
}
