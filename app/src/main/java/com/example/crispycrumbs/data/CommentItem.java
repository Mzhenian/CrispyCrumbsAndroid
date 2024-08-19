package com.example.crispycrumbs.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class CommentItem implements Serializable,  Parcelable  {
    public static final Creator<CommentItem> CREATOR = new Creator<CommentItem>() {
        @Override
        public CommentItem createFromParcel(Parcel in) {
            return new CommentItem(in);
        }

        @Override
        public CommentItem[] newArray(int size) {
            return new CommentItem[size];
        }
    };
    private final int avatarResId;
    private final String userId; //todo migrate userId to _id
    private  String _id;
    private final String userName;
    private final String date;
    private String comment;

    public CommentItem(int avatarResId, String userId, String userName, String comment, String date) {
        this.avatarResId = avatarResId;
        this.userId = userId;
        this.userName = userName;
        this.comment = comment;
        this.date = date;
    }

    protected CommentItem(Parcel in) {
        avatarResId = in.readInt();
        userId = in.readString();
        userName = in.readString();
        comment = in.readString();
        date = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(avatarResId);
        dest.writeString(userId);
        dest.writeString(userName);
        dest.writeString(comment);
        dest.writeString(date);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public int getAvatarResId() {
        return avatarResId;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDate() {
        return date;
    }
}