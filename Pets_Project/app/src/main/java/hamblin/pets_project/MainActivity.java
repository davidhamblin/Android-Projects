package hamblin.pets_project;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.JsonReader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    SharedPreferences myPreference;
    SharedPreferences.OnSharedPreferenceChangeListener listener;
    String petAddress;
    int screenHeight, screenWidth;

    // Needs to be thread safe
    volatile ImageView backgroundImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boolean connected = checkForNetworkConnectivity();

        // Retrieve the screen dimensions for scaling images
        DisplayMetrics metrics = this.getResources().getDisplayMetrics();
        screenHeight = metrics.heightPixels;
        screenWidth = metrics.widthPixels;

        // Ask for permissions once the app first launches, so there are no crashes later
        // Necessary in Android 6.0 and above, with runtime permission checking :(
        String[] permissionList = {Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE};
        ActivityCompat.requestPermissions(this, permissionList, 123);

        backgroundImage = (ImageView) findViewById(R.id.background_image);

        myPreference = PreferenceManager.getDefaultSharedPreferences(this);
        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                if (key.equals("json_list"))
                    connectAndLoadList(prefs.getString(key, ""));
            }
        };

        myPreference.registerOnSharedPreferenceChangeListener(listener);

        if(connected)
            connectAndLoadList(myPreference.getString("json_list", ""));

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setTitle("");
        setSupportActionBar(myToolbar);
    }

    /**
     * Checks if the device is connected to a network, either data or WiFi
     * @return True if connected, false otherwise
     */
    private boolean checkForNetworkConnectivity() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if(cm.getActiveNetworkInfo() == null || !cm.getActiveNetworkInfo().isConnectedOrConnecting()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Network Error")
                    .setMessage("No network connection detected")
                    .setPositiveButton("OK", null)
                    .show();
            return false;
        }
        return true;
    }

    /**
     * Try and connect to the server selected in Settings to download the image list
     */
    private void connectAndLoadList(String address) {
        // No address value attached to key
        if(address.isEmpty()) {
            Log.e("connectAndLoad", "No address listed");
            Toast.makeText(this, "No address attached to the selected list item", Toast.LENGTH_LONG).show();
            return;
        }
        petAddress = address;
        Toast.makeText(this, "Loading: " + address, Toast.LENGTH_SHORT).show();
        DownloadList task = new DownloadList();
        task.execute(address);
    }

    /**
     * Method to set the activity's menu to the custom menu main.xml
     * @param menu The menu of the activity
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * Listener for selection of choices in the overflow menu
     * @param item An option within the overflow menu
     * @return true
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent myIntent = new Intent(this, SettingsActivity.class);
                startActivity(myIntent);
                break;
            case R.id.action_about:
                showAbout();
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * Shows personal/project information in an About dialog opened from the overflow menu
     */
    private void showAbout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("David Hamblin")
                .setMessage("Project 3, Pets Project\nGraduate student in CPSC 575\nCool dude overall");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Snackbar.make(findViewById(R.id.activity_main), "Thanks for reading my About!", Snackbar.LENGTH_LONG)
                        .setAction("CLOSE", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Toast.makeText(MainActivity.this, "A bit hasty aren't we?", Toast.LENGTH_SHORT).show();
                            }
                        }).show();
            }
        });
        builder.setIcon(R.drawable.david);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Creates a Spinner in the title bar to select an ImageView to display
     * Removes the Spinner and background image if the parameter is null
     */
    private synchronized void setupSpinner(List<Pet> pets) {
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        if(pets != null) {
            List<String> petNames = new ArrayList<>();
            for (Pet p : pets) {
                petNames.add(p.name);
            }
            final List<Pet> finalPets = pets;

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item_simple, petNames);
            spinner.setVisibility(View.VISIBLE);
            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                static final int SELECTED_ITEM = 0;

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (parent.getChildAt(SELECTED_ITEM) != null) {
                        ((TextView) parent.getChildAt(SELECTED_ITEM)).setTextColor(Color.WHITE);
                        Toast.makeText(MainActivity.this, (String) parent.getItemAtPosition(position), Toast.LENGTH_SHORT).show();
                        changeBackground(position, finalPets);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }
        // If the List object is null, no website was loaded and remove the spinner/background
        else {
            spinner.setVisibility(View.INVISIBLE);
            backgroundImage.setImageResource(0);
        }
    }

    /**
     * Method to change the background image to the one selected by the Spinner
     */
    private synchronized void changeBackground(int index, List<Pet> pets) {
        String fileToGrab = pets.get(index).file;
        DownloadFile task = new DownloadFile();
        task.execute(petAddress + fileToGrab);
    }

    private class DownloadList extends AsyncTask<String, Void, List<Pet>> {
        String address;
        HttpURLConnection HttpURL;

        /**
         * The method to be executed on the AsyncTask thread, making a connection to the address and downloading the list of pets
         * @param params The address a connection is made to
         * @return The list of pets gathered from the address
         */
        @Override
        public List<Pet> doInBackground(String... params) {
            address = params[0] + "pets.json";
            Log.d("IO", address);
            try {
                URL url = new URL(address);
                URLConnection connection = url.openConnection();
                HttpURL = (HttpURLConnection) connection;
                HttpURL.connect();
                Log.d("IO", "" + HttpURL.getResponseCode());
                InputStream input = new BufferedInputStream(url.openStream(), 8192);
                JsonReader reader = new JsonReader(new InputStreamReader(input, "UTF-8"));
                List<Pet> pets = new ArrayList<>();

                // Begin iterating over the JSON object
                reader.beginObject();
                while(reader.hasNext()) {
                    if(reader.nextName().equals("pets")) {
                        reader.beginArray();
                        while(reader.hasNext()) {
                            pets.add(readPet(reader));
                        }
                        reader.endArray();
                    }
                    else { reader.skipValue(); }
                }
                reader.endObject();
                reader.close();
                return pets;
            } catch (Exception e) {
                Log.e("IO", "Error: " + e);
                if(e instanceof FileNotFoundException || e instanceof IOException)
                    this.cancel(true);
            }
            return null;
        }

        /**
         * Parses the given JsonReader by object, looking for the specific layout of a pets object
         * @param reader The reader to be parsed by JSONObject
         * @return A pet with a name and file extracted from the reader
         * @throws IOException The reader may not be able to access a new object if formatted incorrectly
         */
        private Pet readPet(JsonReader reader) throws IOException {
            String name = null;
            String file = null;
            reader.beginObject();
            while(reader.hasNext()) {
                String id = reader.nextName();
                switch(id) {
                    case "name":
                        name = reader.nextString();
                        break;
                    case "file":
                        file = reader.nextString();
                        break;
                    default:
                        reader.skipValue();
                }
            }
            reader.endObject();
            return new Pet(name, file);
        }

        /**
         * Called if a successful connection is made to the address
         * @param pets List of pets gathered from the address, or null if there is something wrong with the file
         */
        @Override
        public void onPostExecute(List<Pet> pets) {
            // If a file was found, but not in the proper JSON format
            if(pets == null || pets.isEmpty()) {
                Toast.makeText(MainActivity.this, "ERROR: There is something wrong with the file at\n" + address, Toast.LENGTH_LONG).show();
            }
            else {
                for (Pet p : pets) {
                    Log.d("onPostExecute", "Name: " + p.name + ", File: " + p.file);
                }
            }
            setupSpinner(pets);
        }

        /**
         * Method called when the thread is cancelled due to a problem with connecting to the given address
         */
        @Override
        protected void onCancelled() {
            // Tell the user what response code was returned from the system, or a generic error message if there is an exception
            try {
                Toast.makeText(MainActivity.this, "ERROR when connecting to:\n" + address + "\nServer returned " + HttpURL.getResponseCode(), Toast.LENGTH_LONG).show();
            }
            catch(Exception e) {
                Toast.makeText(MainActivity.this, "ERROR when connecting to:\n" + address + "\nServer returned an error", Toast.LENGTH_LONG).show();
            }
            super.onCancelled();
            setupSpinner(null);
        }
    }

    private class DownloadFile extends AsyncTask<String, Void, Bitmap> {
        /**
         * The method to be executed on the AsyncTask thread, making a connection to the address and downloading the specific pet
         * @param params The address a connection is made to
         * @return A Bitmap of the image from the address
         */
        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                Log.d("changeBackground", params[0]);
                URL url = new URL(params[0]);
                URLConnection connection = url.openConnection();
                connection.connect();
                InputStream input = connection.getInputStream();
                return BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                Log.e("IO", "Error: " + e);
            }
            return null;
        }

        /**
         * Sets the background image of the MainActivity to the loaded image
         * @param bitmap The bitmap to set to the background image
         */
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            // If there was an error in loading the file
            if(bitmap == null) {
                Toast.makeText(MainActivity.this, "Could not load image file", Toast.LENGTH_SHORT).show();
            }
            else {
                backgroundImage.setImageBitmap(Bitmap.createScaledBitmap(bitmap, screenWidth, screenHeight, false));
                backgroundImage.setScaleType(ImageView.ScaleType.FIT_XY);
            }
        }
    }

    private class Pet {
        public String name, file;
        Pet(String n, String f) {
            this.name = n;
            this.file = f;
        }
    }
}
