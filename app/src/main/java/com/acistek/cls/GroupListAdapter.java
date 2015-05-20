package com.acistek.cls;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by acistek on 5/7/2015.
 */
public class GroupListAdapter extends ArrayAdapter<String> {

    Activity context;
    ArrayList<String> groupList;
    int resource;

    static class ViewHolder {
        TextView groupName;
    }

    public GroupListAdapter(Activity context, int resource, ArrayList<String> groupList) {
        super(context, resource, groupList);
        this.context = context;
        this.groupList = groupList;
        this.resource = resource;
    }

    public View getView(int position, View v, ViewGroup parent){
        ViewHolder holder;
        String gname = this.groupList.get(position);

        LayoutInflater inflater = context.getLayoutInflater();
        if(v == null){
            v = inflater.inflate(resource, null);
            holder = new ViewHolder();
            holder.groupName = (TextView) v.findViewById(R.id.favorite_group_list);
            v.setTag(holder);
        }
        else{
            holder = (ViewHolder) v.getTag();
        }

        holder.groupName.setText(gname);

        if(position == ProfileActivity.group_counter){
            v.setBackgroundColor(Color.parseColor("#33B5E5"));
        }
        else{
            v.setBackgroundColor(Color.WHITE);
        }

        return v;
    }

}
