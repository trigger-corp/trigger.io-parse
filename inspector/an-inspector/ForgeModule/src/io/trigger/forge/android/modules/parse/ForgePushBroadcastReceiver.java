package io.trigger.forge.android.modules.parse;

import io.trigger.forge.android.core.ForgeActivity;

import android.content.Context;
import android.content.Intent;

import com.parse.ParsePushBroadcastReceiver;

public class ForgePushBroadcastReceiver extends ParsePushBroadcastReceiver {
	@Override
    public void onPushOpen(Context context, Intent intent) {
        Intent activity = new Intent(context, ForgeActivity.class);
        activity.putExtras(intent.getExtras());
        activity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(activity);
    }
}
