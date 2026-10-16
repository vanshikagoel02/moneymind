# Money Mind • Editorial FinTech Personal Finance Android Application

> **Course**: Mobile Application Development (MAD)  
> **Platform**: Native Android (`Java` & `XML`)  
> **SDK Version**: Target SDK 37 | Minimum SDK 24  
> **Database**: Embedded SQLite Database (`moneymind.db`)  
> **Cloud & API Integrations**: Google Maps SDK, Android Geocoder API, Firebase Cloud Firestore, Material 3 Design  

---

## 📌 Executive Summary

**Money Mind** is a production-ready, editorial FinTech Android application built natively in Java and XML for the **Mobile Application Development** subject. Designed with an intentional local-first architecture, the application provides comprehensive financial planning, location-tagged transaction recording, interactive budgeting, goal tracking, market watchlist monitoring, EMI loan calculations, and automated data exports (CSV/TXT & Firebase Cloud Firestore sync).

---

## 🏛️ System Architecture & Design Patterns

The project follows clean native Android architectural guidelines centered around the **Repository Pattern** and **Single Activity Container with Fragments** pattern.

```
┌────────────────────────────────────────────────────────────────────────┐
│                          PRESENTATION LAYER                            │
│  [AuthActivity]                      [MainActivity]                    │
│   • Login / Signup                    • Top Editorial Action Bar       │
│   • Multi-User Sessions               • Bottom Navigation Container    │
│                                       • Global Bottom Sheet Dialogs    │
│                                                                        │
│  [9 Feature Fragments]                                                 │
│   HomeFragment     ActivityFragment  BudgetFragment    InsightsFragment │
│   MapFragment      GoalsFragment     BillsFragment     MarketsFragment │
│   LoansFragment    SettingsFragment                                    │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
┌───────────────────────────────────▼────────────────────────────────────┐
│                            DOMAIN & REPOSITORY                         │
│  [MoneyRepository]                                                     │
│   • Business Logic Engine & Multi-User Data Scoping                    │
│   • Income, Expense & Net Balance Aggregations                         │
│   • Search, Category, Payment Method & Date Filtering                  │
│   • EMI Loan Amortization Calculations                                 │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
┌───────────────────────────────────┼────────────────────────────────────┐
│                                   │                                    │
┌───────────────────────────────────▼──┐  ┌──────────────────────────────▼─┐
│        LOCAL PERSISTENCE             │  │       CLOUD & EXTERNAL SERVICES   │
│  [DatabaseHelper (SQLiteOpenHelper)] │  │  [FirebaseHelper]                │
│   • DB: moneymind.db (Version 2)     │  │   • Firebase Cloud Firestore     │
│   • Tables: users, transactions,     │  │   • Google Maps SDK v19          │
│     budgets, bills, goals, loans,    │  │   • Android Native Geocoder API  │
│     watchlist, notifications, mood   │  │   • Android FileProvider (CSV)   │
└──────────────────────────────────────┘  └──────────────────────────────────┘
```

### Key Software Engineering Design Patterns Used:
1. **Repository Pattern (`MoneyRepository`)**: Centralized single source of truth managing all local SQLite database CRUD operations and user session state.
2. **Singleton Pattern**: Thread-safe instance creation of `MoneyRepository` and `DatabaseHelper`.
3. **Custom View Component (`DonutProgressView`)**: Extended native Android `View` utilizing Canvas graphics (`Paint`, `Path`, `Arc`) for smooth percentage arc rendering.
4. **Adapter & ViewHolder Pattern (`TransactionAdapter`, `BillAdapter`, etc.)**: Efficient `RecyclerView` memory recycling for long transaction feeds.
5. **Scoped Multi-User Isolation**: Every database table includes a `user_id` foreign key ensuring real user accounts start completely empty while the Demo account retains pre-seeded sample data for faculty evaluation.

---

## 📱 Key Features & Technical Details

| Feature | Technical Implementation | MAD Concept Demonstrated |
| :--- | :--- | :--- |
| **Authentication & User Management** | `AuthActivity`, SQLite `users` table, Session Management, Demo Account Mode | Activity Lifecycle, Shared Intents, User State Isolation |
| **Home Financial Dashboard** | Deep ocean slate hero card (`#1B3B52`), Canvas Donut Progress Ring, Balance Math | Custom Views, Layout Inflation, Dynamic Text Styling |
| **Activity Feed & Filters** | `ActivityFragment`, Real-time text search, Multi-column filters, From/To Date Pickers | `TextWatcher`, `DatePickerDialog`, `SQLite` Query Builder |
| **Google Maps & Geocoding** | `MapFragment`, Google Maps SDK, `Geocoder` address lookup, Autocomplete suggestion list | External SDK Integration, Asynchronous Threading, Location Services |
| **Monthly Budgeting** | Custom budget limit setting, real-time pace status, category spending breakdowns | Data Aggregation, `SharedPreferences` / `SQLite` Sync |
| **Bills & Regular Payments** | `BillsFragment`, Overdue warning indicators, 1-tap "Mark Paid" auto-transaction logging | Interactive Item Recyclers, Relational DB Transactions |
| **Savings Goals** | `GoalsFragment`, Percentage progress bars, savings deposit dialogs | Dynamic Progress Bar Updating, Modal Dialogs |
| **Loans & EMI Calculator** | `LoansFragment`, Financial interest compound formulas ($EMI = P \times r \times \frac{(1+r)^n}{(1+r)^n - 1}$) | Mathematical Modeling, Input Parsing |
| **Data Export (CSV & TXT)** | `SettingsFragment`, `FileProvider`, `Intent.ACTION_SEND` chooser | Android Storage Access Framework, File I/O, MIME Types |
| **Cloud Synchronization** | `FirebaseHelper`, Firebase Cloud Firestore SDK, Google Services Gradle Plugin | Remote Cloud DB Sync, JSON Metadata Parsing |

---

## 🗄️ Database Schema & SQLite Query Design

The application persists all records locally in **`moneymind.db`** via `SQLiteOpenHelper`.

### Relational Schema Design:
1. **`users`**: `id (PK)`, `name`, `email (UNIQUE)`, `password`, `is_logged_in`
2. **`transactions`**: `id (PK)`, `user_id (FK)`, `type`, `amount`, `category`, `date`, `description`, `note`, `payment_method`, `receipt_uri`, `place_name`, `latitude`, `longitude`, `created_at`
3. **`budgets`**: `id (PK)`, `user_id (FK)`, `month`, `amount`
4. **`bills`**: `id (PK)`, `user_id (FK)`, `name`, `amount`, `due_date`, `is_paid`, `paid_date`
5. **`goals`**: `id (PK)`, `user_id (FK)`, `name`, `target_amount`, `current_amount`
6. **`loans`**: `id (PK)`, `user_id (FK)`, `name`, `principal`, `annual_interest`, `tenure_months`, `outstanding`, `next_payment_date`
7. **`watchlist`**: `id (PK)`, `user_id (FK)`, `symbol`, `name`, `note`
8. **`notifications`**: `id (PK)`, `user_id (FK)`, `title`, `message`, `type`
9. **`mood`**: `id (PK)`, `user_id (FK)`, `mood`, `note`, `date`

---

## 🚀 How to Run & Evaluate the Project

### Prerequisites:
- **Android Studio**: Android Studio Ladybug / Koala or newer
- **Android SDK**: Target SDK 37, Minimum SDK 24
- **Emulator / Device**: Android 7.0+ (API 24+) with Google Play Services

### Steps to Build and Run:
1. Open **Android Studio** and select `File > Open`, then choose this project directory: `MyApplication2`.
2. Allow Gradle to perform project sync.
3. Click the **Run** button (`Shift + F10`) or execute in the Android Studio Terminal:
   ```powershell
   ./gradlew app:assembleDebug
   ```

---

## 🔍 Database Inspection (For Faculty Evaluation)

You can inspect the live SQLite database (`moneymind.db`) directly inside Android Studio without external tools:

1. Open the app on the emulator or physical device.
2. At the bottom toolbar in Android Studio, open the **App Inspection** tab.
3. Select **Database Inspector**.
4. Double-click any table (`users`, `transactions`, `bills`, `goals`, `loans`) on the left panel to inspect live rows and columns.
5. Click the **Query Editor** icon (`[田 🔍]`) to execute custom SQL queries (e.g. `SELECT * FROM transactions;`).

---

## 📄 License & Academic Attribution
Developed as part of the **Mobile Application Development (MAD)** coursework. All visual assets and architectural components strictly adhere to native Android development best practices.
