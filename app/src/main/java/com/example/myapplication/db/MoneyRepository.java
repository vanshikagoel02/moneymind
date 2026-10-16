package com.example.myapplication.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.myapplication.model.BillItem;
import com.example.myapplication.model.GoalItem;
import com.example.myapplication.model.LoanItem;
import com.example.myapplication.model.NotificationItem;
import com.example.myapplication.model.TransactionItem;
import com.example.myapplication.model.WatchlistItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// data repository
public class MoneyRepository {

    private static MoneyRepository instance;
    private final DatabaseHelper dbHelper;

    private MoneyRepository(Context context) {
        dbHelper = new DatabaseHelper(context.getApplicationContext());
    }

    // singleton instance
    public static synchronized MoneyRepository getInstance(Context context) {
        if (instance == null) {
            instance = new MoneyRepository(context);
        }
        return instance;
    }

    // active user
    public long getCurrentUserId() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(DatabaseHelper.TABLE_USERS, new String[]{"id"}, "is_logged_in = 1", null, null, null, null);
        long userId = 1; // Default to demo user if none active
        if (c != null) {
            if (c.moveToFirst()) {
                userId = c.getLong(0);
            }
            c.close();
        }
        return userId;
    }

    // transaction list
    public List<TransactionItem> getAllTransactions() {
        return getTransactionsFiltered("", "All", "All categories", "Any method", "Newest first", "", "");
    }

    // filtered query
    public List<TransactionItem> getTransactionsFiltered(String query, String typeFilter, String categoryFilter,
                                                         String methodFilter, String sortOption, String fromDate, String toDate) {
        List<TransactionItem> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        StringBuilder where = new StringBuilder("user_id = ?");
        List<String> argsList = new ArrayList<>();
        argsList.add(String.valueOf(getCurrentUserId()));

        if (query != null && !query.trim().isEmpty()) {
            where.append(" AND (description LIKE ? OR note LIKE ? OR category LIKE ? OR amount LIKE ?)");
            String q = "%" + query.trim() + "%";
            argsList.add(q);
            argsList.add(q);
            argsList.add(q);
            argsList.add(q);
        }

        if (typeFilter != null && !typeFilter.equals("All")) {
            where.append(" AND type = ?");
            argsList.add(typeFilter.equals("Out") ? "Expense" : "Income");
        }

        if (categoryFilter != null && !categoryFilter.equals("All categories") && !categoryFilter.isEmpty()) {
            where.append(" AND category = ?");
            argsList.add(categoryFilter);
        }

        if (methodFilter != null && !methodFilter.equals("Any method") && !methodFilter.isEmpty()) {
            where.append(" AND payment_method = ?");
            argsList.add(methodFilter);
        }

        if (fromDate != null && !fromDate.trim().isEmpty()) {
            where.append(" AND date >= ?");
            argsList.add(fromDate.trim());
        }

        if (toDate != null && !toDate.trim().isEmpty()) {
            where.append(" AND date <= ?");
            argsList.add(toDate.trim());
        }

        String orderBy = "date DESC, id DESC";
        if ("Oldest first".equals(sortOption)) {
            orderBy = "date ASC, id ASC";
        } else if ("Amount high-low".equals(sortOption)) {
            orderBy = "amount DESC";
        } else if ("Amount low-high".equals(sortOption)) {
            orderBy = "amount ASC";
        }

        Cursor c = db.query(DatabaseHelper.TABLE_TRANSACTIONS, null, where.toString(),
                argsList.toArray(new String[0]), null, null, orderBy);

        if (c != null) {
            while (c.moveToNext()) {
                list.add(cursorToTransaction(c));
            }
            c.close();
        }
        return list;
    }

    public TransactionItem getTransactionById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(DatabaseHelper.TABLE_TRANSACTIONS, null, "id = ? AND user_id = ?",
                new String[]{String.valueOf(id), String.valueOf(getCurrentUserId())}, null, null, null);
        TransactionItem item = null;
        if (c != null) {
            if (c.moveToFirst()) {
                item = cursorToTransaction(c);
            }
            c.close();
        }
        return item;
    }

    public long addTransaction(TransactionItem item) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = transactionToContentValues(item);
        cv.put("user_id", getCurrentUserId());
        if (item.getCreatedAt() == 0) {
            cv.put("created_at", System.currentTimeMillis());
        }
        long id = db.insert(DatabaseHelper.TABLE_TRANSACTIONS, null, cv);
        item.setId(id);
        return id;
    }

    public boolean updateTransaction(TransactionItem item) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = transactionToContentValues(item);
        int rows = db.update(DatabaseHelper.TABLE_TRANSACTIONS, cv, "id = ? AND user_id = ?",
                new String[]{String.valueOf(item.getId()), String.valueOf(getCurrentUserId())});
        return rows > 0;
    }

    public boolean deleteTransaction(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete(DatabaseHelper.TABLE_TRANSACTIONS, "id = ? AND user_id = ?",
                new String[]{String.valueOf(id), String.valueOf(getCurrentUserId())});
        return rows > 0;
    }

    private TransactionItem cursorToTransaction(Cursor c) {
        TransactionItem item = new TransactionItem();
        item.setId(c.getLong(c.getColumnIndexOrThrow("id")));
        item.setType(c.getString(c.getColumnIndexOrThrow("type")));
        item.setAmount(c.getDouble(c.getColumnIndexOrThrow("amount")));
        item.setCategory(c.getString(c.getColumnIndexOrThrow("category")));
        item.setDate(c.getString(c.getColumnIndexOrThrow("date")));
        item.setDescription(c.getString(c.getColumnIndexOrThrow("description")));
        item.setNote(c.getString(c.getColumnIndexOrThrow("note")));
        item.setPaymentMethod(c.getString(c.getColumnIndexOrThrow("payment_method")));
        item.setReceiptUri(c.getString(c.getColumnIndexOrThrow("receipt_uri")));
        item.setPlaceName(c.getString(c.getColumnIndexOrThrow("place_name")));
        item.setLatitude(c.getDouble(c.getColumnIndexOrThrow("latitude")));
        item.setLongitude(c.getDouble(c.getColumnIndexOrThrow("longitude")));
        item.setCreatedAt(c.getLong(c.getColumnIndexOrThrow("created_at")));
        return item;
    }

    private ContentValues transactionToContentValues(TransactionItem item) {
        ContentValues cv = new ContentValues();
        cv.put("type", item.getType());
        cv.put("amount", item.getAmount());
        cv.put("category", item.getCategory());
        cv.put("date", item.getDate());
        cv.put("description", item.getDescription());
        cv.put("note", item.getNote());
        cv.put("payment_method", item.getPaymentMethod());
        cv.put("receipt_uri", item.getReceiptUri());
        cv.put("place_name", item.getPlaceName());
        cv.put("latitude", item.getLatitude());
        cv.put("longitude", item.getLongitude());
        return cv;
    }

    // monthly income
    public double getIncomeThisMonth() {
        return getSum("Income", getCurrentMonthPrefix());
    }

    // monthly expenses
    public double getExpensesThisMonth() {
        return getSum("Expense", getCurrentMonthPrefix());
    }

    // net balance
    public double getAvailableBalance() {
        double income = getSum("Income", null);
        double expense = getSum("Expense", null);
        return income - expense;
    }

    private double getSum(String type, String monthPrefix) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT SUM(amount) FROM " + DatabaseHelper.TABLE_TRANSACTIONS + " WHERE type = ? AND user_id = ?";
        List<String> args = new ArrayList<>();
        args.add(type);
        args.add(String.valueOf(getCurrentUserId()));

        if (monthPrefix != null) {
            sql += " AND date LIKE ?";
            args.add(monthPrefix + "%");
        }

        Cursor c = db.rawQuery(sql, args.toArray(new String[0]));
        double sum = 0;
        if (c != null) {
            if (c.moveToFirst()) {
                sum = c.getDouble(0);
            }
            c.close();
        }
        return sum;
    }

    public Map<String, Double> getCategoryTotalsThisMonth() {
        Map<String, Double> map = new LinkedHashMap<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT category, SUM(amount) FROM " + DatabaseHelper.TABLE_TRANSACTIONS +
                " WHERE type = 'Expense' AND user_id = ? AND date LIKE ? GROUP BY category ORDER BY SUM(amount) DESC";
        Cursor c = db.rawQuery(sql, new String[]{String.valueOf(getCurrentUserId()), getCurrentMonthPrefix() + "%"});
        if (c != null) {
            while (c.moveToNext()) {
                map.put(c.getString(0), c.getDouble(1));
            }
            c.close();
        }
        return map;
    }

    public Map<String, Double> getPaymentMethodTotalsThisMonth() {
        Map<String, Double> map = new LinkedHashMap<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT payment_method, SUM(amount) FROM " + DatabaseHelper.TABLE_TRANSACTIONS +
                " WHERE type = 'Expense' AND user_id = ? AND date LIKE ? GROUP BY payment_method ORDER BY SUM(amount) DESC";
        Cursor c = db.rawQuery(sql, new String[]{String.valueOf(getCurrentUserId()), getCurrentMonthPrefix() + "%"});
        if (c != null) {
            while (c.moveToNext()) {
                map.put(c.getString(0), c.getDouble(1));
            }
            c.close();
        }
        return map;
    }

    public List<TransactionItem> getTransactionsWithLocation() {
        List<TransactionItem> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(DatabaseHelper.TABLE_TRANSACTIONS, null,
                "place_name IS NOT NULL AND place_name != '' AND user_id = ?",
                new String[]{String.valueOf(getCurrentUserId())}, null, null, "date DESC");
        if (c != null) {
            while (c.moveToNext()) {
                list.add(cursorToTransaction(c));
            }
            c.close();
        }
        return list;
    }

    private String getCurrentMonthPrefix() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        return sdf.format(new Date());
    }

    // --- BUDGET ---

    public double getMonthlyBudget() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(DatabaseHelper.TABLE_BUDGETS, new String[]{"amount"},
                "month = ? AND user_id = ?", new String[]{getCurrentMonthPrefix(), String.valueOf(getCurrentUserId())}, null, null, null);
        double budget = 12000.0;
        if (c != null) {
            if (c.moveToFirst()) {
                budget = c.getDouble(0);
            }
            c.close();
        }
        return budget;
    }

    public void setMonthlyBudget(double amount) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("user_id", getCurrentUserId());
        cv.put("month", getCurrentMonthPrefix());
        cv.put("amount", amount);
        int rows = db.update(DatabaseHelper.TABLE_BUDGETS, cv, "month = ? AND user_id = ?",
                new String[]{getCurrentMonthPrefix(), String.valueOf(getCurrentUserId())});
        if (rows == 0) {
            db.insert(DatabaseHelper.TABLE_BUDGETS, null, cv);
        }
    }

    // --- BILLS ---

    public List<BillItem> getBills() {
        List<BillItem> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(DatabaseHelper.TABLE_BILLS, null, "user_id = ?",
                new String[]{String.valueOf(getCurrentUserId())}, null, null, "due_date ASC");
        if (c != null) {
            while (c.moveToNext()) {
                BillItem b = new BillItem();
                b.setId(c.getLong(c.getColumnIndexOrThrow("id")));
                b.setName(c.getString(c.getColumnIndexOrThrow("name")));
                b.setAmount(c.getDouble(c.getColumnIndexOrThrow("amount")));
                b.setDueDate(c.getString(c.getColumnIndexOrThrow("due_date")));
                b.setPaid(c.getInt(c.getColumnIndexOrThrow("is_paid")) == 1);
                b.setPaidDate(c.getString(c.getColumnIndexOrThrow("paid_date")));
                list.add(b);
            }
            c.close();
        }
        return list;
    }

    public long addBill(BillItem bill) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("user_id", getCurrentUserId());
        cv.put("name", bill.getName());
        cv.put("amount", bill.getAmount());
        cv.put("due_date", bill.getDueDate());
        cv.put("is_paid", bill.isPaid() ? 1 : 0);
        cv.put("paid_date", bill.getPaidDate());
        long id = db.insert(DatabaseHelper.TABLE_BILLS, null, cv);
        bill.setId(id);
        return id;
    }

    public void markBillPaid(long billId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        BillItem bill = null;
        Cursor c = db.query(DatabaseHelper.TABLE_BILLS, null, "id = ? AND user_id = ?",
                new String[]{String.valueOf(billId), String.valueOf(getCurrentUserId())}, null, null, null);
        if (c != null) {
            if (c.moveToFirst()) {
                bill = new BillItem();
                bill.setId(c.getLong(c.getColumnIndexOrThrow("id")));
                bill.setName(c.getString(c.getColumnIndexOrThrow("name")));
                bill.setAmount(c.getDouble(c.getColumnIndexOrThrow("amount")));
            }
            c.close();
        }

        if (bill != null) {
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            ContentValues cv = new ContentValues();
            cv.put("is_paid", 1);
            cv.put("paid_date", today);
            db.update(DatabaseHelper.TABLE_BILLS, cv, "id = ? AND user_id = ?",
                    new String[]{String.valueOf(billId), String.valueOf(getCurrentUserId())});

            // Record bill payment transaction
            TransactionItem tx = new TransactionItem();
            tx.setType("Expense");
            tx.setAmount(bill.getAmount());
            tx.setCategory("Bills");
            tx.setDate(today);
            tx.setDescription(bill.getName());
            tx.setNote("Bill payment recorded");
            tx.setPaymentMethod("UPI");
            addTransaction(tx);
        }
    }

    public void deleteBill(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_BILLS, "id = ? AND user_id = ?",
                new String[]{String.valueOf(id), String.valueOf(getCurrentUserId())});
    }

    // goals

    public List<GoalItem> getGoals() {
        List<GoalItem> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(DatabaseHelper.TABLE_GOALS, null, "user_id = ?",
                new String[]{String.valueOf(getCurrentUserId())}, null, null, "id ASC");
        if (c != null) {
            while (c.moveToNext()) {
                GoalItem g = new GoalItem();
                g.setId(c.getLong(c.getColumnIndexOrThrow("id")));
                g.setName(c.getString(c.getColumnIndexOrThrow("name")));
                g.setTargetAmount(c.getDouble(c.getColumnIndexOrThrow("target_amount")));
                g.setCurrentAmount(c.getDouble(c.getColumnIndexOrThrow("current_amount")));
                list.add(g);
            }
            c.close();
        }
        return list;
    }

    public long addGoal(GoalItem goal) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("user_id", getCurrentUserId());
        cv.put("name", goal.getName());
        cv.put("target_amount", goal.getTargetAmount());
        cv.put("current_amount", goal.getCurrentAmount());
        long id = db.insert(DatabaseHelper.TABLE_GOALS, null, cv);
        goal.setId(id);
        return id;
    }

    public void addSavingsToGoals(long id, double amount) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("UPDATE " + DatabaseHelper.TABLE_GOALS + " SET current_amount = current_amount + ? WHERE id = ? AND user_id = ?",
                new Object[]{amount, id, getCurrentUserId()});
    }

    public void deleteGoal(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_GOALS, "id = ? AND user_id = ?",
                new String[]{String.valueOf(id), String.valueOf(getCurrentUserId())});
    }

    // --- LOANS ---

    public List<LoanItem> getLoans() {
        List<LoanItem> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(DatabaseHelper.TABLE_LOANS, null, "user_id = ?",
                new String[]{String.valueOf(getCurrentUserId())}, null, null, "id ASC");
        if (c != null) {
            while (c.moveToNext()) {
                LoanItem l = new LoanItem();
                l.setId(c.getLong(c.getColumnIndexOrThrow("id")));
                l.setName(c.getString(c.getColumnIndexOrThrow("name")));
                l.setPrincipal(c.getDouble(c.getColumnIndexOrThrow("principal")));
                l.setAnnualInterest(c.getDouble(c.getColumnIndexOrThrow("annual_interest")));
                l.setTenureMonths(c.getInt(c.getColumnIndexOrThrow("tenure_months")));
                l.setOutstanding(c.getDouble(c.getColumnIndexOrThrow("outstanding")));
                l.setNextPaymentDate(c.getString(c.getColumnIndexOrThrow("next_payment_date")));
                list.add(l);
            }
            c.close();
        }
        return list;
    }

    public long addLoan(LoanItem loan) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("user_id", getCurrentUserId());
        cv.put("name", loan.getName());
        cv.put("principal", loan.getPrincipal());
        cv.put("annual_interest", loan.getAnnualInterest());
        cv.put("tenure_months", loan.getTenureMonths());
        cv.put("outstanding", loan.getOutstanding());
        cv.put("next_payment_date", loan.getNextPaymentDate());
        long id = db.insert(DatabaseHelper.TABLE_LOANS, null, cv);
        loan.setId(id);
        return id;
    }

    // --- WATCHLIST ---

    public List<WatchlistItem> getWatchlist() {
        List<WatchlistItem> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(DatabaseHelper.TABLE_WATCHLIST, null, "user_id = ?",
                new String[]{String.valueOf(getCurrentUserId())}, null, null, "id ASC");
        if (c != null) {
            while (c.moveToNext()) {
                WatchlistItem w = new WatchlistItem();
                w.setId(c.getLong(c.getColumnIndexOrThrow("id")));
                w.setSymbol(c.getString(c.getColumnIndexOrThrow("symbol")));
                w.setName(c.getString(c.getColumnIndexOrThrow("name")));
                w.setNote(c.getString(c.getColumnIndexOrThrow("note")));
                list.add(w);
            }
            c.close();
        }
        return list;
    }

    public long addWatchlist(WatchlistItem item) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("user_id", getCurrentUserId());
        cv.put("symbol", item.getSymbol());
        cv.put("name", item.getName());
        cv.put("note", item.getNote());
        long id = db.insert(DatabaseHelper.TABLE_WATCHLIST, null, cv);
        item.setId(id);
        return id;
    }

    public void deleteWatchlist(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_WATCHLIST, "id = ? AND user_id = ?",
                new String[]{String.valueOf(id), String.valueOf(getCurrentUserId())});
    }

    // --- NOTIFICATIONS ---

    public List<NotificationItem> getNotifications() {
        List<NotificationItem> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(DatabaseHelper.TABLE_NOTIFICATIONS, null, "user_id = ?",
                new String[]{String.valueOf(getCurrentUserId())}, null, null, "id ASC");
        if (c != null) {
            while (c.moveToNext()) {
                NotificationItem n = new NotificationItem();
                n.setId(c.getLong(c.getColumnIndexOrThrow("id")));
                n.setTitle(c.getString(c.getColumnIndexOrThrow("title")));
                n.setMessage(c.getString(c.getColumnIndexOrThrow("message")));
                n.setType(c.getString(c.getColumnIndexOrThrow("type")));
                list.add(n);
            }
            c.close();
        }
        return list;
    }

    // --- MOOD ---

    public String getLatestMood() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(DatabaseHelper.TABLE_MOOD, new String[]{"mood"}, "user_id = ?",
                new String[]{String.valueOf(getCurrentUserId())}, null, null, "id DESC", "1");
        String m = "Calm";
        if (c != null) {
            if (c.moveToFirst()) {
                m = c.getString(0);
            }
            c.close();
        }
        return m;
    }

    public String getLatestMoodNote() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(DatabaseHelper.TABLE_MOOD, new String[]{"note"}, "user_id = ?",
                new String[]{String.valueOf(getCurrentUserId())}, null, null, "id DESC", "1");
        String note = "Your finances are a work in progress, just like everything else.";
        if (c != null) {
            if (c.moveToFirst()) {
                note = c.getString(0);
            }
            c.close();
        }
        return note;
    }

    public void saveMood(String mood, String note) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("user_id", getCurrentUserId());
        cv.put("mood", mood);
        cv.put("note", note);
        cv.put("date", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        db.insert(DatabaseHelper.TABLE_MOOD, null, cv);
    }

    // --- USER PROFILE & AUTH ---

    public String getUserName() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(DatabaseHelper.TABLE_USERS, new String[]{"name"}, "is_logged_in = 1", null, null, null, "id ASC", "1");
        String name = "Riya Sharma";
        if (c != null) {
            if (c.moveToFirst()) {
                name = c.getString(0);
            }
            c.close();
        }
        return name;
    }

    public String getUserEmail() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(DatabaseHelper.TABLE_USERS, new String[]{"email"}, "is_logged_in = 1", null, null, null, "id ASC", "1");
        String email = "riya@example.com";
        if (c != null) {
            if (c.moveToFirst()) {
                email = c.getString(0);
            }
            c.close();
        }
        return email;
    }

    public void updateUserProfile(String name, String email) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("email", email);
        db.update(DatabaseHelper.TABLE_USERS, cv, "id = ?", new String[]{String.valueOf(getCurrentUserId())});
    }

    public boolean isLoggedIn() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(DatabaseHelper.TABLE_USERS, new String[]{"is_logged_in"}, "is_logged_in = 1", null, null, null, "id ASC", "1");
        boolean loggedIn = false;
        if (c != null) {
            if (c.moveToFirst()) {
                loggedIn = true;
            }
            c.close();
        }
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("is_logged_in", loggedIn ? 1 : 0);
        db.update(DatabaseHelper.TABLE_USERS, cv, "id = ?", new String[]{String.valueOf(getCurrentUserId())});
    }

    public boolean setDemoAccountActive() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("UPDATE " + DatabaseHelper.TABLE_USERS + " SET is_logged_in = 0");

        ContentValues cv = new ContentValues();
        cv.put("is_logged_in", 1);
        int rows = db.update(DatabaseHelper.TABLE_USERS, cv, "id = 1", null);
        if (rows == 0) {
            dbHelper.seedDemoData(db);
        }
        return true;
    }

    public boolean login(String email, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(DatabaseHelper.TABLE_USERS, new String[]{"id"}, "email = ? AND password = ?",
                new String[]{email, password}, null, null, null);
        long foundUserId = -1;
        if (c != null) {
            if (c.moveToFirst()) {
                foundUserId = c.getLong(0);
            }
            c.close();
        }

        if (foundUserId != -1) {
            SQLiteDatabase wdb = dbHelper.getWritableDatabase();
            wdb.execSQL("UPDATE " + DatabaseHelper.TABLE_USERS + " SET is_logged_in = 0");
            ContentValues cv = new ContentValues();
            cv.put("is_logged_in", 1);
            wdb.update(DatabaseHelper.TABLE_USERS, cv, "id = ?", new String[]{String.valueOf(foundUserId)});
            return true;
        }

        if (email.toLowerCase().contains("demo") || email.equals("riya@example.com") || password.equals("demo")) {
            return setDemoAccountActive();
        }
        return false;
    }

    public boolean signup(String name, String email, String password) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // Clear logged in status from all users
        db.execSQL("UPDATE " + DatabaseHelper.TABLE_USERS + " SET is_logged_in = 0");

        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("email", email);
        cv.put("password", password);
        cv.put("is_logged_in", 1);

        long id = db.insertWithOnConflict(DatabaseHelper.TABLE_USERS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        if (id != -1) {
            // Set default budget for new user
            ContentValues budgetCv = new ContentValues();
            budgetCv.put("user_id", id);
            budgetCv.put("month", getCurrentMonthPrefix());
            budgetCv.put("amount", 12000.0);
            db.insert(DatabaseHelper.TABLE_BUDGETS, null, budgetCv);
            return true;
        }
        return false;
    }

    public void resetDemoData() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        dbHelper.onUpgrade(db, 1, 2);
    }
}
