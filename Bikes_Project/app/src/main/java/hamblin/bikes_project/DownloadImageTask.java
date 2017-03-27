package hamblin.bikes_project;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    private static final String TAG = "DownloadTask";
    private static final int DEFAULTBUFFERSIZE = 50;
    private static final int NODATA = -1;

    private String pictureID;
    private ImageView imageView;
    private Drawable placeholder;

    public DownloadImageTask(String pictureID, ImageView imageView) {
        this.pictureID = pictureID;
        this.imageView = imageView;
        Resources resources = imageView.getContext().getResources();
        this.placeholder = resources.getDrawable(R.drawable.generic);
    }

    @Override
    protected void onPreExecute() {
        imageView.setImageDrawable(placeholder);
    }

    @Override
    protected Bitmap doInBackground(String... params) {

        // site we want to connect to
        String url = params[0];
        Log.d(TAG, "URL is " + url);

        // note streams are left willy-nilly here because it declutters the
        // example
        try {
            URL url1 = new URL(url);

            // this does no network IO
            HttpURLConnection connection = (HttpURLConnection) url1.openConnection();

            // can further configure connection before getting data
            // cannot do this after connected
            // connection.setRequestMethod("GET");
            connection.setReadTimeout(500);
            connection.setConnectTimeout(500);

            // this opens a connection, then sends GET & headers
            connection.connect();

            // lets see what we got make sure its one of
            // the 200 codes (there can be 100 of them
            // http_status / 100 != 2 does integer div any 200 code will = 2
            int statusCode = connection.getResponseCode();
            if (statusCode / 100 != 2) {
                Log.e(TAG, "Error-connection.getResponseCode returned " + Integer.toString(statusCode));
                return null;
            }

            // get our streams, a more concise implementation is
            // BufferedInputStream bis = new
            // BufferedInputStream(connection.getInputStream());
            InputStream is = connection.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);

            // the following buffer will grow as needed
            ByteArrayOutputStream baf = new ByteArrayOutputStream(DEFAULTBUFFERSIZE);
            int current = 0;

            // wrap in finally so that stream bis is sure to close
            try {
                while ((current = bis.read()) != NODATA) {
                    baf.write((byte) current);
                }

                // convert to a bitmap
                byte[] imageData = baf.toByteArray();
                return BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
            } finally {
                // close resource no matter what exception occurs
                bis.close();
            }
        } catch (IOException exc) {
            if (exc != null)
                Log.e(TAG, " " + exc.getMessage());
            return null;
        }
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        //TODO Your Stuff Here
    }
}
