package com.hivefi;

import com.hivefi.db.ExpenseDAO;
import com.hivefi.models.Expense;
import com.hivefi.models.Transaction;
import com.hivefi.services.LedgerService;
import com.hivefi.services.FXService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class App {

    private static final DateTimeFormatter OUT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final List<DateTimeFormatter> IN_FMTS = List.of(
            DateTimeFormatter.ofPattern("d/M/yyyy"),
            DateTimeFormatter.ofPattern("d-M-yyyy")
    );

    public static void main(String[] args) {
        try {
            if (args.length >= 4) {
                runWithArgs(args);
            } else {
                menuLoop();
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void runWithArgs(String[] args) {
        String category    = args[0];
        double amount      = parseAmount(args[1]);
        String currency    = args[2].toUpperCase(Locale.ROOT);
        String description = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
        String date        = LocalDate.now().format(OUT_FMT);

        validate(category, amount, currency);
        
        LedgerService ledger = new LedgerService(new ExpenseDAO());
        Expense e = ledger.recordExpense(category, currency, amount, description, date, null);

        System.out.println(e);
        System.out.println("Total expenses in DB: " + ledger.count());
    }

    // Menu
    private static void menuLoop() {
        LedgerService ledger = new LedgerService(new ExpenseDAO());
        FXService fx = new FXService();
        try (Scanner sc = new Scanner(System.in)) {
            while (true) {
                System.out.println("What would you like to do?");
                System.out.println("  [1] Add expense");
                System.out.println("  [2] View all history");
                System.out.println("  [3] View by category");
                System.out.println("  [4] View by date range");
                System.out.println("  [5] Show total count");
                System.out.println("  [6] FX: get rate (FROM to TO)");
                System.out.println("  [7] FX: convert amount (AMOUNT FROM to TO)");
                System.out.println("  [8] View transaction log");
                System.out.println("  [0] Exit");
                System.out.print("Choose: ");

                String choice = sc.nextLine().trim();
                switch (choice) {
                    case "1":
                        addExpenseFlow(sc, ledger);
                        break;
                    case "2":
                        printExpenses(ledger.listAll());
                        break;
                    case "3":
                        String cat = promptNonEmpty(sc, "Category");
                        printExpenses(ledger.listByCategory(cat));
                        break;
                    case "4":
                        LocalDate from = promptDateAsLocal(sc, "From date (e.g., 01/09/2025 or 1-9-2025)");
                        LocalDate to   = promptDateAsLocal(sc, "To date (e.g., 30/09/2025 or 30-9-2025)");
                        printExpenses(ledger.listByDateRange(from, to));
                        break;
                    case "5":
                        System.out.println("Total expenses in DB: " + ledger.count());
                        break;
                    case "6": 
                        fxRateFlow(sc, fx); 
                        break; 
                    case "7":
                        fxConvertFlow(sc, fx); 
                        break;
                    case "0":
                        System.out.println("Bye!");
                        return;
                    default:
                        System.out.println("Invalid choice. Try again.");
                }
            }
        }
    }

    private static void addExpenseFlow(Scanner sc, LedgerService ledger) {
        String category    = promptNonEmpty(sc, "Category");
        String currency    = promptCurrency(sc);
        double amount      = promptAmount(sc);
        System.out.print("Description: ");
        String description = sc.nextLine().trim();
        String date        = promptDateNormalized(sc);

        validate(category, amount, currency);

        Expense e = ledger.recordExpense(category, currency, amount, description, date, /*requestId*/ null);


        System.out.println("Saved: " + e);
        System.out.println("Total expenses in DB: " + ledger.count());
    }

    private static void fxRateFlow(Scanner sc, FXService fx) {
        String from = promptCurrency(sc, "From currency (e.g., USD)");
        String to   = promptCurrency(sc, "To currency   (e.g., EUR)");
        double rate = fx.getRate(from, to);
        System.out.printf("1 %s = %.6f %s%n", from, rate, to);
        double inv = fx.getRate(to, from);
        System.out.printf("1 %s = %.6f %s%n", to, inv, from);
    }

    private static void fxConvertFlow(Scanner sc, FXService fx) {
        double amount = promptAmount(sc);
        String from   = promptCurrency(sc, "From currency (e.g., USD)");
        String to     = promptCurrency(sc, "To currency   (e.g., EUR)");
        double rate   = fx.getRate(from, to);
        double out    = amount * rate;
        System.out.printf("Converted: %s%.2f (%s to %s at %.6f)%n",
                symbolFor(to), out, from, to, rate);
    }

    private static void runInteractive() {
        try (Scanner sc = new Scanner(System.in)) {

            String category = promptCategory(sc);
            String currency = promptCurrency(sc);
            double amount   = promptAmount(sc);
            String description = prompt(sc, "Description");
            String date     = promptDate(sc);

            validate(category, amount, currency);
            
            Expense e = new Expense(category, currency, amount, description, date);
            ExpenseDAO dao = new ExpenseDAO();
            dao.insert(e);

            System.out.println("Saved: " + e);
            System.out.println("Total expenses in DB: " + dao.findAll().size());
        }
    }

    private static String promptNonEmpty(Scanner sc, String label) {
        while (true) {
            System.out.print(label + ": ");
            String s = sc.nextLine().trim();
            if (!s.isEmpty()) return s;
            System.out.println(label + " is required. Try again.");
        }
    }

    private static String promptDateNormalized(Scanner sc) {
        while (true) {
            System.out.print("Date: ");
            String raw = sc.nextLine().trim();
            if (raw.isEmpty()) return LocalDate.now().format(OUT_FMT);
            try {
                return parseDate(raw);
            } catch (IllegalArgumentException ex) {
                System.out.println(ex.getMessage() + " Try again.");
            }
        }
    }

    private static LocalDate promptDateAsLocal(Scanner sc, String label) {
        while (true) {
            System.out.print(label + ": ");
            String raw = sc.nextLine().trim();
            try {
                return parseDateToLocal(raw);
            } catch (IllegalArgumentException ex) {
                System.out.println(ex.getMessage() + " Try again.");
            }
        }
    }

    private static String prompt(Scanner sc, String label) {
        System.out.print(label + ": ");
        return sc.nextLine().trim();
    }

    private static String promptCategory(Scanner sc) {
        while (true) {
            String s = prompt(sc, "Category");
            if (!s.isEmpty()) return s;
            System.out.println("Category is required. Try again.");
        }
    }

    private static String promptCurrency(Scanner sc) {
        return promptCurrency(sc, "Currency (3-letter code, e.g., USD, EUR)");
    }

    private static String promptCurrency(Scanner sc, String label) {
        while (true) {
            System.out.print(label + ": ");
            String s = sc.nextLine().trim().toUpperCase(Locale.ROOT);
            if (s.matches("[A-Z]{3}")) return s;
            System.out.println("  -> Must be a 3-letter code like USD/EUR/JPY. Try again.");
        }
    }

    private static double promptAmount(Scanner sc) {
        while (true) {
            String raw = prompt(sc, "Amount");
            try {
                double val = parseAmount(raw);
                if (val > 0) return val;
                System.out.println("Amount must be > 0. Try again.");
            } catch (IllegalArgumentException ex) {
                System.out.println(ex.getMessage() + " Try again.");
            }
        }
    }

    private static String promptDate(Scanner sc) {
        while (true) {
            String raw = prompt(sc, "Date: ");
            if (raw.isEmpty()) return LocalDate.now().format(OUT_FMT);
            try {
                return parseDate(raw);
            } catch (IllegalArgumentException ex) {
                System.out.println(ex.getMessage() + " Try again.");
            }
        }
    }
    
    private static double parseAmount(String raw) {
        try {
            return Double.parseDouble(raw.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Amount must be a number.");
        }
    }

    private static String parseDate(String raw) {
        String s = raw.trim();
        for (DateTimeFormatter f : IN_FMTS) {
            try {
                LocalDate d = LocalDate.parse(s, f);
                return d.format(OUT_FMT); 
            } catch (DateTimeParseException ignored) {}
        }
        throw new IllegalArgumentException("Date must be valid (e.g., 12/03/1963 or 12-03-1963).");
    }

    private static LocalDate parseDateToLocal(String raw) {
        String s = raw.trim();
        for (DateTimeFormatter f : IN_FMTS) {
            try { return LocalDate.parse(s, f); }
            catch (DateTimeParseException ignored) {}
        }
        throw new IllegalArgumentException("Date must be valid (e.g., 12/03/1963 or 12-03-1963).");
    }

    private static void validate(String category, double amount, String currency) {
        if (category == null || category.isEmpty()) {
            throw new IllegalArgumentException("Category is required.");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be > 0.");
        }
        if (!currency.matches("[A-Z]{3}")) {
            throw new IllegalArgumentException("Currency must be a 3-letter code (e.g., USD).");
        }
    }

    private static void printExpenses(List<Expense> items) {
        if (items.isEmpty()) {
            System.out.println("(no expenses found)");
            return;
        }
        int i = 1;
        for (Expense e : items) {
            System.out.println(i++ + ". " + e.toString());
        }
    }

    private static void printTransactions(List<Transaction> txs) {
        if (txs.isEmpty()) { System.out.println("(no transactions logged yet)"); return; }
        int i = 1;
        for (Transaction t : txs) {
            System.out.println(i++ + ".");
            System.out.println(t.toString());
        }
    }

    private static String symbolFor(String code) {
        switch (code.toUpperCase(Locale.ROOT)) {
            case "USD": return "$";
            case "EUR": return "€";
            case "GBP": return "£";
            case "JPY": return "¥";
            case "CNY": return "¥";
            case "INR": return "₹";
            case "AUD": return "A$";
            case "CAD": return "C$";
            case "CHF": return "CHF";
            case "KRW": return "₩";
            case "RUB": return "₽";
            default:    return code;
        }
    }
}
