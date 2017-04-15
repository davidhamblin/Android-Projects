package graduate.aws_project;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
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

    SharedPreferences myPreference;

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

    /**
     * Informs the user about the two chill dudes who created this project
     */
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

    /**
     * Creates a JSON object from the EditText view, for storage on the server
     * @return JSONObject with one item as {"text": value}
     */
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

    /**
     * Takes the string form of a JSON object and extracts the value, placing in the EditText view
     * @param stringToExtract String of a JSON file
     */
    public void extractStringFromJSON(String stringToExtract) {
        try {
            JSONObject extractedString = new JSONObject(stringToExtract);
            String newText = extractedString.getString("text");
            editView.setText(newText);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Invalid JSON file", Toast.LENGTH_SHORT).show();
            Log.e("extractStringFromJSON", "Invalid JSON file");
        }
    }

    /**
     * Button listener for Push/Pull, calls specific methods for saving and loading from the Amazon server
     * @param view The button pressed in the main activity
     */
    public void pushPull(View view) {
        boolean connected = checkForNetworkConnectivity();
        final String fileTitle = retrieveTitle();
        final String address = retrieveAddress();
        final int port = retrievePort();
        if(fileTitle != null && address != null && port != 0 && connected) {
            switch (view.getId()) {
                case R.id.push_button:
                    final JSONObject objToWrite = createJSONObject();
                    saveFileToAmazon(objToWrite, fileTitle, address, port);
                    break;
                case R.id.pull_button:
                    readFileFromAmazon(fileTitle, address);
                    break;
            }
        }
    }

    /**
     * Calls an ASyncTask for connecting to and saving a file to the Amazon server
     * @param objToWrite JSONObject to write on the server
     * @param fileTitle Name of file to save
     * @param address Address of the Amazon server
     * @param port Port to access on the Amazon server (22 for FTP)
     */
    private void saveFileToAmazon(JSONObject objToWrite, String fileTitle, String address, int port) {
        final String username = retrieveUsername();
        final String password = retrievePassword();
        if(username != null && password != null)
            new SaveTask(this, username, password, fileTitle, port, objToWrite).execute(address);
    }

    /**
     * Calls an ASyncTask to connect and load a JSON file from the server
     * @param fileTitle Name of file to load
     * @param address Address of the Amazon server (HTTP connection, port is 80)
     */
    private void readFileFromAmazon(String fileTitle, String address) {
        new DownloadTask(this, fileTitle).execute("http://" + address + "/");
    }

    /**
     * Starts the Settings activity if trying to push or pull with missing information
     * @param missing Item missing information in Settings
     */
    private void openSettingsIfMissingInfo(String missing) {
        Toast.makeText(this, missing + " not set in Settings", Toast.LENGTH_LONG).show();
        Intent myIntent = new Intent(this, SettingsActivity.class);
        startActivity(myIntent);
    }

    /**
     * Returns the filename from the Settings, or opens Settings if it missing
     * @return Filename
     */
    @Nullable
    private String retrieveTitle() {
        String fileTitle = myPreference.getString("filename", "");
        if(fileTitle.isEmpty()) {
            openSettingsIfMissingInfo("File Name");
            return null;
        }
        else
            return fileTitle;
    }

    /**
     * Returns the address from the Settings, or opens Settings if it missing
     * @return Address
     */
    @Nullable
    private String retrieveAddress() {
        String address = myPreference.getString("address", "");
        if(address.isEmpty()) {
            openSettingsIfMissingInfo("Address");
            return null;
        }
        else
            return address;
    }

    /**
     * Returns the port from the Settings, or opens Settings if it missing
     * @return Port
     */
    private int retrievePort() {
        int port = Integer.valueOf(myPreference.getString("port", "0"));
        if(port == 0)
            openSettingsIfMissingInfo("Port");
        return port;
    }

    /**
     * Returns the username from the Settings, or opens Settings if it missing
     * @return Username
     */
    @Nullable
    private String retrieveUsername() {
        String username = myPreference.getString("username", "");
        if(username.isEmpty()) {
            openSettingsIfMissingInfo("Username");
            return null;
        }
        else
            return username;
    }

    /**
     * Returns the password from the Settings, or opens Settings if it missing
     * @return Password
     */
    @Nullable
    private String retrievePassword() {
        String password = myPreference.getString("password", "");
        if(password.isEmpty()) {
            openSettingsIfMissingInfo("Password");
            return null;
        }
        else
            return password;
    }
}
