package com.example.myapplication.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.adapter.BillAdapter;
import com.example.myapplication.adapter.TransactionAdapter;
import com.example.myapplication.db.MoneyRepository;
import com.example.myapplication.model.BillItem;
import com.example.myapplication.model.GoalItem;
import com.example.myapplication.model.TransactionItem;
import com.example.myapplication.view.DonutProgressView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private TextView tvHeaderDateUpper, tvGreeting, tvAvailableBalance, tvBalanceSubText;
    private TextView tvCardIncomeReceived, tvCardSpentSoFar, tvCardKept;
    private TextView tvBudgetRemaining, tvBudgetFromTotal, tvBudgetStatusPace;
    private TextView tvHomeIncome, tvHomeSpent, tvHomeSavingsRate;
    private TextView tvWin1, tvWin2, tvWin3;

    private Spinner spinnerTimeFilter;
    private DonutProgressView donutProgressView;
    private RecyclerView rvHomeRecentActivity, rvHomeBillsDue;

    private MoneyRepository repo;
    private TransactionAdapter transactionAdapter;
    private BillAdapter billAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        repo = MoneyRepository.getInstance(requireContext());

        tvHeaderDateUpper = view.findViewById(R.id.tvHeaderDateUpper);
        tvGreeting = view.findViewById(R.id.tvGreeting);
        tvAvailableBalance = view.findViewById(R.id.tvAvailableBalance);
        tvBalanceSubText = view.findViewById(R.id.tvBalanceSubText);

        tvCardIncomeReceived = view.findViewById(R.id.tvCardIncomeReceived);
        tvCardSpentSoFar = view.findViewById(R.id.tvCardSpentSoFar);
        tvCardKept = view.findViewById(R.id.tvCardKept);

        tvBudgetRemaining = view.findViewById(R.id.tvBudgetRemaining);
        tvBudgetFromTotal = view.findViewById(R.id.tvBudgetFromTotal);
        tvBudgetStatusPace = view.findViewById(R.id.tvBudgetStatusPace);

        tvHomeIncome = view.findViewById(R.id.tvHomeIncome);
        tvHomeSpent = view.findViewById(R.id.tvHomeSpent);
        tvHomeSavingsRate = view.findViewById(R.id.tvHomeSavingsRate);

        tvWin1 = view.findViewById(R.id.tvWin1);
        tvWin2 = view.findViewById(R.id.tvWin2);
        tvWin3 = view.findViewById(R.id.tvWin3);

        spinnerTimeFilter = view.findViewById(R.id.spinnerHomeTimeFilter);
        donutProgressView = view.findViewById(R.id.donutProgressView);

        rvHomeRecentActivity = view.findViewById(R.id.rvHomeRecentActivity);
        rvHomeBillsDue = view.findViewById(R.id.rvHomeBillsDue);

        rvHomeRecentActivity.setLayoutManager(new LinearLayoutManager(getContext()));
        rvHomeBillsDue.setLayoutManager(new LinearLayoutManager(getContext()));

        // Spinner Setup
        String[] timeFilters = {"This month ˅", "Last month ˅", "This year ˅"};
        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, timeFilters);
        spinnerTimeFilter.setAdapter(timeAdapter);

        transactionAdapter = new TransactionAdapter(new ArrayList<>(), new TransactionAdapter.OnTransactionClickListener() {
            @Override
            public void onTransactionClick(TransactionItem item) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).showAddTransactionDialog(item);
                }
            }

            @Override
            public void onTransactionOptionClick(TransactionItem item, View anchor) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).showAddTransactionDialog(item);
                }
            }
        });
        rvHomeRecentActivity.setAdapter(transactionAdapter);

        billAdapter = new BillAdapter(new ArrayList<>(), bill -> {
            repo.markBillPaid(bill.getId());
            refreshData();
        });
        rvHomeBillsDue.setAdapter(billAdapter);

        // Navigation Clicks
        view.findViewById(R.id.btnHomeAddExpense).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) ((MainActivity) getActivity()).showAddTransactionDialog("Expense");
        });
        view.findViewById(R.id.btnHomeAddIncome).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) ((MainActivity) getActivity()).showAddTransactionDialog("Income");
        });
        view.findViewById(R.id.btnHomeSeeActivity).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) ((MainActivity) getActivity()).navigateToTab(1);
        });
        view.findViewById(R.id.btnSeeAllActivity).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) ((MainActivity) getActivity()).navigateToTab(1);
        });
        view.findViewById(R.id.btnReviewBudget).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) ((MainActivity) getActivity()).navigateToTab(2);
        });
        view.findViewById(R.id.btnSeeAllBills).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) ((MainActivity) getActivity()).navigateToTab(6);
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

        // Header upper date
        String upperDate = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(new Date()).toUpperCase();
        tvHeaderDateUpper.setText(upperDate);

        String name = repo.getUserName();
        tvGreeting.setText("Welcome back, " + name.split(" ")[0]);

        double balance = repo.getAvailableBalance();
        double income = repo.getIncomeThisMonth();
        double spent = repo.getExpensesThisMonth();
        double budget = repo.getMonthlyBudget();
        double remaining = budget - spent;

        tvAvailableBalance.setText("₹" + String.format(Locale.getDefault(), "%,.0f", balance));
        String monthName = new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(new Date());
        tvBalanceSubText.setText(monthName + " · across your tracked accounts");

        tvCardIncomeReceived.setText("₹" + String.format(Locale.getDefault(), "%,.0f", income));
        tvCardSpentSoFar.setText("₹" + String.format(Locale.getDefault(), "%,.0f", spent));
        tvCardKept.setText("₹" + String.format(Locale.getDefault(), "%,.0f", Math.max(0, balance)));

        tvBudgetRemaining.setText("₹" + String.format(Locale.getDefault(), "%,.0f", Math.max(0, remaining)));
        tvBudgetFromTotal.setText("remaining from ₹" + String.format(Locale.getDefault(), "%,.0f", budget));

        int pct = budget > 0 ? (int) Math.round((spent / budget) * 100) : 0;
        pct = Math.min(100, Math.max(0, pct));
        if (donutProgressView != null) {
            donutProgressView.setProgress(pct);
        }

        if (remaining >= 0) {
            tvBudgetStatusPace.setText("Spending is within your planned pace.");
            tvBudgetStatusPace.setTextColor(getResources().getColor(R.color.text_secondary, null));
        } else {
            tvBudgetStatusPace.setText("Over planned spending pace.");
            tvBudgetStatusPace.setTextColor(getResources().getColor(R.color.expense_red, null));
        }

        tvHomeIncome.setText("₹" + String.format(Locale.getDefault(), "%,.0f", income));
        tvHomeSpent.setText("₹" + String.format(Locale.getDefault(), "%,.0f", spent));

        int savingsRate = income > 0 ? (int) Math.round(((income - spent) / income) * 100) : 0;
        tvHomeSavingsRate.setText(Math.max(0, savingsRate) + "%");

        // Transactions & Bills
        List<TransactionItem> allTx = repo.getAllTransactions();
        List<TransactionItem> recentTx = allTx.size() > 4 ? allTx.subList(0, 4) : allTx;
        transactionAdapter.setItems(recentTx);

        List<BillItem> allBills = repo.getBills();
        List<BillItem> unpaidBills = new ArrayList<>();
        for (BillItem b : allBills) {
            if (!b.isPaid()) unpaidBills.add(b);
        }
        billAdapter.setItems(unpaidBills);

        // Money Wins
        tvWin1.setText("Kept ₹" + String.format(Locale.getDefault(), "%,.0f", Math.max(0, balance)) + " of this month’s income");
        tvWin2.setText("You’re ₹" + String.format(Locale.getDefault(), "%,.0f", Math.max(0, remaining)) + " under your monthly plan");

        List<GoalItem> goals = repo.getGoals();
        if (!goals.isEmpty()) {
            GoalItem g = goals.get(0);
            tvWin3.setText(g.getName() + " is " + g.getPercentComplete() + "% funded");
        }
    }
}
