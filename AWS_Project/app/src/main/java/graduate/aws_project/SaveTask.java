package graduate.aws_project;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPSClient;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * @author david
 *
 */
public class SaveTask extends AsyncTask<String, Void, Boolean> {

    private static final String TAG = "DownloadTask";
    private static final int BUFFER_SIZE = 8096;
    private MainActivity myActivity;

    // 3 second timeout
    private static final int TIMEOUT = 3000;
    private String myQuery = "bikes.json";
    private String USERNAME;
    private String PASSWORD;
    private String FILE_NAME;
    private JSONObject FILE_CONTENTS;
    private int PORT:


    SaveTask(MainActivity activity,String username, String password, String file_name, int port, JSONObject file_contents) {
        attach(activity);
        USERNAME = username;
        PASSWORD = password;
        FILE_NAME = file_name;
        FILE_CONTENTS = file_contents;
        PORT = port;
    }

    //
    /**
     * @param name
     * @param value
     * @return this allows you to build a safe URL with all spaces and illegal
     *         characters URLEncoded usage mytask.setnameValuePair("param1",
     *         "value1").setnameValuePair("param2",
     *         "value2").setnameValuePair("param3", "value3")....
     */
    public SaveTask setnameValuePair(String name, String value) {
        try {
            if (name.length() != 0 && value.length() != 0) {

                // if 1st pair that include ? otherwise use the joiner char &
                if (myQuery.length() == 0)
                    myQuery += "?";
                else
                    myQuery += "&";

                myQuery += name + "=" + URLEncoder.encode(value, "utf-8");
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return this;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        String server = params[0];
        try{
            JSch jsch = new JSch();
            Session session = jsch.getSession(USERNAME, server, PORT);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(PASSWORD);
            session.connect();
            Channel channel = session.openChannel("sftp");
            ChannelSftp sftp = (ChannelSftp) channel;
            sftp.connect();
            sftp.cd("/var/www/html/");

            InputStream stream = new ByteArrayInputStream(FILE_CONTENTS.toString().getBytes(StandardCharsets.UTF_8));

            sftp.put(stream,FILE_NAME+".json");

            stream.close();
            sftp.disconnect();
            sftp.exit();


        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;

    }

    @Override
    protected void onPostExecute(Boolean result) {
        if(result)
            Toast.makeText(myActivity, "Successfully pushed to Amazon as " + FILE_NAME + ".json", Toast.LENGTH_LONG).show();
        else
            Toast.makeText(myActivity, "Failed to push to Amazon as " + FILE_NAME + ".json", Toast.LENGTH_LONG).show();
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

