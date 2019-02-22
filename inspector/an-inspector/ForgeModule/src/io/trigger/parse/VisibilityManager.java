package io.trigger.parse;

import io.trigger.forge.android.core.ForgeLog;

public class VisibilityManager {
    private static int resumed = 0;
    private static int paused = 0;

    public static void resumed() {
        resumed++;
        ForgeLog.d("io.trigger.parse.VisibilityManager resumed count: " + resumed);
    }

    public static void paused() {
        paused++;
        ForgeLog.d("io.trigger.parse.VisibilityManager paused count: " + resumed);
    }

    public static boolean isVisible() {
        boolean isVisible = (resumed - paused) != 0;
        ForgeLog.d("io.trigger.parse.VisibilityManager isVisible: " + isVisible);
        return isVisible;
    }
}
