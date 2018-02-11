package com.example.laotian.myapplication;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.tinkerun.debug.LogActivity;
import com.tinkerun.debug.NotificationUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends Activity {

    @BindView(R.id.tvName)
    TextView tvName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
//        showN(this,"what???");
    }

    private void showN(Context context,String notificationText){
//        NotificationManager manager =
//                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        Notification.Builder builder = new Notification.Builder(context);
//        PendingIntent contentIntent = PendingIntent.getActivities(context, 0,
//                new Intent[]{new Intent(context, LogActivity.class)}, PendingIntent.FLAG_UPDATE_CURRENT);
//        builder.setContentIntent(contentIntent)
//                .setSmallIcon(R.mipmap.ic_launcher)         //设置状态栏里面的图标（小图标）
////              .setLargeIcon(BitmapFactory.decodeResource(resource, R.mipmap.ic_launcher)) //下拉下拉列表里面的图标（大图标）
////                .setTicker("This is bitch.")                //设置状态栏的显示的信息
//                .setWhen(System.currentTimeMillis())        //设置时间发生时间
//                .setAutoCancel(false)                        //设置可以清除
//                .setContentTitle("Tinkerun")    //设置下拉列表里的标题
//                .setContentText(notificationText);     //设置上下文内容
//        Notification notification = builder.getNotification();
//        manager.notify(1, notification);

    }

    public void show(View view){
        tvName.setText("hello,,you2223446622aa");
       int a=100;
        Toast.makeText(this,getResources().getText(R.string.zy),Toast.LENGTH_LONG).show();
    }

}
