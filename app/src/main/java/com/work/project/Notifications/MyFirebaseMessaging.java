package com.work.project.Notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.work.project.MessageActivity;
import com.work.project.R;

import java.util.Map;

public class MyFirebaseMessaging extends FirebaseMessagingService {
    private static int notificationID = 1;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(MessageActivity.TAG, "message received!");
        for(Map.Entry<String, String> kv : remoteMessage.getData().entrySet()){
            Log.d(MessageActivity.TAG, "k, v: " + kv.getKey() + " " + kv.getValue());
        }
        super.onMessageReceived(remoteMessage);

        Data data = new Data(remoteMessage.getData());
        int icon = R.mipmap.ic_launcher;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            sendOreoNotification(data.getTitle(), data.getBody(), data.getIntent(this), icon);
        } else {
            sendNotification(data.getTitle(), data.getBody(), data.getIntent(this), icon);
        }
    }

    private void sendOreoNotification(String title, String body, Intent intent, int icon){
        int id = notificationID++;

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

    private void sendNotification(String title, String body, Intent intent, int icon) {
        int id = notificationID++;

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
