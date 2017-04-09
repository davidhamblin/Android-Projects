package hamblin.eggs_project;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void addOneEgg(View view) {
        Toast.makeText(this, "Added One Egg", Toast.LENGTH_SHORT).show();
    }

    public void addTwoEggs(View view) {
        Toast.makeText(this, "Added Two Eggs", Toast.LENGTH_SHORT).show();
    }

    public void subtractOneEgg(View view) {
        Toast.makeText(this, "Subtracted One Egg", Toast.LENGTH_SHORT).show();
    }

    public void makeBreakfast(View view) {
        Toast.makeText(this, "Made Breakfast", Toast.LENGTH_SHORT).show();
    }
}
