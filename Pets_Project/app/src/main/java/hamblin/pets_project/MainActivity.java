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
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    List<Pet> listPets;
    Spinner spinner;
    SharedPreferences myPreference;
    SharedPreferences.OnSharedPreferenceChangeListener listener;
    String petAddress;
    ImageView backgroundImage;
    int screenHeight, screenWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // TODO -- TELL USER SERVER STATUS CODE, NOT JUST FILENOTFOUNDEXCEPTION
        checkForNetworkConnectivity();

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
        connectAndLoadList(myPreference.getString("json_list", ""));

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setTitle("");
        setSupportActionBar(myToolbar);
    }

    private void checkForNetworkConnectivity() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if(cm.getActiveNetworkInfo() == null || !cm.getActiveNetworkInfo().isConnectedOrConnecting()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Network Error")
                    .setMessage("No network connection detected")
                    .setPositiveButton("OK", null)
                    .show();
        }
    }

    /**
     * Try and connect to the server selected in Settings to download the image list
     */
    private void connectAndLoadList(String address) {
        // No address value attached to key
        if(address.isEmpty()) {
            Log.e("connectAndLoad", "No address listed");
            return;
        }
        petAddress = address;
        Toast.makeText(this, "Loading: " + address, Toast.LENGTH_SHORT).show();
        DownloadList task = new DownloadList();
        task.execute(address);
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
//                Toast.makeText(MainActivity.this, "Thanks for reading my About!", Toast.LENGTH_SHORT).show();
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
     */
    private void setupSpinner(List<Pet> pets) {
        List<String> petNames = new ArrayList<>();
        for(Pet p : pets) {
            petNames.add(p.name);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item_simple, petNames);
        spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setVisibility(View.VISIBLE);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            static final int SELECTED_ITEM = 0;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(parent.getChildAt(SELECTED_ITEM) != null) {
                    ((TextView) parent.getChildAt(SELECTED_ITEM)).setTextColor(Color.WHITE);
                    Toast.makeText(MainActivity.this, (String) parent.getItemAtPosition(position), Toast.LENGTH_SHORT).show();
                    changeBackground(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    /**
     * Method to change the background image to the one selected by the Spinner
     */
    private void changeBackground(int index) {
        String fileToGrab = listPets.get(index).file;
        DownloadFile task = new DownloadFile();
        task.execute(petAddress + fileToGrab);
    }

    private class DownloadList extends AsyncTask<String, Void, List<Pet>> {
        String address;
        List<Pet> pets;

        @Override
        public List<Pet> doInBackground(String... params) {
            address = params[0] + "pets.json";
            Log.d("IO", address);
            try {
                URL url = new URL(address);
                URLConnection connection = url.openConnection();
                connection.connect();
                InputStream input = new BufferedInputStream(url.openStream(), 8192);
                JsonReader reader = new JsonReader(new InputStreamReader(input, "UTF-8"));
//                reader.setLenient(true);
                pets = new ArrayList<>();
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
                if(e instanceof FileNotFoundException)
                    this.cancel(true);
            }
            return null;
        }

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
                listPets = pets;
                setupSpinner(listPets);
            }
        }

        @Override
        protected void onCancelled() {
            // Only called with a FileNotFoundException, telling us that there was a 404 error
            Toast.makeText(MainActivity.this, "ERROR when connecting to:\n" + address + "\nServer returned 404", Toast.LENGTH_LONG).show();
            super.onCancelled();
        }
    }

    private class DownloadFile extends AsyncTask<String, Void, Bitmap> {
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

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            backgroundImage.setImageBitmap(Bitmap.createScaledBitmap(bitmap, screenWidth, screenHeight, false));
            backgroundImage.setScaleType(ImageView.ScaleType.FIT_XY);
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
