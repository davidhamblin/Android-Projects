package hamblin.graduate_project;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;
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
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.OpenFileActivityBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    GoogleApiClient mGoogleApiClient;
    final int RESOLVE_CONNECTION_REQUEST_CODE = 456;
    final int REQUEST_CODE_CREATOR = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ask for permissions once the app first launches, so there are no crashes later
        // Necessary in Android 6.0 and above, with runtime permission checking :(
        String[] permissionList = {Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        ActivityCompat.requestPermissions(this, permissionList, 123);

//        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(this, DriveScopes.DRIVE);
//        credential.setSelectedAccountName(accountName);
//        Drive service = new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential).build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mGoogleApiClient.connect();


    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, RESOLVE_CONNECTION_REQUEST_CODE);
            } catch (IntentSender.SendIntentException e) {
                // Unable to resolve, message user appropriately
            }
        } else {
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), this, 0).show();
        }
    }






    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.e("onConnected", "Hit onConnected method");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e("onConnectedSuspended", "Hit onConnectedSuspended method");
    }




//    @Override
//    public void onConnectionFailed(ConnectionResult connectionResult) {
//        if (connectionResult.hasResolution()) {
//            try {
//                connectionResult.startResolutionForResult(this, 528);
//            } catch (IntentSender.SendIntentException e) {
//                // Unable to resolve, message user appropriately
//            }
//        } else {
//            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), this, 0).show();
//        }
//    }

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
//        builder.setIcon(R.drawable.david);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void pushButton(View view) {
        boolean connected = checkForNetworkConnectivity();
        Log.e("Push", "Past Connected function");
        Drive.DriveApi.newDriveContents(getGoogleApiClient())
                .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                    @Override
                    public void onResult(DriveApi.DriveContentsResult result) {
                        if (!result.getStatus().isSuccess()) {
                            Log.e("Help", "Error with results");
                            return;
                        }
                        final DriveContents driveContents = result.getDriveContents();

                        // Perform I/O off the UI thread.
                        // write content to DriveContents
                        OutputStream outputStream = driveContents.getOutputStream();
                        Writer writer = new OutputStreamWriter(outputStream);
                        try {
                            writer.write("Hello World!");
                            writer.close();
                        } catch (IOException e) {
                            Log.e("IO exception", e.getMessage());
                        }

                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                .setTitle("New file")
                                .setMimeType("text/plain")
                                .setStarred(true).build();

                        // create a file on root folder
                        Drive.DriveApi.getRootFolder(getGoogleApiClient())
                                .createFile(getGoogleApiClient(), changeSet, driveContents)
                                .setResultCallback(fileCallback);
                    }



                });




    }

    public void pullButton(View view) {
        // Connect to Drive server
        Log.e("Help","Shit Should work");

    }

    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_CREATOR:
                if (resultCode == RESULT_OK) {
                    DriveId driveId = (DriveId) data.getParcelableExtra(
                            OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
                    Log.e("activity result","File created with ID: " + driveId);
                }
                finish();
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }



    final private ResultCallback<DriveFolder.DriveFileResult> fileCallback = new
            ResultCallback<DriveFolder.DriveFileResult>() {
                @Override
                public void onResult(DriveFolder.DriveFileResult result) {
                    if (!result.getStatus().isSuccess()) {
                        Log.e("not success","Error while trying to create the file");
                        return;
                    }
                    Log.d("Created file","Created a file with content: " + result.getDriveFile().getDriveId());
                }
            };



}
