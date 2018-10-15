package io.trigger.forge.android.modules.parse;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.JsonObject;
import com.parse.*;
import io.trigger.forge.android.core.ForgeApp;
import io.trigger.forge.android.core.ForgeEventListener;
import io.trigger.forge.android.core.ForgeLog;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;

import com.google.gson.JsonParser;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;

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
				if (e != null) {
                    ForgeLog.e("com.parse.push failed to initialize: " + e.getLocalizedMessage());
                    e.printStackTrace();
                    return;
                }

                ForgeLog.d("com.parse.push initialized successfully");
                Object deviceToken = ParseInstallation.getCurrentInstallation().get("deviceToken");
                if (deviceToken == null) {
                    new AsyncTask() {
                        @Override
                        protected Object doInBackground(Object[] objects) {
                            setDeviceTokenManually(GCMSenderId);
                            return null;
                        }
                    }.execute(null, null, null);
                }

				ParsePush.subscribeInBackground("", new SaveCallback() {
					@Override
					public void done(com.parse.ParseException e) {
						if (e != null) {
                            ForgeLog.e("com.parse.push failed to subscribe for push: " + e.getLocalizedMessage());
                            e.printStackTrace();
                            return;
						}
                        ForgeLog.d("com.parse.push successfully subscribed to the broadcast channel.");
					}
				});
			}
		});

		ForgeLog.i("Initializing Parse and subscribing to default channel.");
	}


    /**
     * Workaround for Android P devices no longer managing to receive a deviceToken from GCM
     *
     * See: https://github.com/parse-community/Parse-SDK-Android/issues/880
     *      https://www.back4app.com/docs/android/parse-cloud-code
     *
     * @param gcmSenderId
     */
    private void setDeviceTokenManually(final String gcmSenderId) {
        // 1. Register device with GCM
        String deviceToken = null;
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(ForgeApp.getActivity().getApplicationContext());
        try {
            deviceToken = gcm.register(gcmSenderId);
            ForgeLog.d("com.parse.push obtained device token");
        } catch (Exception e) {
            ForgeLog.d("com.parse.push could not obtain device token: " + e.getLocalizedMessage());
            e.printStackTrace();
            return;
        }

        // 2. Set Parse installation's deviceToken
        try {
            ParseInstallation installation = ParseInstallation.getCurrentInstallation();
            Method[] methods = installation.getClass().getDeclaredMethods();
            for (Method method : methods) {
                if (method.getName().equals("setDeviceToken")) {
                    method.setAccessible(true);
                    Object result = method.invoke(installation, deviceToken);
                    break;
                }
            }
        } catch (Exception e) {
            ForgeLog.d("com.parse.push failed to set device token: " + e.getLocalizedMessage());
            e.printStackTrace();
            return;
        }

        // 3. Update Parse Installation
        ParseInstallation.getCurrentInstallation().saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    ForgeLog.e("com.parse.push failed to save device token: " + e.getLocalizedMessage());
                    e.printStackTrace();
                    return;
                }
                ForgeLog.d("com.parse.push successfully saved device token");
            }
        });
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
