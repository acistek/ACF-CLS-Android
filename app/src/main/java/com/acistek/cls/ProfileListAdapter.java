package com.acistek.cls;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by acistek on 4/28/2015.
 */
public class ProfileListAdapter extends ArrayAdapter<UserProfile>{

    static class ViewHolder {
        LinearLayout layout;
        LinearLayout headerLayout;
        LinearLayout infoLayout;
        TextView bighead;
        TextView head;
        TextView data;
    }

    private final ArrayList<UserProfile> userProfile;

    public ProfileListAdapter(Activity context, ArrayList<UserProfile> userProfile) {
        super(context, R.layout.user_profile_listview, userProfile);
        this.userProfile = userProfile;
    }

    public boolean areAllItemsEnabled(){
        return false;
    }

    public boolean isEnabled(int position){
        UserProfile user = this.userProfile.get(position);

        if(user.isHeader() || user.getTitle().equalsIgnoreCase("contactlistid"))
            return false;
        else
            return true;
    }

    public View getView(int position, View view, ViewGroup parent){
        View v = view;
        ViewHolder holder;
        UserProfile user = this.userProfile.get(position);

        if(v == null){
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.user_profile_listview, parent, false);
            holder = new ViewHolder();
            holder.layout = (LinearLayout) v.findViewById(R.id.profile_list_layout);
            holder.headerLayout = (LinearLayout) v.findViewById(R.id.profile_header_layout);
            holder.infoLayout = (LinearLayout) v.findViewById(R.id.profile_info_layout);
            holder.bighead = (TextView) v.findViewById(R.id.profile_big_header);
            holder.head = (TextView) v.findViewById(R.id.profile_sub_header);
            holder.data = (TextView) v.findViewById(R.id.profile_sub_data);
            v.setTag(holder);
        }
        else {
            holder = (ViewHolder) v.getTag();
        }

        if(user.isHeader()){
            holder.infoLayout.setVisibility(View.GONE);
            holder.headerLayout.setVisibility(View.VISIBLE);
            holder.bighead.setText(user.getDescription());
        }
        else{
            holder.headerLayout.setVisibility(View.GONE);

            if(user.getTitle().equalsIgnoreCase("contactlistid"))
                holder.infoLayout.setVisibility(View.INVISIBLE);
            else
                holder.infoLayout.setVisibility(View.VISIBLE);

            if(position == ProfileActivity.counter){
                holder.layout.setBackgroundColor(Color.parseColor("#E6E2E2"));
            }
            else if(position % 2 == 0){
                holder.layout.setBackgroundColor(Color.parseColor("#FFFFFF"));
            }
            else{
                holder.layout.setBackgroundColor(Color.parseColor("#F6F3EE"));
            }

            holder.head.setText(user.getTitle());
            holder.data.setText(user.getDescription());
        }
        return v;
    }


}
