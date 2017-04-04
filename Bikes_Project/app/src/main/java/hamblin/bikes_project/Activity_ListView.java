package hamblin.bikes_project;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
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
import android.widget.Toast;

import java.util.List;

public class Activity_ListView extends AppCompatActivity {
	ListView my_listview;
    SharedPreferences myPreference;
    SharedPreferences.OnSharedPreferenceChangeListener listener;
    CustomAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        // Ask for permissions once the app first launches, so there are no crashes later
        // Necessary in Android 6.0 and above, with runtime permission checking :(
        String[] permissionList = {Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE};
        ActivityCompat.requestPermissions(this, permissionList, 123);

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

        // Sets up the preference listener for changing download site
        myPreference = PreferenceManager.getDefaultSharedPreferences(this);
        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                if (key.equals("json_list"))
                    connectAndLoadList(prefs.getString(key, ""));
            }
        };

        myPreference.registerOnSharedPreferenceChangeListener(listener);

        // Refreshes the listview with the default site in preferences on startup
        refreshList();
	}

    /**
     * Try and connect to the server selected in Settings to download the image list
     */
    private void connectAndLoadList(String address) {
        // No address value attached to key
        if(address.isEmpty()) {
            Log.e("connectAndLoad", "No address listed");
            Toast.makeText(this, "No address attached to the selected list item", Toast.LENGTH_LONG).show();
            return;
        }
        Toast.makeText(this, "Loading: " + address, Toast.LENGTH_SHORT).show();
        DownloadTask task = new DownloadTask(this);
        task.execute(address);
    }

    /**
     * Creates the OnClickListener for the ListView
     * Opens an alert dialog with pertinent information for the bike, beyond the Model, Price, Description info
     */
	private void setupListViewOnClickListener() {
        my_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Activity_ListView.this);
                builder.setMessage(parent.getItemAtPosition(position).toString());
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
	}

	/**
	 * Takes the string of bikes, parses it using JSONHelper
	 * Sets the adapter with this list using a custom row layout and an instance of the CustomAdapter
	 * binds the adapter to the ListView using setAdapter
	 *
	 * @param JSONString  complete string of all bikes
	 */
	private void bindData(String JSONString) {
        Log.d("bindData", JSONString);
        List<BikeData> bikesList = JSONHelper.parseAll(JSONString);
        this.adapter = new CustomAdapter(this, R.layout.listview_row_layout, bikesList);
        my_listview.setAdapter(adapter);
    }

	Spinner spinner;
	/**
	 * create a data adapter to fill above spinner with choices(Company,Location and Price),
	 * bind it to the spinner
	 * Also create a OnItemSelectedListener for this spinner so
	 * when a user clicks the spinner the list of bikes is resorted according to selection
	 */
	private void setupSimpleSpinner() {
        spinner = (Spinner) findViewById(R.id.spinner);

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.sortable_fields, R.layout.spinner_item);
        spinner.setVisibility(View.VISIBLE);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            static final int SELECTED_ITEM = 0;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (parent.getChildAt(SELECTED_ITEM) != null) {
                    // Sorts according to selection using Comparator
                    if(Activity_ListView.this.adapter != null)
                        Activity_ListView.this.adapter.sortList(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
	}

    /**
     * Gives life to the menu, using design and items from menu.xml
     * @param menu Menu to inflate, typically the toolbar set
     * @return true, the menu has been inflated
     */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

    /**
     * Once an item is selected from the menu, execute a specific method or activity
     * @param item Item selected by the user
     * @return true, the action has been taken
     */
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
                refreshList();
                break;
			default:
				break;
		}
		return true;
	}

    /**
     * Resets the ListView to nothing, and the spinner to default
     * Checks for network connectivity
     * and then loads the ListView with the selected list in preferences
     */
    private void refreshList() {
        // Clears the ListView and resets spinner
        my_listview.setAdapter(null);
        spinner.setSelection(0);

        // Checks for network connectivity, then downloads the list of bikes
        if(ConnectivityCheck.isNetworkReachableAlertUserIfNot(this))
            connectAndLoadList(myPreference.getString("json_list", ""));
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
        builder.setIcon(R.drawable.david);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Method to extract the JSONString from the finished DownloadTask and use it in this activity
     * @param JSONData String extracted from the online path defined in Settings
     */
    public synchronized void setJSONData(String JSONData) {
        bindData(JSONData);
    }
}
