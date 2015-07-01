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
 * Created by acistek on 5/14/2015.
 */
public class NotificationListAdapter extends ArrayAdapter<NotificationItem> {

    static class ViewHolder {
        LinearLayout list;
        TextView system_name;
//        TextView time_down;
//        TextView responsible;
        TextView system_id;
        TextView system_url;
        TextView description;
    }

    private final ArrayList<NotificationItem> notificationsItems;
    private Resources resources;

    public NotificationListAdapter(Activity context, ArrayList<NotificationItem> notificationsItems, Resources resources) {
        super(context, R.layout.notification_listview, notificationsItems);
        this.notificationsItems = notificationsItems;
        this.resources = resources;
    }

    public View getView(int position, View view, ViewGroup parent){
        View v = view;
        ViewHolder holder;
        NotificationItem note = this.notificationsItems.get(position);

        if(v == null){
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.notification_listview, parent, false);
            holder = new ViewHolder();
            holder.system_name = (TextView) v.findViewById(R.id.notification_system_name);
//            holder.time_down = (TextView) v.findViewById(R.id.notification_time_down);
//            holder.responsible = (TextView) v.findViewById(R.id.notification_responsible);
            holder.description = (TextView) v.findViewById(R.id.notification_description);
            holder.system_id = (TextView) v.findViewById(R.id.notification_system_id);
            holder.system_url = (TextView) v.findViewById(R.id.notification_system_url);
            holder.list = (LinearLayout) v.findViewById(R.id.notification_list_layout);
            v.setTag(holder);
        }
        else {
            holder = (ViewHolder) v.getTag();
        }

        if(position % 2 == 0){
            holder.list.setBackgroundColor(Color.parseColor("#FFFFFF"));
        }
        else{
            holder.list.setBackgroundColor(Color.parseColor("#F6F3EE"));
        }

        holder.system_name.setText(note.getSystem_name());
        holder.system_id.setText(note.getSystem_id());
//        holder.time_down.setText(resources.getString(R.string.notifications_timedown) + " " + note.getTime_down());
//        holder.responsible.setText(resources.getString(R.string.notifications_responsible) + " " + note.getResponsible());
        holder.description.setText(note.getDescription());
        holder.system_url.setText(note.getSystem_url());

        return v;
    }
}
