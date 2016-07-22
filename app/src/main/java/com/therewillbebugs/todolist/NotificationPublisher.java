/*
  Modeled after code found at https://gist.github.com/BrandonSmith/6679223
 */

package com.therewillbebugs.todolist;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class NotificationPublisher extends BroadcastReceiver {
    private static Map<Long, AbstractMap.SimpleEntry<String, String>> notifications = new HashMap<>();
    private static List<Long> notificationTimes = new LinkedList<>();

    public static void addNotificationKeys(Long time, String nKey, String nIdKey) {
        notifications.put(time, new AbstractMap.SimpleEntry<>(nKey, nIdKey));
    }

    public static void addNotificationTime(Long time) {
        notificationTimes.add(time);
        Collections.sort(notificationTimes);
    }

    public static void removeNotificationTime(Long time) {
        if (notificationTimes.contains(time)) {
            notificationTimes.remove(time);
            Collections.sort(notificationTimes);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        AbstractMap.SimpleEntry<String, String> firstNotification =
                notifications.get(notificationTimes.remove(0));

        // if no notification is found for the specified time, return
        if (firstNotification == null) {
            return;
        }

        String notificationKey   = firstNotification.getKey();
        String notificationIdKey = firstNotification.getValue();

        int id = intent.getIntExtra(notificationIdKey, 0);
        Notification n = intent.getParcelableExtra(notificationKey);

        // if our Intent was passed properly, notify user
        if (n != null) {
            manager.notify(id, n);
        }
    }
}
