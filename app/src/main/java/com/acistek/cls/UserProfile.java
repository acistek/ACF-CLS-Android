package com.acistek.cls;

/**
 * Created by acistek on 4/21/2015.
 */
public class UserProfile {

    public String title;
    public String description;
    public boolean isHeader;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isHeader() {
        return isHeader;
    }

    public void setHeader(boolean isHeader) {
        this.isHeader = isHeader;
    }
}
