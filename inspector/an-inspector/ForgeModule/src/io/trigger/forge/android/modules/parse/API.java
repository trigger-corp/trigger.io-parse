package io.trigger.forge.android.modules.parse;

import io.trigger.forge.android.core.ForgeApp;
import io.trigger.forge.android.core.ForgeLog;
import io.trigger.forge.android.core.ForgeParam;
import io.trigger.forge.android.core.ForgeTask;

import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.SaveCallback;


public class API {
	public static void installationInfo(final ForgeTask task) {
		JsonObject result = new JsonObject();
		result.addProperty("id", ParseInstallation.getCurrentInstallation().getInstallationId());
		task.success(result);
	}

	public static void push_subscribe(final ForgeTask task, @ForgeParam("channel") final String channel) {
		ParsePush.subscribeInBackground(channel, new SaveCallback() {
			@Override
			public void done(com.parse.ParseException e) {
				if (e == null) {
					ForgeLog.d("com.parse.push successfully subscribed to: " + channel);
					task.success();
				} else {
					ForgeLog.e("com.parse.push failed to subscribe to " + channel + ": " + e.getMessage());
					task.error(e);
				}
			}
		});
	}

	public static void push_unsubscribe(final ForgeTask task, @ForgeParam("channel") final String channel) {
		ParsePush.unsubscribeInBackground(channel, new SaveCallback() {
			@Override
			public void done(com.parse.ParseException e) {
				if (e == null) {
					ForgeLog.d("com.parse.push successfully unsubscribed from: " + channel);
					task.success();
				} else {
					ForgeLog.e("com.parse.push failed to unsubscribe from " + channel + ": " + e.getMessage());
					task.error(e);
				}
			}
		});
	}

	public static void push_subscribedChannels(final ForgeTask task) {
		JsonArray result = new JsonArray();
		List<String> subscribedChannels = ParseInstallation.getCurrentInstallation().getList("channels");
		for (String channel : subscribedChannels) {
			result.add(new JsonPrimitive(channel));
		}
		task.success(result);
	}

	public static void push_messagePushed(final ForgeTask task) {
		if (ForgeApp.getActivity().getIntent().getExtras() != null && ForgeApp.getActivity().getIntent().getExtras().get("com.parse.Data") != null) {
			ForgeApp.event("event.messagePushed", new JsonParser().parse((String)ForgeApp.getActivity().getIntent().getExtras().get("com.parse.Data")));
		}
		task.success();
	}

	public static void registerForNotifications(final ForgeTask task) {
		// shim for iOS-only functionality
		task.success();
	}
}
