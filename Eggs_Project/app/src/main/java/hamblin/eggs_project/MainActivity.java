package hamblin.eggs_project;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    // USE CONSTANTS, NOT EGGS > 6, BUT EGGS > MIN_EGGS_FOR_BREAKFAST
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void launchEggIntent(View view) {
        switch(view.getId()) {
            case R.id.button_add:
                Toast.makeText(this, "Added One Egg", Toast.LENGTH_SHORT).show();
                break;
            case R.id.button_add_two:
                Toast.makeText(this, "Added Two Eggs", Toast.LENGTH_SHORT).show();
                break;
            case R.id.button_subtract:
                Toast.makeText(this, "Subtracted One Egg", Toast.LENGTH_SHORT).show();
                break;
            case R.id.button_make:
                Toast.makeText(this, "Made Breakfast", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }
}
