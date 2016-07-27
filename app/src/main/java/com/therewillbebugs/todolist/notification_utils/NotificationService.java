package com.therewillbebugs.todolist.notification_utils;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.therewillbebugs.todolist.R;
import com.therewillbebugs.todolist.task_components.Task;

import java.util.Calendar;

public class NotificationService {
    private NotificationManager manager;
    private NotificationCompat.Builder builder;
    private AlarmManager alarmManager;
    private Context ctx;

    private final StringBuilder notificationIdKey = new StringBuilder("notificationId");
    private final StringBuilder notificationKey   = new StringBuilder("notification");
    private int notificationIdIndex;
    private int notificationIndex;

    public NotificationService(Context ctx) {
        this.ctx     = ctx;
        builder      = new NotificationCompat.Builder(ctx);
        manager      = (NotificationManager)ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        alarmManager = (AlarmManager)ctx.getSystemService(Context.ALARM_SERVICE);
    }

    public void createNotification(Task t) {
        // set required fields for notification
        // TODO: add proper icon, get team's opinion on notification contents
        Notification n = builder.setSmallIcon(R.drawable.notification_template_icon_bg)
            .setContentTitle(t.getTitle())
            .setContentText(t.getDateTimeString())
            .setPriority(Notification.PRIORITY_HIGH)
            .setVibrate(new long[0])
            .setAutoCancel(true)
            .build();

        // schedule notification for future if task has specified time
        long currentTime = Calendar.getInstance().getTimeInMillis();
        long taskTime    = t.getScheduledTimeInMillis();
        t.setNotificationDelayInMillis(taskTime - currentTime);

        if (taskTime > currentTime) {
            scheduleNotification(n, t.getNotificationDelayInMillis());
        } else {
            manager.notify(0, n);
        }
    }

    public void deleteNotification(Task t) {
        long notificationTime = t.getNotificationDelayInMillis();

        NotificationPublisher.removeNotificationTime(notificationTime);
    }

    private void scheduleNotification(Notification n, long notifyDelayInMillis) {
        incrementNotificationIntentKeys();

        // add intent keys to NotificationPublisher map
        NotificationPublisher.addNotificationKeys(
            notifyDelayInMillis,
            notificationKey.toString(), notificationIdKey.toString()
        );

        // add notification delay to NotificationPublisher list -
        // ensures that notifications scheduled 'out of order' are
        // displayed in chronological order
        NotificationPublisher.addNotificationTime(notifyDelayInMillis);

        // create Intent for NotificationPublisher
        Intent notificationIntent = new Intent(ctx, NotificationPublisher.class);
        notificationIntent.putExtra(notificationIdKey.toString(), notificationIdIndex);
        notificationIntent.putExtra(notificationKey.toString(), n);

        PendingIntent pendingIntent
            = PendingIntent.getBroadcast(
                ctx,
                notificationIdIndex,
                notificationIntent,
                0
            );

        // schedule task
        alarmManager.set(AlarmManager.RTC_WAKEUP, notifyDelayInMillis, pendingIntent);
    }

    private void incrementNotificationIntentKeys() {
        // ensure each key is unique for each pending intent
        // so notifications aren't overwriting eachother
        notificationIdKey.append(++notificationIdIndex);
        notificationKey.append(++notificationIndex);
    }
}
