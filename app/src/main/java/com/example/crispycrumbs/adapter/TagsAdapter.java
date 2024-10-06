package com.example.crispycrumbs.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.crispycrumbs.R;

import java.util.List;

public class TagsAdapter extends RecyclerView.Adapter<TagsAdapter.TagViewHolder> {
    private List<String> tags;

    public TagsAdapter(List<String> tags) {
        this.tags = tags;
    }

    @NonNull
    @Override
    public TagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tag_item, parent, false);
        return new TagViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TagViewHolder holder, int position) {
        String tag = tags.get(position);
        holder.tvTag.setText(tag);
        holder.tvTag.setOnClickListener(v -> {
            deleteTag(position);
        });
    }

    public void deleteTag(int position) {
        tags.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount());
    }


    @Override
    public int getItemCount() {
        return tags.size();
    }

    public static class TagViewHolder extends RecyclerView.ViewHolder {
        TextView tvTag;

        public TagViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTag = itemView.findViewById(R.id.tvTag);
        }
    }
}