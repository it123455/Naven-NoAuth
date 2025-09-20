package moe.ichinomiya.naven.modules.impl.move;

import io.netty.buffer.Unpooled;
import moe.ichinomiya.naven.Naven;
import moe.ichinomiya.naven.events.api.EventTarget;
import moe.ichinomiya.naven.events.api.types.EventType;
import moe.ichinomiya.naven.events.impl.EventMotion;
import moe.ichinomiya.naven.events.impl.EventPacket;
import moe.ichinomiya.naven.events.impl.EventSlowdown;
import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleInfo;
import moe.ichinomiya.naven.modules.impl.misc.Disabler;
import moe.ichinomiya.naven.protocols.HYTUtils;
import moe.ichinomiya.naven.values.ValueBuilder;
import moe.ichinomiya.naven.values.impl.BooleanValue;
import net.minecraft.item.*;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.network.play.server.S30PacketWindowItems;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

@ModuleInfo(name = "NoSlow", description = "Prevents you from slowing down when eating", category = Category.MOVEMENT)
public class NoSlow extends Module {
    BooleanValue sword = ValueBuilder.create(this, "Sword").setDefaultBooleanValue(true).build().getBooleanValue();
    BooleanValue food = ValueBuilder.create(this, "Food").setDefaultBooleanValue(true).build().getBooleanValue();
    BooleanValue bow = ValueBuilder.create(this, "Bow").setDefaultBooleanValue(false).build().getBooleanValue();
    BooleanValue foodUsingStateFix = ValueBuilder.create(this, "Fix Food State").setDefaultBooleanValue(true).build().getBooleanValue();
    boolean serverSetSlot = false;

    @EventTarget
    public void onSlowdown(EventSlowdown e) {
        if (e.isSlowdown()) {
            ItemStack heldItem = mc.thePlayer.getHeldItem();
            if (heldItem != null && heldItem.getItem() instanceof ItemFood && food.getCurrentValue() && heldItem.stackSize >= 2 && !HYTUtils.isEnchantedGoldenApple(heldItem) && serverSetSlot) {
                e.setSlowdown(false);
            } else if (heldItem != null && heldItem.getItem() instanceof ItemSword && sword.getCurrentValue()) {
                e.setSlowdown(false);
            } else if (heldItem != null && heldItem.getItem() instanceof ItemBow && bow.getCurrentValue() && !mc.thePlayer.isSneaking()) {
                e.setSlowdown(false);
            }
        }
    }

    @EventTarget
    public void onPre(EventMotion e) {
        ItemStack heldItem = mc.thePlayer.getHeldItem();

        if (e.getType() == EventType.PRE && mc.thePlayer.isUsingItem() && heldItem != null && heldItem.getItem() instanceof ItemBow && bow.getCurrentValue() && !mc.thePlayer.isSneaking()) {
            mc.getNetHandler().getNetworkManager().sendPacket(new C09PacketHeldItemChange((mc.thePlayer.inventory.currentItem + 1) % 9));
            mc.getNetHandler().getNetworkManager().sendPacket(new C17PacketCustomPayload("MC|Brand", (new PacketBuffer(Unpooled.buffer())).writeString("fml,forge")));
            mc.getNetHandler().getNetworkManager().sendPacket(new C17PacketCustomPayload("MC|Brand", (new PacketBuffer(Unpooled.buffer())).writeString("fml,forge")));
        }

        if (sword.getCurrentValue()) {
            if (Naven.getInstance().getModuleManager().getModule(Disabler.class).isEnabled()) {
                if (e.getType() == EventType.PRE) {
                    if (mc.thePlayer.isUsingItem() && heldItem != null && heldItem.getItem() instanceof ItemSword) {
                        mc.getNetHandler().getNetworkManager().sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                    }
                } else {
                    if (mc.thePlayer.isUsingItem() && heldItem != null && heldItem.getItem() instanceof ItemSword) {
                        if (Disabler.disabled) {
                            mc.getNetHandler().getNetworkManager().sendPacket(new CPacketPlayerTryUseItem(1));
                        }
                    }
                }
            } else {
                if (e.getType() == EventType.PRE && mc.thePlayer.isUsingItem() && heldItem != null && heldItem.getItem() instanceof ItemSword) {
                    mc.getNetHandler().getNetworkManager().sendPacket(new CPacketPlayerTryUseItem(1));
                }
            }
        }

        if (e.getType() == EventType.PRE && foodUsingStateFix.getCurrentValue()) {
            if (mc.thePlayer.isUsingItem() && !mc.thePlayer.serverUsingItem && mc.thePlayer.getItemInUseCount() < 30) {
                mc.thePlayer.stopUsingItem();
            }
        }
    }

    @EventTarget
    public void onPacket(EventPacket e) {
        Packet<?> packet = e.getPacket();

        if (mc.thePlayer == null) {
            return;
        }

        ItemStack heldItem = mc.thePlayer.getHeldItem();
        if (heldItem != null && heldItem.getItem() instanceof ItemFood && food.getCurrentValue()) {
            if (packet instanceof C08PacketPlayerBlockPlacement) {
                C08PacketPlayerBlockPlacement currentPacket = (C08PacketPlayerBlockPlacement) packet;
                if (currentPacket.getPlacedBlockDirection() == 255 && currentPacket.getPosition().equals(C08PacketPlayerBlockPlacement.field_179726_a) && heldItem.stackSize >= 2 && !HYTUtils.isEnchantedGoldenApple(heldItem)) {
                    serverSetSlot = false;
                    if (!Naven.getInstance().getModuleManager().getModule(Stuck.class).isEnabled()) {
                        mc.getNetHandler().getNetworkManager().sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.DROP_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                    }
                }
            }

            if (packet instanceof S30PacketWindowItems && mc.thePlayer.isUsingItem()) {
                e.setCancelled(true);
            }

            if (packet instanceof S2FPacketSetSlot) {
                serverSetSlot = true;
                if (mc.thePlayer.isUsingItem()) {
                    e.setCancelled(true);
                }
            }
        }
    }
}

