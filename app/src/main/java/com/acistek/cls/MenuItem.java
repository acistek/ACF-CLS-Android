package com.acistek.cls;

/**
 * Created by greed on 7/1/2015.
 */
public class MenuItem {
    public String text;
    public Integer imgId;
    public boolean isHeader;

    public MenuItem(String text, Integer imgId, boolean isHeader){
        this.text = text;
        this.imgId = imgId;
        this.isHeader = isHeader;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getImgId() {
        return imgId;
    }

    public void setImgId(Integer imgId) {
        this.imgId = imgId;
    }

    public boolean isHeader() {
        return isHeader;
    }

    public void setHeader(boolean isHeader) {
        this.isHeader = isHeader;
    }
}
