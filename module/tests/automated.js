module("forge.parse");

if (forge.is.mobile()) {
	asyncTest("installationInfo", 1, function () {
		forge.partners.parse.installationInfo(function (info) {
			ok('id' in info);
			start();
		}, function () {
			ok(false);
			start();
		});
	});

	asyncTest("subscribe", 1, function () {
		forge.parse.push.subscribe("test", function () {
			ok(true);
			start()
		}, function () {
			ok(false);
			start()
		});
	});

	asyncTest("unsubscribe", 1, function () {
		forge.parse.push.unsubscribe("test", function () {
			ok(true);
			start()
		}, function () {
			ok(false);
			start()
		});
	});

	asyncTest("list", 1, function () {
		forge.parse.push.subscribedChannels(function () {
			ok(true);
			start()
		}, function () {
			ok(false);
			start()
		});
	});
}
