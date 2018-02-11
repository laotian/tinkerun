package com.tinkerun.debug;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.tinkerun.R;

/**
 * Created by tianlupan on 2018/2/11.
 */

public class NotificationUtil {
    private static final int NOTIFICATION_ID=0x129;
    private static final int ACTIVITY_REQUEST_CODE=0x233;


    public static void showNotification(Context context, String notificationText){
        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(context);
        PendingIntent contentIntent = PendingIntent.getActivities(context, 0,
                new Intent[]{new Intent(context, LogActivity.class)}, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent)
                .setSmallIcon(R.mipmap.ic_launcher)         //设置状态栏里面的图标（小图标）
//              .setLargeIcon(BitmapFactory.decodeResource(resource, R.mipmap.ic_launcher)) //下拉下拉列表里面的图标（大图标）
                .setTicker("Tinkerun-"+notificationText)                //设置状态栏的显示的信息
                .setWhen(System.currentTimeMillis())        //设置时间发生时间
                .setAutoCancel(false)                        //设置可以清除
                .setContentTitle("Tinkerun")    //设置下拉列表里的标题
                .setContentText(notificationText);     //设置上下文内容
        Notification notification = builder.getNotification();
        manager.notify(1, notification);
    }
}
