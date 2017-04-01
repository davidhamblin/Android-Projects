package hamblin.bikes_project;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class Activity_ListView extends AppCompatActivity {
	ListView my_listview;
    SharedPreferences myPreference;
    SharedPreferences.OnSharedPreferenceChangeListener listener;
    String JSONOutput;
    CustomAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Change title to indicate sort by
		setTitle("Sort by:");

		//listview that you will operate on
		my_listview = (ListView)findViewById(R.id.lv);

		//toolbar
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		setupSimpleSpinner();

		//set the listview onclick listener
		setupListViewOnClickListener();

		//TODO call a thread to get the JSON list of bikes
		//TODO when it returns it should process this data with bindData
        myPreference = PreferenceManager.getDefaultSharedPreferences(this);
        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                if (key.equals("json_list"))
                    connectAndLoadList(prefs.getString(key, ""));
            }
        };

        myPreference.registerOnSharedPreferenceChangeListener(listener);

        if(ConnectivityCheck.isNetworkReachableAlertUserIfNot(this))
            connectAndLoadList(myPreference.getString("json_list", ""));
	}

    private void connectAndLoadList(String address) {
        // No address value attached to key
        if(address.isEmpty()) {
            Log.e("connectAndLoad", "No address listed");
            Toast.makeText(this, "No address attached to the selected list item", Toast.LENGTH_LONG).show();
            return;
        }
//        petAddress = address;
        Toast.makeText(this, "Loading: " + address, Toast.LENGTH_SHORT).show();
        DownloadTask task = new DownloadTask(this);
        task.execute(address);
    }

	private void setupListViewOnClickListener() {
		//TODO you want to call my_listviews setOnItemClickListener with a new instance of android.widget.AdapterView.OnItemClickListener() {
        my_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
	}

	/**
	 * Takes the string of bikes, parses it using JSONHelper
	 * Sets the adapter with this list using a custom row layout and an instance of the CustomAdapter
	 * binds the adapter to the Listview using setAdapter
	 *
	 * @param JSONString  complete string of all bikes
	 */
	private synchronized void bindData(String JSONString) {
        Toast.makeText(this, "Loaded List", Toast.LENGTH_SHORT).show();
        Log.d("bindData", JSONString);
        List<BikeData> bikesList = JSONHelper.parseAll(JSONString);
        int counter = 0;
        List<String> bikeNames = new ArrayList<>();
        for(BikeData b : bikesList) {
            bikeNames.add(b.toString());
            Log.d("bindData", "" + counter++);
        }
        this.adapter = new CustomAdapter(this, R.layout.listview_row_layout, bikesList);
        my_listview.setAdapter(adapter);
    }

	Spinner spinner;
	/**
	 * create a data adapter to fill above spinner with choices(Company,Location and Price),
	 * bind it to the spinner
	 * Also create a OnItemSelectedListener for this spinner so
	 * when a user clicks the spinner the list of bikes is resorted according to selection
	 * dontforget to bind the listener to the spinner with setOnItemSelectedListener!
	 */
	private void setupSimpleSpinner() {
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        List<String> bikeNames = new ArrayList<>();

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.sortable_fields, R.layout.spinner_item);
        spinner.setVisibility(View.VISIBLE);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            static final int SELECTED_ITEM = 0;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (parent.getChildAt(SELECTED_ITEM) != null) {
                    // Sort according to selection. Use comparators.
                    if(Activity_ListView.this.adapter != null)
                        Activity_ListView.this.adapter.sortList(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_settings:
                Intent myIntent = new Intent(this, activityPreference.class);
                startActivity(myIntent);
                break;
            case R.id.action_about:
                showAbout();
                break;
            case R.id.action_refresh:
                break;
			default:
				break;
		}
		return true;
	}

    /**
     * Shows personal/project information in an About dialog opened from the overflow menu
     */
    private void showAbout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("David Hamblin")
                .setMessage("Project 4, Bikes Project\nGraduate student in CPSC 575\nRad dude overall");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Snackbar.make(findViewById(R.id.activity_main), "Thanks for reading my About!", Snackbar.LENGTH_LONG)
                        .setAction("CLOSE", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Toast.makeText(Activity_ListView.this, "A bit hasty aren't we?", Toast.LENGTH_SHORT).show();
                            }
                        }).show();
            }
        });
//        builder.setIcon(R.drawable.david);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public synchronized void setJSONData(String JSONData) {
        this.JSONOutput = JSONData;
        bindData(JSONData);
    }
}
