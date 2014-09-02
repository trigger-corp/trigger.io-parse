//
//  parse_EventListener.m
//  Forge
//
//  Created by Connor Dunn on 13/03/2012.
//  Copyright (c) 2012 Trigger Corp. All rights reserved.
//

#import "parse_EventListener.h"
#import "parse_Util.h"
#import "Parse.h"

@implementation parse_EventListener

+ (void)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
	[parse_Util setLaunchOptions:launchOptions];

	Boolean delayRegistration = false;
	NSDictionary* config = [[ForgeApp sharedApp] configForPlugin:@"parse"];
	if ([config objectForKey:@"delayRegistration"] != nil) {
		delayRegistration = [[config objectForKey:@"delayRegistration"] boolValue];
	}

	NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
	Boolean registeredForNotifications = [prefs boolForKey:@"parse_registeredForNotifications"];

	if (!delayRegistration || registeredForNotifications) {
		[parse_Util registerForNotifications:application
                               applicationId:[config objectForKey:@"applicationId"]
								   clientKey:[config objectForKey:@"clientKey"]];
	}
}

+ (void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)newDeviceToken {
	// Tell Parse about the device token.
	[PFPush storeDeviceToken:newDeviceToken];
	// Subscribe to the global broadcast channel.
	[PFPush subscribeToChannelInBackground:@""];

	// We can safely disable delayRegistration now
	NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
	[prefs setBool:YES forKey:@"parse_registeredForNotifications"];

	[ForgeLog i:@"Registering for parse push notifications, subscribing to default channel."];
	[ForgeLog d:[NSString stringWithFormat:@"Device Token is: %@", newDeviceToken]];
}

+ (void)application:(UIApplication *)application didFailToRegisterForRemoteNotificationsWithError:(NSError *)error {
	[ForgeLog e:[NSString stringWithFormat:@"Failed to register for remote notifications: %@", [error description]]];
}

+ (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo {
	[parse_Util notifRecieved:userInfo];
	[parse_Util triggerMessagePushedEvent];
	[ForgeLog i:@"Received push notification."];
}
@end
