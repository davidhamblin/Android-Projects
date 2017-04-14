package hamblin.eggs_project;

import android.Manifest;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ask for permissions once the app first launches, so there are no crashes later
        // Necessary in Android 6.0 and above, with runtime permission checking :(
        String[] permissionList = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
        ActivityCompat.requestPermissions(this, permissionList, Constants.PERMISSION_REQUEST);
    }

    /**
     * onClick handler for every button, putting different extras into an Intent depending on the view
     * Launches a broadcast receiver intent with eggs to add/remove, or make breakfast.
     * @param view Button clicked in activity_main.xml
     */
    public void launchEggIntent(View view) {
        Intent myIntent = new Intent("hamblin.EGG_ACTION");
        switch(view.getId()) {
            case R.id.button_add:
                Toast.makeText(this, "Added One Egg", Toast.LENGTH_SHORT).show();
                myIntent.putExtra("eggs", Constants.ADD_ONE_EGG_COUNT);
                break;
            case R.id.button_add_two:
                Toast.makeText(this, "Added Two Eggs", Toast.LENGTH_SHORT).show();
                myIntent.putExtra("eggs", Constants.ADD_TWO_EGGS_COUNT);
                break;
            case R.id.button_subtract:
                Toast.makeText(this, "Subtracted One Egg", Toast.LENGTH_SHORT).show();
                myIntent.putExtra("eggs", Constants.SUBTRACT_EGG_COUNT);
                break;
            case R.id.button_make:
                Toast.makeText(this, "Made Breakfast", Toast.LENGTH_SHORT).show();
                myIntent.putExtra("breakfast", true);
                myIntent.putExtra("eggs", Constants.MAKE_BREAKFAST_EGG_COUNT);
                break;
            default:
                break;
        }
        sendBroadcast(myIntent);
    }
}
