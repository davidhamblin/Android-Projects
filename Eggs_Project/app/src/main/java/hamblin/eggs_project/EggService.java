package hamblin.eggs_project;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

public class EggService extends Service {

    private int egg_count = MainActivity.getEggCount();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Toast.makeText(this, "In Service", Toast.LENGTH_SHORT).show();
        boolean makeBreakfast = intent.getBooleanExtra("breakfast", false);
        sendNotification(intent.getIntExtra("eggs", Constants.MIN_EGG_COUNT), makeBreakfast);
        MainActivity.setEggCount(egg_count);
        stopSelf();
        return Service.START_STICKY;
    }

    public void sendNotification(int eggs, boolean make) {
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Service.NOTIFICATION_SERVICE);
        if(make) {
            String msg;
            if(egg_count >= Constants.MAKE_BREAKFAST_EGG_COUNT) {
                egg_count -= Constants.MAKE_BREAKFAST_EGG_COUNT;
                msg = "We are having omelets, we have " + Integer.toString(egg_count) + " eggs available";
            }
            else {
                msg = "We are having gruel, we have " + Integer.toString(egg_count) + " eggs available";
            }
            Notification noti = new Notification.Builder(this)
                    .setContentTitle("Notification")
                    .setContentText(msg)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setOngoing(false)
                    .build();
            noti.flags |= Notification.FLAG_INSISTENT;
            notificationManager.notify(egg_count, noti);
        }
        else {
            if(eggs > Constants.MIN_EGG_COUNT || (eggs < Constants.MIN_EGG_COUNT && egg_count > Constants.MIN_EGG_COUNT))
                egg_count += eggs;
            Notification noti = new Notification.Builder(this)
                    .setContentTitle("Notification")
                    .setContentText(Integer.toString(eggs) + " eggs added!")
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setOngoing(false)
                    .build();
            noti.flags |= Notification.FLAG_INSISTENT;
            notificationManager.notify(egg_count, noti);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
