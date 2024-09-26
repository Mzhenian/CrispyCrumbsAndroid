package com.example.crispycrumbs.dataUnit;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.gson.annotations.SerializedName;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

@Entity(tableName = "comments")
public class CommentItem implements Serializable, Parcelable {

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

    @PrimaryKey
    @SerializedName("commentId") // Use this to match the field name from the server response, assuming MongoDB's ObjectId field is called `_id`
    @NonNull
    private String id; // Now it's a String instead of int

    private int avatarResId;
    private String userId;
    private String userName;
    private String date;
    private String comment;

    public CommentItem(int avatarResId, String userId, String userName, String comment, String date, String id) {
        this.avatarResId = avatarResId;
        this.userId = userId;
        this.userName = userName;
        this.comment = comment;
        this.date = date;
        this.id = id; // Initialize with String id
    }

    protected CommentItem(Parcel in) {
        avatarResId = in.readInt();
        userId = in.readString();
        userName = in.readString();
        comment = in.readString();
        date = in.readString();
        id = in.readString(); // Read the String id from Parcel
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(avatarResId);
        dest.writeString(userId);
        dest.writeString(userName);
        dest.writeString(comment);
        dest.writeString(date);
        dest.writeString(id); // Write the String id to Parcel
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

    public String getId() { // Now returns a String
        return id;
    }

    public void setId(String id) { // Now accepts a String
        this.id = id;
    }
}
