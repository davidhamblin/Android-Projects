package hamblin.bikes_project;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lynn
 *         static helper class for accessing JSON data
 *         <p/>
 *         see:http://prativas.wordpress.com/category/android/part-1-retrieving-a-json-file/
 */
public class JSONHelper {
    private static final int NUMB_PROJECTS_UNDEFINED = -1;
    private static final String BIKES = "Bikes";
    private static final String TAG = "JSONHelper";
    private static final String COMPANY = "Company";
    private static final String MODEL = "Model";
    private static final String PRICE = "Price";
    private static final String LOCATION = "Location";
    private static final String DATE = "Date";
    private static final String DESCRIPTION = "Description";
    private static final String PICTURE = "Picture";
    private static final String LINK = "Link";

    private JSONHelper() {
    }

    /**
     * @param jsonString
     * @return List<UserData>
     * takes a json string and parses it into json objects
     * You must know what is in the data and what to parse out of it
     */
    public static List<BikeData> parseAll(String jsonString) {
        //list of all the bikes
        List<BikeData> myList = new ArrayList<BikeData>();

        try {
            JSONObject jAll = new JSONObject(jsonString);

            //get the raw array of bikes
            JSONArray jo = (JSONArray) jAll.get(BIKES);
            //UserData mydata = new UserData.Builder(first,last).addProject(proj1).addProject(proj2).build()

            //how many bikes?
            int numbBikes = jo.length();
            Log.d(TAG, "Number of bikes=" + Integer.toString(numbBikes));

            for (int i = 0; i < numbBikes; ++i) {
                JSONObject rec = jo.getJSONObject(i);

                //first add the mandatory fields
                BikeData.Builder myDataBuilder = new BikeData.Builder(rec.getString(COMPANY), rec.getString(MODEL), rec.getDouble(PRICE))
                        .setLocation(rec.getString(LOCATION))
                        .setDate(rec.getString(DATE))
                        .setDescription(rec.getString(DESCRIPTION))
                        .setPicture(rec.getString(PICTURE))
                        .setLink(rec.getString(LINK));
                myList.add(myDataBuilder.build());
            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return myList;
    }
}
