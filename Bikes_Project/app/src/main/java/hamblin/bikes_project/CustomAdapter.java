package hamblin.bikes_project;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by David on 4/1/2017.
 */

public class CustomAdapter extends BaseAdapter {
    private static final 	int COMPANY = 0;
    private static final 	int MODEL = 1;
    private static final 	int PRICE = 2;
    private static final 	int LOCATION = 3;
    private 				List<BikeData> bikeList;
    private 				Activity activity;
    private 				int layoutId;
    private LayoutInflater inflater;

    public CustomAdapter(Activity activity, int layoutId, List<BikeData> bikes) {
        this.activity = activity;
        this.layoutId = layoutId;
        this.bikeList = bikes;

        if (activity != null)
            this.inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void sortList(int sort_style) {
        switch(sort_style) {
            case COMPANY:
                Collections.sort(bikeList, new Comparator<BikeData>() {
                    @Override
                    public int compare(BikeData o1, BikeData o2) {
                        return o1.COMPANY.compareTo(o2.COMPANY);
                    }
                });
                break;
            case MODEL:
                Collections.sort(bikeList, new Comparator<BikeData>() {
                    @Override
                    public int compare(BikeData o1, BikeData o2) {
                        return o1.MODEL.compareTo(o2.MODEL);
                    }
                });
                break;
            case PRICE:
                Collections.sort(bikeList, new Comparator<BikeData>() {
                    @Override
                    public int compare(BikeData o1, BikeData o2) {
                        return Double.compare(o1.PRICE, o2.PRICE);
                    }
                });
                break;
            case LOCATION:
                Collections.sort(bikeList, new Comparator<BikeData>() {
                    @Override
                    public int compare(BikeData o1, BikeData o2) {
                        return o1.LOCATION.compareTo(o2.LOCATION);
                    }
                });
                break;
            default:
                break;
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return (bikeList != null) ? bikeList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return (bikeList != null) ? bikeList.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        if (convertView == null) {
            view = inflater.inflate(layoutId, parent, false);
        } else {
            view = convertView;
        }

        // set the model
        TextView modelView = (TextView) view.findViewById(R.id.Model);
        modelView.setText(bikeList.get(position).MODEL);

        // set the price
        TextView priceView = (TextView) view.findViewById(R.id.Price);
        priceView.setText(Double.toString(bikeList.get(position).PRICE));

        // set the description
        TextView descView = (TextView) view.findViewById(R.id.Description);
        descView.setText(bikeList.get(position).DESCRIPTION);

        return view;
    }
}
