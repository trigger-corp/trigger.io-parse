package io.trigger.forge.android.modules.parse;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.parse.*;
import io.trigger.forge.android.core.ForgeApp;
import io.trigger.forge.android.core.ForgeEventListener;
import io.trigger.forge.android.core.ForgeLog;
import android.content.Intent;

import com.google.gson.JsonParser;

public class EventListener extends ForgeEventListener {
	@Override
	public void onApplicationCreate() {

		final JsonObject config = ForgeApp.configForPlugin(Constant.MODULE_NAME);

    	String server = config.has("server")
        		? config.get("server").getAsString()
        		: "https://api.parse.com/1/";

		// interim workaround for: https://github.com/ParsePlatform/Parse-SDK-Android/pull/436
		if (!server.endsWith("/")) {
			server += "/";
		}

		final String applicationId = config.has("applicationId")
				? config.get("applicationId").getAsString()
				: "";

		final String clientKey = config.has("clientKey")
				? ForgeApp.configForPlugin(Constant.MODULE_NAME).get("clientKey").getAsString()
				: null;

		final String GCMSenderId = (config.has("android") && config.get("android").getAsJsonObject().has("GCMsenderID"))
				? config.get("android").getAsJsonObject().get("GCMsenderID").getAsString()
				: null;

	    final Parse.Configuration configuration = new Parse.Configuration.Builder(ForgeApp.getApp())
				.server(server)
        		.applicationId(applicationId)
        		.clientKey(clientKey)
        		.build();

    	ForgeLog.d("com.parse.push initializing with server: " + server);

    	Parse.initialize(configuration);
		if (GCMSenderId != null) {
			ParseInstallation.getCurrentInstallation().put("GCMSenderId", GCMSenderId);
		}

    	ParseInstallation.getCurrentInstallation().saveInBackground(new SaveCallback() {
			@Override
			public void done(ParseException e) {
				if (e == null) {
					ForgeLog.d("com.parse.push initialized successfully");
				} else {
					ForgeLog.e("com.parse.push failed to initialize: " + e.getLocalizedMessage());
					e.printStackTrace();
					return;
				}

				ParsePush.subscribeInBackground("", new SaveCallback() {
					@Override
					public void done(com.parse.ParseException e) {
						if (e == null) {
							ForgeLog.d("com.parse.push successfully subscribed to the broadcast channel.");
						} else {
							ForgeLog.e("com.parse.push failed to subscribe for push: " + e.getLocalizedMessage());
							e.printStackTrace();
						}
					}
				});
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
