package com.example.myapplication.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.db.MoneyRepository;

import java.util.Map;

public class InsightsFragment extends Fragment {

    private TextView tvIncome, tvExpenses, tvSavingsRate, tvHighestCategory;
    private LinearLayout layoutSixMonthChart, layoutWhereItWentList, layoutWeekdayChart, layoutPaymentMethodsList;

    private MoneyRepository repo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_insights, container, false);
        repo = MoneyRepository.getInstance(requireContext());

        tvIncome = view.findViewById(R.id.tvInsightsIncome);
        tvExpenses = view.findViewById(R.id.tvInsightsExpenses);
        tvSavingsRate = view.findViewById(R.id.tvInsightsSavingsRate);
        tvHighestCategory = view.findViewById(R.id.tvHighestCategory);

        layoutSixMonthChart = view.findViewById(R.id.layoutSixMonthChart);
        layoutWhereItWentList = view.findViewById(R.id.layoutWhereItWentList);
        layoutWeekdayChart = view.findViewById(R.id.layoutWeekdayChart);
        layoutPaymentMethodsList = view.findViewById(R.id.layoutPaymentMethodsList);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    public void refreshData() {
        if (getContext() == null) return;

        double income = repo.getIncomeThisMonth();
        double expenses = repo.getExpensesThisMonth();

        tvIncome.setText("₹" + String.format("%,.0f", income));
        tvExpenses.setText("₹" + String.format("%,.0f", expenses));

        int savingsRate = income > 0 ? (int) Math.round(((income - expenses) / income) * 100) : 0;
        tvSavingsRate.setText(Math.max(0, savingsRate) + "%");

        // Where it went
        layoutWhereItWentList.removeAllViews();
        Map<String, Double> catMap = repo.getCategoryTotalsThisMonth();
        String highestCat = "";
        double highestVal = 0;

        for (Map.Entry<String, Double> entry : catMap.entrySet()) {
            if (entry.getValue() > highestVal) {
                highestVal = entry.getValue();
                highestCat = entry.getKey();
            }

            View row = LayoutInflater.from(getContext()).inflate(R.layout.item_category_progress_row, layoutWhereItWentList, false);
            TextView tvCatName = row.findViewById(R.id.tvCategoryName);
            TextView tvCatAmount = row.findViewById(R.id.tvCategoryAmount);
            ProgressBar pbCat = row.findViewById(R.id.pbCategory);

            tvCatName.setText(entry.getKey());
            tvCatAmount.setText("₹" + String.format("%,.0f", entry.getValue()));
            int pct = expenses > 0 ? (int) Math.round((entry.getValue() / expenses) * 100) : 0;
            pbCat.setProgress(Math.min(100, Math.max(0, pct)));

            layoutWhereItWentList.addView(row);
        }

        if (!highestCat.isEmpty()) {
            tvHighestCategory.setText("📚 Highest category: " + highestCat + " at ₹" + String.format("%,.0f", highestVal));
        } else {
            tvHighestCategory.setText("No expense records yet this month.");
        }

        // 6 Month Chart
        setupSixMonthChart();

        // Weekday Chart
        setupWeekdayChart();

        // Payment Methods List
        setupPaymentMethods();
    }

    private void setupSixMonthChart() {
        layoutSixMonthChart.removeAllViews();
        String[] months = {"May", "Jun", "Jul", "Aug", "Sept", "Oct"};
        double maxVal = 10000.0;

        for (int i = 0; i < months.length; i++) {
            LinearLayout col = new LinearLayout(getContext());
            col.setOrientation(LinearLayout.VERTICAL);
            col.setGravity(android.view.Gravity.BOTTOM | android.view.Gravity.CENTER_HORIZONTAL);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
            col.setLayoutParams(lp);

            LinearLayout bars = new LinearLayout(getContext());
            bars.setOrientation(LinearLayout.HORIZONTAL);
            bars.setGravity(android.view.Gravity.BOTTOM);

            View barIn = new View(getContext());
            View barOut = new View(getContext());

            double inVal = (i == 5) ? repo.getIncomeThisMonth() : 0;
            double outVal = (i == 5) ? repo.getExpensesThisMonth() : 0;

            int hIn = (int) Math.max(8, (inVal / maxVal) * 140);
            int hOut = (int) Math.max(8, (outVal / maxVal) * 140);

            LinearLayout.LayoutParams lpIn = new LinearLayout.LayoutParams(16, hIn);
            lpIn.setMargins(2, 0, 2, 0);
            barIn.setLayoutParams(lpIn);
            barIn.setBackgroundColor(0xFF7B9BB8);

            LinearLayout.LayoutParams lpOut = new LinearLayout.LayoutParams(16, hOut);
            lpOut.setMargins(2, 0, 2, 0);
            barOut.setLayoutParams(lpOut);
            barOut.setBackgroundColor(0xFFC59A27);

            bars.addView(barIn);
            bars.addView(barOut);

            TextView tvLabel = new TextView(getContext());
            tvLabel.setText(months[i]);
            tvLabel.setTextSize(11f);
            tvLabel.setTextColor(getResources().getColor(R.color.text_secondary, null));
            tvLabel.setGravity(android.view.Gravity.CENTER);

            col.addView(bars);
            col.addView(tvLabel);

            layoutSixMonthChart.addView(col);
        }
    }

    private void setupWeekdayChart() {
        layoutWeekdayChart.removeAllViews();
        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        double maxVal = 2000.0;

        for (int i = 0; i < days.length; i++) {
            LinearLayout col = new LinearLayout(getContext());
            col.setOrientation(LinearLayout.VERTICAL);
            col.setGravity(android.view.Gravity.BOTTOM | android.view.Gravity.CENTER_HORIZONTAL);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
            col.setLayoutParams(lp);

            View bar = new View(getContext());
            double val = (i == 4) ? repo.getExpensesThisMonth() : 0; // Thursday sample
            int h = (int) Math.max(8, (val / maxVal) * 110);

            LinearLayout.LayoutParams barLp = new LinearLayout.LayoutParams(24, h);
            bar.setLayoutParams(barLp);
            bar.setBackgroundColor(0xFFC59A27);

            TextView tvLabel = new TextView(getContext());
            tvLabel.setText(days[i]);
            tvLabel.setTextSize(11f);
            tvLabel.setTextColor(getResources().getColor(R.color.text_secondary, null));
            tvLabel.setGravity(android.view.Gravity.CENTER);

            col.addView(bar);
            col.addView(tvLabel);

            layoutWeekdayChart.addView(col);
        }
    }

    private void setupPaymentMethods() {
        layoutPaymentMethodsList.removeAllViews();
        Map<String, Double> map = repo.getPaymentMethodTotalsThisMonth();
        double totalExp = repo.getExpensesThisMonth();

        for (Map.Entry<String, Double> entry : map.entrySet()) {
            View row = LayoutInflater.from(getContext()).inflate(R.layout.item_category_progress_row, layoutPaymentMethodsList, false);
            TextView tvName = row.findViewById(R.id.tvCategoryName);
            TextView tvAmount = row.findViewById(R.id.tvCategoryAmount);
            ProgressBar pb = row.findViewById(R.id.pbCategory);

            tvName.setText(entry.getKey());
            tvAmount.setText("₹" + String.format("%,.0f", entry.getValue()));
            int pct = totalExp > 0 ? (int) Math.round((entry.getValue() / totalExp) * 100) : 0;
            pb.setProgress(Math.min(100, Math.max(0, pct)));

            layoutPaymentMethodsList.addView(row);
        }
    }
}
