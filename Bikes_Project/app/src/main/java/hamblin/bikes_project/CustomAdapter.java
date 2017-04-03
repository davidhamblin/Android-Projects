package hamblin.bikes_project;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

class CustomAdapter extends BaseAdapter {
    private static final 	int COMPANY = 0;
    private static final 	int MODEL = 1;
    private static final 	int PRICE = 2;
    private static final 	int LOCATION = 3;
    private 				List<BikeData> bikeList;
    private 				Activity activity;
    private 				int layoutId;
    private LayoutInflater inflater;

    CustomAdapter(Activity activity, int layoutId, List<BikeData> bikes) {
        this.activity = activity;
        this.layoutId = layoutId;
        this.bikeList = bikes;

        if (activity != null)
            this.inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    void sortList(int sort_style) {
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

    static class ViewHolder {
        TextView modelView, priceView, descView;
        ImageView bikeImage;
        int viewPos;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder myVh;

        if (convertView == null) {
            convertView = inflater.inflate(layoutId, parent, false);

            myVh = new ViewHolder();
            myVh.bikeImage = (ImageView) convertView.findViewById(R.id.imageView1);
            myVh.modelView = (TextView) convertView.findViewById(R.id.Model);
            myVh.priceView = (TextView) convertView.findViewById(R.id.Price);
            myVh.descView = (TextView) convertView.findViewById(R.id.Description);
            convertView.setTag(myVh);
        }

        myVh = (ViewHolder)convertView.getTag();

        // set the model, price, and description
        myVh.modelView.setText(bikeList.get(position).MODEL);
        myVh.priceView.setText(String.format(Locale.US, "%.2f", bikeList.get(position).PRICE));
        myVh.descView.setText(bikeList.get(position).DESCRIPTION);

        myVh.viewPos = position;

        String address = PreferenceManager.getDefaultSharedPreferences(activity).getString("json_list", "");

        new DownloadImageTask(bikeList.get(position).PICTURE, myVh.bikeImage, myVh).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, address);

        return convertView;
    }
}
