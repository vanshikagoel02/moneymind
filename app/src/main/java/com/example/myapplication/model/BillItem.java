package com.example.myapplication.model;

public class BillItem {
    private long id;
    private String name;
    private double amount;
    private String dueDate; // YYYY-MM-DD
    private boolean isPaid;
    private String paidDate;

    public BillItem() {}

    public BillItem(long id, String name, double amount, String dueDate, boolean isPaid, String paidDate) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.dueDate = dueDate;
        this.isPaid = isPaid;
        this.paidDate = paidDate;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public boolean isPaid() { return isPaid; }
    public void setPaid(boolean paid) { isPaid = paid; }

    public String getPaidDate() { return paidDate; }
    public void setPaidDate(String paidDate) { this.paidDate = paidDate; }
}
