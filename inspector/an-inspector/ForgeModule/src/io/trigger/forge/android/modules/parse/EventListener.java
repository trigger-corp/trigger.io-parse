package io.trigger.forge.android.modules.parse;

import com.google.gson.JsonElement;
import com.parse.*;
import io.trigger.forge.android.core.ForgeApp;
import io.trigger.forge.android.core.ForgeEventListener;
import io.trigger.forge.android.core.ForgeLog;
import android.content.Intent;

import com.google.gson.JsonParser;

public class EventListener extends ForgeEventListener {
	@Override
	public void onApplicationCreate() {

    final String server = ForgeApp.configForPlugin(Constant.MODULE_NAME).has("server")
        ? ForgeApp.configForPlugin(Constant.MODULE_NAME).get("server").getAsString()
        : "https://api.parse.com/1/";

    final Parse.Configuration configuration = new Parse.Configuration.Builder(ForgeApp.getApp())
        .server(server)
        .applicationId(ForgeApp.configForPlugin(Constant.MODULE_NAME).get("applicationId").getAsString())
        .clientKey(ForgeApp.configForPlugin(Constant.MODULE_NAME).get("clientKey").getAsString())
        .build();

    ForgeLog.d("com.parse.push initializing with server: " + server);

    Parse.initialize(configuration);

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
			ForgeApp.event("event.messagePushed", new JsonParser().parse((String) intent.getExtras()																							.get("com.parse.Data")));
		}
	}

	@Override
	public void onStart() {
		ForgeLog.d("com.parse.push onStart");
		VisibilityManager.resumed();
	}

	@Override
	public void onStop() {
		ForgeLog.d("com.parse.push onStop");
		VisibilityManager.paused();
	}
}
