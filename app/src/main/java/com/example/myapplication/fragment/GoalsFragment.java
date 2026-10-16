package com.example.myapplication.fragment;

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
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapter.GoalAdapter;
import com.example.myapplication.db.MoneyRepository;
import com.example.myapplication.model.GoalItem;

import java.util.ArrayList;
import java.util.List;

public class GoalsFragment extends Fragment {

    private RecyclerView rvGoals;
    private MoneyRepository repo;
    private GoalAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_goals, container, false);
        repo = MoneyRepository.getInstance(requireContext());

        rvGoals = view.findViewById(R.id.rvGoalsList);
        rvGoals.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new GoalAdapter(new ArrayList<>(), new GoalAdapter.OnGoalClickListener() {
            @Override
            public void onAddSavingsClick(GoalItem goal) {
                showAddSavingsDialog(goal);
            }

            @Override
            public void onGoalOptionClick(GoalItem goal, View anchor) {
                showGoalOptionMenu(goal, anchor);
            }
        });
        rvGoals.setAdapter(adapter);

        view.findViewById(R.id.btnAddGoal).setOnClickListener(v -> showAddGoalDialog());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    public void refreshData() {
        if (getContext() == null) return;
        List<GoalItem> goals = repo.getGoals();
        adapter.setItems(goals);
    }

    private void showAddGoalDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_goal);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        }

        EditText etName = dialog.findViewById(R.id.etGoalName);
        EditText etTarget = dialog.findViewById(R.id.etGoalTarget);
        EditText etInitial = dialog.findViewById(R.id.etGoalInitial);
        Button btnSave = dialog.findViewById(R.id.btnSaveGoal);
        Button btnClose = dialog.findViewById(R.id.btnCloseGoal);

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String targetStr = etTarget.getText().toString().trim();
            String initStr = etInitial.getText().toString().trim();

            if (name.isEmpty()) {
                etName.setError("Name required");
                return;
            }
            if (targetStr.isEmpty()) {
                etTarget.setError("Target required");
                return;
            }

            double target = Double.parseDouble(targetStr);
            double initial = initStr.isEmpty() ? 0 : Double.parseDouble(initStr);

            GoalItem goal = new GoalItem(0, name, target, initial);
            repo.addGoal(goal);
            Toast.makeText(getContext(), "Goal created!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            refreshData();
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showAddSavingsDialog(GoalItem goal) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setTitle("Add savings to " + goal.getName());
        EditText etAmt = new EditText(requireContext());
        etAmt.setHint("Amount (₹)");
        etAmt.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);
        layout.addView(etAmt);

        Button btnAdd = new Button(requireContext());
        btnAdd.setText("Add savings");
        btnAdd.setBackgroundColor(getResources().getColor(R.color.navy_primary, null));
        btnAdd.setTextColor(Color.WHITE);
        layout.addView(btnAdd);

        dialog.setContentView(layout);

        btnAdd.setOnClickListener(v -> {
            String val = etAmt.getText().toString().trim();
            if (!val.isEmpty()) {
                double amt = Double.parseDouble(val);
                repo.addSavingsToGoals(goal.getId(), amt);
                Toast.makeText(getContext(), "Savings added to " + goal.getName(), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                refreshData();
            }
        });

        dialog.show();
    }

    private void showGoalOptionMenu(GoalItem goal, View anchor) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenu().add("Delete goal");
        popup.setOnMenuItemClickListener(item -> {
            if ("Delete goal".equals(item.getTitle())) {
                repo.deleteGoal(goal.getId());
                Toast.makeText(getContext(), "Goal deleted", Toast.LENGTH_SHORT).show();
                refreshData();
            }
            return true;
        });
        popup.show();
    }
}
