package com.acistek.cls;

/**
 * Created by acistek on 6/23/2015.
 */
public class PODetail {

    public String lastname;
    public String firstname;
    public String division;
    public String daysNotResponded;
    public String clsID;
    public String subtitle;
    public boolean isUser;

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getDaysNotResponded() {
        return daysNotResponded;
    }

    public void setDaysNotResponded(String daysNotResponded) {
        this.daysNotResponded = daysNotResponded;
    }

    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public String getClsID() {
        return clsID;
    }

    public void setClsID(String clsID) {
        this.clsID = clsID;
    }

    public boolean isUser() {
        return isUser;
    }

    public void setIsUser(boolean isUser) {
        this.isUser = isUser;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }
}
