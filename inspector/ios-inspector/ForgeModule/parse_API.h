//
//  parse_API.h
//  Forge
//
//  Created by Connor Dunn on 14/03/2012.
//  Copyright (c) 2012 Trigger Corp. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "Parse.h"
#import "PFInstallation.h"

@interface parse_API : NSObject

+ (void)installationInfo:(ForgeTask*)task;
+ (void)push_subscribe:(ForgeTask*)task channel:(NSString*)channel;
+ (void)push_unsubscribe:(ForgeTask*)task channel:(NSString*)channel;
+ (void)push_subscribedChannels:(ForgeTask*)task;
+ (void)push_messagePushed:(ForgeTask*)task;

@end
