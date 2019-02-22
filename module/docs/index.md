``parse``: Push notifications with Parse
====================================

["Add a Backend to Your Mobile App in Minutes"](https://parse.com/) - allows you to add backend features to
your mobile app without a server. Their back-end features include a datastore, user accounts and push notifications.

For a small example on our blog, see [Using Parse and Trigger.io for cross-platform apps without pain in the
back-end](http://trigger.io/cross-platform-application-development-blog/2012/03/23/using-parse-and-trigger-io-for-cross-platform-apps-without-pain-in-the-back-end/).

Parse push notifications are integrated directly Forge. Other Parse features may be accessed by using [forge.request.ajax](/modules/request/current/docs/index.html#forgerequestajaxoptions) with the [Parse REST API](https://parse.com/docs/rest).

----

## Migrating to parse module v3.x

> ::Important:: As of April 10, 2018, Google has deprecated GCM. The GCM server and client APIs are deprecated and will be removed as soon as April 11, 2019.

Version 3.x of the parse module removes support for GCM and adds FCM support.

### App

**If you don't have a Firebase project you will first need to create one:**

1. Go to the [Firebase console](https://console.firebase.google.com/).
2. Click Add project, then select or enter a Project name.
    * If you have an existing Google project associated with your app, select the project from the Project name dropdown menu.
    * If you don't have an existing Google project, enter a new Project name.
4. Follow the remaining setup steps in the Firebase console, then click Create project (or Add Firebase, if you're using an existing Google project).

**Add your app to the Firebase project:**

1. Go to the [Firebase console](https://console.firebase.google.com/).
2. Click Add Firebase to your Android app and follow the setup steps. If you're importing an existing Google project, this may happen automatically and you can just download the config file.
3. When prompted, enter your app's package name. It's important to enter the package name your app is using; this can only be set when you add the app to your Firebase project.
4. Add the Firebase Android configuration file to your app:
    * Click *"Download google-services.json"* to obtain your Firebase Android config file.
    * Create a new directory called `assets` in your app directory:

            cd <your app directory> && mkdir assets

    * Move your `google-services.json` file to `<your app directory>/assets/google-services.json`

**Configure your app:**

1. Open your app's `src/config.json` file in an editor and look for the `parse.android` section.
2. Update the `version` field to the latest `3.x` version.
3. Remove the `parse.android.GCMsenderID` field.
4. Add an entry for your `google-services.json` file to the `parse.android` section:

        "android": {
            "googleServicesJson": "google-services.json",
            ...

5. Rename the following fields in the `parse.android` section:
    * `notification_icon` to `notificationIcon`
    * `background-color` to `backgroundColour`
6. Rename the corresponding value in the `parse.android.notificationIcon` field:
    * `icon` to `@mipmap/ic_launcher` *_or_*
    * `custom_push_icon` to `@drawable/custom_push_icon`

More info: [https://firebase.google.com/docs/android/setup](https://firebase.google.com/docs/android/setup)


### Server

If you are using a hosted Parse server you will need to contact your hosting provider to update the server to the latest Parse version. (3.1.3 at the time of writing)

If you are hosting your own parse server you will need to manually update it to at least 3.1.3.

More info: [https://docs.parseplatform.org/parse-server/guide/#push-notifications](https://docs.parseplatform.org/parse-server/guide/#push-notifications)

----

## Config options

server
:   The api endpoint of the Parse Server (Optional: use for self-hosted Parse Server).

applicationId
:   An application ID from [parse.com](https://parse.com/).

clientKey
:   A client key from [parse.com](https://parse.com/).

delayRegistration
:	Don't register for remote notifications at app startup (iOS Only)

### Android

Update Notifications
:   Update notifications on Android using the Inbox Style.  [More information.](https://blog.safaribooksonline.com/2012/08/29/android-4-1-jelly-bean-notifications/)

Show Notifications While Visible
:   Show incoming notifications on Android, even if the app is currently visible in foreground. Default is to not show them in this case.

Background Color
:   Use a custom color for the background for your notification icon, e.g. #303045. (Android 5.0+ only)

Notification Icon
:	Choose `@mipmap/ic_launcher` to use the app icon or `@drawable/custom_push_icon` to use a custom icon for notifications.

24, 36, 48, 72, 96
:	Paths to the icons used if you have enabled a custom push notification icon.

----

## Concepts

> ::Important:: In order to use this module you'll need to either [host your own instance of the Open Source Parse Server](https://github.com/ParsePlatform/parse-server/wiki/Parse-Server-Guide) or make use of one of the many available [hosted solutions](https://www.quora.com/Is-there-anyone-offering-Parse-com-hosting-now-that-Facebook-is-going-to-shut-Parse-com-down).

Push notifications received through Parse can be used with the generic
push notification event in Forge, see the [event API](/docs/current/api/core/event.html) for
more details. The following code is an example of how to show an alert
to a user when a push notification is received.

**Example**:

    forge.event.messagePushed.addListener(function (msg) {
        alert(msg.alert);
    });

You can try out sending a push notification from your app's control
panel at [parse.com](https://parse.com).

Parse uses channels to send push notifications to specific groups of
users. By default all users are subscribed to the empty channel; if you
wish to send push notifications to specific users, you can use the
following methods to manage which channels a user is subscribed to.


----


## API

!method: forge.parse.installationInfo(success, error)
!param: success `function(info)` called if the request is successful: ``info`` will contain at least an ``id`` entry
!description: Every Parse installation has a unique ID associated with it; you can use this method to retrieve the installation ID for this user and do things like [advanced targeting of push notifications](https://parse.com/docs/push_guide#sending-queries/REST).
!platforms: iOS, Android
!param: error `function(content)` called with details of any error which may occur

**Example**:

    forge.parse.installationInfo(function (info) {
        forge.logging.info("installation: "+JSON.stringify(info));
    });

!method: forge.parse.push.subscribe(channel, success, error)
!param: channel `string` identifier of the channel to subscribe to
!param: success `function()` called if the request is successful
!description: Subscribe to a channel to receive push notifications.
!platforms: iOS, Android
!param: error `function(content)` called with details of any error which may occur

**Example**:

    forge.parse.push.subscribe("beta-testers",
    function () {
      forge.logging.info("subscribed to beta-tester push notifications!");
    },
    function (err) {
      forge.logging.error("error subscribing to beta-tester notifications: "+
        JSON.stringify(err));
    });

!method: forge.parse.push.unsubscribe(channel, success, error)
!param: channel `string` identifier of the channel to unsubscribe from
!param: success `function()` called if the request is successful
!description: Un-subscribe from a channel to stop receiving push notifications.
!platforms: iOS, Android
!param: error `function(content)` called with details of any error which may occur

**Example**:

    forge.parse.push.unsubscribe("beta-testers",
    function () {
      forge.logging.info("no more beta-tester notifications...");
    },
    function (err) {
      forge.logging.error("couldn't unsubscribe from beta-tester notifications: "+
        JSON.stringify(err));
    });

!method: forge.parse.push.subscribedChannels(success, error)
!param: success `function(channels)` called with an array of subscribed channels
!description: Query subscribed channels.
!platforms: iOS, Android
!param: error `function(content)` called with details of any error which may occur

**Example**:

    forge.parse.push.subscribedChannels(
    function (channels) {
      forge.logging.info("subscribed to: "+JSON.stringify(channels));
    },
    function (err) {
      forge.logging.error("couldn't retrieve subscribed channels: "+
        JSON.stringify(err));
    });

> ::Important:: In previous verions of Forge, the parse module was exposed as ``forge.partners.parse``.
To ease transition to v2 of Forge, ``forge.partners.parse`` is an alias of ``forge.parse``. However use of
the ``forge.partners.parse`` form is deprecated and should be changed to just ``forge.parse``.

!method: forge.parse.setBadgeNumber(number, success, error)
!param: number `integer` number
!param: success `function()` callback to be invoked when no errors occur
!description: Updates your badge count on the Parse server and sets or removes a badge for your app's icon on the iOS home screen.
!platforms: iOS
!param: error `function(content)` called with details of any error which may occur

> ::Note:: If you pass in 0 as number, it will remove this badge. This is
particularly useful if you want to clear a badge set by a push
notification.

!method: forge.parse.getBadgeNumber(success, error)
!param: success `function(number)` called if the request is successful. ``number`` contains the current badge count for your app.
!description: Returns the badge count registered for your app on the Parse server.
!platforms: iOS
!param: error `function(content)` called with details of any error which may occur

!method: forge.parse.registerForNotifications(success, error)
!param: success Called if the request is successful.
!description: When used in conjunction with the config option `delayRegistration` this function can be used to delay the appearance of the notifications confirmation dialog until such time as this function is called rather than the first time the app is run.
!platforms: iOS
!param: error `function(content)` called with details of any error which may occur

**Example**:

    forge.parse.registerForNotifications(function () {
      // iOS users have now received the push notifications permission alert
    }, function (err) {
      forge.logging.error("Failed to register for notifications: " +
        JSON.stringify(err));
    });

> ::Important:: Instructions for resetting the push notifications permission alert on iOS can be found in ["Technical Note 2265"](https://developer.apple.com/library/ios/technotes/tn2265/_index.html)

## Permissions

On Android this module will add the ``VIBRATE`` and
``RECEIVE_BOOT_COMPLETED`` permissions to your app, users will be
prompted to accept this when they install your app.
