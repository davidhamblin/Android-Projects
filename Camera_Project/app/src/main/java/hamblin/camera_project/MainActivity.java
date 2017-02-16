package hamblin.camera_project;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.preference.EditTextPreference;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.net.URI;

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
                shareImage();
                break;
            default:
                break;
        }
        return true;
    }

    /** Method to share the current image background to a given source.
     *  The subject and text of the message are retrieved from the preferences.
     */
    private void shareImage() {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String subject = preferences.getString("subject", "");
        String text = preferences.getString("text", "");

        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, text);

        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    /** Method to reset the background of the Main Activity to the default Borat picture.
     * Also, the storage for the taken pictures are cleared.
     */
    public void resetBackground() {
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.main_activity_id);
        layout.setBackgroundResource(R.drawable.borat);
        // TODO: Remove storage of pictures. Use Camera_Helpers.
        ImageView taken_picture = (ImageView) findViewById(R.id.taken_picture);
        taken_picture.setImageResource(R.drawable.borat);
        taken_picture.setScaleType(ImageView.ScaleType.FIT_CENTER);
        taken_picture.setScaleType(ImageView.ScaleType.FIT_XY);
    }

    /**
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bitmap photo = (Bitmap) data.getExtras().get("data");
        // TODO: Replace pulling bitmap from data to storage
        if(requestCode != 5) {
            return;
        }
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.main_activity_id);
        ImageView taken_picture = (ImageView) findViewById(R.id.taken_picture);
        taken_picture.setImageBitmap(photo);
        taken_picture.setScaleType(ImageView.ScaleType.FIT_CENTER);
        taken_picture.setScaleType(ImageView.ScaleType.FIT_XY);
    }

    /** Switch to taking a picture using the phone's camera for use in the app
     *
     * @param view The current view of the activity, in this case the ImageView of the camera
     */
    public void takePicture(View view) {
        Toast.makeText(this, "CAMERA", Toast.LENGTH_SHORT).show();
        // TODO: Find URI to save photo to, using Environment.getExternalStoragePublicDirectory().
        URI Uri = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toURI();
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri);
        startActivityForResult(intent, 5);
    }
}

