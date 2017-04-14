package hamblin.eggs_project;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.Random;

public class EggService extends Service {

    private int egg_count = Constants.MIN_EGG_COUNT;

    /**
     * Retrieves the current egg count from an external file
     * Sends appropriate notification depending on intent extras
     * Sets the new egg count to an external file, and stops self
     * @param intent Intent with extras received from Receiver
     * @param flags Conditions for service
     * @param startId ID of the service
     * @return Terminate the service and wait for explicit call to start again
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean makeBreakfast = intent.getBooleanExtra("breakfast", false);
        egg_count = getEggCount();
        sendNotification(intent.getIntExtra("eggs", Constants.MIN_EGG_COUNT), makeBreakfast);
        setEggCount(egg_count);
        stopSelf();
        return Service.START_NOT_STICKY;
    }

    /**
     * Retrieves the current egg count from the external documents directory file defined in Constants
     * @return Current egg count
     */
    private int getEggCount() {
        String file_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getPath() + Constants.FILENAME;
        File file = new File(file_path);
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while((line = reader.readLine()) != null)
                text.append(line);
            reader.close();
            if(text.toString().isEmpty())
                throw new IOException();
        }
        catch(IOException e) {
            Log.e("getEggCount", "File not found");
            setEggCount(Constants.MIN_EGG_COUNT);
        }
        try {
            return Integer.valueOf(text.toString());
        }
        catch(NumberFormatException e) {
            setEggCount(Constants.MIN_EGG_COUNT);
        }
        return Constants.MIN_EGG_COUNT;
    }

    /**
     * Writes a new file containing the current egg count to the external documents directory
     * @param newEggCount Integer to write to file
     */
    private void setEggCount(int newEggCount) {
        String file_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getPath() + Constants.FILENAME;
        File file = new File(file_path);

        try {
            try (BufferedWriter out = new BufferedWriter(new FileWriter(file))) {
                out.write(Integer.toString(newEggCount));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            Log.e("IO", "Problem with creating new file");
        }
    }

    /**
     * Sends a notification to the device, depending on number of eggs and making breakfast
     * Random used to generate random ID for notification, ensuring unique notifications
     * @param eggs Number of eggs to add or subtract
     * @param make If breakfast is being made or not
     */
    public void sendNotification(int eggs, boolean make) {
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Service.NOTIFICATION_SERVICE);
        Random r = new Random();
        if(make) {
            String msg;
            if(egg_count >= Constants.MAKE_BREAKFAST_EGG_COUNT) {
                egg_count -= Constants.MAKE_BREAKFAST_EGG_COUNT;
                msg = "We are having omelets, we have " + Integer.toString(egg_count) + " eggs available";
            }
            else {
                msg = "We are having gruel, we have " + Integer.toString(egg_count) + " eggs available";
            }
            Notification n = new Notification.Builder(this)
                    .setContentTitle("Notification")
                    .setContentText(msg)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setOngoing(false)
                    .build();
            n.flags |= Notification.FLAG_INSISTENT;
            notificationManager.notify((egg_count+r.nextInt()) * r.nextInt(Constants.RANDOM_RANGE), n);
        }
        else {
            String msg;
            if(eggs > Constants.MIN_EGG_COUNT || (eggs < Constants.MIN_EGG_COUNT && egg_count > Constants.MIN_EGG_COUNT)) {
                egg_count += eggs;
                msg = Integer.toString(eggs) + " egg(s) added!";
            }
            else {
                msg = "No eggs to remove!";
            }
            Notification n = new Notification.Builder(this)
                    .setContentTitle("Notification")
                    .setContentText(msg)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setOngoing(false)
                    .build();
            n.flags |= Notification.FLAG_INSISTENT;
            notificationManager.notify((egg_count+r.nextInt()) * r.nextInt(Constants.RANDOM_RANGE), n);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
