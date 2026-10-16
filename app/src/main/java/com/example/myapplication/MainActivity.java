package com.example.myapplication;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.NotificationAdapter;
import com.example.myapplication.db.MoneyRepository;
import com.example.myapplication.fragment.ActivityFragment;
import com.example.myapplication.fragment.BillsFragment;
import com.example.myapplication.fragment.BudgetFragment;
import com.example.myapplication.fragment.GoalsFragment;
import com.example.myapplication.fragment.HomeFragment;
import com.example.myapplication.fragment.InsightsFragment;
import com.example.myapplication.fragment.MapFragment;
import com.example.myapplication.fragment.MoreFragment;
import com.example.myapplication.fragment.SettingsFragment;
import com.example.myapplication.model.NotificationItem;
import com.example.myapplication.model.TransactionItem;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private FrameLayout fragmentContainer;
    private LinearLayout[] navItems;
    private ImageView[] navIcons;
    private TextView[] navTexts;

    private Fragment currentFragment;
    private int currentTabIndex = 0;
    private MoneyRepository repo;

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private String tempReceiptUri = "";
    private TextView tvReceiptStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        repo = MoneyRepository.getInstance(this);
        if (!repo.isLoggedIn()) {
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        fragmentContainer = findViewById(R.id.fragment_container);

        // Top bar
        findViewById(R.id.btnTopAdd).setOnClickListener(v -> showAddChoiceDialog());
        findViewById(R.id.btnTopBadge).setOnClickListener(v -> showHeadsUpDialog());
        findViewById(R.id.btnTopAvatar).setOnClickListener(v -> showAccountDialog());

        TextView tvAvatarLetter = findViewById(R.id.tvAvatarLetter);
        String name = repo.getUserName();
        if (name != null && !name.isEmpty()) {
            tvAvatarLetter.setText(String.valueOf(name.charAt(0)));
        }

        setupBottomNavigation();

        // Image picker launcher
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                Uri selectedImage = result.getData().getData();
                tempReceiptUri = selectedImage.toString();
                if (tvReceiptStatus != null) {
                    tvReceiptStatus.setText("✓ Receipt attached");
                    tvReceiptStatus.setTextColor(getResources().getColor(R.color.income_green, getTheme()));
                }
                Toast.makeText(this, "Receipt photo attached!", Toast.LENGTH_SHORT).show();
            }
        });

        // Default tab: Home
        navigateToTab(0);
    }

    private void setupBottomNavigation() {
        navItems = new LinearLayout[]{
                findViewById(R.id.navHome),
                findViewById(R.id.navActivity),
                findViewById(R.id.navBudget),
                findViewById(R.id.navInsights),
                findViewById(R.id.navMap),
                findViewById(R.id.navGoals),
                findViewById(R.id.navBills),
                findViewById(R.id.navMore),
                findViewById(R.id.navSettings)
        };

        navIcons = new ImageView[]{
                findViewById(R.id.icHome),
                findViewById(R.id.icActivity),
                findViewById(R.id.icBudget),
                findViewById(R.id.icInsights),
                findViewById(R.id.icMap),
                findViewById(R.id.icGoals),
                findViewById(R.id.icBills),
                findViewById(R.id.icMore),
                findViewById(R.id.icSettings)
        };

        navTexts = new TextView[]{
                findViewById(R.id.tvHome),
                findViewById(R.id.tvActivity),
                findViewById(R.id.tvBudget),
                findViewById(R.id.tvInsights),
                findViewById(R.id.tvMap),
                findViewById(R.id.tvGoals),
                findViewById(R.id.tvBills),
                findViewById(R.id.tvMore),
                findViewById(R.id.tvSettings)
        };

        for (int i = 0; i < navItems.length; i++) {
            final int index = i;
            navItems[i].setOnClickListener(v -> navigateToTab(index));
        }
    }

    public void navigateToTab(int index) {
        currentTabIndex = index;

        // Highlight active bottom tab
        for (int i = 0; i < navItems.length; i++) {
            if (i == index) {
                navItems[i].setBackgroundResource(R.drawable.bg_card_warm);
                navItems[i].setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.nav_active_bg, getTheme())));
                navIcons[i].setColorFilter(getResources().getColor(R.color.navy_primary, getTheme()));
                navTexts[i].setTextColor(getResources().getColor(R.color.navy_primary, getTheme()));
                navTexts[i].setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                navItems[i].setBackgroundResource(0);
                navItems[i].setBackgroundTintList(null);
                navIcons[i].setColorFilter(getResources().getColor(R.color.nav_inactive_text, getTheme()));
                navTexts[i].setTextColor(getResources().getColor(R.color.nav_inactive_text, getTheme()));
                navTexts[i].setTypeface(null, android.graphics.Typeface.NORMAL);
            }
        }

        switch (index) {
            case 0: currentFragment = new HomeFragment(); break;
            case 1: currentFragment = new ActivityFragment(); break;
            case 2: currentFragment = new BudgetFragment(); break;
            case 3: currentFragment = new InsightsFragment(); break;
            case 4: currentFragment = new MapFragment(); break;
            case 5: currentFragment = new GoalsFragment(); break;
            case 6: currentFragment = new BillsFragment(); break;
            case 7: currentFragment = new MoreFragment(); break;
            case 8: currentFragment = new SettingsFragment(); break;
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, currentFragment)
                .commit();
    }

    public void replaceSubFragment(Fragment fragment) {
        currentFragment = fragment;
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    public void refreshCurrentFragment() {
        navigateToTab(currentTabIndex);
    }

    public void showAddChoiceDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_choice);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        }

        dialog.findViewById(R.id.choiceBtnExpense).setOnClickListener(v -> {
            dialog.dismiss();
            showAddTransactionDialog("Expense");
        });

        dialog.findViewById(R.id.choiceBtnIncome).setOnClickListener(v -> {
            dialog.dismiss();
            showAddTransactionDialog("Income");
        });

        dialog.findViewById(R.id.choiceBtnClose).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    public void showAddTransactionDialog(String type) {
        showAddTransactionDialogInternal(type, null, "", 0, 0);
    }

    public void showAddTransactionDialog(TransactionItem editItem) {
        showAddTransactionDialogInternal(editItem.getType(), editItem, editItem.getPlaceName(), editItem.getLatitude(), editItem.getLongitude());
    }

    public void showAddTransactionWithPlace(String type, String placeName, double lat, double lng) {
        showAddTransactionDialogInternal(type, null, placeName, lat, lng);
    }

    private void showAddTransactionDialogInternal(String type, TransactionItem editItem, String placeName, double lat, double lng) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_transaction);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        }

        TextView tvTitle = dialog.findViewById(R.id.tvTxTitle);
        EditText etAmount = dialog.findViewById(R.id.etTxAmount);
        Spinner spinnerCategory = dialog.findViewById(R.id.spinnerTxCategory);
        TextView tvDate = dialog.findViewById(R.id.tvTxDate);
        Spinner spinnerMethod = dialog.findViewById(R.id.spinnerTxMethod);
        EditText etDescription = dialog.findViewById(R.id.etTxDescription);
        EditText etNote = dialog.findViewById(R.id.etTxNote);
        Button btnReceipt = dialog.findViewById(R.id.btnTxReceipt);
        Button btnLocation = dialog.findViewById(R.id.btnTxLocation);
        TextView tvPlaceSelected = dialog.findViewById(R.id.tvTxPlaceSelected);
        Button btnSave = dialog.findViewById(R.id.btnTxSave);
        Button btnClose = dialog.findViewById(R.id.btnTxClose);

        boolean isExpense = "Expense".equalsIgnoreCase(type);
        tvTitle.setText(isExpense ? "Add expense" : "Add income");
        btnSave.setText(isExpense ? "Save expense" : "Save income");

        // Spinner options
        String[] categories = isExpense ?
                new String[]{"Food", "Travel", "Education", "Shopping", "Entertainment", "Bills", "Other"} :
                new String[]{"Salary", "Pocket Money", "Allowance", "Freelance", "Gift", "Other"};
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        spinnerCategory.setAdapter(catAdapter);

        String[] methods = {"UPI", "Cash", "Card", "Bank Transfer"};
        ArrayAdapter<String> methodAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, methods);
        spinnerMethod.setAdapter(methodAdapter);

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        tvDate.setText(today);

        tempReceiptUri = "";
        final String[] currentPlace = {placeName != null ? placeName : ""};
        final double[] currentLat = {lat};
        final double[] currentLng = {lng};

        if (editItem != null) {
            etAmount.setText(String.format(Locale.getDefault(), "%.0f", editItem.getAmount()));
            tvDate.setText(editItem.getDate());
            etDescription.setText(editItem.getDescription());
            etNote.setText(editItem.getNote());
            tempReceiptUri = editItem.getReceiptUri() != null ? editItem.getReceiptUri() : "";
            currentPlace[0] = editItem.getPlaceName() != null ? editItem.getPlaceName() : "";
            currentLat[0] = editItem.getLatitude();
            currentLng[0] = editItem.getLongitude();
        }

        if (!currentPlace[0].isEmpty()) {
            tvPlaceSelected.setVisibility(View.VISIBLE);
            tvPlaceSelected.setText("Place: " + currentPlace[0]);
        }

        tvReceiptStatus = btnReceipt;
        if (!tempReceiptUri.isEmpty()) {
            btnReceipt.setText("✓ Receipt attached");
        }

        tvDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            DatePickerDialog dpd = new DatePickerDialog(this, (view1, year, month, dayOfMonth) -> {
                String formatted = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                tvDate.setText(formatted);
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            dpd.show();
        });

        btnReceipt.setOnClickListener(v -> {
            if (!tempReceiptUri.isEmpty()) {
                showReceiptPreviewDialog(tempReceiptUri, () -> {
                    tempReceiptUri = "";
                    btnReceipt.setText("+ Add receipt");
                });
            } else {
                Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                imagePickerLauncher.launch(pickIntent);
            }
        });

        btnLocation.setOnClickListener(v -> {
            dialog.dismiss();
            navigateToTab(4); // Map tab
        });

        btnSave.setOnClickListener(v -> {
            String amtStr = etAmount.getText().toString().trim();
            if (amtStr.isEmpty()) {
                etAmount.setError("Amount required");
                return;
            }

            double amount = Double.parseDouble(amtStr);
            if (amount <= 0) {
                etAmount.setError("Amount must be greater than 0");
                return;
            }

            String cat = spinnerCategory.getSelectedItem().toString();
            String dateVal = tvDate.getText().toString().trim();
            String methodVal = spinnerMethod.getSelectedItem().toString();
            String desc = etDescription.getText().toString().trim();
            String noteVal = etNote.getText().toString().trim();

            if (desc.isEmpty()) desc = cat;

            TransactionItem item = editItem != null ? editItem : new TransactionItem();
            item.setType(isExpense ? "Expense" : "Income");
            item.setAmount(amount);
            item.setCategory(cat);
            item.setDate(dateVal);
            item.setDescription(desc);
            item.setNote(noteVal);
            item.setPaymentMethod(methodVal);
            item.setReceiptUri(tempReceiptUri);
            item.setPlaceName(currentPlace[0]);
            item.setLatitude(currentLat[0]);
            item.setLongitude(currentLng[0]);

            if (editItem != null) {
                repo.updateTransaction(item);
                Toast.makeText(this, "Transaction updated!", Toast.LENGTH_SHORT).show();
            } else {
                repo.addTransaction(item);
                Toast.makeText(this, (isExpense ? "Expense" : "Income") + " added!", Toast.LENGTH_SHORT).show();
            }

            dialog.dismiss();
            refreshCurrentFragment();
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showReceiptPreviewDialog(String receiptUri, Runnable onRemove) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_receipt_preview);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        }

        ImageView ivPreview = dialog.findViewById(R.id.ivReceiptPreview);
        try {
            ivPreview.setImageURI(Uri.parse(receiptUri));
        } catch (Exception e) {
            ivPreview.setImageResource(R.drawable.ic_nav_bills);
        }

        dialog.findViewById(R.id.btnRemoveReceipt).setOnClickListener(v -> {
            if (onRemove != null) onRemove.run();
            dialog.dismiss();
            Toast.makeText(this, "Receipt removed.", Toast.LENGTH_SHORT).show();
        });

        dialog.findViewById(R.id.btnCloseReceiptPreview).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    public void showHeadsUpDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_heads_up);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        }

        RecyclerView rv = dialog.findViewById(R.id.rvNotifications);
        rv.setLayoutManager(new LinearLayoutManager(this));

        List<NotificationItem> list = repo.getNotifications();
        NotificationAdapter notifAdapter = new NotificationAdapter(list);
        rv.setAdapter(notifAdapter);

        dialog.findViewById(R.id.btnCloseHeadsUp).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    public void showAccountDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_account_summary);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        }

        TextView tvDetails = dialog.findViewById(R.id.tvAccountDetails);
        tvDetails.setText(repo.getUserName() + " · " + repo.getUserEmail());

        dialog.findViewById(R.id.btnAccountProfile).setOnClickListener(v -> {
            dialog.dismiss();
            navigateToTab(8); // Settings tab (index 8)
        });

        dialog.findViewById(R.id.btnAccountLogout).setOnClickListener(v -> {
            dialog.dismiss();
            repo.setLoggedIn(false);
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, AuthActivity.class));
            finish();
        });

        dialog.findViewById(R.id.btnAccountClose).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}
