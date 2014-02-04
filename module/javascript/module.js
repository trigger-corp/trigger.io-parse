// Implementation
var parse = {};

var originalMessagePushed = forge.event.messagePushed;

parse.push = {
	subscribe: function (channel, success, error) {
		forge.internal.call("parse.push_subscribe", {channel: channel}, success, error);
	},
	unsubscribe: function (channel, success, error) {
		forge.internal.call("parse.push_unsubscribe", {channel: channel}, success, error);
	},
	subscribedChannels: function (success, error) {
		forge.internal.call("parse.push_subscribedChannels", {}, success, error);
	},
	messagePushed: {
		addListener: function (callback, error) {
			originalMessagePushed.addListener(callback, error);
			forge.internal.call("parse.push_messagePushed", {});
		}
	}
};

// Deprecated access through forge.partners
forge['partners'] = forge;

forge['parse'] = {
	'installationInfo': function (success, error) {
		forge.internal.call("parse.installationInfo", {}, success, error);
	},
	'getBadgeNumber': function (success, error) {
		forge.internal.call("parse.getBadgeNumber", {}, success, error);
	},
	'setBadgeNumber': function (number, success, error) {
		forge.internal.call("parse.setBadgeNumber", {number: number}, success, error);
	},
	'registerForNotifications': function (success, error) {
		forge.internal.call("parse.registerForNotifications", {}, success, error);
	},
	'push': {
		'subscribe': parse.push.subscribe,
		'unsubscribe': parse.push.unsubscribe,
		'subscribedChannels': parse.push.subscribedChannels
	}
};

// Override default messagePushed event
forge['event']['messagePushed'] = parse.push.messagePushed;
