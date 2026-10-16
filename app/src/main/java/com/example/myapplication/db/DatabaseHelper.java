package com.example.myapplication.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

// sqlite manager
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "moneymind.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_USERS = "users";
    public static final String TABLE_TRANSACTIONS = "transactions";
    public static final String TABLE_BUDGETS = "budgets";
    public static final String TABLE_BILLS = "bills";
    public static final String TABLE_GOALS = "goals";
    public static final String TABLE_LOANS = "loans";
    public static final String TABLE_WATCHLIST = "watchlist";
    public static final String TABLE_NOTIFICATIONS = "notifications";
    public static final String TABLE_MOOD = "mood";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // user table
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, email TEXT UNIQUE, password TEXT, is_logged_in INTEGER DEFAULT 0)");

        // transaction table
        db.execSQL("CREATE TABLE " + TABLE_TRANSACTIONS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER DEFAULT 1, " +
                "type TEXT, amount REAL, category TEXT, date TEXT, " +
                "description TEXT, note TEXT, payment_method TEXT, " +
                "receipt_uri TEXT, place_name TEXT, latitude REAL, longitude REAL, " +
                "created_at INTEGER)");

        // budget table
        db.execSQL("CREATE TABLE " + TABLE_BUDGETS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER DEFAULT 1, " +
                "month TEXT, amount REAL)");

        // bill table
        db.execSQL("CREATE TABLE " + TABLE_BILLS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER DEFAULT 1, " +
                "name TEXT, amount REAL, due_date TEXT, is_paid INTEGER DEFAULT 0, paid_date TEXT)");

        // goal table
        db.execSQL("CREATE TABLE " + TABLE_GOALS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER DEFAULT 1, " +
                "name TEXT, target_amount REAL, current_amount REAL)");

        // loan table
        db.execSQL("CREATE TABLE " + TABLE_LOANS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER DEFAULT 1, " +
                "name TEXT, principal REAL, annual_interest REAL, tenure_months INTEGER, outstanding REAL, next_payment_date TEXT)");

        // watchlist table
        db.execSQL("CREATE TABLE " + TABLE_WATCHLIST + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER DEFAULT 1, " +
                "symbol TEXT, name TEXT, note TEXT)");

        // alert table
        db.execSQL("CREATE TABLE " + TABLE_NOTIFICATIONS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER DEFAULT 1, " +
                "title TEXT, message TEXT, type TEXT)");

        // mood table
        db.execSQL("CREATE TABLE " + TABLE_MOOD + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER DEFAULT 1, " +
                "mood TEXT, note TEXT, date TEXT)");

        // seed records
        seedDemoData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUDGETS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BILLS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GOALS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOANS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WATCHLIST);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MOOD);
        onCreate(db);
    }

    // seed data
    public void seedDemoData(SQLiteDatabase db) {
        ContentValues userCv = new ContentValues();
        userCv.put("id", 1);
        userCv.put("name", "Riya Sharma");
        userCv.put("email", "riya@example.com");
        userCv.put("password", "password123");
        userCv.put("is_logged_in", 1);
        db.insertWithOnConflict(TABLE_USERS, null, userCv, SQLiteDatabase.CONFLICT_REPLACE);

        ContentValues budgetCv = new ContentValues();
        budgetCv.put("user_id", 1);
        budgetCv.put("month", "2026-10");
        budgetCv.put("amount", 18000.0);
        db.insert(TABLE_BUDGETS, null, budgetCv);

        insertTx(db, 1, "Expense", 1250.0, "Education", "2026-10-08", "Course books", "Textbooks for 3rd sem", "UPI", null, null, 0, 0);
        insertTx(db, 1, "Expense", 540.0, "Travel", "2026-10-08", "Metro card", "Monthly recharge", "UPI", null, null, 0, 0);
        insertTx(db, 1, "Income", 8000.0, "Pocket Money", "2026-10-08", "Pocket money", "Monthly allowance", "UPI", null, null, 0, 0);

        ContentValues bill1 = new ContentValues();
        bill1.put("user_id", 1);
        bill1.put("name", "Netflix (shared)");
        bill1.put("amount", 199.0);
        bill1.put("due_date", "2026-10-10");
        bill1.put("is_paid", 0);
        db.insert(TABLE_BILLS, null, bill1);

        ContentValues bill2 = new ContentValues();
        bill2.put("user_id", 1);
        bill2.put("name", "Hostel Wi-Fi share");
        bill2.put("amount", 300.0);
        bill2.put("due_date", "2026-10-14");
        bill2.put("is_paid", 0);
        db.insert(TABLE_BILLS, null, bill2);

        ContentValues goal1 = new ContentValues();
        goal1.put("user_id", 1);
        goal1.put("name", "Goa weekend");
        goal1.put("target_amount", 18000.0);
        goal1.put("current_amount", 7200.0);
        db.insert(TABLE_GOALS, null, goal1);

        insertWatchlist(db, 1, "NIFTY 50", "NIFTY 50", "Index • NSE");
        insertWatchlist(db, 1, "TATA", "Tata Technologies", "Saved for reference");
        insertWatchlist(db, 1, "HDFCBANK", "HDFC Bank", "Saved for reference");

        insertNotification(db, 1, "Hostel Wi-Fi share due soon", "₹300 • Due in 2 days", "due");
        insertNotification(db, 1, "Netflix (shared) is overdue", "₹199 • 2 days overdue", "warning");
        insertNotification(db, 1, "🥳 We're getting a little tight.", "Only ₹134 left for food this month.", "warning");
        insertNotification(db, 1, "🥳 We're getting a little tight.", "Only ₹500 left for hostel this month.", "warning");
        insertNotification(db, 1, "Oops! We crossed the line 😳", "₹8 over on entertainment. No stress — let's get back on track.", "warning");
        insertNotification(db, 1, "Your Goa goal is taking shape", "A little more saved brings the trip closer.", "info");

        ContentValues moodCv = new ContentValues();
        moodCv.put("user_id", 1);
        moodCv.put("mood", "Calm");
        moodCv.put("note", "Your finances are a work in progress, just like everything else.");
        moodCv.put("date", "2026-10-12");
        db.insert(TABLE_MOOD, null, moodCv);
    }

    private void insertTx(SQLiteDatabase db, long userId, String type, double amount, String category, String date,
                          String desc, String note, String method, String receipt, String place, double lat, double lng) {
        ContentValues cv = new ContentValues();
        cv.put("user_id", userId);
        cv.put("type", type);
        cv.put("amount", amount);
        cv.put("category", category);
        cv.put("date", date);
        cv.put("description", desc);
        cv.put("note", note);
        cv.put("payment_method", method);
        cv.put("receipt_uri", receipt);
        cv.put("place_name", place);
        cv.put("latitude", lat);
        cv.put("longitude", lng);
        cv.put("created_at", System.currentTimeMillis());
        db.insert(TABLE_TRANSACTIONS, null, cv);
    }

    private void insertWatchlist(SQLiteDatabase db, long userId, String symbol, String name, String note) {
        ContentValues cv = new ContentValues();
        cv.put("user_id", userId);
        cv.put("symbol", symbol);
        cv.put("name", name);
        cv.put("note", note);
        db.insert(TABLE_WATCHLIST, null, cv);
    }

    private void insertNotification(SQLiteDatabase db, long userId, String title, String message, String type) {
        ContentValues cv = new ContentValues();
        cv.put("user_id", userId);
        cv.put("title", title);
        cv.put("message", message);
        cv.put("type", type);
        db.insert(TABLE_NOTIFICATIONS, null, cv);
    }
}
