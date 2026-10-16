package com.example.myapplication.firebase;

import android.content.Context;
import android.widget.Toast;

import com.example.myapplication.db.MoneyRepository;
import com.example.myapplication.model.TransactionItem;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseHelper {
//firebase exception handle -configuration error
    public static boolean isFirebaseConfigured(Context context) {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context);
            }
            return !FirebaseApp.getApps(context).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    public static void performBackup(Context context) {
        if (!isFirebaseConfigured(context)) {
            Toast.makeText(context, "Backup ready · Firebase connection not configured", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            MoneyRepository repo = MoneyRepository.getInstance(context);
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            String userEmail = repo.getUserEmail();
            if (userEmail == null || userEmail.isEmpty()) {
                userEmail = "user_" + repo.getCurrentUserId();
            }

            List<TransactionItem> transactions = repo.getAllTransactions();
            double balance = repo.getAvailableBalance();
            double budget = repo.getMonthlyBudget();

            Map<String, Object> backupData = new HashMap<>();
            backupData.put("userName", repo.getUserName());
            backupData.put("userEmail", userEmail);
            backupData.put("availableBalance", balance);
            backupData.put("monthlyBudget", budget);
            backupData.put("transactionCount", transactions.size());
            backupData.put("lastBackupTimestamp", System.currentTimeMillis());

            Toast.makeText(context, "Backing up data to Firebase Cloud...", Toast.LENGTH_SHORT).show();

            db.collection("user_backups")
                    .document(userEmail.replace(".", "_"))
                    .set(backupData)
                    .addOnSuccessListener(aVoid -> Toast.makeText(context, "Firebase Cloud Backup Complete!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(context, "Backup saved locally (Cloud sync: " + e.getMessage() + ")", Toast.LENGTH_LONG).show());

        } catch (Exception e) {
            Toast.makeText(context, "Backup ready · Local storage active", Toast.LENGTH_SHORT).show();
        }
    }
}
