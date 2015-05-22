package com.acistek.cls;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by acistek on 4/17/2015.
 */
public class SearchListAdapter extends ArrayAdapter<UserSearch> {

    private final ArrayList<UserSearch> userSearch;

    public SearchListAdapter(Activity context, ArrayList<UserSearch> userSearch){
        super(context, R.layout.search_user_listview, userSearch);

        this.userSearch = userSearch;
    }

    public View getView(int position, View view, ViewGroup parent){
        View v = view;

        if(v == null){
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.search_user_listview, null, true);
        }

        LinearLayout layout = (LinearLayout) v.findViewById(R.id.search_list_layout);
        TextView userName = (TextView) v.findViewById(R.id.search_list_name);
        TextView userEmail = (TextView) v.findViewById(R.id.search_list_email);
        TextView userID = (TextView) v.findViewById(R.id.search_list_id);

        if(position == SearchActivity.counter)
            layout.setBackgroundColor(Color.parseColor("#E6E2E2"));
        else if(position % 2 == 0)
            layout.setBackgroundColor(Color.parseColor("#FFFFFF"));
        else
            layout.setBackgroundColor(Color.parseColor("#F6F3EE"));

        UserSearch user = userSearch.get(position);

        userName.setText(user.getFirstname() + " " + user.getLastname());
        userEmail.setText(user.getEmail());
        userID.setText(user.getContactlistid());

        return v;
    }

}
