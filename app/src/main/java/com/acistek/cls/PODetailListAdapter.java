package com.acistek.cls;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by acistek on 6/23/2015.
 */
public class PODetailListAdapter extends ArrayAdapter<PODetail> {

    static class ViewHolder {
        LinearLayout layout;
        LinearLayout poNameLayout;
        TextView poName;
        LinearLayout subHeaderLayout;
        TextView subHeader;
        LinearLayout divisionLayout;
        TextView division;
        LinearLayout userLayout;
        TextView username;
        TextView days;
        TextView userid;
    }

    private final ArrayList<PODetail> poDetailArrayList;

    public PODetailListAdapter(Activity context, ArrayList<PODetail> poDetailArrayList) {
        super(context, R.layout.po_detail_listview, poDetailArrayList);
        this.poDetailArrayList = poDetailArrayList;
    }

    public boolean areAllItemsEnabled(){
        return false;
    }

    public boolean isEnabled(int position){
        PODetail po = this.poDetailArrayList.get(position);

        if(po.isUser())
            return true;
        else
            return false;
    }

    public View getView(int position, View view, ViewGroup parent){
        View v = view;
        ViewHolder holder;
        PODetail po = this.poDetailArrayList.get(position);

        if(v == null){
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.po_detail_listview, parent, false);
            holder = new ViewHolder();
            holder.layout = (LinearLayout) v.findViewById(R.id.po_detail_layout);
            holder.poNameLayout = (LinearLayout) v.findViewById(R.id.po_detail_header_layout);
            holder.poName = (TextView) v.findViewById(R.id.po_detail_big_header);
            holder.subHeaderLayout = (LinearLayout) v.findViewById(R.id.po_detail_subheader_layout);
            holder.subHeader = (TextView) v.findViewById(R.id.po_detail_small_header);
            holder.divisionLayout = (LinearLayout) v.findViewById(R.id.po_detail_division_layout);
            holder.division = (TextView) v.findViewById(R.id.po_detail_division_header);
            holder.userLayout = (LinearLayout) v.findViewById(R.id.po_detail_user_layout);
            holder.username = (TextView) v.findViewById(R.id.po_detail_username);
            holder.days = (TextView) v.findViewById(R.id.po_detail_days);
            holder.userid = (TextView) v.findViewById(R.id.po_detail_contactlistid);
            v.setTag(holder);
        }
        else {
            holder = (ViewHolder) v.getTag();
        }

        if(po.isUser()){
            holder.poNameLayout.setVisibility(View.GONE);
            holder.subHeaderLayout.setVisibility(View.GONE);
            holder.divisionLayout.setVisibility(View.GONE);
            holder.userLayout.setVisibility(View.VISIBLE);

            if(position == StaffNotRespUsersActivity.counter){
                holder.layout.setBackgroundColor(Color.parseColor("#E6E2E2"));
            }
            else if(position % 2 == 0){
                holder.layout.setBackgroundColor(Color.parseColor("#FFFFFF"));
            }
            else{
                holder.layout.setBackgroundColor(Color.parseColor("#F6F3EE"));
            }

            holder.username.setText(po.getFirstname() + " " + po.getLastname());
            holder.userid.setText(po.getClsID());
            holder.days.setText(po.getSubtitle() + po.getDaysNotResponded());
        }
        else{
            holder.userLayout.setVisibility(View.GONE);

            if(po.getLastname().equalsIgnoreCase("withEmail") || po.getLastname().equalsIgnoreCase("ExternalEmail") || po.getLastname().equalsIgnoreCase("noEmail")){
                holder.poNameLayout.setVisibility(View.GONE);
                holder.subHeaderLayout.setVisibility(View.VISIBLE);
                holder.divisionLayout.setVisibility(View.GONE);

                if(po.getLastname().equalsIgnoreCase("withEmail")){
                    holder.subHeader.setText("ACF Network Accounts with ACF Email");
                }
                else if(po.getLastname().equalsIgnoreCase("ExternalEmail")){
                    holder.subHeader.setText("ACF Network Accounts with External Email");
                }
                else if(po.getLastname().equalsIgnoreCase("noEmail")){
                    holder.subHeader.setText("ACF Network Accounts with No Email");
                }

                holder.subHeader.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
            }
            else if(po.getLastname().equalsIgnoreCase("officeTitle")){
                holder.poNameLayout.setVisibility(View.GONE);
                holder.subHeaderLayout.setVisibility(View.GONE);
                holder.divisionLayout.setVisibility(View.VISIBLE);

                holder.division.setText("Division - " + po.getDivision());
            }
            else if(po.getLastname().equalsIgnoreCase("POName")){
                holder.poNameLayout.setVisibility(View.VISIBLE);
                holder.subHeaderLayout.setVisibility(View.GONE);
                holder.divisionLayout.setVisibility(View.GONE);

                holder.poName.setText(po.getFirstname());
            }
        }

        return v;
    }
}
