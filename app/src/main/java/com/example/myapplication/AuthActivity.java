package com.example.myapplication;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.db.MoneyRepository;

public class AuthActivity extends AppCompatActivity {

    private TextView tabLogin, tabSignup, tvAuthHeader, tvAuthSubtitle, btnForgot, btnBack;
    private LinearLayout layoutName;
    private EditText etName, etEmail, etPassword;
    private Button btnPrimaryAction, btnDemoAccount;

    private boolean isSignUpMode = false;
    private MoneyRepository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repo = MoneyRepository.getInstance(this);

        // If already logged in, launch MainActivity directly
        if (repo.isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_auth);

        tabLogin = findViewById(R.id.tabLogin);
        tabSignup = findViewById(R.id.tabSignup);
        tvAuthHeader = findViewById(R.id.tvAuthHeader);
        tvAuthSubtitle = findViewById(R.id.tvAuthSubtitle);
        btnForgot = findViewById(R.id.btnForgot);
        btnBack = findViewById(R.id.btnBack);
        layoutName = findViewById(R.id.layoutName);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnPrimaryAction = findViewById(R.id.btnPrimaryAction);
        btnDemoAccount = findViewById(R.id.btnDemoAccount);

        tabLogin.setOnClickListener(v -> setSignUpMode(false));
        tabSignup.setOnClickListener(v -> setSignUpMode(true));

        btnPrimaryAction.setOnClickListener(v -> handleAuthAction());
        btnDemoAccount.setOnClickListener(v -> handleDemoLogin());
        btnForgot.setOnClickListener(v -> showResetPasswordDialog());
        btnBack.setOnClickListener(v -> setSignUpMode(false));
    }

    private void setSignUpMode(boolean signUp) {
        isSignUpMode = signUp;
        if (signUp) {
            tabLogin.setBackgroundColor(Color.TRANSPARENT);
            tabLogin.setTextColor(getResources().getColor(R.color.text_secondary, getTheme()));
            tabSignup.setBackgroundColor(getResources().getColor(R.color.white, getTheme()));
            tabSignup.setTextColor(getResources().getColor(R.color.text_primary, getTheme()));

            tvAuthHeader.setText("A good place to begin");
            tvAuthSubtitle.setText("Log in to view and manage your money.");
            layoutName.setVisibility(View.VISIBLE);
            btnPrimaryAction.setText("Create account");
        } else {
            tabSignup.setBackgroundColor(Color.TRANSPARENT);
            tabSignup.setTextColor(getResources().getColor(R.color.text_secondary, getTheme()));
            tabLogin.setBackgroundColor(getResources().getColor(R.color.white, getTheme()));
            tabLogin.setTextColor(getResources().getColor(R.color.text_primary, getTheme()));

            tvAuthHeader.setText("Welcome back");
            tvAuthSubtitle.setText("Log in to view and manage your money.");
            layoutName.setVisibility(View.GONE);
            btnPrimaryAction.setText("Log in");
        }
    }

    private void handleAuthAction() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            return;
        }
        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            return;
        }

        if (isSignUpMode) {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) name = "Riya Sharma";
            boolean ok = repo.signup(name, email, password);
            if (ok) {
                Toast.makeText(this, "Account created! Welcome to Money Mind.", Toast.LENGTH_SHORT).show();
                launchMain();
            } else {
                Toast.makeText(this, "Failed to create account.", Toast.LENGTH_SHORT).show();
            }
        } else {
            boolean success = repo.login(email, password);
            if (success) {
                Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show();
                launchMain();
            } else {
                Toast.makeText(this, "Invalid credentials.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void handleDemoLogin() {
        repo.setDemoAccountActive();
        Toast.makeText(this, "Signed in with Demo Account!", Toast.LENGTH_SHORT).show();
        launchMain();
    }

    private void showResetPasswordDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_reset_password);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        }

        EditText etResetEmail = dialog.findViewById(R.id.etResetEmail);
        Button btnSendReset = dialog.findViewById(R.id.btnSendReset);
        Button btnCloseReset = dialog.findViewById(R.id.btnCloseReset);

        btnSendReset.setOnClickListener(v -> {
            String mail = etResetEmail.getText().toString().trim();
            if (mail.isEmpty()) {
                etResetEmail.setError("Email required");
            } else {
                Toast.makeText(this, "Reset link prepared for " + mail, Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });

        btnCloseReset.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void launchMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
