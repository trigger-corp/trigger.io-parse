package com.parse;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.os.Build;
import android.os.Bundle;

import com.google.gson.JsonObject;

import io.trigger.forge.android.core.ForgeActivity;
import io.trigger.forge.android.core.ForgeLog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import io.trigger.forge.android.core.ForgeApp;
import io.trigger.forge.android.modules.parse.Constant;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class ForgePushBroadcastReceiver extends ParsePushBroadcastReceiver {
    private static final String UPDATE_NOTIFICATIONS_FEATURE = "updateNotifications";

    static ArrayList<HashMap<String, String>> history = new ArrayList<HashMap<String, String>>();

    private boolean isUpdateNotificationsFeature() {
        JsonObject config = ForgeApp.configForModule(Constant.MODULE_NAME);

        return config.has("android") &&
        	   config.getAsJsonObject("android").has(UPDATE_NOTIFICATIONS_FEATURE) &&
       		   config.getAsJsonObject("android").get(UPDATE_NOTIFICATIONS_FEATURE).getAsBoolean();
    }

    private Notification setBackgroundColor(Notification notification) {
    	JsonObject config = ForgeApp.configForModule(Constant.MODULE_NAME);
    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
    		config.has("android") &&
    		config.getAsJsonObject("android").has("background-color")) {
    		try {
    			notification.color = Color.parseColor(config.getAsJsonObject("android").get("background-color").getAsString());
    		} catch (IllegalArgumentException e) {
                ForgeLog.e("Invalid color string for parse.android.background-color: " + e.getMessage());
            }
    	}
		return notification;
    }

    @Override
    public void onPushOpen(Context context, Intent intent) {
        ParseAnalytics.trackAppOpenedInBackground(intent);

        Intent activity = new Intent(context, ForgeActivity.class);
        activity.putExtras(intent.getExtras());
        activity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(activity);

        if (isUpdateNotificationsFeature()) {
            history.clear();
        }
    }

    @Override
    protected void onPushDismiss(Context context, Intent intent) {
        super.onPushDismiss(context, intent);

        if (isUpdateNotificationsFeature()) {
            history.clear();
        }
    }

    @Override
    protected Notification getNotification(Context context, Intent intent) {
        if (!isUpdateNotificationsFeature()) {
            return setBackgroundColor(super.getNotification(context, intent));
        } else if (VisibilityManager.isVisible()) {
            return null;
        } else {
            buildAndShowUpdatableNotification(context, intent);
            return null;
        }
    }

    protected void buildAndShowUpdatableNotification(Context context, Intent intent) {
        JSONObject pushData = this.getPushData(intent);
        if(pushData != null && (pushData.has("alert") || pushData.has("title"))) {
            HashMap<String, String> message = new HashMap<String, String>();
            message.put("alert", pushData.optString("alert", "Notification received."));
            message.put("title", pushData.optString("title", ManifestInfo.getDisplayName(context)));
            history.add(message);

            Bundle extras = intent.getExtras();
            Random random = new Random();
            int contentIntentRequestCode = random.nextInt();
            int deleteIntentRequestCode = random.nextInt();
            String packageName = context.getPackageName();
            Intent contentIntent = new Intent("com.parse.push.intent.OPEN");
            contentIntent.putExtras(extras);
            contentIntent.setPackage(packageName);
            Intent deleteIntent = new Intent("com.parse.push.intent.DELETE");
            deleteIntent.putExtras(extras);
            deleteIntent.setPackage(packageName);
            PendingIntent pContentIntent = PendingIntent.getBroadcast(context, contentIntentRequestCode, contentIntent, 134217728);
            PendingIntent pDeleteIntent = PendingIntent.getBroadcast(context, deleteIntentRequestCode, deleteIntent, 134217728);

            ForgeNotificationCompat.Builder builder = new ForgeNotificationCompat.Builder(context);
            builder.setContentTitle(message.get("title"))
                   .setContentText(message.get("alert"))
                   .setSmallIcon(this.getSmallIconId(context, intent))
                   .setLargeIcon(this.getLargeIcon(context, intent))
                   .setContentIntent(pContentIntent)
                   .setDeleteIntent(pDeleteIntent)
                   .setAutoCancel(true)
                   .setDefaults(-1);

            if (history.size() > 1) {
                ForgeNotificationCompat.Builder.InboxStyle inboxStyle = new ForgeNotificationCompat.Builder.InboxStyle();
                inboxStyle.setSummaryText(history.size() + " messages received");
                builder.setContentText(history.size() + " messages received");

                // Add each form submission to the inbox list
                for (int i = 0; i < history.size(); i++) {
                    inboxStyle.addLine(history.get(i).get("alert"));
                }
                builder.setStyle(inboxStyle);
            } else {
            	builder.setStyle(new NotificationCompat.Builder.BigTextStyle().bigText(message.get("alert")));
            }

            showUpdatableNotification(context, setBackgroundColor(builder.build()));
        }
    }

    public void showUpdatableNotification(Context context, Notification notification) {
        if(context != null && notification != null) {
            if(context != null && notification != null) {
                NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
                int notificationId = 1;

                try {
                    nm.notify(notificationId, notification);
                } catch (SecurityException var6) {
                    notification.defaults = 5;
                    nm.notify(notificationId, notification);
                }
            }
        }
    }

    private JSONObject getPushData(Intent intent) {
        try {
            return new JSONObject(intent.getStringExtra("com.parse.Data"));
        } catch (JSONException e) {
            ForgeLog.e("com.parse.ParsePushReceiver: Unexpected JSONException when receiving push data: " + e.getLocalizedMessage());
            return null;
        }
    }
}
