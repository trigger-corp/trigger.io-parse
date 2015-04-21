package com.parse;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import com.google.gson.JsonObject;
import io.trigger.forge.android.core.ForgeActivity;

import android.content.Context;
import android.content.Intent;

import io.trigger.forge.android.core.ForgeApp;
import io.trigger.forge.android.core.ForgeLog;
import io.trigger.forge.android.modules.parse.Constant;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

public class ForgePushBroadcastReceiver extends ParsePushBroadcastReceiver {
    private static final String PARSE_PREFS = "forge.parse.com";
    private static final String MESSAGE_COUNTER_KEY = "messages.counter";
    private static final String LOGGER_TAG = "ForgePushBroadcastReceiver";
    private static final String UPDATE_NOTIFICATIONS_FEATURE = "updateNotifications";

    protected static JsonObject getForgeConfig() {
        return ForgeApp.configForModule(Constant.MODULE_NAME);
    }

    @Override
    public void onPushOpen(Context context, Intent intent) {
        ParseAnalytics.trackAppOpened(intent);

        Intent activity = new Intent(context, ForgeActivity.class);
        activity.putExtras(intent.getExtras());
        activity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(activity);

        boolean updateNotificationsFeature = getForgeConfig().has(UPDATE_NOTIFICATIONS_FEATURE) && getForgeConfig().get(UPDATE_NOTIFICATIONS_FEATURE).getAsBoolean();
        if (updateNotificationsFeature) {
            Log.i(LOGGER_TAG, "Resetting Message Number");
            setMessageNumber(context, 0);
        }
    }

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        boolean updateNotificationsFeature = getForgeConfig().has(UPDATE_NOTIFICATIONS_FEATURE) && getForgeConfig().get(UPDATE_NOTIFICATIONS_FEATURE).getAsBoolean();

        if (!updateNotificationsFeature) {
            super.onPushReceive(context, intent);
            return;
        }

        JSONObject pushData = null;

        try {
            pushData = new JSONObject(intent.getStringExtra("com.parse.Data"));
        } catch (JSONException ex) {
            ex.printStackTrace();
            ForgeLog.e("com.parse.ParsePushReceiver: Unexpected JSONException when receiving push data: " + ex.getMessage());
        }

        String action = null;
        if(pushData != null) {
            action = pushData.optString("action", (String)null);
        }

        if(action != null) {
            Bundle extras = intent.getExtras();
            Intent broadcastIntent = new Intent();
            broadcastIntent.putExtras(extras);
            broadcastIntent.setAction(action);
            broadcastIntent.setPackage(context.getPackageName());
            context.sendBroadcast(broadcastIntent);
        }

        Notification notification = this.getNotification(context, intent);

        if(notification != null) {
            this.showUpdatableNotification(context, notification);
        }
    }

    @Override
    protected Notification getNotification(Context context, Intent intent) {
        JSONObject pushData = this.getPushData(intent);
        if(pushData != null && (pushData.has("alert") || pushData.has("title"))) {
            String alert = pushData.optString("alert", "Notification received.");
            String title = pushData.optString("title", ManifestInfo.getDisplayName());
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

            ForgeNotificationCompat.Builder parseBuilder = new ForgeNotificationCompat.Builder(context);
            parseBuilder.setContentTitle(title).setContentText(alert).setSmallIcon(this.getSmallIconId(context, intent)).setLargeIcon(this.getLargeIcon(context, intent)).setContentIntent(pContentIntent).setDeleteIntent(pDeleteIntent).setAutoCancel(true).setDefaults(-1);
            setMessageCount(context, parseBuilder);

            if(alert != null && alert.length() > 38) {
                parseBuilder.setStyle((new NotificationCompat.Builder.BigTextStyle()).bigText(alert));
            }

            return parseBuilder.build();
        } else {
            return null;
        }
    }

    protected SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PARSE_PREFS, Context.MODE_PRIVATE);
    }

    protected void setMessageNumber(Context context, int messageNumber) {
        Log.i(LOGGER_TAG, "Message Number: " + messageNumber);
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt(MESSAGE_COUNTER_KEY, messageNumber);
        editor.commit();
    }

    public void showUpdatableNotification(Context context, Notification notification) {
        if(context != null && notification != null) {
            if(context != null && notification != null) {
                NotificationManager nm = (NotificationManager)context.getSystemService("notification");
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
        } catch (JSONException var3) {
            Parse.logE("com.parse.ParsePushReceiver", "Unexpected JSONException when receiving push data: ", var3);
            return null;
        }
    }

    private ForgeNotificationCompat.Builder setMessageCount(Context context, ForgeNotificationCompat.Builder parseBuilder) {
        int messageNumber = 1 + getSharedPreferences(context).getInt(MESSAGE_COUNTER_KEY, 0);
        setMessageNumber(context, messageNumber);
        if (messageNumber > 1) {
            Log.i(LOGGER_TAG, "Setting Message Number");
            parseBuilder.setNumber(messageNumber);
        }
        return parseBuilder;
    }
}
