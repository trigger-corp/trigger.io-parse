//
//  parse_Util.h
//  Forge
//
//  Created by Connor Dunn on 14/03/2012.
//  Copyright (c) 2012 Trigger Corp. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface parse_Util : NSObject {
	
}

+ (void)setLaunchOptions:(NSDictionary*) launchOptions;
+ (void)registerForNotifications:(UIApplication*)application server:(NSString*)server applicationId:(NSString*)applicationId clientKey:(NSString*)clientKey;
+ (void)didReceiveRemoteNotification:(NSDictionary*)userInfo;
+ (void)triggerMessagePushedEvent;

@end
