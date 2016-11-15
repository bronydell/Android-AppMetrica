package ru.equestriadev.appmetrica.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import ru.equestriadev.appmetrica.R;
import ru.equestriadev.appmetrica.model.Application;

/**
 * Created by Bronydell on 11/12/16.
 */

public class AppsAdapter extends ArrayAdapter<Application>{

    private ArrayList<Application> dataSet;
    Context mContext;

    public void clearAll(){
        dataSet.clear();
    }

    // View lookup cache
    private static class ViewHolder {
        TextView appName;
        TextView appPermission;
    }

    public AppsAdapter(ArrayList<Application> data, Context context) {
        super(context, R.layout.apps_row, data);
        this.dataSet = data;
        this.mContext=context;

    }

    private int lastPosition = -1;


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Application application = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.apps_row, parent, false);
            viewHolder.appName = (TextView) convertView.findViewById(R.id.textAppName);
            viewHolder.appPermission = (TextView) convertView.findViewById(R.id.textPermission);

            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

        lastPosition = position;

        viewHolder.appName.setText(application.getName());
        viewHolder.appPermission.setText(application.getPermission(mContext));
        // Return the completed view to render on screen
        return convertView;
    }
}
