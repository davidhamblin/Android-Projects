package graduate.aws_project;

import android.os.AsyncTask;
import android.widget.Toast;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class SaveTask extends AsyncTask<String, Void, Boolean> {

    private MainActivity myActivity;

    // 5 second timeout
    private static final int TIMEOUT = 5000;
    private String USERNAME;
    private String PASSWORD;
    private String FILE_NAME;
    private JSONObject FILE_CONTENTS;
    private int PORT;


    SaveTask(MainActivity activity, String username, String password, String file_name, int port, JSONObject file_contents) {
        attach(activity);
        USERNAME = username;
        PASSWORD = password;
        FILE_NAME = file_name;
        FILE_CONTENTS = file_contents;
        PORT = port;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        String server = params[0];
        try{
            JSch jsch = new JSch();
            Session session = jsch.getSession(USERNAME, server, PORT);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(PASSWORD);
            session.connect(TIMEOUT);
            Channel channel = session.openChannel("sftp");
            ChannelSftp sftp = (ChannelSftp) channel;
            sftp.connect(TIMEOUT);
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
            Toast.makeText(myActivity, "Failed to push " + FILE_NAME + ".json to Amazon address", Toast.LENGTH_LONG).show();
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

