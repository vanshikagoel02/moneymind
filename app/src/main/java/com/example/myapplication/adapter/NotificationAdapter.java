package com.example.myapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.NotificationItem;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private final List<NotificationItem> items;

    public NotificationAdapter(List<NotificationItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationItem notif = items.get(position);
        holder.tvTitle.setText(notif.getTitle());
        holder.tvMessage.setText(notif.getMessage());

        if ("warning".equals(notif.getType())) {
            holder.viewDot.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    holder.itemView.getContext().getResources().getColor(R.color.expense_red, null)));
        } else if ("due".equals(notif.getType())) {
            holder.viewDot.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    holder.itemView.getContext().getResources().getColor(R.color.gold_primary, null)));
        } else {
            holder.viewDot.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    holder.itemView.getContext().getResources().getColor(R.color.income_green, null)));
        }
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMessage;
        View viewDot;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvNotifTitle);
            tvMessage = itemView.findViewById(R.id.tvNotifMessage);
            viewDot = itemView.findViewById(R.id.viewDot);
        }
    }
}
