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
import com.example.myapplication.db.MoneyRepository;
import com.example.myapplication.model.LoanItem;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class LoansFragment extends Fragment {

    private EditText etPrincipal, etRate, etTenure;
    private TextView tvEmiResult, tvInterestResult, tvLoansEmpty;
    private RecyclerView rvLoans;

    private MoneyRepository repo;
    private LoanAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_loans, container, false);
        repo = MoneyRepository.getInstance(requireContext());

        etPrincipal = view.findViewById(R.id.etEmiPrincipal);
        etRate = view.findViewById(R.id.etEmiRate);
        etTenure = view.findViewById(R.id.etEmiTenure);
        tvEmiResult = view.findViewById(R.id.tvEmiResultValue);
        tvInterestResult = view.findViewById(R.id.tvEmiInterestValue);
        tvLoansEmpty = view.findViewById(R.id.tvLoansEmpty);
        rvLoans = view.findViewById(R.id.rvLoanRecords);

        rvLoans.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new LoanAdapter(repo.getLoans());
        rvLoans.setAdapter(adapter);

        view.findViewById(R.id.btnCalculateEmi).setOnClickListener(v -> calculateEMI());
        view.findViewById(R.id.btnAddLoanRecord).setOnClickListener(v -> showAddLoanDialog());

        return view;
    }

    private void calculateEMI() {
        String pStr = etPrincipal.getText().toString().trim();
        String rStr = etRate.getText().toString().trim();
        String tStr = etTenure.getText().toString().trim();

        if (pStr.isEmpty() || rStr.isEmpty() || tStr.isEmpty()) {
            Toast.makeText(getContext(), "Please enter all EMI parameters.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double p = Double.parseDouble(pStr);
            double r = Double.parseDouble(rStr);
            int n = Integer.parseInt(tStr);

            LoanItem item = new LoanItem(0, "Calc", p, r, n, p, "");
            double emi = item.calculateEMI();
            double totalPayment = emi * n;
            double totalInterest = Math.max(0, totalPayment - p);

            tvEmiResult.setText("₹" + String.format(Locale.getDefault(), "%,.2f", emi));
            tvInterestResult.setText("₹" + String.format(Locale.getDefault(), "%,.2f", totalInterest));
        } catch (Exception e) {
            Toast.makeText(getContext(), "Invalid calculation inputs.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    public void refreshData() {
        if (getContext() == null) return;
        List<LoanItem> loans = repo.getLoans();
        if (loans.isEmpty()) {
            tvLoansEmpty.setVisibility(View.VISIBLE);
            rvLoans.setVisibility(View.GONE);
        } else {
            tvLoansEmpty.setVisibility(View.GONE);
            rvLoans.setVisibility(View.VISIBLE);
            adapter.setItems(loans);
        }
    }

    private void showAddLoanDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_loan);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        }

        EditText etName = dialog.findViewById(R.id.etLoanName);
        EditText etP = dialog.findViewById(R.id.etLoanPrincipal);
        EditText etR = dialog.findViewById(R.id.etLoanInterest);
        EditText etT = dialog.findViewById(R.id.etLoanTenure);
        EditText etOut = dialog.findViewById(R.id.etLoanOutstanding);
        TextView tvNextDate = dialog.findViewById(R.id.tvLoanNextDate);
        Button btnSave = dialog.findViewById(R.id.btnSaveLoan);
        Button btnClose = dialog.findViewById(R.id.btnCloseLoan);

        tvNextDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            DatePickerDialog dpd = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
                String formatted = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                tvNextDate.setText(formatted);
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            dpd.show();
        });

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String pStr = etP.getText().toString().trim();
            String rStr = etR.getText().toString().trim();
            String tStr = etT.getText().toString().trim();
            String outStr = etOut.getText().toString().trim();
            String nextDate = tvNextDate.getText().toString().trim();

            if (name.isEmpty() || pStr.isEmpty() || rStr.isEmpty() || tStr.isEmpty()) {
                Toast.makeText(getContext(), "Fill in loan name and parameters.", Toast.LENGTH_SHORT).show();
                return;
            }

            double p = Double.parseDouble(pStr);
            double r = Double.parseDouble(rStr);
            int t = Integer.parseInt(tStr);
            double out = outStr.isEmpty() ? p : Double.parseDouble(outStr);

            LoanItem item = new LoanItem(0, name, p, r, t, out, nextDate);
            repo.addLoan(item);
            Toast.makeText(getContext(), "Loan record added!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            refreshData();
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private static class LoanAdapter extends RecyclerView.Adapter<LoanAdapter.ViewHolder> {
        private List<LoanItem> items;
        LoanAdapter(List<LoanItem> items) { this.items = items; }
        void setItems(List<LoanItem> items) { this.items = items; notifyDataSetChanged(); }

        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loan_row, parent, false);
            return new ViewHolder(view);
        }

        @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            LoanItem item = items.get(position);
            holder.tvName.setText(item.getName());
            holder.tvEmi.setText("EMI: ₹" + String.format(Locale.getDefault(), "%,.2f", item.calculateEMI()));
            holder.tvSub.setText("Principal: ₹" + String.format(Locale.getDefault(), "%,.0f", item.getPrincipal()) + " · Outstanding: ₹" + String.format(Locale.getDefault(), "%,.0f", item.getOutstanding()));
        }

        @Override public int getItemCount() { return items != null ? items.size() : 0; }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvEmi, tvSub;
            ViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvLoanName);
                tvEmi = itemView.findViewById(R.id.tvLoanEmi);
                tvSub = itemView.findViewById(R.id.tvLoanSub);
            }
        }
    }
}
