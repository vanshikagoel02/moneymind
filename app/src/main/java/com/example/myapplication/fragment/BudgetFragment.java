package com.example.myapplication.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.db.MoneyRepository;

import java.util.Map;

public class BudgetFragment extends Fragment {

    private TextView tvLimit, tvCurrent, tvRemaining, tvPct;
    private ProgressBar progressBar;
    private EditText etLimitInput;
    private LinearLayout layoutCategoryProgressList;

    private MoneyRepository repo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budget, container, false);
        repo = MoneyRepository.getInstance(requireContext());

        tvLimit = view.findViewById(R.id.tvBudgetCardLimit);
        tvCurrent = view.findViewById(R.id.tvBudgetCardCurrent);
        tvRemaining = view.findViewById(R.id.tvBudgetCardRemaining);
        tvPct = view.findViewById(R.id.tvBudgetCardPct);
        progressBar = view.findViewById(R.id.progressBudgetCard);
        etLimitInput = view.findViewById(R.id.etBudgetLimit);
        layoutCategoryProgressList = view.findViewById(R.id.layoutCategoryProgressList);

        view.findViewById(R.id.btnSaveBudgetLimit).setOnClickListener(v -> {
            String val = etLimitInput.getText().toString().trim();
            if (!val.isEmpty()) {
                try {
                    double newLimit = Double.parseDouble(val);
                    if (newLimit > 0) {
                        repo.setMonthlyBudget(newLimit);
                        Toast.makeText(getContext(), "Monthly budget updated!", Toast.LENGTH_SHORT).show();
                        refreshData();
                    } else {
                        etLimitInput.setError("Enter valid limit");
                    }
                } catch (Exception e) {
                    etLimitInput.setError("Invalid number");
                }
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    public void refreshData() {
        if (getContext() == null) return;
        double budget = repo.getMonthlyBudget();
        double spent = repo.getExpensesThisMonth();
        double remaining = budget - spent;

        tvLimit.setText("₹" + String.format("%,.0f", budget));
        tvCurrent.setText("Current spending · ₹" + String.format("%,.0f", spent));
        tvRemaining.setText("₹" + String.format("%,.0f", Math.max(0, remaining)) + " remaining");

        int pct = budget > 0 ? (int) Math.round((spent / budget) * 100) : 0;
        pct = Math.min(100, Math.max(0, pct));
        tvPct.setText(pct + "%");
        progressBar.setProgress(pct);

        etLimitInput.setText(String.format("%.0f", budget));

        // Spending by category list
        layoutCategoryProgressList.removeAllViews();
        Map<String, Double> categoryMap = repo.getCategoryTotalsThisMonth();
        for (Map.Entry<String, Double> entry : categoryMap.entrySet()) {
            View row = LayoutInflater.from(getContext()).inflate(R.layout.item_category_progress_row, layoutCategoryProgressList, false);
            TextView tvCatName = row.findViewById(R.id.tvCategoryName);
            TextView tvCatAmount = row.findViewById(R.id.tvCategoryAmount);
            ProgressBar pbCat = row.findViewById(R.id.pbCategory);

            tvCatName.setText(entry.getKey());
            tvCatAmount.setText("₹" + String.format("%,.0f", entry.getValue()));

            int catPct = budget > 0 ? (int) Math.round((entry.getValue() / budget) * 100) : 0;
            pbCat.setProgress(Math.min(100, Math.max(0, catPct)));

            layoutCategoryProgressList.addView(row);
        }
    }
}
