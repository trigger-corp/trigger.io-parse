//
//  parse_API.m
//  Forge
//
//  Created by Connor Dunn on 14/03/2012.
//  Copyright (c) 2012 Trigger Corp. All rights reserved.
//

#import "parse_API.h"
#import "parse_Util.h"

@implementation parse_API

+ (void)installationInfo:(ForgeTask *)task {
	NSMutableDictionary *result = [NSMutableDictionary dictionary];
	PFInstallation *installation = [PFInstallation currentInstallation];
	result[@"id"] = installation.installationId;
	[task success:result];
}

+ (void)getBadgeNumber:(ForgeTask *)task {
	PFInstallation *installation = [PFInstallation currentInstallation];
	[task success:[NSNumber numberWithLong:installation.badge]];
}

+ (void)setBadgeNumber:(ForgeTask *)task number:(NSNumber*)number {
	PFInstallation *installation = [PFInstallation currentInstallation];
	installation.badge = [number integerValue];
	[installation saveEventually];
	[task success:nil];
}

+ (void)push_subscribe:(ForgeTask*)task channel:(NSString*)channel {
	[PFPush subscribeToChannelInBackground:channel block:^(BOOL succeeded, NSError *error) {
		if (succeeded) {
			[task success:nil];
		} else {
			[task error:[@"Subscribing to Parse channel failed: " stringByAppendingString:[error description]]];
		}
	}];
}

+ (void)push_unsubscribe:(ForgeTask*)task channel:(NSString*)channel {
	[PFPush unsubscribeFromChannelInBackground:channel block:^(BOOL succeeded, NSError *error) {
		if (succeeded) {
			[task success:nil];
		} else {
			[task error:[@"Unsubscribing from Parse channel failed: " stringByAppendingString:[error description]]];
		}
	}];
}

+ (void)push_subscribedChannels:(ForgeTask*)task {
	[PFPush getSubscribedChannelsInBackgroundWithBlock:^(NSSet *channels, NSError *error) {
		if (error == nil) {
			[task success:[channels allObjects]];
		} else {
			[task error:[@"Listing Parse channels failed: " stringByAppendingString:[error description]]];
		}
	}];
}

+ (void)push_messagePushed:(ForgeTask*)task {
	[parse_Util triggerMessagePushedEvent];
	[task success:nil];
}

+ (void)registerForNotifications:(ForgeTask *)task {
	NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
	Boolean registeredForNotifications = [prefs boolForKey:@"parse_registeredForNotifications"];
	if (registeredForNotifications) {
		[task success:nil];
		return;
	}

	Boolean delayRegistration = false;
	NSDictionary* config = [[ForgeApp sharedApp] configForPlugin:@"parse"];
	if ([config objectForKey:@"delayRegistration"] != nil) {
		delayRegistration = [[config objectForKey:@"delayRegistration"] boolValue];
	}

	if (delayRegistration) {
		[parse_Util registerForNotifications:[config objectForKey:@"applicationId"]
								   clientKey:[config objectForKey:@"clientKey"]];
		[task success:nil];
	} else {
		[task error:@"You need to enable the 'delayRegistration' option in your app configuration to use this feature."];
	}
}

@end
