package hamblin.testapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

public class MainActivity extends AppCompatActivity {

    private TextView tv1, tv2;
    private int n1, n2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv1 = (TextView)findViewById(R.id.numb1);
        tv2 = (TextView)findViewById(R.id.numb2);
    }

    public void loadFromPrefs() {
        SharedPreferences myPrefs = getSharedPreferences(Constants.FILENAME,MODE_PRIVATE);

        n1 = myPrefs.getInt(Constants.NUMBER1, Constants.UNDEFINED);
        n2 = myPrefs.getInt(Constants.NUMBER2, Constants.UNDEFINED);

        tv1.setText(Integer.toString(n1));
        tv2.setText(Integer.toString(n2));
    }

    public void saveToPrefs(int n1, int n2) {
        SharedPreferences myPrefs = getSharedPreferences(Constants.FILENAME,MODE_PRIVATE);
        SharedPreferences.Editor myEditor = myPrefs.edit();

        //save values
        myEditor.putInt(Constants.NUMBER1,n1);
        myEditor.putInt(Constants.NUMBER2,n2);

        //commit values
        myEditor.commit();
    }

    public void doAdd(View view) {
        String s1 = tv1.getText().toString().trim();
        String s2 = tv2.getText().toString().trim();

        if (s1.isEmpty() || s2.isEmpty()) {
            Toast.makeText(this, "PLease enter some numbers.", Toast.LENGTH_SHORT).show();
            tv1.requestFocus();
            return;
        }

        try {
            n1 = Integer.parseInt(s1);
            n2 = Integer.parseInt(s2);

        }catch(Exception e) {
            Toast.makeText(this, "Please enter NUMBERS only.", Toast.LENGTH_SHORT).show();
            tv1.requestFocus();
            return;
        }

        Intent myIntent = new Intent(this, DisplayMessageActivity.class);
        myIntent.putExtra(Constants.NUMBER1, n1);
        myIntent.putExtra(Constants.NUMBER2, n2);
        startActivity(myIntent);
    }
}
