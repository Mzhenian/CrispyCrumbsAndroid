package com.example.crispycrumbs.dataUnit;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.gson.annotations.SerializedName;
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
    @PrimaryKey(autoGenerate = true)
    private int id;  // Room requires a primary key

    private int avatarResId;
    private String userId;
    private String userName;
    private String date;
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
