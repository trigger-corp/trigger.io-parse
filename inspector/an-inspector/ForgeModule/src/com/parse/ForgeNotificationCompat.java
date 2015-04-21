package com.parse;

import android.app.Notification;
import android.content.Context;
import android.os.Build;

/**
 * Created for an-inspector
 * User: arolave
 * Date: 20/04/15
 * Time: 14:30
 */
public class ForgeNotificationCompat extends NotificationCompat {
  private static final NotificationCompat.NotificationCompatImpl IMPL;

  static {
    if(Build.VERSION.SDK_INT >= 16) {
      IMPL = new ForgeNotificationCompat.NotificationCompatPostJellyBean();
    } else {
      IMPL = new ForgeNotificationCompat.NotificationCompatImplBase();
    }
  }

  static class Builder extends NotificationCompat.Builder {
    public Builder(Context context) {
      super(context);
    }

    public Builder setNumber(int number) {
      this.mNotification.number = number;
      return this;
    }

    @Override
    public Notification build() {
      return ForgeNotificationCompat.IMPL.build(this);
    }
  }

  static class NotificationCompatPostJellyBean implements NotificationCompat.NotificationCompatImpl {
    private android.app.Notification.Builder postJellyBeanBuilder;

    NotificationCompatPostJellyBean() {
    }

    public Notification build(NotificationCompat.Builder b) {
      this.postJellyBeanBuilder = new android.app.Notification.Builder(b.mContext);
      this.postJellyBeanBuilder.setContentTitle(b.mContentTitle).setContentText(b.mContentText).setSmallIcon(b.mNotification.icon, b.mNotification.iconLevel).setContentIntent(b.mContentIntent).setDeleteIntent(b.mNotification.deleteIntent).setAutoCancel((b.mNotification.flags & 16) != 0).setLargeIcon(b.mLargeIcon).setDefaults(b.mNotification.defaults).setNumber(b.mNotification.number);

      if(b.mStyle != null && b.mStyle instanceof NotificationCompat.Builder.BigTextStyle) {
        NotificationCompat.Builder.BigTextStyle staticStyle = (NotificationCompat.Builder.BigTextStyle)b.mStyle;
        android.app.Notification.BigTextStyle style = (new android.app.Notification.BigTextStyle(this.postJellyBeanBuilder)).setBigContentTitle(staticStyle.mBigContentTitle).bigText(staticStyle.mBigText);
        if(staticStyle.mSummaryTextSet) {
          style.setSummaryText(staticStyle.mSummaryText);
        }
      }

      return this.postJellyBeanBuilder.build();
    }
  }

  static class NotificationCompatImplBase implements NotificationCompat.NotificationCompatImpl {
    NotificationCompatImplBase() {
    }

    public Notification build(NotificationCompat.Builder b) {
      Notification result = b.mNotification;
      result.setLatestEventInfo(b.mContext, b.mContentTitle, b.mContentText, b.mContentIntent);
      if(b.mPriority > 0) {
        result.flags |= 128;
      }

      return result;
    }
  }

}
