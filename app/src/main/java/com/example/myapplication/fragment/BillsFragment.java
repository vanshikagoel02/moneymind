package com.example.myapplication.fragment;

import android.app.DatePickerDialog;
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
import com.example.myapplication.adapter.BillAdapter;
import com.example.myapplication.db.MoneyRepository;
import com.example.myapplication.model.BillItem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class BillsFragment extends Fragment {

    private TextView tvStillToPay, tvUpcomingCount, tvPaidCount;
    private RecyclerView rvUpcoming, rvPaid;

    private MoneyRepository repo;
    private BillAdapter upcomingAdapter, paidAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bills, container, false);
        repo = MoneyRepository.getInstance(requireContext());

        tvStillToPay = view.findViewById(R.id.tvBillsStillToPay);
        tvUpcomingCount = view.findViewById(R.id.tvBillsUpcomingCount);
        tvPaidCount = view.findViewById(R.id.tvBillsPaidCount);

        rvUpcoming = view.findViewById(R.id.rvBillsUpcoming);
        rvPaid = view.findViewById(R.id.rvBillsPaid);

        rvUpcoming.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPaid.setLayoutManager(new LinearLayoutManager(getContext()));

        upcomingAdapter = new BillAdapter(new ArrayList<>(), bill -> {
            repo.markBillPaid(bill.getId());
            Toast.makeText(getContext(), "Bill marked as paid!", Toast.LENGTH_SHORT).show();
            refreshData();
        });
        rvUpcoming.setAdapter(upcomingAdapter);

        paidAdapter = new BillAdapter(new ArrayList<>(), bill -> {});
        rvPaid.setAdapter(paidAdapter);

        view.findViewById(R.id.btnAddBill).setOnClickListener(v -> showAddBillDialog());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    public void refreshData() {
        if (getContext() == null) return;
        List<BillItem> all = repo.getBills();
        List<BillItem> upcoming = new ArrayList<>();
        List<BillItem> paid = new ArrayList<>();
        double stillToPay = 0;

        for (BillItem b : all) {
            if (b.isPaid()) {
                paid.add(b);
            } else {
                upcoming.add(b);
                stillToPay += b.getAmount();
            }
        }

        tvStillToPay.setText("₹" + String.format("%,.0f", stillToPay));
        tvUpcomingCount.setText(upcoming.size() + " upcoming or overdue bills");
        tvPaidCount.setText(String.valueOf(paid.size()));

        upcomingAdapter.setItems(upcoming);
        paidAdapter.setItems(paid);
    }

    private void showAddBillDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_bill);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        }

        EditText etName = dialog.findViewById(R.id.etBillName);
        EditText etAmount = dialog.findViewById(R.id.etBillAmount);
        TextView tvDueDate = dialog.findViewById(R.id.tvBillDueDate);
        Button btnSave = dialog.findViewById(R.id.btnSaveBill);
        Button btnClose = dialog.findViewById(R.id.btnCloseBill);

        tvDueDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            DatePickerDialog dpd = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
                String formatted = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                tvDueDate.setText(formatted);
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            dpd.show();
        });

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String amtStr = etAmount.getText().toString().trim();
            String date = tvDueDate.getText().toString().trim();

            if (name.isEmpty()) {
                etName.setError("Name required");
                return;
            }
            if (amtStr.isEmpty()) {
                etAmount.setError("Amount required");
                return;
            }

            double amt = Double.parseDouble(amtStr);
            BillItem bill = new BillItem(0, name, amt, date, false, null);
            repo.addBill(bill);
            Toast.makeText(getContext(), "Bill reminder saved!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            refreshData();
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}
