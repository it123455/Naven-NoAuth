package moe.ichinomiya.naven.modules.impl.misc;

import moe.ichinomiya.naven.Naven;
import moe.ichinomiya.naven.modules.Category;
import moe.ichinomiya.naven.modules.Module;
import moe.ichinomiya.naven.modules.ModuleInfo;
import moe.ichinomiya.naven.ui.notification.Notification;
import moe.ichinomiya.naven.ui.notification.NotificationLevel;
import moe.ichinomiya.naven.utils.TimeHelper;

@ModuleInfo(name = "ClientFriend", description = "Treat other users as friend!", category = Category.MISC)
public class ClientFriend extends Module {
    public static TimeHelper attackTimer = new TimeHelper();

    @Override
    public void onDisable() {
        attackTimer.reset();
        Notification notification = new Notification(NotificationLevel.INFO, "You can attack other players after 15 seconds.", 15000);
        Naven.getInstance().getNotificationManager().addNotification(notification);
    }
}
