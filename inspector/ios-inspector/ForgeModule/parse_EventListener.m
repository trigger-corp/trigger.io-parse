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
	[Parse setApplicationId:[[[ForgeApp sharedApp] configForPlugin:@"parse"] objectForKey:@"applicationId"]
				  clientKey:[[[ForgeApp sharedApp] configForPlugin:@"parse"] objectForKey:@"clientKey"]];
	
	[application registerForRemoteNotificationTypes:UIRemoteNotificationTypeBadge|UIRemoteNotificationTypeAlert|UIRemoteNotificationTypeSound];

	if ([launchOptions objectForKey:UIApplicationLaunchOptionsRemoteNotificationKey] != nil) {
		[parse_Util notifRecieved:[launchOptions objectForKey:UIApplicationLaunchOptionsRemoteNotificationKey]];
		[parse_Util triggerMessagePushedEvent];
	}
}

+ (void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)newDeviceToken {
	// Tell Parse about the device token.
	[PFPush storeDeviceToken:newDeviceToken];
	// Subscribe to the global broadcast channel.
	[PFPush subscribeToChannelInBackground:@""];
	
	[ForgeLog i:@"Registering for parse push notifications, subscribing to default channel."];
}

+ (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo {
	[parse_Util notifRecieved:userInfo];
	[parse_Util triggerMessagePushedEvent];
	[ForgeLog i:@"Received push notification."];
}
@end
