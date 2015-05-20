package com.acistek.cls;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by acistek on 5/13/2015.
 */
public class CoopListAdapter extends BaseAdapter {

    private Activity context;
    private ArrayList<HashMap<String, String>> data;
    private HashMap<String, String> resultp;

    private int[] colors = new int[] { 0xFFFFFFFF, 0xFFF6F3EE };
    private LayoutInflater inflater;

    public CoopListAdapter(Activity context, ArrayList<HashMap<String, String>> contactlist) {
        this.inflater = LayoutInflater.from(context);
        this.resultp = new HashMap<String, String>();
        this.context = context;
        this.data = contactlist;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView name;
        TextView cell_phone;
        TextView office_phone;

        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View itemView = inflater.inflate(R.layout.list_item, parent, false);
        resultp = data.get(position);
        String coop_name = resultp.get(CoopActivity.TAG_COOP_NAME);
        String cell_phone_number = resultp.get(CoopActivity.TAG_CELL_PHONE);
        String office_phone_number = resultp.get(CoopActivity.TAG_OFFICE_PHONE);
        // Locate the TextViews in list_item.xml
        name = (TextView) itemView.findViewById(R.id.name);
        cell_phone = (TextView) itemView.findViewById(R.id.cell_phone);
        office_phone = (TextView) itemView.findViewById(R.id.office_phone);

        // Capture position and set results to the TextViews
        if(coop_name.equals("coop")||coop_name.equals("header")){
            if(coop_name.equals("coop")){
                itemView.setBackgroundColor(Color.parseColor("#AAAAAA"));
            }
            else{
                itemView.setBackgroundColor(Color.parseColor("#CFC3AE"));
            }
            itemView.setMinimumHeight(3);
            itemView.setPadding(10, 10, 10, 10);
            name.setText(resultp.get(CoopActivity.TAG_GROUP_NAME));
            itemView.findViewById(R.id.cell_phone_label).setVisibility(View.GONE);
            itemView.findViewById(R.id.office_phone_label).setVisibility(View.GONE);
            itemView.findViewById(R.id.cell_phone).setVisibility(View.GONE);
            itemView.findViewById(R.id.office_phone).setVisibility(View.GONE);
            itemView.findViewById(R.id.detail_icon).setVisibility(View.GONE);
        }
        else{
            name.setText(resultp.get(CoopActivity.TAG_COOP_NAME));
            cell_phone.setText(resultp.get(CoopActivity.TAG_CELL_PHONE));
            office_phone.setText(resultp.get(CoopActivity.TAG_OFFICE_PHONE));
            final CharSequence[] items = {
                    "Call Cell " +  cell_phone_number, "Text Cell " + cell_phone_number, "Call Office " + office_phone_number
            };
            int colorPos = position % colors.length;
            itemView.setBackgroundColor(colors[colorPos]);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Please select the action below");
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            Intent phoneIntent = new Intent(Intent.ACTION_CALL);
                            Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                            if(item==0){
                                phoneIntent.setData(Uri.parse("tel:" + items[item].toString().substring(10)));
                                context.startActivity(phoneIntent);
                            }
                            else if(item==2){
                                phoneIntent.setData(Uri.parse("tel:"+items[item].toString().substring(12)));
                                context.startActivity(phoneIntent);
                            }
                            else{
                                sendIntent.setData(Uri.parse("sms:"+items[item].toString().substring(10)));
                                context.startActivity(sendIntent);
                            }
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                    alert.setCanceledOnTouchOutside(true);
                }
            });
        }
        return itemView;
    }
}
