package com.example.myapplication.fragment;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.db.MoneyRepository;
import com.example.myapplication.model.WatchlistItem;

import java.util.List;

public class MarketsFragment extends Fragment {

    private TextView tvWatchlistCount;
    private RecyclerView rvWatchlist;
    private MoneyRepository repo;
    private WatchlistAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_markets, container, false);
        repo = MoneyRepository.getInstance(requireContext());

        tvWatchlistCount = view.findViewById(R.id.tvWatchlistCountHeader);
        rvWatchlist = view.findViewById(R.id.rvWatchlist);
        rvWatchlist.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new WatchlistAdapter(repo.getWatchlist(), item -> {
            repo.deleteWatchlist(item.getId());
            Toast.makeText(getContext(), "Company removed from watchlist", Toast.LENGTH_SHORT).show();
            refreshData();
        });
        rvWatchlist.setAdapter(adapter);

        view.findViewById(R.id.btnAddWatchlistCompany).setOnClickListener(v -> showAddCompanyDialog());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    public void refreshData() {
        if (getContext() == null) return;
        List<WatchlistItem> list = repo.getWatchlist();
        tvWatchlistCount.setText(list.size() + " companies");
        adapter.setItems(list);
    }

    private void showAddCompanyDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_watchlist);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        }

        EditText etName = dialog.findViewById(R.id.etWatchlistName);
        EditText etNote = dialog.findViewById(R.id.etWatchlistNote);
        Button btnSave = dialog.findViewById(R.id.btnSaveWatchlist);
        Button btnClose = dialog.findViewById(R.id.btnCloseWatchlist);

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String note = etNote.getText().toString().trim();

            if (name.isEmpty()) {
                etName.setError("Company name required");
                return;
            }

            WatchlistItem item = new WatchlistItem(0, name.toUpperCase().replace(" ", ""), name, note.isEmpty() ? "Saved for reference" : note);
            repo.addWatchlist(item);
            Toast.makeText(getContext(), "Company added!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            refreshData();
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private static class WatchlistAdapter extends RecyclerView.Adapter<WatchlistAdapter.ViewHolder> {
        public interface OnRemoveListener { void onRemove(WatchlistItem item); }
        private List<WatchlistItem> items;
        private final OnRemoveListener listener;

        WatchlistAdapter(List<WatchlistItem> items, OnRemoveListener listener) {
            this.items = items;
            this.listener = listener;
        }

        void setItems(List<WatchlistItem> items) { this.items = items; notifyDataSetChanged(); }

        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_watchlist_row, parent, false);
            return new ViewHolder(view);
        }

        @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            WatchlistItem item = items.get(position);
            holder.tvName.setText(item.getName());
            holder.tvSub.setText(item.getNote());
            holder.btnRemove.setOnClickListener(v -> { if (listener != null) listener.onRemove(item); });
        }

        @Override public int getItemCount() { return items != null ? items.size() : 0; }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvSub, btnRemove;
            ViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvWatchlistName);
                tvSub = itemView.findViewById(R.id.tvWatchlistSub);
                btnRemove = itemView.findViewById(R.id.btnWatchlistRemove);
            }
        }
    }
}
