package com.work.project.Notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.work.project.MessageActivity;

public class MyFirebaseMessaging extends FirebaseMessagingService {
    private static int notificationID = 1;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Data data = new Data(remoteMessage.getData());

        // если мы в чате с пользователем - не посылаем уведомление
        if(data.getNotificationType() == Data.NEW_MESSAGE && data.getUserId().equals(MessageActivity.globalUserChatId)){
            return;
        }

        int id = notificationID++;

        Resources res = getApplicationContext().getResources();
        String title = data.getTitle(res);
        String body = data.getBody(res);
        Intent intent = data.getIntent(this);
        int icon = data.getIcon();

        sendNotification(id, title, body, intent, icon);
    }

    private void sendNotification(int id, String title, String body, Intent intent, int icon) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            sendOreoNotification(id, title, body, intent, icon);
        } else {
            sendOldNotification(id, title, body, intent, icon);
        }
    }

    private void sendOreoNotification(int id, String title, String body, Intent intent, int icon){
        PendingIntent pendingIntent;
        if(intent != null)
            pendingIntent = PendingIntent.getActivity(this, id, intent, PendingIntent.FLAG_ONE_SHOT);
        else
            pendingIntent = null;

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        OreoNotification oreoNotification = new OreoNotification(this);
        Notification.Builder builder = oreoNotification.getOreoNotification(title, body, pendingIntent,
                defaultSound, icon);

        oreoNotification.getManager().notify(id, builder.build());

    }

    private void sendOldNotification(int id, String title, String body, Intent intent, int icon) {
        PendingIntent pendingIntent;
        if(intent != null)
            pendingIntent = PendingIntent.getActivity(this, id, intent, PendingIntent.FLAG_ONE_SHOT);
        else
            pendingIntent = null;

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(icon)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(defaultSound)
                .setContentIntent(pendingIntent);
        NotificationManager noti = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        noti.notify(id, builder.build());
    }
}
