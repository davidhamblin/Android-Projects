package hamblin.graduate_project;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.DriveApi.DriveIdResult;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;

import android.support.annotation.NonNull;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;

import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.OpenFileActivityBuilder;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private EditText editView;
    private boolean connected;

    SharedPreferences myPreference;
    SharedPreferences.OnSharedPreferenceChangeListener listener;

    private static final String TAG = "drive-quickstart";
    private static final int REQUEST_CODE_CAPTURE_IMAGE = 1;
    private static final int REQUEST_CODE_CREATOR = 2;
    private static final int REQUEST_CODE_RESOLUTION = 3;

    private GoogleApiClient mGoogleApiClient;

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
            case R.id.drive_account:
                changeDriveAccount();
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
        if(fileTitle != null && connected && mGoogleApiClient.isConnected()) {
            saveFileToDrive(objToWrite, fileTitle);
            Toast.makeText(this, "Pushed to Google Drive as " + fileTitle + ".json", Toast.LENGTH_LONG).show();
        }
    }

    public void pullButton(View view) {
        // Connect to Drive server
        connected = checkForNetworkConnectivity();
        final String fileTitle = retrieveTitle();
        if(fileTitle != null && connected && mGoogleApiClient.isConnected()) {
            readFileFromDrive(fileTitle);
            Toast.makeText(this, "Pulled from " + fileTitle + ".json", Toast.LENGTH_LONG).show();
        }
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

    private void changeDriveAccount() {
        Log.e("Preferences", "Clicked Drive Account");
        // TODO -- Code required here for changing the current Drive account from Settings
        mGoogleApiClient.clearDefaultAccountAndReconnect();
    }

    public void readFileFromDrive(final String fileTitle) {
        Query query = new Query.Builder().addFilter(Filters.and(
                Filters.eq(SearchableField.MIME_TYPE, "application/json"),
                Filters.eq(SearchableField.TITLE, fileTitle + ".json"))).build();
        Drive.DriveApi.query(mGoogleApiClient,query).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
            @Override
            public void onResult(@NonNull DriveApi.MetadataBufferResult metadataBufferResult) {
                for (Metadata buffer : metadataBufferResult.getMetadataBuffer()){
                    new RetrieveDriveFileContentsAsyncTask(
                            MainActivity.this).execute(buffer.getDriveId());
                    break;
                }
            }
        });
    }

    final private ResultCallback<DriveIdResult> idCallback = new ResultCallback<DriveIdResult>() {
        @Override
        public void onResult(DriveIdResult result) {
            new RetrieveDriveFileContentsAsyncTask(
                    MainActivity.this).execute(result.getDriveId());
        }
    };

    private void saveFileToDrive(final JSONObject objToWrite, final String fileTitle) {
        // Start by creating a new contents, and setting a callback.
        Log.i(TAG, "Creating new contents.");
        Drive.DriveApi.newDriveContents(mGoogleApiClient)
                .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {

                    @Override
                    public void onResult(DriveApi.DriveContentsResult result) {

                        // If the operation was not successful, we cannot do anything
                        // and must fail.
                        if (!result.getStatus().isSuccess()) {
                            Log.i(TAG, "Failed to create new contents.");
                            return;
                        }
                        // Otherwise, we can write our data to the new contents.
                        Log.i(TAG, "New contents created.");
                        // Get an output stream for the contents.
                        OutputStream outputStream = result.getDriveContents().getOutputStream();
                        try {
                            outputStream.write(objToWrite.toString().getBytes());
                        } catch (IOException e1) {
                            Log.i(TAG, "Unable to write file contents.");
                        }
                        // Create the initial metadata - MIME type and title.
                        // Note that the user will be able to change the title later.
                        MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                                .setMimeType("application/json").setTitle(fileTitle + ".json").build();
                        Query query = new Query.Builder().addFilter(Filters.and(
                                Filters.eq(SearchableField.MIME_TYPE, "application/json"),
                                Filters.eq(SearchableField.TITLE, fileTitle + ".json"))).build();
                        Drive.DriveApi.query(mGoogleApiClient,query).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
                            @Override
                            public void onResult(@NonNull DriveApi.MetadataBufferResult metadataBufferResult) {
                                for(Metadata buffer : metadataBufferResult.getMetadataBuffer()) {
                                    if(buffer.isTrashable())
                                        buffer.getDriveId().asDriveFile().trash(mGoogleApiClient);
                                }
                            }
                        });
                        Drive.DriveApi.getRootFolder(mGoogleApiClient)
                                .createFile(mGoogleApiClient, metadataChangeSet, result.getDriveContents());
                        // Create an intent for the file chooser, and start it.
//                        IntentSender intentSender = Drive.DriveApi
//                                .newCreateFileActivityBuilder()
//                                .setInitialMetadata(metadataChangeSet)
//                                .setInitialDriveContents(result.getDriveContents())
//                                .build(mGoogleApiClient);
//                        try {
//                            startIntentSenderForResult(
//                                    intentSender, REQUEST_CODE_CREATOR, null, 0, 0, 0);
//                        } catch (IntentSender.SendIntentException e) {
//                            Log.i(TAG, "Failed to launch file chooser.");
//                        }
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient == null) {
            // Create the API client and bind it to an instance variable.
            // We use this instance as the callback for connection and connection
            // failures.
            // Since no account name is passed, the user is prompted to choose.
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        // Connect the client. Once connected, the camera is launched.
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onPause();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_CREATOR:
                // Called after a file is saved to Drive.
                if (resultCode == RESULT_OK) {
                    Log.i(TAG, "JSON successfully saved.");
                }
                break;
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Called whenever the API client fails to connect.
        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(this, result.getErrorCode(), 0).show();
            return;
        }
        // The failure has a resolution. Resolve it.
        // Called typically when the app is not yet authorized, and an
        // authorization
        // dialog is displayed to the user.
        try {
            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "API client connected.");
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "GoogleApiClient connection suspended");
    }

    final private class RetrieveDriveFileContentsAsyncTask
            extends ApiClientAsyncTask<DriveId, Boolean, String> {

        public RetrieveDriveFileContentsAsyncTask(Context context) {
            super(context);
        }

        @Override
        protected String doInBackgroundConnected(DriveId... params) {
            String contents = null;
            DriveFile file = params[0].asDriveFile();
            DriveContentsResult driveContentsResult =
                    file.open(getGoogleApiClient(), DriveFile.MODE_READ_ONLY, new DriveFile.DownloadProgressListener() {
                        @Override
                        public void onProgress(long l, long l1) {

                        }
                    }).await();
            if (!driveContentsResult.getStatus().isSuccess()) {
                return null;
            }
            DriveContents driveContents = driveContentsResult.getDriveContents();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(driveContents.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                contents = builder.toString();
            } catch (IOException e) {
                Log.e(TAG, "IOException while reading from the stream", e);
            }

            driveContents.discard(getGoogleApiClient());
            return contents;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result == null) {
                Log.e("onPostExecute","Error while reading from the file");
                return;
            }
            extractStringFromJSON(result);
        }
    }
    public abstract class ApiClientAsyncTask<Params, Progress, Result>
            extends AsyncTask<Params, Progress, Result> {

        private GoogleApiClient mClient;

        public ApiClientAsyncTask(Context context) {
            GoogleApiClient.Builder builder = new GoogleApiClient.Builder(context)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE);
            mClient = builder.build();
        }

        @Override
        protected final Result doInBackground(Params... params) {
            Log.d("TAG", "in background");
            final CountDownLatch latch = new CountDownLatch(1);
            mClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                @Override
                public void onConnectionSuspended(int cause) {
                }

                @Override
                public void onConnected(Bundle arg0) {
                    latch.countDown();
                }
            });
            mClient.registerConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                @Override
                public void onConnectionFailed(ConnectionResult arg0) {
                    latch.countDown();
                }
            });
            mClient.connect();
            try {
                latch.await();
            } catch (InterruptedException e) {
                return null;
            }
            if (!mClient.isConnected()) {
                return null;
            }
            try {
                return doInBackgroundConnected(params);
            } finally {
                mClient.disconnect();
            }
        }

        /**
         * Override this method to perform a computation on a background thread, while the client is
         * connected.
         */
        protected abstract Result doInBackgroundConnected(Params... params);

        /**
         * Gets the GoogleApliClient owned by this async task.
         */
        protected GoogleApiClient getGoogleApiClient() {
            return mClient;
        }
    }


}
