package io.trigger.forge.android.modules.parse;

import io.trigger.forge.android.core.ForgeApp;
import io.trigger.forge.android.core.ForgeEventListener;
import io.trigger.forge.android.core.ForgeLog;
import android.content.Intent;

import com.google.gson.JsonParser;
import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.SaveCallback;

public class EventListener extends ForgeEventListener {
	@Override
	public void onApplicationCreate() {
		Parse.initialize(ForgeApp.getApp(),
				ForgeApp.configForPlugin("parse").get("applicationId").getAsString(),
				ForgeApp.configForPlugin("parse").get("clientKey").getAsString());
		ParseInstallation.getCurrentInstallation().saveInBackground();		
		ParsePush.subscribeInBackground("", new SaveCallback() {
			@Override
			public void done(com.parse.ParseException e) {
				if (e == null) {
					ForgeLog.d("com.parse.push successfully subscribed to the broadcast channel.");
				} else {
					ForgeLog.e("com.parse.push failed to subscribe for push: " + e.getMessage());
				}
			}
		});
		ForgeLog.i("Initializing Parse and subscribing to default channel.");
	}

	@Override
	public void onNewIntent(Intent intent) {		
		if (intent.getExtras() != null && intent.getExtras().get("com.parse.Data") != null) {
			ForgeApp.event("event.messagePushed", new JsonParser().parse((String) intent.getExtras().get("com.parse.Data")));
		}
	}
}
