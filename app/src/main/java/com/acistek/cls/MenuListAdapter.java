package com.acistek.cls;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by acistek on 4/17/2015.
 */
public class MenuListAdapter extends ArrayAdapter<String> {

    private final LayoutInflater _inflater;
    private final String[] item_name;
    private final Integer[] img_id;

    public MenuListAdapter(Context context, LayoutInflater inflater, String[] item_name, Integer[] img_id){
        super(context, R.layout.fragment_custom_listview, item_name);

        this._inflater = inflater;
        this.item_name = item_name;
        this.img_id = img_id;
    }

    public View getView(int position, View view, ViewGroup parent){
        LayoutInflater inflater = this._inflater;
        View rowView = inflater.inflate(R.layout.fragment_custom_listview, null, true);

        TextView txtTitle = (TextView) rowView.findViewById(R.id.menu_name);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.menu_image);

        imageView.setImageResource(img_id[position]);
        txtTitle.setText(item_name[position]);
        return rowView;
    }

}
