package com.example.myapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.TransactionItem;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    public interface OnTransactionClickListener {
        void onTransactionClick(TransactionItem item);
        void onTransactionOptionClick(TransactionItem item, View anchor);
    }

    private List<TransactionItem> items;
    private final OnTransactionClickListener listener;

    public TransactionAdapter(List<TransactionItem> items, OnTransactionClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void setItems(List<TransactionItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TransactionItem item = items.get(position);
        holder.tvName.setText(item.getDescription() != null && !item.getDescription().isEmpty() ? item.getDescription() : item.getCategory());

        String sub = item.getCategory() + " · " + item.getDate() + " · " + item.getPaymentMethod();
        if (item.getPlaceName() != null && !item.getPlaceName().isEmpty()) {
            sub += " · " + item.getPlaceName();
        }
        holder.tvSub.setText(sub);

        if (item.isExpense()) {
            holder.tvAmount.setText("-₹" + String.format("%.0f", item.getAmount()));
            holder.tvAmount.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.text_primary, null));
        } else {
            holder.tvAmount.setText("+₹" + String.format("%.0f", item.getAmount()));
            holder.tvAmount.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.income_green, null));
        }

        // Category Emoji mapping
        String emoji = "💳";
        String cat = item.getCategory().toLowerCase();
        if (cat.contains("education") || cat.contains("book")) emoji = "📚";
        else if (cat.contains("travel") || cat.contains("metro")) emoji = "🛺";
        else if (cat.contains("pocket") || cat.contains("income") || cat.contains("salary")) emoji = "👛";
        else if (cat.contains("food") || cat.contains("swiggy")) emoji = "🍕";
        else if (cat.contains("bill") || cat.contains("rent")) emoji = "📅";
        else if (cat.contains("shopping")) emoji = "🛍️";
        else if (cat.contains("entertainment")) emoji = "🍿";
        holder.tvEmoji.setText(emoji);

        holder.btnMore.setVisibility(View.VISIBLE);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onTransactionClick(item);
        });
        holder.btnMore.setOnClickListener(v -> {
            if (listener != null) listener.onTransactionOptionClick(item, v);
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmoji, tvName, tvSub, tvAmount, btnMore;

        ViewHolder(View itemView) {
            super(itemView);
            tvEmoji = itemView.findViewById(R.id.tvCategoryEmoji);
            tvName = itemView.findViewById(R.id.tvTxName);
            tvSub = itemView.findViewById(R.id.tvTxSub);
            tvAmount = itemView.findViewById(R.id.tvTxAmount);
            btnMore = itemView.findViewById(R.id.btnTxMoreMenu);
        }
    }
}
