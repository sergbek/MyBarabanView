package com.example.sergbek.mybarabanview.libView;


public class Item {

    private int mID;
    private int mPhoto;
    private int mColor;

    private int mStartAngle;
    private int mEndAngle;

    public int getColor() {
        return mColor;
    }

    public void setColor(int color) {
        mColor = color;
    }

    public int getStartAngle() {
        return mStartAngle;
    }

    public void setStartAngle(int startAngle) {
        mStartAngle = startAngle;
    }

    public int getEndAngle() {
        return mEndAngle;
    }

    public void setEndAngle(int endAngle) {
        mEndAngle = endAngle;
    }

    public int getID() {
        return mID;
    }

    public void setID(int ID) {
        mID = ID;
    }

    public int getPhoto() {
        return mPhoto;
    }

    public void setPhoto(int photo) {
        mPhoto = photo;
    }

    @Override
    public String toString() {
        return "Item{" +
                "mID=" + mID +
                ", mPhoto=" + mPhoto +
                ", mColor=" + mColor +
                ", mStartAngle=" + mStartAngle +
                ", mEndAngle=" + mEndAngle +
                '}';
    }
}
