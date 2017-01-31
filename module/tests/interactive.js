module("forge.parse");

var channel = "channel"+Math.floor(Math.random()*10000);

if (forge.is.mobile()) {
	forge.parse.registerForNotifications(function () {
		asyncTest("receive push", 1, function () {
			forge.parse.push.subscribe(channel, function () {
				$.ajax({
					url: 'http://docker.trigger.io:1337/parse/push',
					headers: {
						"X-Parse-Application-Id": "45732VjSzMiN8HN90ztWcSeEl05T92XUrE70MJgI",
						"X-Parse-MASTER-Key": "MASTER_KEY"
					},
					contentType: "application/json",
					type: 'POST',
					data: JSON.stringify({
						channels: [channel],
						data: { "alert": "†és† push "+((new Date()).toString()) }
					}),
					success: function () {
						setTimeout(function () {
							forge.partners.parse.push.unsubscribe(channel);
						}, 60000);
					},
					error: function () {
						setTimeout(function () {
							forge.partners.parse.push.unsubscribe(channel);
						}, 10000);
					}
				});
			});
			forge.event.messagePushed.addListener(runOnce(function (msg) {
				askQuestion("Done.");
				ok(true);
				start();
			}));
			askQuestion("Wait a short while for a parse notification.", {
				"No notification arrived": function () {
					ok(false);
					start();
				}
			});
		});
	}, function (error) {
		forge.logging.log("Failed to register for remote notifications: " + JSON.stringify(error));
	});
}
