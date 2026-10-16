package com.example.myapplication.model;

public class LoanItem {
    private long id;
    private String name;
    private double principal;
    private double annualInterest;
    private int tenureMonths;
    private double outstanding;
    private String nextPaymentDate;

    public LoanItem() {}

    public LoanItem(long id, String name, double principal, double annualInterest, int tenureMonths, double outstanding, String nextPaymentDate) {
        this.id = id;
        this.name = name;
        this.principal = principal;
        this.annualInterest = annualInterest;
        this.tenureMonths = tenureMonths;
        this.outstanding = outstanding;
        this.nextPaymentDate = nextPaymentDate;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrincipal() { return principal; }
    public void setPrincipal(double principal) { this.principal = principal; }

    public double getAnnualInterest() { return annualInterest; }
    public void setAnnualInterest(double annualInterest) { this.annualInterest = annualInterest; }

    public int getTenureMonths() { return tenureMonths; }
    public void setTenureMonths(int tenureMonths) { this.tenureMonths = tenureMonths; }

    public double getOutstanding() { return outstanding; }
    public void setOutstanding(double outstanding) { this.outstanding = outstanding; }

    public String getNextPaymentDate() { return nextPaymentDate; }
    public void setNextPaymentDate(String nextPaymentDate) { this.nextPaymentDate = nextPaymentDate; }

    public double calculateEMI() {
        if (principal <= 0 || tenureMonths <= 0) return 0;
        if (annualInterest <= 0) return principal / tenureMonths;
        double monthlyRate = (annualInterest / 12) / 100;
        return (principal * monthlyRate * Math.pow(1 + monthlyRate, tenureMonths)) / (Math.pow(1 + monthlyRate, tenureMonths) - 1);
    }
}
