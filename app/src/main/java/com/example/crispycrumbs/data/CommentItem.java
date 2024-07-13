package com.example.crispycrumbs.data;

import android.os.Parcel;
import android.os.Parcelable;

public class CommentItem implements Parcelable {
    private int avatarResId;
    private String userId;
    private String userName;
    private String comment;
    private String date;

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

    public String getDate() {
        return date;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
