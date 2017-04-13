package graduate.aws_project;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private EditText editView;
    private boolean connected;

    SharedPreferences myPreference;
    SharedPreferences.OnSharedPreferenceChangeListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editView = (EditText) findViewById(R.id.edit_text);

        // Ask for permissions once the app first launches, so there are no crashes later
        // Necessary in Android 6.0 and above, with runtime permission checking :(
        String[] permissionList = {Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.GET_ACCOUNTS};
        ActivityCompat.requestPermissions(this, permissionList, 123);

        myPreference = PreferenceManager.getDefaultSharedPreferences(this);
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
     * Checks if the device is connected to a network, either data or WiFi
     *
     * @return True if connected, false otherwise
     */
    private boolean checkForNetworkConnectivity() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm.getActiveNetworkInfo() == null || !cm.getActiveNetworkInfo().isConnectedOrConnecting()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Network Error")
                    .setMessage("No network connection detected")
                    .setPositiveButton("OK", null)
                    .show();
            return false;
        }
        return true;
    }

    private void showAbout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("David Hamblin & Jake Hayhurst")
                .setMessage("Graduate Project\nGraduate students in CPSC 575\nCool dudes overall");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Snackbar.make(findViewById(R.id.activity_main), "Thanks for reading our About!", Snackbar.LENGTH_LONG)
                        .setAction("CLOSE", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Toast.makeText(MainActivity.this, "A bit hasty aren't we?", Toast.LENGTH_SHORT).show();
                            }
                        }).show();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public JSONObject createJSONObject() {
        JSONObject textToPush = new JSONObject();
        String enteredText = editView.getText().toString();
        try {
            textToPush.put("text", enteredText);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return textToPush;
    }

    public void extractStringFromJSON(String stringToExtract) {
        try {
            JSONObject extractedString = new JSONObject(stringToExtract);
            String newText = extractedString.getString("text");
            editView.setText(newText);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("extractStringFromJSON", "Invalid JSON file");
        }
    }

    public void pushButton(View view) {
        connected = checkForNetworkConnectivity();
        final JSONObject objToWrite = createJSONObject();
        final String fileTitle = retrieveTitle();
        if(fileTitle != null && connected) {
            saveFileToAmazon(objToWrite, fileTitle);
            Toast.makeText(this, "Pushed to Amazon as " + fileTitle + ".json", Toast.LENGTH_LONG).show();
        }
    }

    private void saveFileToAmazon(JSONObject objToWrite, String fileTitle) {
        // TODO -- Amazon address and port pulled from Preferences
        // TODO -- Pumped into ASyncTask, connect to server, pull down file
        connectToAmazon();
        // Create file, save to location, disconnect
    }

    public void pullButton(View view) {
        // Connect to Drive server
        connected = checkForNetworkConnectivity();
        final String fileTitle = retrieveTitle();
        if(fileTitle != null && connected) {
            readFileFromAmazon(fileTitle);
            Toast.makeText(this, "Pulled from " + fileTitle + ".json", Toast.LENGTH_LONG).show();
        }
    }

    private void readFileFromAmazon(String fileTitle) {
        connectToAmazon();
        // Read contents of file, disconnect
    }

    private void connectToAmazon() {
        // Launch ASyncTask to access server, take from Bikes Project
    }

    private String retrieveTitle() {
        String fileTitle = myPreference.getString("filename", "");
        if(fileTitle.equals("")) {
            Toast.makeText(this, "File Name not set in Settings", Toast.LENGTH_LONG).show();
            Intent myIntent = new Intent(this, SettingsActivity.class);
            startActivity(myIntent);
            return null;
        }
        else
            return fileTitle;
    }
}
