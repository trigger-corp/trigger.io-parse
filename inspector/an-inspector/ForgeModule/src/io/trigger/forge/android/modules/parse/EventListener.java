package io.trigger.forge.android.modules.parse;

import io.trigger.forge.android.core.ForgeActivity;
import io.trigger.forge.android.core.ForgeApp;
import io.trigger.forge.android.core.ForgeEventListener;
import io.trigger.forge.android.core.ForgeLog;
import android.content.Intent;

import com.google.gson.JsonParser;
import com.parse.Parse;
import com.parse.PushService;

public class EventListener extends ForgeEventListener {
	@Override
	public void onApplicationCreate() {
		Parse.initialize(ForgeApp.getApp(),
				ForgeApp.configForPlugin("parse").get("applicationId").getAsString(),
				ForgeApp.configForPlugin("parse").get("clientKey").getAsString());
		PushService.subscribe(ForgeApp.getApp(), "", ForgeActivity.class);
        PushService.setDefaultPushCallback(ForgeApp.getApp(), ForgeActivity.class);
		ForgeLog.i("Initializing Parse and subscribing to default channel.");
	}

	@Override
	public void onNewIntent(Intent intent) {
		if (intent.getExtras() != null && intent.getExtras().get("com.parse.Data") != null) {
			ForgeApp.event("event.messagePushed", new JsonParser().parse((String) intent.getExtras().get("com.parse.Data")));
		}
	}
}
