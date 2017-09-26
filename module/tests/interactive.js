/* globals module, $, forge, asyncTest, askQuestion, ok, start, runOnce */

module("forge.parse");

var channel = "channel" + Math.floor(Math.random()*10000);

var parse_endpoint = "http://docker.trigger.io:1337/parse/push";
var parse_headers = {
    "X-Parse-Application-Id": "45732VjSzMiN8HN90ztWcSeEl05T92XUrE70MJgI",
    "X-Parse-MASTER-Key": "MASTER_KEY"
};

asyncTest("Delay registration", 1, function () {
    forge.parse.registerForNotifications(function () {
        askQuestion("Did the device register for remote notifications?", {
            Yes: function () {
                ok(true);
                start();
            },
            No: function () {
                ok(false);
                start();
            }
        });
    }, function (error) {
        forge.logging.log("Failed to register for remote notifications: " + JSON.stringify(error));
        ok(false, "Failed to register: " + JSON.stringify(error));
        start();
    });
});


function send_push(channel, message) {
    $.ajax({
        url: parse_endpoint,
        headers: parse_headers,
        contentType: "application/json",
        type: "POST",
        data: JSON.stringify({
            channels: [channel],
            data: {
                "alert": message + ": " + ((new Date()).toString())
            }
        }),
        success: function () {
            setTimeout(function () {
                forge.partners.parse.push.unsubscribe(channel);
            }, 60000);
        },
        error: function () {
            setTimeout(function () {
                forge.partners.parse.push.unsubscribe(channel);
            }, 60000);
        }
    });
}


/*asyncTest("receive push", 1, function () {
    forge.parse.push.subscribe(channel, function () {
        send_push(channel, "†és† push");
    });
    forge.event.messagePushed.addListener(runOnce(function () {
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
});*/


asyncTest("receive push in background", 1, function () {
    askQuestion("Click OK, put your app in the background and click on the notification when it arrives", {
        Ok: function () {
            forge.event.messagePushed.addListener(runOnce(function () {
                ok(true);
                start();
            }));
            forge.parse.push.subscribe(channel, function () {
                forge.event.appPaused.addListener(function () {
                    forge.logging.log("Sending push");
                    send_push(channel, "update notifications push");
                });
            });
            askQuestion("Wait a short while for a parse notification.", {
                "No notification arrived": function () {
                    ok(false);
                    start();
                }
            });
        }
    });
});
