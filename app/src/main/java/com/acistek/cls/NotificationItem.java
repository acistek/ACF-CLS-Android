package com.acistek.cls;

/**
 * Created by acistek on 5/14/2015.
 */
public class NotificationItem {

    public String system_id;
    public String system_name;
    public String time_down;
    public String system_url;
    public String responsible;

    public String getSystem_id() {
        return system_id;
    }

    public void setSystem_id(String system_id) {
        this.system_id = system_id;
    }

    public String getTime_down() {
        return time_down;
    }

    public void setTime_down(String time_down) {
        this.time_down = time_down;
    }

    public String getSystem_name() {
        return system_name;
    }

    public void setSystem_name(String system_name) {
        this.system_name = system_name;
    }

    public String getSystem_url() {
        return system_url;
    }

    public void setSystem_url(String system_url) {
        this.system_url = system_url;
    }

    public String getResponsible() {
        return responsible;
    }

    public void setResponsible(String responsible) {
        this.responsible = responsible;
    }
}
