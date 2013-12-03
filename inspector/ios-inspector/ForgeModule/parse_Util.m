//
//  parse_Util.m
//  Forge
//
//  Created by Connor Dunn on 14/03/2012.
//  Copyright (c) 2012 Trigger Corp. All rights reserved.
//

#import "parse_Util.h"
	
static NSDictionary* lastNotif;

@implementation parse_Util

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
