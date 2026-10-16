package com.example.myapplication.model;

public class GoalItem {
    private long id;
    private String name;
    private double targetAmount;
    private double currentAmount;

    public GoalItem() {}

    public GoalItem(long id, String name, double targetAmount, double currentAmount) {
        this.id = id;
        this.name = name;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getTargetAmount() { return targetAmount; }
    public void setTargetAmount(double targetAmount) { this.targetAmount = targetAmount; }

    public double getCurrentAmount() { return currentAmount; }
    public void setCurrentAmount(double currentAmount) { this.currentAmount = currentAmount; }

    public int getPercentComplete() {
        if (targetAmount <= 0) return 0;
        int pct = (int) Math.round((currentAmount / targetAmount) * 100);
        return Math.min(100, Math.max(0, pct));
    }
}
