package com.parse;

import android.util.Log;
import io.trigger.forge.android.modules.parse.Constant;

/**
 * Created for an-inspector
 * User: arolave
 * Date: 21/04/15
 * Time: 15:48
 */
public class VisibilityManager {
  private static int mResumed = 0;
  private static int mPaused = 0;

  public static void resumed() {
    mResumed ++;
    Log.i(Constant.LOGGER_TAG, "Resumed Count: " + mResumed);
  }

  public static void paused() {
    mPaused ++;
    Log.i(Constant.LOGGER_TAG, "Paused Count: " + mPaused);
  }

  public static boolean isVisible() {
    boolean isVisible = (mResumed - mPaused) != 0;
    Log.i(Constant.LOGGER_TAG, "isVisible: " + isVisible);
    return isVisible;
  }
}
