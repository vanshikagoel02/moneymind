package com.example.myapplication.model;

public class TransactionItem {
    private long id;
    private String type; // "Income" or "Expense"
    private double amount;
    private String category;
    private String date; // YYYY-MM-DD
    private String description;
    private String note;
    private String paymentMethod;
    private String receiptUri;
    private String placeName;
    private double latitude;
    private double longitude;
    private long createdAt;

    public TransactionItem() {}

    public TransactionItem(long id, String type, double amount, String category, String date, String description,
                           String note, String paymentMethod, String receiptUri, String placeName,
                           double latitude, double longitude, long createdAt) {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.description = description;
        this.note = note;
        this.paymentMethod = paymentMethod;
        this.receiptUri = receiptUri;
        this.placeName = placeName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getReceiptUri() { return receiptUri; }
    public void setReceiptUri(String receiptUri) { this.receiptUri = receiptUri; }

    public String getPlaceName() { return placeName; }
    public void setPlaceName(String placeName) { this.placeName = placeName; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public boolean isExpense() {
        return "Expense".equalsIgnoreCase(type);
    }
}
