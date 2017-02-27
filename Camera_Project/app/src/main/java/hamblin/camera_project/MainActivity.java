package hamblin.camera_project;


import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.library.bitmap_utilities.BitMap_Helpers;

import java.io.File;
import java.io.IOException;
import java.net.URI;

public class MainActivity extends AppCompatActivity  {

    private ImageView backgroundImage;
    private Uri picUri;
    private int screenHeight, screenWidth;
    private Bitmap bmpImage;
    private int cameraAngle = 90;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Retrieve the screen dimensions for scaling images
        DisplayMetrics metrics = this.getResources().getDisplayMetrics();
        screenHeight = metrics.heightPixels;
        screenWidth = metrics.widthPixels;

        // Set the initial URI path of a taken picture.
        backgroundImage = (ImageView) findViewById(R.id.taken_picture);
        String file_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath() + Constants.full_file;
        File loaded_file = new File(file_path);
        picUri = Uri.fromFile(loaded_file);

        // If there is a picture saved, load it in when opening the app
        if(loaded_file.exists()) {
            bmpImage = Camera_Helpers.loadAndScaleImage(picUri.getPath(), screenHeight, screenWidth, cameraAngle);
            setBackground(false);
        }
        else {
            Log.e(Constants.IO_TAG, "File not loaded");
            setBackground(true);
        }

        // Create the toolbar and remove the project name from the bar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setTitle("");
        setSupportActionBar(myToolbar);

        // Ask for permissions once the app first launches, so there are no crashes later
        // Necessary in Android 6.0 and above, with runtime permission checking :(
        String[] permissionList = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        ActivityCompat.requestPermissions(this, permissionList, Constants.PERMISSION_REQUEST);
    }

    /**
     * A method to set the background image of the main view.
     * @param def True if loading the default image, false if loading a taken picture
     */
    private void setBackground(boolean def) {
        if(def)
            backgroundImage.setImageResource(R.drawable.borat);
        else
            backgroundImage.setImageBitmap(bmpImage);
        backgroundImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
        backgroundImage.setScaleType(ImageView.ScaleType.FIT_XY);
        reloadBitmap();
    }

    /**
     * Reload the current image into the global bitmap image
     */
    private void reloadBitmap() {
        bmpImage = BitMap_Helpers.copyBitmap(backgroundImage.getDrawable());
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
                bwPicture();
                break;
            case R.id.colorize_action:
                Toast.makeText(this, "COLORIZE", Toast.LENGTH_SHORT).show();
                colorizePicture();
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

    /**
     * Convert the current ImageView to a colorized picture using bitmap_utilities
     */
    private void colorizePicture() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int colorThreshold = preferences.getInt("saturation_value", 50);
        int bwThreshold = preferences.getInt("bw_value", 50);
        Bitmap colorImage = BitMap_Helpers.colorBmp(bmpImage, colorThreshold);
        Bitmap bwImage = BitMap_Helpers.thresholdBmp(bmpImage, bwThreshold);
        BitMap_Helpers.merge(colorImage, bwImage);
        Camera_Helpers.saveProcessedImage(colorImage, picUri.getPath());
        backgroundImage.setImageBitmap(colorImage);
    }

    /**
     * Convert the current ImageView to black and white using bitmap_utilities
     */
    private void bwPicture() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int threshold = preferences.getInt("bw_value", 50);
        Bitmap bwImage = BitMap_Helpers.thresholdBmp(bmpImage, threshold);
        Camera_Helpers.saveProcessedImage(bwImage, picUri.getPath());
        backgroundImage.setImageBitmap(bwImage);
    }

    /**
     * Method to share the current image background to a given source.
     * The subject and text of the message are retrieved from the preferences.
     */
    private void shareImage() {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("image/bmp");

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String subject = preferences.getString("subject", "");
        String text = preferences.getString("text", "");

        reloadBitmap();
        setBackground(false);
        Camera_Helpers.saveProcessedImage(bmpImage, picUri.getPath());

        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, text);
        sharingIntent.putExtra(Intent.EXTRA_STREAM, picUri);

        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    /**
     * Method to reset the background of the Main Activity to the default Borat picture.
     * Also, the storage for the taken pictures are cleared.
     */
    public void resetBackground() {
        // Change the image back to Borat
        setBackground(true);

        // Once the image is reset, delete the file.
        try {
            Camera_Helpers.delSavedImage(picUri.getPath());
        }
        catch(Exception e) {
            Log.e(Constants.IO_TAG, "Cannot delete file");
        }
    }

    /**
     * Takes the picture result, and loads it into the ImageView of the app.
     * @param requestCode A code designating what result is being returned.
     * @param resultCode Whether the result was okay or canceled
     * @param data The intent that is being returned, with any data it attained
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == Constants.TAKE_PICTURE && resultCode == RESULT_OK) {
            // Depending on the picture's orientation after it is taken, determine rotation angle
            // Required when switching between the front and back camera, as the images are flipped
            try {
                ExifInterface ei = new ExifInterface(picUri.getPath());
                int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        cameraAngle = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        cameraAngle = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        cameraAngle = 270;
                        break;
                    default:
                        break;
                }
            } catch(IOException e) { Log.e(Constants.IO_TAG, "File not found"); }
            Log.d(Constants.IO_TAG, "Picture at angle: " + String.valueOf(cameraAngle));
            bmpImage = Camera_Helpers.loadAndScaleImage(picUri.getPath(), screenHeight, screenWidth, cameraAngle);
            Camera_Helpers.saveProcessedImage(bmpImage, picUri.getPath());
            setBackground(false);
        }
        else if(requestCode == Constants.TAKE_PICTURE && resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "Canceled Picture", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Switch to taking a picture using the phone's camera for use in the app
     * @param view The current view of the activity, in this case the ImageView of the camera
     */
    public void takePicture(View view) {
        Toast.makeText(this, "CAMERA", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, picUri);
        startActivityForResult(intent, Constants.TAKE_PICTURE);
    }
}

