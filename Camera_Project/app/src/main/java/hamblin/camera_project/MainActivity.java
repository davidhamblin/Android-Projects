package hamblin.camera_project;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity  {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setTitle("");
        setSupportActionBar(myToolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent myIntent = new Intent(this, SettingsActivity.class);
                startActivity(myIntent);
                break;
            case R.id.reset_action:
                Toast.makeText(this, "RESET", Toast.LENGTH_SHORT).show();
                resetBackground();
                break;
            case R.id.bw_action:
                Toast.makeText(this, "BW", Toast.LENGTH_SHORT).show();
                break;
            case R.id.colorize_action:
                Toast.makeText(this, "COLORIZE", Toast.LENGTH_SHORT).show();
                break;
            case R.id.share_action:
                Toast.makeText(this, "SHARE", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
        return true;
    }

    /** Method to reset the background of the Main Activity to the default Borat picture.
     * Also, the storage for the taken pictures are cleared.
     */
    public void resetBackground() {
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.main_activity_id);
        layout.setBackgroundResource(R.drawable.borat);
        // TODO: Remove storage of pictures
    }

    /** Switch to taking a picture using the phone's camera for use in the app
     *
     * @param view The current view of the activity, in this case the ImageView of the camera
     */
    public void takePicture(View view) {
        // Switch to picture mode to capture an image.
        // Change the layout background to the picture.
        Toast.makeText(this, "CAMERA", Toast.LENGTH_SHORT).show();
    }
}

