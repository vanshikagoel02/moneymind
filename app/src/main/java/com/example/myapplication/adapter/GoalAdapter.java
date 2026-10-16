package com.example.myapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.GoalItem;

import java.util.List;

public class GoalAdapter extends RecyclerView.Adapter<GoalAdapter.ViewHolder> {

    public interface OnGoalClickListener {
        void onAddSavingsClick(GoalItem goal);
        void onGoalOptionClick(GoalItem goal, View anchor);
    }

    private List<GoalItem> items;
    private final OnGoalClickListener listener;

    public GoalAdapter(List<GoalItem> items, OnGoalClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void setItems(List<GoalItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_goal_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GoalItem goal = items.get(position);
        holder.tvName.setText(goal.getName());
        holder.tvCurrent.setText("₹" + String.format("%.0f", goal.getCurrentAmount()));
        holder.tvTarget.setText("of ₹" + String.format("%.0f", goal.getTargetAmount()));

        int pct = goal.getPercentComplete();
        holder.progressBar.setProgress(pct);
        holder.tvPct.setText(pct + "% there");

        holder.btnAddSavings.setOnClickListener(v -> {
            if (listener != null) listener.onAddSavingsClick(goal);
        });

        holder.btnMore.setOnClickListener(v -> {
            if (listener != null) listener.onGoalOptionClick(goal, v);
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCurrent, tvTarget, tvPct, btnAddSavings, btnMore;
        ProgressBar progressBar;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvGoalName);
            tvCurrent = itemView.findViewById(R.id.tvGoalCurrent);
            tvTarget = itemView.findViewById(R.id.tvGoalTarget);
            tvPct = itemView.findViewById(R.id.tvGoalPct);
            btnAddSavings = itemView.findViewById(R.id.btnGoalAddSavings);
            btnMore = itemView.findViewById(R.id.btnGoalMore);
            progressBar = itemView.findViewById(R.id.progressGoal);
        }
    }
}
