//
//  parse_Util.m
//  Forge
//
//  Created by Connor Dunn on 14/03/2012.
//  Copyright (c) 2012 Trigger Corp. All rights reserved.
//

#import "parse_Util.h"
#import "Parse/Parse.h"

#import <UserNotifications/UserNotifications.h>


static NSDictionary* launchOptions;
static NSDictionary* lastNotif;

@implementation parse_Util

+ (void)setLaunchOptions:(NSDictionary*) launchOptionsDict {
	launchOptions = launchOptionsDict != NULL ? [NSDictionary dictionaryWithDictionary:launchOptionsDict] : NULL;
}

+ (void)registerForNotifications:(UIApplication*)application server:(NSString*)server applicationId:(NSString*)applicationId clientKey:(NSString*)clientKey {
    if (!server) {
        server = @"https://api.parse.com/1";
    }
    
    [Parse initializeWithConfiguration:[ParseClientConfiguration configurationWithBlock:^(id<ParseMutableClientConfiguration> configuration) {
        configuration.applicationId = applicationId;
        configuration.clientKey = clientKey;
        configuration.server = server;
    }]];
    
    UNUserNotificationCenter *center = [UNUserNotificationCenter currentNotificationCenter];
    [center requestAuthorizationWithOptions:(UNAuthorizationOptionSound | UNAuthorizationOptionAlert | UNAuthorizationOptionBadge) completionHandler:^(BOOL granted, NSError * _Nullable error) {
        if (!error) {
            dispatch_async(dispatch_get_main_queue(), ^{
                [[UIApplication sharedApplication] registerForRemoteNotifications];
            });
        } else {
            [ForgeLog w:@"User did not grant notification permissions"];
        }
    }];
    
    if (launchOptions != NULL && [launchOptions objectForKey:UIApplicationLaunchOptionsRemoteNotificationKey] != nil) {
        [parse_Util didReceiveRemoteNotification:[launchOptions objectForKey:UIApplicationLaunchOptionsRemoteNotificationKey]];
        [parse_Util triggerMessagePushedEvent];
    }
}

+ (void)didReceiveRemoteNotification:(NSDictionary*)userInfo {
	NSMutableDictionary *data = [NSMutableDictionary dictionaryWithDictionary:[userInfo objectForKey:@"aps"]];
	for (id key in [userInfo allKeys]) {
		if (![key isEqualToString:@"aps"]) {
			[data setObject:[userInfo objectForKey:key] forKey:key];
		}
	}
	lastNotif = data;
}

+ (void)triggerMessagePushedEvent {
	if (lastNotif != nil) {
		[[ForgeApp sharedApp] event:@"event.messagePushed" withParam:lastNotif];
	}
}

@end
