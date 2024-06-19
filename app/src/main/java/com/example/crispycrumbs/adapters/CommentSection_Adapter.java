package com.example.crispycrumbs.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.crispycrumbs.R;
import com.example.crispycrumbs.data.CommentItem;

import java.util.ArrayList;

public class CommentSection_Adapter extends RecyclerView.Adapter<CommentSection_Adapter.MyViewHolder> {

    Context context;
    ArrayList<CommentItem> commentItemArrayList;

    public interface CommentActionListener {
        void onEditComment(int position);
        void onDeleteComment(int position);
    }

    private CommentActionListener commentActionListener;

    // Constructor to initialize context and comment list
    public CommentSection_Adapter(Context context, ArrayList<CommentItem> commentItemArrayList, CommentActionListener listener) {
        this.context = context;
        this.commentItemArrayList = commentItemArrayList;
        this.commentActionListener = listener;
    }

    @NonNull
    @Override
    public CommentSection_Adapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each comment item
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.comment_item, parent, false);
        return new CommentSection_Adapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentSection_Adapter.MyViewHolder holder, int position) {
        // Bind the data to the views for each comment item
        holder.userName.setText(commentItemArrayList.get(position).getUserId());
        holder.content.setText(commentItemArrayList.get(position).getComment());
        holder.date.setText(commentItemArrayList.get(position).getDate());
        holder.imageView.setImageResource(commentItemArrayList.get(position).getAvatarResId());
    }

    @Override
    public int getItemCount() {
        // Return the total number of comments
        return commentItemArrayList.size();
    }

    public void editComment(int position, CommentItem updatedItem) {
        commentItemArrayList.set(position, updatedItem);
        notifyItemChanged(position);
    }

    public void deleteComment(int position) {
        commentItemArrayList.remove(position);
        notifyItemRemoved(position);
    }

    public ArrayList<CommentItem> getComments() {
        return commentItemArrayList;
    }

    // ViewHolder class to hold the views for each comment item
    public static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView userName, content, date;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize the views
            imageView = itemView.findViewById(R.id.profile_picture);
            userName = itemView.findViewById(R.id.comment_user);
            content = itemView.findViewById(R.id.comment_text);
            date = itemView.findViewById(R.id.comment_date);
        }
    }
}
