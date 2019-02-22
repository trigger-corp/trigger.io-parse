package io.trigger.parse;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import com.google.gson.JsonObject;
import com.parse.ManifestInfo;
import com.parse.ParseAnalytics;
import com.parse.ParsePushBroadcastReceiver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import org.json.JSONException;
import org.json.JSONObject;

import io.trigger.forge.android.core.ForgeActivity;
import io.trigger.forge.android.core.ForgeApp;
import io.trigger.forge.android.core.ForgeLog;
import io.trigger.forge.android.modules.parse.Constant;



public class ForgePushBroadcastReceiver extends ParsePushBroadcastReceiver {
    private static final String channelId = "default";
    private static final String channelDescription = "Default";

    private static ArrayList<HashMap<String, String>> history = new ArrayList<HashMap<String, String>>();


    @Override
    public void onPushOpen(Context context, Intent intent) {
        ForgeLog.d("io.trigger.parse.ForgePushBroadcastReceiver onPushOpen");
        ParseAnalytics.trackAppOpenedInBackground(intent);

        Intent activity = new Intent(context, ForgeActivity.class);
        activity.putExtras(intent.getExtras());
        activity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(activity);

        if (hasUpdateNotificationsFeature()) {
            history.clear();
        }
    }


    @Override
    protected void onPushDismiss(Context context, Intent intent) {
        ForgeLog.d("io.trigger.parse.ForgePushBroadcastReceiver onPushDismiss");
        super.onPushDismiss(context, intent);

        if (hasUpdateNotificationsFeature()) {
            history.clear();
        }
    }


    @Override
    protected NotificationCompat.Builder getNotification(Context context, Intent intent) {
        ForgeLog.d("io.trigger.parse.ForgePushBroadcastReceiver getNotification");
        if (VisibilityManager.isVisible() && !hasShowNotificationsWhileVisibleFeature()) {
            return null;
        }

        if (hasUpdateNotificationsFeature()) {
            if (updateNotification(context, intent)) {
                ForgeLog.d("io.trigger.parse.ForgePushBroadcastReceiver createUpdatableNotification success");
                return null; // successfully built and sent it
            } else {
                ForgeLog.e("io.trigger.parse.ForgePushBroadcastReceiver error failed to create updatable notification. Falling back.");
            }
        }

        NotificationCompat.Builder builder = super.getNotification(context, intent);
        if (hasBackgroundColourFeature()) {
            builder = setBackgroundColor(builder);
        }

        return builder;
    }


    protected JSONObject getPushData(Intent intent) {
        ForgeLog.d("io.trigger.parse.ForgePushBroadcastReceiver getPushData");
        try {
            return new JSONObject(intent.getStringExtra("com.parse.Data"));
        } catch (JSONException e) {
            ForgeLog.e("io.trigger.parse.ForgePushBroadcastReceiver: Unexpected JSONException when receiving push data: " + e.getLocalizedMessage());
            return null;
        }
    }


    protected boolean updateNotification(Context context, Intent intent) {
        JSONObject pushData = this.getPushData(intent);
        if (pushData == null) {
            ForgeLog.e("io.trigger.parse.ForgePushBroadcastReceiver createUpdatableNotification received null pushData");
            return false;
        }

        if (!(pushData.has("alert") || pushData.has("title"))) {
            ForgeLog.e("io.trigger.parse.ForgePushBroadcastReceiver createUpdatableNotification requires either an alert or title");
            return false;
        }

        HashMap<String, String> message = new HashMap<String, String>();
        message.put("alert", pushData.optString("alert", "Notification received."));
        message.put("title", pushData.optString("title", ManifestInfo.getDisplayName(context)));
        history.add(message);

        String packageName = context.getPackageName();
        Bundle extras = intent.getExtras();
        Random random = new Random();

        Intent contentIntent = new Intent("com.parse.push.intent.OPEN");
        contentIntent.setPackage(packageName);
        contentIntent.putExtras(extras);
        int contentIntentRequestCode = random.nextInt();
        PendingIntent pendingContentIntent = PendingIntent.getBroadcast(context, contentIntentRequestCode, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent deleteIntent = new Intent("com.parse.push.intent.DELETE");
        deleteIntent.setPackage(packageName);
        deleteIntent.putExtras(extras);
        int deleteIntentRequestCode = random.nextInt();
        PendingIntent pendingDeleteIntent = PendingIntent.getBroadcast(context, deleteIntentRequestCode, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId);
        builder.setContentTitle(message.get("title"))
                .setContentText(message.get("alert"))
                .setSmallIcon(this.getSmallIconId(context, intent))
                .setLargeIcon(this.getLargeIcon(context, intent))
                .setContentIntent(pendingContentIntent)
                .setDeleteIntent(pendingDeleteIntent)
                .setAutoCancel(true)
                .setChannelId(channelId) // TODO
                .setDefaults(-1);

        if (history.size() > 1) {
            String text = history.size() + " messages received";

            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
            for (int i = 0; i < history.size(); i++) {
                inboxStyle.addLine(history.get(i).get("alert"));
            }
            inboxStyle.setSummaryText(text);

            builder.setStyle(inboxStyle);
            builder.setContentText(text);
            builder.setNumber(history.size());

        } else {
            builder.setStyle(new NotificationCompat.BigTextStyle().bigText(message.get("alert")));
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    channelDescription,
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        if (hasBackgroundColourFeature()) {
            builder = setBackgroundColor(builder);
        }

        Notification notification = builder.build();
        if (notification == null) {
            return false;
        }

        try {
            notificationManager.notify(1, notification);
        } catch (SecurityException e) {
            builder.setDefaults(5);
            notificationManager.notify(1, builder.build());
            return false;
        }

        return true;
    }


    private boolean hasUpdateNotificationsFeature() {
        JsonObject config = ForgeApp.configForModule(Constant.MODULE_NAME);
        return config.has("android") &&
                config.getAsJsonObject("android").has("updateNotifications") &&
                config.getAsJsonObject("android").get("updateNotifications").getAsBoolean();
    }


    private boolean hasShowNotificationsWhileVisibleFeature() {
        JsonObject config = ForgeApp.configForModule(Constant.MODULE_NAME);
        return config.has("android") &&
                config.getAsJsonObject("android").has("showNotificationsWhileVisible") &&
                config.getAsJsonObject("android").get("showNotificationsWhileVisible").getAsBoolean();
    }


    private boolean hasBackgroundColourFeature() {
        JsonObject config = ForgeApp.configForModule(Constant.MODULE_NAME);
        return config.has("android") &&
                config.getAsJsonObject("android").has("backgroundColour");
    }


    private NotificationCompat.Builder setBackgroundColor(NotificationCompat.Builder builder) {
        JsonObject config = ForgeApp.configForModule(Constant.MODULE_NAME);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                String colourString = config.getAsJsonObject("android").get("backgroundColour").getAsString();
                builder.setColor(Color.parseColor(colourString));
            } catch (IllegalArgumentException e) {
                ForgeLog.e("Invalid color string for parse.android.backgroundColour: " + e.getMessage());
            }
        }
        return builder;
    }
}
