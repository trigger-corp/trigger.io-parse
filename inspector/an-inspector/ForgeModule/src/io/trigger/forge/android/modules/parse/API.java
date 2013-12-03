package io.trigger.forge.android.modules.parse;

import io.trigger.forge.android.core.ForgeActivity;
import io.trigger.forge.android.core.ForgeApp;
import io.trigger.forge.android.core.ForgeParam;
import io.trigger.forge.android.core.ForgeTask;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonObject;
import com.parse.PushService;
import com.parse.ParseInstallation;


public class API {
	public static void installationInfo(final ForgeTask task) {
		JsonObject result = new JsonObject();
		result.addProperty("id", ParseInstallation.getCurrentInstallation().getInstallationId());
		task.success(result);
	}

	public static void push_subscribe(final ForgeTask task, @ForgeParam("channel") final String channel) {
		PushService.subscribe(ForgeApp.getActivity(), channel, ForgeActivity.class);
		task.success();
	}

	public static void push_unsubscribe(final ForgeTask task, @ForgeParam("channel") final String channel) {
		PushService.unsubscribe(ForgeApp.getActivity(), channel);
		task.success();
	}

	public static void push_subscribedChannels(final ForgeTask task) {
		JsonArray result = new JsonArray();
		for (String channel : PushService.getSubscriptions(ForgeApp.getActivity())) {
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
}
