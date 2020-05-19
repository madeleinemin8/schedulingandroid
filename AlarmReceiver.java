package edu.tjhsst.a2018mmin.onthedot;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
public class AlarmReceiver extends BroadcastReceiver{
    public void onReceive(Context context, Intent intent) {

        String name = intent.getStringExtra("name");
        String duration = "For " + intent.getStringExtra("duration") + " minutes";
        int id = intent.getIntExtra("id", 0);

        long when = System.currentTimeMillis();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(context, ReminderListActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //notificationIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mNotifyBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.icon)
                //.setContentTitle("TEST")
                .setContentTitle(name)
                .setContentText(duration).setSound(alarmSound)
                .setAutoCancel(true).setWhen(when)
                .setContentIntent(pendingIntent)
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        notificationManager.notify(id, mNotifyBuilder.build());
    }

}