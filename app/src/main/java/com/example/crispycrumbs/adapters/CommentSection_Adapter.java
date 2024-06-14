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
    public CommentSection_Adapter(Context context, ArrayList<CommentItem> commentItemArrayList) {
        this.context = context;
        this.commentItemArrayList = commentItemArrayList;
    }
    @NonNull
    @Override
    public CommentSection_Adapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.comment_item,parent,false);
        return new CommentSection_Adapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentSection_Adapter.MyViewHolder holder, int position) {
        holder.userName.setText(commentItemArrayList.get(position).getUsername());
        holder.content.setText(commentItemArrayList.get(position).getContent());
        holder.date.setText(commentItemArrayList.get(position).getDate());
        holder.imageView.setImageResource(commentItemArrayList.get(position).getImage());

    }

    @Override
    public int getItemCount() {
        return commentItemArrayList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        ImageView imageView;
        TextView userName, content, date;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.profile_picture);
            userName = itemView.findViewById(R.id.comment_user);
            content = itemView.findViewById(R.id.comment_text);
            date = itemView.findViewById(R.id.comment_date);
        }
    }
}
