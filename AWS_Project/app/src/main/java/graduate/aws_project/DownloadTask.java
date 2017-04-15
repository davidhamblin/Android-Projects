package graduate.aws_project;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadTask extends AsyncTask<String, Void, String> {

    private static final String TAG = "DownloadTask";
    private MainActivity myActivity;

    // 3 second timeout
    private static final int TIMEOUT = 3000;
    private String myQuery;

    /**
     * Constructor to set the filename and extension to download
     * @param activity MainActivity instance
     * @param query Filename to access on the server
     */
    DownloadTask(MainActivity activity, String query) {
        myQuery = query + ".json";
        attach(activity);
    }

    /**
     * Opens and downloads the contents of the file from the server using HTTP
     * @param params Address to access Amazon server
     * @return Contents of the file or null if there is an issue
     */
    @Override
    protected String doInBackground(String... params) {
        // site we want to connect to
        String myURL = params[0];

        try {
            URL url = new URL(myURL + myQuery);

            // this does no network IO
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // can further configure connection before getting data
            // cannot do this after connected
            connection.setRequestMethod("GET");
            connection.setReadTimeout(TIMEOUT);
            connection.setConnectTimeout(TIMEOUT);
            connection.setRequestProperty("Accept-Charset", "UTF-8");
            // this opens a connection, then sends GET & headers

            // wrap in finally so that stream bis is sure to close
            // and we disconnect the HttpURLConnection
            BufferedReader in = null;
            try {
                connection.connect();

                // lets see what we got make sure its one of
                // the 200 codes (there can be 100 of them
                // http_status / 100 != 2 does integer div any 200 code will = 2
                int statusCode = connection.getResponseCode();
                if (statusCode / 100 != 2) {
                    Log.e(TAG, "Error-connection.getResponseCode returned "
                            + Integer.toString(statusCode));
                    return null;
                }

                in = new BufferedReader(new InputStreamReader(connection.getInputStream()), 8096);

                // the following buffer will grow as needed
                String myData;
                StringBuffer sb = new StringBuffer();

                while ((myData = in.readLine()) != null) {
                    sb.append(myData);
                }
                return sb.toString();

            } finally {
                // close resource no matter what exception occurs
                if(in != null) in.close();
                connection.disconnect();
            }
        } catch (Exception exc) {
            exc.printStackTrace();
            return null;
        }
    }

    /**
     * Sets the EditText of MainActivity to the extracted string in result, or warns the user of an issue
     * @param result String contents of the downloaded JSON file, or null if there was an issue
     */
    @Override
    protected void onPostExecute(String result) {
        if(result != null) {
            myActivity.extractStringFromJSON(result);
            Toast.makeText(myActivity, "Pulled from " + myQuery, Toast.LENGTH_LONG).show();
        }
        else
            Toast.makeText(myActivity, "Could not find file " + myQuery + " or connect to server", Toast.LENGTH_LONG).show();
    }

    /*
     * (non-Javadoc)
     *
     * @see android.os.AsyncTask#onCancelled(java.lang.Object)
     */
    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    /**
     * important do not hold a reference so garbage collector can grab old
     * defunct dying activity
     */
    void detach() {
        myActivity = null;
    }

    /**
     * @param activity
     *            grab a reference to this activity, mindful of leaks
     */
    void attach(MainActivity activity) {
        this.myActivity = activity;
    }

};

