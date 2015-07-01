package com.acistek.cls;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by acistek on 6/16/2015.
 */
public class BuildingInfoListAdapter extends ArrayAdapter<BuildingInfo> {

    static class ViewHolder {
        LinearLayout building_info_layout;
        TextView building_name;
        TextView address;
        TextView city;
        TextView state;
        TextView zipcode;
        TextView country;
        TextView distance;
    }

    private final ArrayList<BuildingInfo> buildingInfoArrayList;

    public BuildingInfoListAdapter(Activity context, ArrayList<BuildingInfo> buildingInfoArrayList) {
        super(context, R.layout.building_list_item, buildingInfoArrayList);
        this.buildingInfoArrayList = buildingInfoArrayList;
    }

    public View getView(int position, View view, ViewGroup parent){
        View v = view;
        ViewHolder holder;
        BuildingInfo buildingInfo = this.buildingInfoArrayList.get(position);

        if(v == null){
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.building_list_item, parent, false);
            holder = new ViewHolder();
            holder.building_name = (TextView) v.findViewById(R.id.building_name);
            holder.address = (TextView) v.findViewById(R.id.address);
            holder.city = (TextView) v.findViewById(R.id.city);
            holder.state = (TextView) v.findViewById(R.id.state);
            holder.zipcode = (TextView) v.findViewById(R.id.zipcode);
            holder.country = (TextView) v.findViewById(R.id.country);
            holder.distance = (TextView) v.findViewById(R.id.distance);
            holder.building_info_layout = (LinearLayout) v.findViewById(R.id.building_info_layout);
            v.setTag(holder);
        }
        else {
            holder = (ViewHolder) v.getTag();
        }

        if(position % 2 == 0){
            holder.building_info_layout.setBackgroundColor(Color.parseColor("#FFFFFF"));
        }
        else{
            holder.building_info_layout.setBackgroundColor(Color.parseColor("#F6F3EE"));
        }

        holder.building_name.setText(buildingInfo.getBuilding_name());
        holder.address.setText(buildingInfo.getAddress());
        holder.city.setText(buildingInfo.getCity());
        holder.state.setText(buildingInfo.getState());
        holder.zipcode.setText(buildingInfo.getZipcode());
        holder.country.setText(buildingInfo.getCountry());
        holder.distance.setText(buildingInfo.getDistance_miles());

        return v;
    }
}
