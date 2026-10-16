package com.example.myapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.BillItem;

import java.util.List;

public class BillAdapter extends RecyclerView.Adapter<BillAdapter.ViewHolder> {

    public interface OnBillClickListener {
        void onMarkPaidClick(BillItem bill);
    }

    private List<BillItem> items;
    private final OnBillClickListener listener;

    public BillAdapter(List<BillItem> items, OnBillClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void setItems(List<BillItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bill_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BillItem bill = items.get(position);
        holder.tvName.setText(bill.getName());
        holder.tvAmount.setText("₹" + String.format("%.0f", bill.getAmount()));

        if (bill.isPaid()) {
            holder.tvSub.setText("Paid · " + (bill.getPaidDate() != null ? bill.getPaidDate() : "Recently"));
            holder.tvSub.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.income_green, null));
            holder.btnMarkPaid.setVisibility(View.GONE);
        } else {
            holder.tvSub.setText("Due · " + bill.getDueDate());
            holder.tvSub.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.expense_red, null));
            holder.btnMarkPaid.setVisibility(View.VISIBLE);
        }

        holder.btnMarkPaid.setOnClickListener(v -> {
            if (listener != null) listener.onMarkPaidClick(bill);
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvSub, tvAmount;
        Button btnMarkPaid;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvBillName);
            tvSub = itemView.findViewById(R.id.tvBillSub);
            tvAmount = itemView.findViewById(R.id.tvBillAmount);
            btnMarkPaid = itemView.findViewById(R.id.btnMarkBillPaid);
        }
    }
}
