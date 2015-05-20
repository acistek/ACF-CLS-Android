package com.acistek.cls;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by acistek on 5/8/2015.
 */
public class FavoriteExpandableListAdapter extends BaseExpandableListAdapter {

    private Activity context;
    private Map<String, ArrayList<UserSearch>> groupItemCollection;
    private ArrayList<String> groupNameList;

    static class ViewHolder {
        LinearLayout itemLayout;
        TextView name;
        TextView email;
        TextView userid;
        TextView userid_remove;
        TextView group_name_remove;
        LinearLayout remove;
        ImageView arrow;
    }

    public FavoriteExpandableListAdapter(Activity context, ArrayList<String> groupNameList, Map<String, ArrayList<UserSearch>> groupItemCollection){
        this.context = context;
        this.groupNameList = groupNameList;
        this.groupItemCollection = groupItemCollection;
    }

    public Object getChild(int groupPosition, int childPosition){
        return groupItemCollection.get(groupNameList.get(groupPosition)).get(childPosition);
    }

    public long getChildId(int groupPosition, int childPosition){
        return childPosition;
    }

    public int getChildrenCount(int groupPosition){
        return groupItemCollection.get(groupNameList.get(groupPosition)).size();
    }

    public Object getGroup(int groupPosition){
        return groupNameList.get(groupPosition);
    }

    public long getGroupId(int groupPosition){
        return groupPosition;
    }

    public int getGroupCount(){
        return groupNameList.size();
    }

    public boolean hasStableIds(){
        return true;
    }

    public boolean isChildSelectable(int groupPosition, int childPosition){
        return true;
    }

    public View getChildView (int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent){
        final UserSearch user = (UserSearch) getChild(groupPosition, childPosition);
        ViewHolder holder;

        if(convertView == null){
            LayoutInflater inflater = context.getLayoutInflater();
            convertView = inflater.inflate(R.layout.favorite_group_item, null);
            holder = new ViewHolder();
            holder.itemLayout = (LinearLayout) convertView.findViewById(R.id.favorite_group_item_layout);
            holder.name = (TextView) convertView.findViewById(R.id.favorite_user_name);
            holder.email = (TextView) convertView.findViewById(R.id.favorite_user_email);
            holder.userid = (TextView) convertView.findViewById(R.id.favorite_user_id);
            holder.arrow = (ImageView) convertView.findViewById(R.id.favorite_user_arrow);
            holder.userid_remove = (TextView) convertView.findViewById(R.id.favorite_user_id_remove);
            holder.group_name_remove = (TextView) convertView.findViewById(R.id.favorite_group_name_remove);
            holder.remove = (LinearLayout) convertView.findViewById(R.id.favorite_user_remove_layout);
            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder) convertView.getTag();
        }

        holder.name.setText(user.getFirstname() + " " + user.getLastname());
        holder.email.setText(user.getEmail());
        holder.userid.setText(user.getContactlistid());
        holder.userid_remove.setText(user.getContactlistid());
        holder.group_name_remove.setText(user.getGroupName());

        if(childPosition == FavoriteActivity.childCounter && groupPosition == FavoriteActivity.groupCounter)
            holder.itemLayout.setBackgroundColor(Color.parseColor("#C8C7CC"));
        else if(childPosition % 2 == 0)
            holder.itemLayout.setBackgroundColor(Color.parseColor("#FFFFFF"));
        else
            holder.itemLayout.setBackgroundColor(Color.parseColor("#F6F3EE"));

        if(FavoriteActivity.isEditMenuClicked){
            holder.remove.setVisibility(View.VISIBLE);
            holder.arrow.setVisibility(View.GONE);
        }
        else{
            holder.remove.setVisibility(View.GONE);
            holder.arrow.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent){
        String groupName = (String) getGroup(groupPosition);

        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.favorite_group_title, null);
        }

        TextView title = (TextView) convertView.findViewById(R.id.favorite_group_title);
        title.setText(groupName);

        return convertView;
    }
}
