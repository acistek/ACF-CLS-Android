package com.acistek.cls;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by greed on 7/1/2015.
 */
public class MenuItemListAdapter extends ArrayAdapter<MenuItem> {

    static class ViewHolder {
        LinearLayout layout;
        LinearLayout menu_header_layout;
        LinearLayout menu_item_layout;
        TextView menu_header_text;
        TextView menu_item_text;
        ImageView menu_icon;
    }

    private final ArrayList<MenuItem> items;

    public MenuItemListAdapter(Context context, ArrayList<MenuItem> items) {
        super(context, R.layout.menu_listview, items);
        this.items = items;
    }

    public boolean areAllItemsEnabled(){
        return false;
    }

    public boolean isEnabled(int position){
        MenuItem item = this.items.get(position);

        if(item.isHeader())
            return false;
        else
            return true;
    }

    public View getView(int position, View view, ViewGroup parent){
        View v = view;
        ViewHolder holder;
        MenuItem item = this.items.get(position);

        if(v == null){
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.menu_listview, parent, false);
            holder = new ViewHolder();
            holder.layout = (LinearLayout) v.findViewById(R.id.menu_list_layout);
            holder.menu_header_layout = (LinearLayout) v.findViewById(R.id.menu_header_layout);
            holder.menu_item_layout = (LinearLayout) v.findViewById(R.id.menu_item_layout);
            holder.menu_header_text = (TextView) v.findViewById(R.id.menu_header_text);
            holder.menu_item_text = (TextView) v.findViewById(R.id.menu_item_text);
            holder.menu_icon = (ImageView) v.findViewById(R.id.menu_item_icon);
            v.setTag(holder);
        }
        else {
            holder = (ViewHolder) v.getTag();
        }

        if(item.isHeader()){
            holder.menu_header_layout.setVisibility(View.VISIBLE);
            holder.menu_item_layout.setVisibility(View.GONE);

            holder.menu_header_text.setText(item.getText());
        }
        else{
            holder.menu_header_layout.setVisibility(View.GONE);
            holder.menu_item_layout.setVisibility(View.VISIBLE);

            holder.menu_item_text.setText(item.getText());
            holder.menu_icon.setImageResource(item.getImgId());
        }

        return v;
    }
}
