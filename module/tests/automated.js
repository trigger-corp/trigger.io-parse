/* globals module, forge, asyncTest, ok, start */

module("forge.parse");

if (forge.is.mobile()) {
    forge.parse.registerForNotifications(function () {
        asyncTest("installationInfo", 1, function () {
            forge.partners.parse.installationInfo(function (info) {
                ok('id' in info);
                start();
            }, function () {
                ok(false);
                start();
            });
        });

        if (forge.is.ios()) {
            asyncTest("setBadgeNumber", 1, function () {
                forge.partners.parse.setBadgeNumber(42, function () {
                    forge.partners.parse.getBadgeNumber(function (badge) {
                        ok(badge === 42);
                        forge.partners.parse.setBadgeNumber(0);
                        start();
                    }, function () {
                        ok(false);
                        start();
                    });
                }, function () {
                    ok(false);
                    start();
                });
            });
        }

        asyncTest("subscribe", 1, function () {
            forge.parse.push.subscribe("test", function () {
                ok(true);
                start();
            }, function () {
                ok(false);
                start();
            });
        });

        asyncTest("unsubscribe", 1, function () {
            forge.parse.push.unsubscribe("test", function () {
                ok(true);
                start();
            }, function () {
                ok(false);
                start();
            });
        });

        asyncTest("list", 1, function () {
            forge.parse.push.subscribedChannels(function () {
                ok(true);
                start();
            }, function () {
                ok(false);
                start();
            });
        });
    }, function (error) {
        forge.logging.log("Failed to register for remote notifications: " + JSON.stringify(error));
    });
}
