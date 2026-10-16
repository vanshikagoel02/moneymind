package com.example.myapplication.model;

public class WatchlistItem {
    private long id;
    private String symbol;
    private String name;
    private String note;

    public WatchlistItem() {}

    public WatchlistItem(long id, String symbol, String name, String note) {
        this.id = id;
        this.symbol = symbol;
        this.name = name;
        this.note = note;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
