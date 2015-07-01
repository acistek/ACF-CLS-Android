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

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by acistek on 6/22/2015.
 */
public class POListAdapter extends ArrayAdapter<POList> {

    static class ViewHolder {
        LinearLayout layout;
        LinearLayout headerLayout;
        LinearLayout infoLayout;
        TextView bighead;
        TextView head;
        TextView data;
        TextView poAbbrev;
        TextView poFullName;
    }

    private final ArrayList<POList> poListArrayList;

    public POListAdapter(Activity context, ArrayList<POList> poListArrayList) {
        super(context, R.layout.po_list_listview, poListArrayList);
        this.poListArrayList = poListArrayList;
    }

    public boolean areAllItemsEnabled(){
        return false;
    }

    public boolean isEnabled(int position){
        POList po = this.poListArrayList.get(position);

        if(po.isHeader())
            return false;
        else
            return true;
    }

    public View getView(int position, View view, ViewGroup parent){
        View v = view;
        ViewHolder holder;
        POList po = this.poListArrayList.get(position);

        if(v == null){
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.po_list_listview, parent, false);
            holder = new ViewHolder();
            holder.layout = (LinearLayout) v.findViewById(R.id.po_list_layout);
            holder.headerLayout = (LinearLayout) v.findViewById(R.id.po_header_layout);
            holder.bighead = (TextView) v.findViewById(R.id.po_big_header);
            holder.infoLayout = (LinearLayout) v.findViewById(R.id.po_info_layout);
            holder.head = (TextView) v.findViewById(R.id.po_sub_header);
            holder.data = (TextView) v.findViewById(R.id.po_sub_data);
            holder.poAbbrev = (TextView) v.findViewById(R.id.po_abbrev);
            holder.poFullName = (TextView) v.findViewById(R.id.po_full_name);
            v.setTag(holder);
        }
        else {
            holder = (ViewHolder) v.getTag();
        }

        if(po.isHeader()){
            holder.infoLayout.setVisibility(View.GONE);
            holder.headerLayout.setVisibility(View.VISIBLE);
            holder.bighead.setText(po.getSubtitle());
        }
        else{
            holder.headerLayout.setVisibility(View.GONE);
            holder.infoLayout.setVisibility(View.VISIBLE);

            if(position == StaffNotRespPOActivity.counter){
                holder.layout.setBackgroundColor(Color.parseColor("#E6E2E2"));
            }
            else if(position % 2 == 0){
                holder.layout.setBackgroundColor(Color.parseColor("#FFFFFF"));
            }
            else{
                holder.layout.setBackgroundColor(Color.parseColor("#F6F3EE"));
            }

            holder.head.setText(po.getPoName() + " - " + po.getPoAbbrev());
            holder.data.setText(po.getSubtitle() + po.getTotalUsers());
            holder.poAbbrev.setText(po.getPoAbbrev());
            holder.poFullName.setText(po.getPoName());
        }
        return v;
    }

}
