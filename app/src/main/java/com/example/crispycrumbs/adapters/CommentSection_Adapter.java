package com.example.crispycrumbs.adapters;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.example.crispycrumbs.R;
import com.example.crispycrumbs.data.CommentItem;
import com.example.crispycrumbs.data.UserItem;
import com.example.crispycrumbs.model.DataManager;

import java.util.ArrayList;

public class CommentSection_Adapter extends RecyclerView.Adapter<CommentSection_Adapter.MyViewHolder> {

    private static final String TAG = "CommentSection_Adapter";
    private final Context context;
    private final ArrayList<CommentItem> commentItemArrayList;
    private final String currentUserId;
    private final CommentActionListener commentActionListener;

    public interface CommentActionListener {
        void onEditComment(int position);
        void onDeleteComment(int position);
    }

    public CommentSection_Adapter(Context context, ArrayList<CommentItem> commentItemArrayList, CommentActionListener listener, String currentUserId) {
        this.context = context;
        this.commentItemArrayList = commentItemArrayList;
        this.commentActionListener = listener;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.comment_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        CommentItem item = commentItemArrayList.get(position);
        if (item != null) {
            UserItem user = DataManager.getInstance().getUserById(item.getUserId());
            if (user != null) {
                holder.profilePicture.setImageURI(DataManager.getUriFromResOrFile(user.getProfilePhoto()));
                holder.userName.setText(user.getUserName());
            } else {
                holder.profilePicture.setImageResource(R.drawable.default_profile_picture);
                holder.userName.setText("[deleted user]");
            }
            holder.content.setText(item.getComment());
            holder.date.setText(item.getDate());

            if (item.getUserId().equals(currentUserId)) {
                holder.editButton.setVisibility(View.VISIBLE);
                holder.deleteButton.setVisibility(View.VISIBLE);
                holder.editButton.setOnClickListener(v -> commentActionListener.onEditComment(position));
                holder.deleteButton.setOnClickListener(v -> commentActionListener.onDeleteComment(holder.getAdapterPosition()));
            } else {
                holder.editButton.setVisibility(View.GONE);
                holder.deleteButton.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return commentItemArrayList.size();
    }

    public void removeComment(int position) {
        commentItemArrayList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount());
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView profilePicture;
        TextView userName, content, date;
        AppCompatButton editButton, deleteButton;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePicture = itemView.findViewById(R.id.profile_picture);
            userName = itemView.findViewById(R.id.comment_user);
            content = itemView.findViewById(R.id.comment_text);
            date = itemView.findViewById(R.id.comment_date);
            editButton = itemView.findViewById(R.id.edit_button);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}
