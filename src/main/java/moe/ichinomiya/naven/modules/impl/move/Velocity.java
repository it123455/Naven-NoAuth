package moe.ichinomiya.naven.modules.impl.move;

import de.florianmichael.viamcp.fixes.AttackOrder;
import moe.ichinomiya.naven.Naven;
import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.api.types.EventType;
import moe.ichinomiya.naven.events.impl.EventPacket;
import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleInfo;
import moe.ichinomiya.naven.modules.impl.combat.Aura;
import moe.ichinomiya.naven.modules.impl.misc.Disabler;
import moe.ichinomiya.naven.ui.cooldown.CooldownBar;
import moe.ichinomiya.naven.utils.*;
import moe.ichinomiya.naven.values.ValueBuilder;
import moe.ichinomiya.naven.values.impl.BooleanValue;
import moe.ichinomiya.naven.values.impl.FloatValue;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S12PacketEntityVelocity;

import java.util.Optional;

import static moe.ichinomiya.naven.events.api.types.Priority.*;

@ModuleInfo(name = "Velocity", description = "Reduces knockback.", category = Category.MOVEMENT)
public class Velocity extends Module {
    private final TimeHelper disableHelper = new TimeHelper(), velocityTimer = new TimeHelper();
    public final FloatValue skippingTicks = ValueBuilder.create(this, "Skip Ticks").setDefaultFloatValue(1).setFloatStep(1).setMinFloatValue(1f).setMaxFloatValue(3f).build().getFloatValue();
    public final BooleanValue debugMessage = ValueBuilder.create(this, "Verbose Output").setDefaultBooleanValue(false).build().getBooleanValue();
    public final BooleanValue onlySprint = ValueBuilder.create(this, "Sprint Only").setDefaultBooleanValue(false).build().getBooleanValue();
    public static boolean toggle = false;
    public static int direction = 1;
    CooldownBar bar;

    private Optional<Entity> findEntity() {
        return mc.theWorld.loadedEntityList.stream()
                .filter(livingBase -> mc.thePlayer.getEntityId() != livingBase.getEntityId() && livingBase instanceof EntityLivingBase)
                .filter(livingBase -> !livingBase.isDead && ((EntityLivingBase) livingBase).getHealth() > 0 && !((EntityLivingBase) livingBase).isPlayerSleeping())
                .filter(livingBase -> !(livingBase instanceof EntityOtherPlayerMP) || !((EntityOtherPlayerMP) livingBase).isFakePlayer())
                .filter(livingBase -> !(livingBase instanceof EntityOtherPlayerMP) || ((EntityOtherPlayerMP) livingBase).getPlayerDeadTimer().delay(1000))
                .filter(livingBase -> RotationUtils.getMinDistance(livingBase, RotationManager.lastRotations) < 3)
                .findAny();
    }

    public static S12PacketEntityVelocity velocityPacket;

    public static void sendLookPacket() {
        mc.skipTicks += 1;
        direction *= -1;
        float playerYaw = RotationManager.rotations.x + 0.0001f * direction;
        mc.getNetHandler().getNetworkManager().sendPacket(new C03PacketPlayer.C05PacketPlayerLook(playerYaw, RotationManager.rotations.y, mc.thePlayer.onGround));
    }

    @EventTarget(HIGHEST)
    public void onPacket(EventPacket e) {
        if (mc.thePlayer != null && e.getType() == EventType.RECEIVE && !e.isCancelled()) {
            if (e.getPacket() instanceof S12PacketEntityVelocity) {
                S12PacketEntityVelocity packet = (S12PacketEntityVelocity) e.getPacket();
                if (packet.getEntityID() == mc.thePlayer.getEntityId()) {
                    double x = packet.getMotionX() / 8000D;
                    double z = packet.getMotionZ() / 8000D;
                    double speed = Math.sqrt(x * x + z * z);

                    if (mc.thePlayer.isInWeb || mc.thePlayer.isInWater() || mc.thePlayer.isInLava() || mc.thePlayer.isOnLadder()) {
                        if (debugMessage.getCurrentValue()) {
                            ChatUtils.addChatMessage("Ignore: Player is in Web\\Water\\Lava\\Ladder!");
                        }
                    } else if (!disableHelper.delay(1000)) {
                        if (debugMessage.getCurrentValue()) {
                            ChatUtils.addChatMessage("Ignore: Player just flagged!");
                        }
                    } else if (speed < 0.1) {
                        if (debugMessage.getCurrentValue()) {
                            ChatUtils.addChatMessage("Ignore: Speed is too low!");
                        }
                    } else if (onlySprint.getCurrentValue() && !mc.thePlayer.serverSprintState) {
                        if (debugMessage.getCurrentValue()) {
                            ChatUtils.addChatMessage("Ignore: You are not sprinting!");
                        }
                    } else if (Disabler.disabled) {
                        Aura aura = (Aura) Naven.getInstance().getModuleManager().getModule(Aura.class);

                        if (aura.isEnabled() && Aura.target != null) {
                            velocityPacket = packet;
                            e.setCancelled(true);
                            return;
                        }

                        Optional<Entity> any = findEntity();

                        if (any.isPresent()) {
                            velocityTimer.reset();
                            Entity entity = any.get();
                            e.setCancelled(true);

                            boolean needSprint = !mc.thePlayer.serverSprintState;

                            if (needSprint) {
                                packet.setToggleSprint(true);
                                mc.getNetHandler().getNetworkManager().sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
                                for (int i = 0; i < 2; i++) {
                                    sendLookPacket();
                                }
                            }

                            for (int i = 0; i < 8; i++) {
                                AttackOrder.sendFixedAttack(mc.thePlayer, entity);
                            }

                            x *= Math.pow(0.6, 5);
                            z *= Math.pow(0.6, 5);

                            if (needSprint) {
                                toggle = true;
                                mc.getNetHandler().getNetworkManager().sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
                            }

                            packet.setMotionX((int) (x * 8000));
                            packet.setMotionZ((int) (z * 8000));
                            packet.setModified(true);

                            velocityPacket = packet;
                        }
                    }
                }
            }

            if (e.getPacket() instanceof S08PacketPlayerPosLook && !velocityTimer.delay(500)) {
                if (bar == null || bar.isExpired()) {
                    bar = new CooldownBar(1000, "Velocity Temporarily Disabled");
                    Naven.getInstance().getCooldownBarManager().addBar(bar);
                } else {
                    bar.setCreateTime(System.currentTimeMillis());
                }
                disableHelper.reset();
            }
        }
    }
}
