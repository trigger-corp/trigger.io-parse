//
//  parse_API.m
//  Forge
//
//  Created by Connor Dunn on 14/03/2012.
//  Copyright (c) 2012 Trigger Corp. All rights reserved.
//

#import "parse_API.h"
#import "parse_Util.h"

#import "Parse/Parse.h"

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

    // Parse broke setBadgeNumber in 1.15.3 - it still updates server side but the app badge is never updated
    // https://github.com/parse-community/Parse-SDK-iOS-OSX/compare/1.15.2...1.15.3
    [UIApplication sharedApplication].applicationIconBadgeNumber = [number integerValue];

    [installation saveEventually:^(BOOL succeeded, NSError * _Nullable error) {
        if (succeeded == YES) {
            NSLog(@"parse.setBadgeNumber completed remote update");
        } else if (error == nil) {
            NSLog(@"parse.setBadgeNumber failed during remote update: unknown error");
        } else {
            NSLog(@"parse.setBadgeNumber failed during remote update: %@", [error localizedDescription]);
        }
    }];

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
        [parse_Util registerForNotifications:[UIApplication sharedApplication]
                                      server:[config objectForKey:@"server"]
                               applicationId:[config objectForKey:@"applicationId"]
                                   clientKey:[config objectForKey:@"clientKey"]];
		[task success:nil];
	} else {
		[task error:@"You need to enable the 'delayRegistration' option in your app configuration to use this feature."];
	}
}

@end
