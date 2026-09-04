# SmartExpenseManager
A Java Swing desktop app with Oracle JDBC backend for tracking daily expenses, add/edit/delete/search entries, set a budget, and view a live category-wise spending chart.
Smart Expense Manager is a standalone Java desktop application that helps users record, edit, search, and analyze their daily expenses. It connects to an Oracle database via JDBC to persist expense data and provides an at-a-glance view of total spending, budget, and remaining balance. A built-in bar chart (rendered with Graphics2D) visualizes spending across categories such as Food, Travel, Shopping, Bills, Entertainment, Health, and Other.

Features
Add / Edit / Delete expenses — record the date, category, title, amount, and an optional note for each entry
Live search — filter the expense table instantly by title or category as you type
Budget tracking — set a budget and see remaining balance update in real time, with a red/green indicator for overspending
Category-wise chart — a color-gradient bar chart summarizing spending by category
Oracle database persistence — all expenses are stored in and loaded from an Oracle database table via JDBC
Dark-themed Swing UI — a clean, modern interface built with Java Swing components
Tech Stack
Component	Technology
UI:	Java Swing (JFrame, JTable, custom Graphics2D chart panel)
Backend logic:	Core Java
Database:	Oracle Database (via JDBC, oracle.jdbc driver)
Data access	:java.sql (Connection, PreparedStatement, ResultSet)
Prerequisites:
JDK 8 or later
Oracle Database (e.g., Oracle XE) running locally
Oracle JDBC driver (ojdbc8.jar or compatible) on the classpath
An expenses table in your Oracle schema:
sql
CREATE TABLE expenses (
    id        NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    exp_date  DATE NOT NULL,
    category  VARCHAR2(50) NOT NULL,
    title     VARCHAR2(200) NOT NULL,
    amount    NUMBER(10,2) NOT NULL,
    note      VARCHAR2(500)
);
Configuration

Database connection details are set as constants near the top of SmartExpenseManagerJDBC1.java:

java
private static final String DB_URL  = "jdbc:oracle:thin:@localhost:1521:XE";
private static final String DB_USER = "system";
private static final String DB_PASS = "system";

Update these to match your local Oracle instance and credentials before running.

Build & Run
bash
# Compile (make sure ojdbc jar is on the classpath)
javac -cp .:ojdbc8.jar SmartExpenseManagerJDBC1.java

# Run
java -cp .:ojdbc8.jar SmartExpenseManagerJDBC1

On Windows, replace : with ; in the classpath.

Usage
Launch the application — the main window opens with the expense form on the left and the expense table/chart on the right.
Fill in Date, Category, Title, Amount, and Note, then click Add.
Select a row in the table and click Edit to update its amount, or Delete to remove it.
Use the Search box to filter expenses by title or category.
Click Set Budget to define a spending limit; the Remaining label turns red if you go over budget.
Click Refresh to reload the latest data from the database.
Project Structure
SmartExpenseManagerJDBC1.java   # Single-file application (UI + DB logic + chart)
