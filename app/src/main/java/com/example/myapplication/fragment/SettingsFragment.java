package com.example.myapplication.fragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.myapplication.AuthActivity;
import com.example.myapplication.R;
import com.example.myapplication.db.MoneyRepository;
import com.example.myapplication.firebase.FirebaseHelper;
import com.example.myapplication.model.TransactionItem;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SettingsFragment extends Fragment {

    private EditText etName, etEmail;
    private TextView tvBackupStatus;
    private MoneyRepository repo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        repo = MoneyRepository.getInstance(requireContext());

        etName = view.findViewById(R.id.etSettingsName);
        etEmail = view.findViewById(R.id.etSettingsEmail);
        tvBackupStatus = view.findViewById(R.id.tvBackupStatus);

        etName.setText(repo.getUserName());
        etEmail.setText(repo.getUserEmail());

        refreshBackupStatus();

        view.findViewById(R.id.btnSaveProfile).setOnClickListener(v -> {
            String n = etName.getText().toString().trim();
            String e = etEmail.getText().toString().trim();
            if (!n.isEmpty() && !e.isEmpty()) {
                repo.updateUserProfile(n, e);
                Toast.makeText(getContext(), "Profile saved in this demo", Toast.LENGTH_SHORT).show();
            }
        });

        view.findViewById(R.id.btnLogout).setOnClickListener(v -> handleLogout());
        view.findViewById(R.id.btnBackupData).setOnClickListener(v -> FirebaseHelper.performBackup(requireContext()));
        view.findViewById(R.id.btnExportData).setOnClickListener(v -> showExportDialog());
        view.findViewById(R.id.btnClearDemoData).setOnClickListener(v -> {
            repo.resetDemoData();
            Toast.makeText(getContext(), "All demo records cleared and reset!", Toast.LENGTH_SHORT).show();
            if (getActivity() instanceof com.example.myapplication.MainActivity) {
                ((com.example.myapplication.MainActivity) getActivity()).refreshCurrentFragment();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshBackupStatus();
    }

    private void refreshBackupStatus() {
        if (tvBackupStatus != null && repo != null) {
            int count = repo.getAllTransactions().size();
            tvBackupStatus.setText("Local storage active · " + count + " transactions");
        }
    }

    private void handleLogout() {
        repo.setLoggedIn(false);
        Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(requireContext(), AuthActivity.class));
        if (getActivity() != null) getActivity().finish();
    }

    private void showExportDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_export);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        }

        Spinner spinnerFormat = dialog.findViewById(R.id.spinnerExportFormat);
        TextView tvFrom = dialog.findViewById(R.id.tvExportFromDate);
        TextView tvTo = dialog.findViewById(R.id.tvExportToDate);
        Button btnDownload = dialog.findViewById(R.id.btnDownloadExport);
        Button btnClose = dialog.findViewById(R.id.btnCloseExport);

        String[] formats = {"CSV · spreadsheet", "TXT · readable list"};
        ArrayAdapter<String> formatAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, formats);
        spinnerFormat.setAdapter(formatAdapter);

        tvFrom.setOnClickListener(v -> showDatePicker(tvFrom));
        tvTo.setOnClickListener(v -> showDatePicker(tvTo));

        btnDownload.setOnClickListener(v -> {
            String format = spinnerFormat.getSelectedItem().toString();
            String from = tvFrom.getText().toString().equals("dd-mm-yyyy") ? "" : tvFrom.getText().toString();
            String to = tvTo.getText().toString().equals("dd-mm-yyyy") ? "" : tvTo.getText().toString();

            exportTransactionsFile(format, from, to);
            dialog.dismiss();
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showDatePicker(TextView target) {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dpd = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            String formatted = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            target.setText(formatted);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        dpd.show();
    }

    private void exportTransactionsFile(String format, String from, String to) {
        List<TransactionItem> list = repo.getTransactionsFiltered("", "All", "All categories", "Any method", "Newest first", from, to);
        boolean isCsv = format.startsWith("CSV");

        double totalIn = 0;
        double totalOut = 0;
        for (TransactionItem t : list) {
            if (t.isExpense()) totalOut += t.getAmount();
            else totalIn += t.getAmount();
        }

        StringBuilder sb = new StringBuilder();
        if (isCsv) {
            sb.append("Transaction ID,Date,Type,Category,Amount (INR),Description,Note,Payment Method,Place Name,Latitude,Longitude,Receipt Attached,Created Timestamp\n");
            for (TransactionItem t : list) {
                sb.append(t.getId()).append(",")
                        .append(t.getDate()).append(",")
                        .append(t.getType()).append(",")
                        .append(t.getCategory()).append(",")
                        .append(String.format(Locale.getDefault(), "%.2f", t.getAmount())).append(",")
                        .append("\"").append(t.getDescription() != null ? t.getDescription().replace("\"", "\"\"") : "").append("\",")
                        .append("\"").append(t.getNote() != null ? t.getNote().replace("\"", "\"\"") : "").append("\",")
                        .append(t.getPaymentMethod() != null ? t.getPaymentMethod() : "").append(",")
                        .append("\"").append(t.getPlaceName() != null ? t.getPlaceName().replace("\"", "\"\"") : "").append("\",")
                        .append(t.getLatitude()).append(",")
                        .append(t.getLongitude()).append(",")
                        .append(t.getReceiptUri() != null && !t.getReceiptUri().isEmpty() ? "Yes" : "No").append(",")
                        .append(t.getCreatedAt()).append("\n");
            }
        } else {
            String todayStr = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
            String userStr = repo.getUserName() + " (" + repo.getUserEmail() + ")";

            sb.append("================================================================================\n");
            sb.append("                         MONEY MIND FINANCIAL REPORT                            \n");
            sb.append("================================================================================\n");
            sb.append("Account Holder : ").append(userStr).append("\n");
            sb.append("Export Date    : ").append(todayStr).append("\n");
            sb.append("Date Filter    : ").append(from.isEmpty() ? "All" : from).append(" to ").append(to.isEmpty() ? "All" : to).append("\n");
            sb.append("Total Entries  : ").append(list.size()).append("\n");
            sb.append("--------------------------------------------------------------------------------\n");
            sb.append("SUMMARY:\n");
            sb.append("Total Income   : ₹").append(String.format(Locale.getDefault(), "%,.2f", totalIn)).append("\n");
            sb.append("Total Expenses : ₹").append(String.format(Locale.getDefault(), "%,.2f", totalOut)).append("\n");
            sb.append("Net Balance    : ₹").append(String.format(Locale.getDefault(), "%,.2f", totalIn - totalOut)).append("\n");
            sb.append("--------------------------------------------------------------------------------\n\n");

            sb.append("TRANSACTION RECORDS:\n");
            sb.append("--------------------------------------------------------------------------------\n");
            for (int i = 0; i < list.size(); i++) {
                TransactionItem t = list.get(i);
                sb.append("[").append(i + 1).append("] ID: ").append(t.getId())
                        .append(" | Date: ").append(t.getDate())
                        .append(" | Type: ").append(t.getType()).append("\n");
                sb.append("    Description : ").append(t.getDescription()).append("\n");
                sb.append("    Category    : ").append(t.getCategory()).append("\n");
                sb.append("    Amount      : ₹").append(String.format(Locale.getDefault(), "%,.2f", t.getAmount())).append("\n");
                sb.append("    Payment     : ").append(t.getPaymentMethod()).append("\n");
                if (t.getNote() != null && !t.getNote().isEmpty()) {
                    sb.append("    Note        : ").append(t.getNote()).append("\n");
                }
                if (t.getPlaceName() != null && !t.getPlaceName().isEmpty()) {
                    sb.append("    Location    : ").append(t.getPlaceName()).append("\n");
                }
                sb.append("--------------------------------------------------------------------------------\n");
            }
            sb.append("                  End of Money Mind Exported Report                             \n");
            sb.append("================================================================================\n");
        }
//file i/o exception
        try {
            File dir = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            if (dir == null) dir = requireContext().getFilesDir();
            String filename = "MoneyMind_Export_" + System.currentTimeMillis() + (isCsv ? ".csv" : ".txt");
            File file = new File(dir, filename);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(sb.toString().getBytes());
            fos.close();

            // Generate FileProvider Uri and Share/View Intent
            Uri contentUri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".fileprovider", file);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType(isCsv ? "text/csv" : "text/plain");
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            if (getView() != null) {
                Snackbar.make(getView(), "File downloaded: " + filename, Snackbar.LENGTH_LONG)
                        .setAction("SHARE / OPEN", v -> startActivity(Intent.createChooser(shareIntent, "Share exported file")))
                        .show();
            } else {
                Toast.makeText(getContext(), "Exported to " + file.getName(), Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            Toast.makeText(getContext(), "Failed to export file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
