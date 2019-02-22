package io.trigger.forge.android.modules.parse;

import android.content.Intent;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import com.parse.*;
import io.trigger.forge.android.core.ForgeApp;
import io.trigger.forge.android.core.ForgeEventListener;
import io.trigger.forge.android.core.ForgeLog;
import io.trigger.parse.VisibilityManager;


public class EventListener extends ForgeEventListener {
    @Override
    public void onApplicationCreate() {
        final JsonObject config = ForgeApp.configForPlugin(Constant.MODULE_NAME);

        // TODO add a DEBUG option to config.json
        Parse.setLogLevel(Parse.LOG_LEVEL_DEBUG);

        String server = config.has("server")
            ? config.get("server").getAsString()
            : "https://api.parse.com/1/";

        final String applicationId = config.has("applicationId")
            ? config.get("applicationId").getAsString()
            : "";

        final String clientKey = config.has("clientKey")
            ? ForgeApp.configForPlugin(Constant.MODULE_NAME).get("clientKey").getAsString()
            : null;

        final Parse.Configuration configuration = new Parse.Configuration.Builder(ForgeApp.getApp())
            .server(server)
            .applicationId(applicationId)
            .clientKey(clientKey)
            .build();

        ForgeLog.d("com.parse.push initializing with server: " + server);
        Parse.initialize(configuration);

        ParseInstallation.getCurrentInstallation().saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    ForgeLog.e("com.parse.push failed to initialize: " + e.getLocalizedMessage());
                    e.printStackTrace();
                    return;
                }
                ForgeLog.d("com.parse.push initialized successfully");

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

        ForgeLog.d("Initializing Parse and subscribing to default channel.");
    }

    @Override
    public void onNewIntent(Intent intent) {
        ForgeLog.d("io.trigger.forge.android.modules.parse.EventListener onNewIntent");
        if (intent.getExtras() != null && intent.getExtras().get("com.parse.Data") != null) {
            ForgeApp.event("event.messagePushed", new JsonParser().parse((String) intent.getExtras().get("com.parse.Data")));
        }
    }

    @Override
    public void onStart() {
        ForgeLog.d("io.trigger.forge.android.modules.parse.EventListener onStart");
        VisibilityManager.resumed();
    }

    @Override
    public void onStop() {
        ForgeLog.d("io.trigger.forge.android.modules.parse.EventListener onStop");
        VisibilityManager.paused();
    }
}
