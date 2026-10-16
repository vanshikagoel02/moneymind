package com.example.myapplication.fragment;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.adapter.TransactionAdapter;
import com.example.myapplication.db.MoneyRepository;
import com.example.myapplication.model.TransactionItem;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ActivityFragment extends Fragment {

    private EditText etSearch;
    private TextView chipAll, chipOut, chipIn;
    private Spinner spinnerCategory, spinnerMethod, spinnerSort;
    private TextView tvFromDate, tvToDate, btnClearDates, tvSummary;
    private RecyclerView rvTransactions;

    private MoneyRepository repo;
    private TransactionAdapter adapter;

    private String selectedType = "All";
    private String selectedCategory = "All categories";
    private String selectedMethod = "Any method";
    private String selectedSort = "Newest first";
    private String fromDate = "";
    private String toDate = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activity, container, false);
        repo = MoneyRepository.getInstance(requireContext());

        etSearch = view.findViewById(R.id.etActSearch);
        chipAll = view.findViewById(R.id.chipTypeAll);
        chipOut = view.findViewById(R.id.chipTypeOut);
        chipIn = view.findViewById(R.id.chipTypeIn);
        spinnerCategory = view.findViewById(R.id.spinnerActCategory);
        spinnerMethod = view.findViewById(R.id.spinnerActMethod);
        spinnerSort = view.findViewById(R.id.spinnerActSort);
        tvFromDate = view.findViewById(R.id.tvActFromDate);
        tvToDate = view.findViewById(R.id.tvActToDate);
        btnClearDates = view.findViewById(R.id.btnActClearDates);
        tvSummary = view.findViewById(R.id.tvActSummary);
        rvTransactions = view.findViewById(R.id.rvActivityTransactions);

        rvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TransactionAdapter(new ArrayList<>(), new TransactionAdapter.OnTransactionClickListener() {
            @Override
            public void onTransactionClick(TransactionItem item) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).showAddTransactionDialog(item);
                }
            }

            @Override
            public void onTransactionOptionClick(TransactionItem item, View anchor) {
                showTxPopupMenu(item, anchor);
            }
        });
        rvTransactions.setAdapter(adapter);

        setupSpinners();
        setupFilterClicks();

        view.findViewById(R.id.btnActAddExpense).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) ((MainActivity) getActivity()).showAddTransactionDialog("Expense");
        });
        view.findViewById(R.id.btnActAddIncome).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) ((MainActivity) getActivity()).showAddTransactionDialog("Income");
        });

        return view;
    }

    private void setupSpinners() {
        String[] categories = {"All categories", "Education", "Travel", "Pocket Money", "Food", "Bills", "Shopping", "Entertainment", "Other"};
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories);
        spinnerCategory.setAdapter(catAdapter);

        String[] methods = {"Any method", "UPI", "Cash", "Card", "Bank Transfer"};
        ArrayAdapter<String> methodAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, methods);
        spinnerMethod.setAdapter(methodAdapter);

        String[] sorts = {"Newest first", "Oldest first", "Amount high-low", "Amount low-high"};
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, sorts);
        spinnerSort.setAdapter(sortAdapter);

        spinnerCategory.setOnItemSelectedListener(new SimpleSpinnerListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = categories[position];
                filterData();
            }
        });

        spinnerMethod.setOnItemSelectedListener(new SimpleSpinnerListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedMethod = methods[position];
                filterData();
            }
        });

        spinnerSort.setOnItemSelectedListener(new SimpleSpinnerListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSort = sorts[position];
                filterData();
            }
        });
    }

    private void setupFilterClicks() {
        chipAll.setOnClickListener(v -> selectTypeChip("All"));
        chipOut.setOnClickListener(v -> selectTypeChip("Out"));
        chipIn.setOnClickListener(v -> selectTypeChip("In"));

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filterData(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        tvFromDate.setOnClickListener(v -> showDatePicker(true));
        tvToDate.setOnClickListener(v -> showDatePicker(false));

        btnClearDates.setOnClickListener(v -> {
            fromDate = "";
            toDate = "";
            tvFromDate.setText("dd-mm-yyyy");
            tvToDate.setText("dd-mm-yyyy");
            filterData();
        });
    }

    private void selectTypeChip(String type) {
        selectedType = type;
        chipAll.setBackgroundColor(type.equals("All") ? getResources().getColor(R.color.bg_card_white, null) : 0);
        chipOut.setBackgroundColor(type.equals("Out") ? getResources().getColor(R.color.bg_card_white, null) : 0);
        chipIn.setBackgroundColor(type.equals("In") ? getResources().getColor(R.color.bg_card_white, null) : 0);
        filterData();
    }

    private void showDatePicker(boolean isFrom) {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dpd = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            String formatted = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            if (isFrom) {
                fromDate = formatted;
                tvFromDate.setText(formatted);
            } else {
                toDate = formatted;
                tvToDate.setText(formatted);
            }
            filterData();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        dpd.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        filterData();
    }

    public void filterData() {
        if (getContext() == null) return;
        String query = etSearch.getText().toString();
        List<TransactionItem> list = repo.getTransactionsFiltered(query, selectedType, selectedCategory, selectedMethod, selectedSort, fromDate, toDate);
        adapter.setItems(list);

        double totalOut = 0;
        double totalIn = 0;
        for (TransactionItem item : list) {
            if (item.isExpense()) totalOut += item.getAmount();
            else totalIn += item.getAmount();
        }

        tvSummary.setText(list.size() + " entries · ₹" + String.format(Locale.getDefault(), "%,.0f", totalOut) +
                " out · ₹" + String.format(Locale.getDefault(), "%,.0f", totalIn) + " in");
    }

    private void showTxPopupMenu(TransactionItem item, View anchor) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenu().add("Edit");
        popup.getMenu().add("Delete");
        popup.setOnMenuItemClickListener(menuItem -> {
            if ("Edit".equals(menuItem.getTitle())) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).showAddTransactionDialog(item);
                }
            } else if ("Delete".equals(menuItem.getTitle())) {
                repo.deleteTransaction(item.getId());
                filterData();
                if (getView() != null) {
                    Snackbar.make(getView(), "Transaction deleted", Snackbar.LENGTH_LONG)
                            .setAction("UNDO", v -> {
                                repo.addTransaction(item);
                                filterData();
                            }).show();
                }
            }
            return true;
        });
        popup.show();
    }

    private abstract static class SimpleSpinnerListener implements AdapterView.OnItemSelectedListener {
        @Override public void onNothingSelected(AdapterView<?> parent) {}
    }
}
