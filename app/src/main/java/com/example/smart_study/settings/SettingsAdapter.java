package com.example.smart_study.settings;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smart_study.R;

import java.util.List;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.SettingsViewHolder> {

    private final List<SettingsItem> items;
    private final OnItemClickListener onItemClick;

    public interface OnItemClickListener {
        void onItemClick(SettingsItem item);
    }

    public SettingsAdapter(List<SettingsItem> items, OnItemClickListener onItemClick) {
        this.items = items;
        this.onItemClick = onItemClick;
    }

    @NonNull
    @Override
    public SettingsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_settings, parent, false);
        return new SettingsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SettingsViewHolder holder, int position) {
        holder.bind(items.get(position), onItemClick);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class SettingsViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleText;
        private final ImageView iconImage;
        private final View iconBackground;

        public SettingsViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.txt_title);
            iconImage = itemView.findViewById(R.id.icon_image);
            iconBackground = itemView.findViewById(R.id.icon_background);
        }

        public void bind(SettingsItem item, OnItemClickListener onItemClick) {
            titleText.setText(item.getTitle());
            iconImage.setImageResource(item.getIconResId());
            iconBackground.setBackgroundResource(item.getBackgroundResId());

            itemView.setOnClickListener(v -> onItemClick.onItemClick(item));
        }
    }
}
