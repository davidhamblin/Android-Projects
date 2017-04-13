package hamblin.eggs_project;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class EggReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
//        Toast.makeText(context, "In Broadcast Receiver", Toast.LENGTH_SHORT).show();
        Intent myIntent = new Intent(context, EggService.class);

        boolean stop = intent.getBooleanExtra("stop", false);
        if(stop) {
            context.stopService(myIntent);
            return;
        }

        myIntent.putExtra("eggs", intent.getIntExtra("eggs", Constants.MIN_EGG_COUNT));
        myIntent.putExtra("breakfast", intent.getBooleanExtra("breakfast", false));
        context.startService(myIntent);
    }
}
