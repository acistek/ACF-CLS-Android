package com.acistek.cls;

/**
 * Created by acistek on 6/22/2015.
 */
public class POList {

    public String poAbbrev;
    public String poName;
    public String totalUsers;
    public String subtitle;
    public boolean isHeader;

    public String getPoAbbrev() {
        return poAbbrev;
    }

    public void setPoAbbrev(String poAbbrev) {
        this.poAbbrev = poAbbrev;
    }

    public String getPoName() {
        return poName;
    }

    public void setPoName(String poName) {
        this.poName = poName;
    }

    public String getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(String totalUsers) {
        this.totalUsers = totalUsers;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public boolean isHeader() {
        return isHeader;
    }

    public void setIsHeader(boolean isHeader) {
        this.isHeader = isHeader;
    }
}
