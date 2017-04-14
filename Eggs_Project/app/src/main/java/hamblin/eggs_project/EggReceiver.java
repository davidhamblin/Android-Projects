package hamblin.eggs_project;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class EggReceiver extends BroadcastReceiver {

    /**
     * Receives MainActivity intent, with eggs and a boolean for making breakfast
     * Starts a service with the extras from MainActivity
     * @param context MainActivity
     * @param intent An intent from MainActivity with eggs/breakfast information
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent myIntent = new Intent(context, EggService.class);

        myIntent.putExtra("eggs", intent.getIntExtra("eggs", Constants.MIN_EGG_COUNT));
        myIntent.putExtra("breakfast", intent.getBooleanExtra("breakfast", false));
        context.startService(myIntent);
    }
}
