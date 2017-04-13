package hamblin.eggs_project;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static int egg_count = Constants.MIN_EGG_COUNT;

    public static void setEggCount(int eggCount) {
        egg_count = eggCount;
    }

    public static int getEggCount() {
        return egg_count;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

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
            case R.id.button_reset:
                Toast.makeText(this, "Service Stopped", Toast.LENGTH_SHORT).show();
                myIntent.putExtra("stop", true);
                break;
            default:
                break;
        }
        sendBroadcast(myIntent);
    }
}
