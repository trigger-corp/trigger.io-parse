//
//  parse_Util.m
//  Forge
//
//  Created by Connor Dunn on 14/03/2012.
//  Copyright (c) 2012 Trigger Corp. All rights reserved.
//

#import "parse_Util.h"
#import "Parse/Parse.h"

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
  
  if ([application respondsToSelector:@selector(registerUserNotificationSettings:)]) {
    [application registerUserNotificationSettings:[UIUserNotificationSettings settingsForTypes:(UIUserNotificationTypeSound|UIUserNotificationTypeAlert|UIUserNotificationTypeBadge) categories:nil]];
    [application registerForRemoteNotifications];
    
  } else {
    [[UIApplication sharedApplication] registerForRemoteNotificationTypes:UIRemoteNotificationTypeBadge|UIRemoteNotificationTypeAlert|UIRemoteNotificationTypeSound];
  }
  
	if (launchOptions != NULL && [launchOptions objectForKey:UIApplicationLaunchOptionsRemoteNotificationKey] != nil) {
		[parse_Util notifRecieved:[launchOptions objectForKey:UIApplicationLaunchOptionsRemoteNotificationKey]];
		[parse_Util triggerMessagePushedEvent];
	}
}

+ (void)notifRecieved:(NSDictionary*)userInfo {
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
