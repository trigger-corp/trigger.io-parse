package com.parse;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.Context;
import android.os.Build;

import java.util.ArrayList;

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

        //All we want is a way to set the number on the notification!
        public Builder setNumber(int number) {
            this.mNotification.number = number;
            return this;
        }

        @Override
        public Notification build() {
      return ForgeNotificationCompat.IMPL.build(this);
    }

        /**
         * Helper class for generating large-format notifications that include a list of (up to 5) strings.
         *
         * <br>
         * If the platform does not provide large-format notifications, this method has no effect. The
         * user will always see the normal notification view.
         * <br>
         * This class is a "rebuilder": It attaches to a Builder object and modifies its behavior, like so:
         * <pre class="prettyprint">
         * Notification noti = new Notification.Builder()
         *     .setContentTitle(&quot;5 New mails from &quot; + sender.toString())
         *     .setContentText(subject)
         *     .setSmallIcon(R.drawable.new_mail)
         *     .setLargeIcon(aBitmap)
         *     .setStyle(new Notification.InboxStyle()
         *         .addLine(str1)
         *         .addLine(str2)
         *         .setContentTitle(&quot;&quot;)
         *         .setSummaryText(&quot;+3 more&quot;))
         *     .build();
         * </pre>
         *
         * @see Notification#bigContentView
         */
        public static class InboxStyle extends Style {
            ArrayList<CharSequence> mTexts = new ArrayList<CharSequence>();

            public InboxStyle() {
            }

            public InboxStyle(Builder builder) {
                setBuilder(builder);
            }

            /**
             * Overrides ContentTitle in the big form of the template.
             * This defaults to the value passed to setContentTitle().
             */
            public InboxStyle setBigContentTitle(CharSequence title) {
                mBigContentTitle = Builder.limitCharSequenceLength(title);
                return this;
            }

            /**
             * Set the first line of text after the detail section in the big form of the template.
             */
            public InboxStyle setSummaryText(CharSequence cs) {
                mSummaryText = Builder.limitCharSequenceLength(cs);
                mSummaryTextSet = true;
                return this;
            }

            /**
             * Append a line to the digest section of the Inbox notification.
             */
            public InboxStyle addLine(CharSequence cs) {
                mTexts.add(Builder.limitCharSequenceLength(cs));
                return this;
            }
        }
    }

  //Straight copy from parse as its the only way to get access to the classes to set IMPL properly - just adds setting number on the notiification
  @TargetApi(16)
  static class NotificationCompatPostJellyBean implements NotificationCompat.NotificationCompatImpl {
    private android.app.Notification.Builder postJellyBeanBuilder;

    NotificationCompatPostJellyBean() {
    }

    public Notification build(NotificationCompat.Builder b) {
      this.postJellyBeanBuilder = new android.app.Notification.Builder(b.mContext);
      this.postJellyBeanBuilder.setContentTitle(b.mContentTitle).setContentText(b.mContentText).setSmallIcon(b.mNotification.icon, b.mNotification.iconLevel).setContentIntent(b.mContentIntent).setDeleteIntent(b.mNotification.deleteIntent).setAutoCancel((b.mNotification.flags & 16) != 0).setLargeIcon(b.mLargeIcon).setDefaults(b.mNotification.defaults).setNumber(b.mNotification.number);

      if(b.mStyle != null) {
          if (b.mStyle instanceof ForgeNotificationCompat.Builder.BigTextStyle) {
              NotificationCompat.Builder.BigTextStyle bigTextStyle = (NotificationCompat.Builder.BigTextStyle) b.mStyle;
              android.app.Notification.BigTextStyle style = (new android.app.Notification.BigTextStyle(this.postJellyBeanBuilder)).setBigContentTitle(bigTextStyle.mBigContentTitle).bigText(bigTextStyle.mBigText);
              if (bigTextStyle.mSummaryTextSet) {
                  style.setSummaryText(bigTextStyle.mSummaryText);
              }
          } else if (b.mStyle instanceof ForgeNotificationCompat.Builder.InboxStyle) {
              ForgeNotificationCompat.Builder.InboxStyle inboxStyle = (ForgeNotificationCompat.Builder.InboxStyle) b.mStyle;
              android.app.Notification.InboxStyle style = (new android.app.Notification.InboxStyle(this.postJellyBeanBuilder)).setBigContentTitle(inboxStyle.mBigContentTitle);
              if (inboxStyle.mSummaryTextSet) {
                  style.setSummaryText(inboxStyle.mSummaryText);
              }
              for (CharSequence text: inboxStyle.mTexts) {
                  style.addLine(text);
              }
          }
      }

      return this.postJellyBeanBuilder.build();
    }
  }

  static class NotificationCompatImplBase implements NotificationCompat.NotificationCompatImpl {
    NotificationCompatImplBase() {
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public Notification build(NotificationCompat.Builder b) {
    	Notification result = new Notification.Builder(b.mContext)
        	.setContentTitle(b.mContentTitle)
        	.setContentText(b.mContentText)
        	.setContentIntent(b.mContentIntent)
        	.build();
      if (b.mPriority > 0) {
    	  result.flags |= 128;
      }

      return result;
    }
  }

}
