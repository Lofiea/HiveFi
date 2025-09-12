# HiveFi Overview 💰🐝

HiveFi is a Java-based travel budgeting tool that helps users track multi-currency expenses, calculate trip totals in a home currency, and plan better while abroad.

Originally developed as a mini expense tracker in BlueJ during AP CSA (high school), HiveFi has since evolved through multiple iterations into a full-featured budgeting tool with:

SQLite persistence for reliable data storage

Live currency exchange rates via API integration

Support for multi-currency trips, expense categories, and breakdowns

# Features

💰 Expense Tracking — log expenses by trip, category, amount, and currency

🌍 Currency Conversion — convert expenses into your home currency using live FX rates (USD, EUR, GBP, JPY, etc.)

🗄️ SQLite Persistence — all expenses and rates are stored in hivefi.db

🔄 API Integration — fetches live FX data from ExchangeRate APIs (with local caching)

📊 Category Breakdown — view totals by category (Food, Hotel, Transport, Shopping, etc.)

# Tech Stack

Language: Java 17

Database: SQLite (via JDBC: https://github.com/xerial/sqlite-jdbc)

APIs: ExchangeRate API (stubbed + live mode)

Build/Run: Plain Java

# Getting Started

Prerequisites

Install JDK 17+ (Java 24 recommended)

Download the SQLite JDBC Driver

Place it in lib/sqlite-jdbc.jar
```
HiveFi/
├── bin/                     # compiled .class files
├── db/
│   └── hivefi.db            # SQLite DB (auto-created on first run)
├── lib/
│   └── sqlite-jdbc.jar      # JDBC driver
├── src/
│   └── com/hivefi/
│       ├── Main.java
│       ├── model/...
│       ├── service/...
│       └── utils/...
└── README.md
```
