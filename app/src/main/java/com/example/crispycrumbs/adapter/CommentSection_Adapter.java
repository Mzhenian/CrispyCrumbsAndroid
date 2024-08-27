package com.example.crispycrumbs.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.crispycrumbs.R;
import com.example.crispycrumbs.dataUnit.CommentItem;
import com.example.crispycrumbs.dataUnit.UserItem;
import com.example.crispycrumbs.model.DataManager;
import com.example.crispycrumbs.serverAPI.ServerAPI;
import com.example.crispycrumbs.view.MainPage;
import com.example.crispycrumbs.view.ProfileFragment;
import com.example.crispycrumbs.viewModel.UserViewModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
            // Get UserViewModel
            UserViewModel userViewModel = new ViewModelProvider((AppCompatActivity) context).get(UserViewModel.class);

            // Observe user information using LiveData
            userViewModel.getUser(item.getUserId()).observe((AppCompatActivity) context, new Observer<UserItem>() {
                @Override
                public void onChanged(UserItem user) {
                    if (user != null) {
                        // Load user profile image
                        String userProfileUrl = ServerAPI.getInstance().constructUrl(user.getProfilePhoto());
                        Glide.with(context)
                                .load(userProfileUrl)
                                .placeholder(R.drawable.default_profile_picture)
                                .into(holder.profilePicture);
                        // Set user name
                        holder.userName.setText(user.getUserName());
                    } else {
                        // Handle case where user is not found (e.g., deleted user)
                        holder.profilePicture.setImageResource(R.drawable.default_profile_picture);
                        holder.userName.setText("[deleted user]");
                        Log.e(TAG, "User not found");
                    }
                }
            });

            // Set comment content and date
            holder.content.setText(item.getComment());

            // Format and set the video date
            String formattedDate = formatDateString(item.getDate());
            holder.date.setText(formattedDate);

            // Show or hide edit/delete buttons based on the current user
            if (item.getUserId().equals(currentUserId)) {
                holder.editButton.setVisibility(View.VISIBLE);
                holder.deleteButton.setVisibility(View.VISIBLE);
                holder.editButton.setOnClickListener(v -> commentActionListener.onEditComment(position));
                holder.deleteButton.setOnClickListener(v -> commentActionListener.onDeleteComment(holder.getAdapterPosition()));
            } else {
                holder.editButton.setVisibility(View.GONE);
                holder.deleteButton.setVisibility(View.GONE);
            }

            // Handle profile picture click to open user's profile
            holder.profilePicture.setOnClickListener(v -> {
                MainPage.getInstance().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ProfileFragment(item.getUserId()))
                        .commit();
            });
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

    private String formatDateString(String originalDate) {
        SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        SimpleDateFormat targetFormat = new SimpleDateFormat("dd.MM.yy", Locale.getDefault());

        try {
            Date date = originalFormat.parse(originalDate);
            return targetFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            // Return the original date if parsing fails
            return originalDate;
        }
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
