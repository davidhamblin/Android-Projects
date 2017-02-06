package hamblin.testapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class DisplayMessageActivity extends AppCompatActivity {

    private TextView myTV;
    private String ms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);

        myTV = (TextView)findViewById(R.id.tvResult);

        int n1, n2 = Constants.UNDEFINED;

        Intent myIntent = getIntent();
        n1 = myIntent.getIntExtra(Constants.NUMBER1, Constants.UNDEFINED);
        n2 = myIntent.getIntExtra(Constants.NUMBER2, Constants.UNDEFINED);

        ms = Integer.toString(n1) + " + " + Integer.toString(n2) + " = " + Integer.toString(n1+n2);

        myTV.setText(ms);
    }

    public void doShare(View view) {
        String text = myTV.getText().toString();

        Intent myIntent = new Intent(Intent.ACTION_SEND);
        myIntent.setType("text/plain");
        myIntent.putExtra(Intent.EXTRA_EMAIL, Constants.EMAIL);
        myIntent.putExtra(Intent.EXTRA_SUBJECT, Constants.SUBJECT);
        myIntent.putExtra(Intent.EXTRA_TEXT, "Did you know that " + text + " ???");

        startActivity(myIntent);
    }
}
