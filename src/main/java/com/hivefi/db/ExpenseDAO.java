package com.hivefi.db;

import com.hivefi.models.Expense;

import java.sql.*; 
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ExpenseDAO {
    private static final DateTimeFormatter OUT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    public ExpenseDAO() {
        ensureSchema();
    }

    // ---------- Schema ----------
    private void ensureSchema() {
        final String createExpenses =
                "CREATE TABLE IF NOT EXISTS expenses (" +
                "  id TEXT PRIMARY KEY," +
                "  category TEXT NOT NULL," +
                "  currency TEXT NOT NULL," +
                "  amount REAL NOT NULL," +
                "  description TEXT," +
                "  date_display TEXT NOT NULL," +   
                "  date_iso TEXT NOT NULL" +        
                ");";
        final String idxCategory = "CREATE INDEX IF NOT EXISTS idx_expenses_category ON expenses(category);";
        final String idxDate     = "CREATE INDEX IF NOT EXISTS idx_expenses_date ON expenses(date_iso);";
        final String createProcessed =
                "CREATE TABLE IF NOT EXISTS processed_requests (" +
                "  id TEXT PRIMARY KEY," +
                "  created_at TEXT NOT NULL" +
                ");";

        try (Connection c = DatabaseManager.getConnection();
             Statement st = c.createStatement()) {
            st.execute(createExpenses);
            st.execute(idxCategory);
            st.execute(idxDate);
            st.execute(createProcessed);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to ensure schema: " + e.getMessage(), e);
        }
    }

    // ---------- CRUD ----------
    public void insert(Expense e) {
        String sql = "INSERT INTO expenses (id, category, currency, amount, description, date_display, date_iso) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        String iso = toIso(e.getDate());
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, e.getID());
            ps.setString(2, e.getCategory());
            ps.setString(3, e.getCurrency());
            ps.setDouble(4, e.getAmount());
            ps.setString(5, e.getDescription());
            ps.setString(6, e.getDate()); 
            ps.setString(7, iso);         
            ps.executeUpdate();

        } catch (SQLException ex) {
            throw new RuntimeException("Insert failed: " + ex.getMessage(), ex);
        }
    }

    public Optional<Expense> findById(String id) {
        String sql = "SELECT id, category, currency, amount, description, date_display FROM expenses WHERE id = ?";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
                return Optional.empty();
            }
        } catch (SQLException ex) {
            throw new RuntimeException("findById failed: " + ex.getMessage(), ex);
        }
    }

    public List<Expense> findAll() {
        String sql = "SELECT id, category, currency, amount, description, date_display FROM expenses ORDER BY date_iso DESC";
        List<Expense> out = new ArrayList<>();
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(map(rs));
            return out;
        } catch (SQLException ex) {
            throw new RuntimeException("findAll failed: " + ex.getMessage(), ex);
        }
    }

    public List<Expense> findByCategory(String category) {
        String sql = "SELECT id, category, currency, amount, description, date_display " +
                     "FROM expenses WHERE category = ? ORDER BY date_iso DESC";
        List<Expense> out = new ArrayList<>();
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, category);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
            return out;
        } catch (SQLException ex) {
            throw new RuntimeException("findByCategory failed: " + ex.getMessage(), ex);
        }
    }

    public List<Expense> findByDateRange(LocalDate fromInclusive, LocalDate toInclusive) {
        String sql = "SELECT id, category, currency, amount, description, date_display " +
                     "FROM expenses WHERE date_iso BETWEEN ? AND ? ORDER BY date_iso DESC";
        List<Expense> out = new ArrayList<>();
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, fromInclusive.format(ISO_FMT));
            ps.setString(2, toInclusive.format(ISO_FMT));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
            return out;
        } catch (SQLException ex) {
            throw new RuntimeException("findByDateRange failed: " + ex.getMessage(), ex);
        }
    }

    // ---------- Idempotency ----------
    /** Returns true if this request id was recorded (i.e., first time), false if it was already processed. */
    public boolean markProcessed(String requestId) {
        String sql = "INSERT OR IGNORE INTO processed_requests (id, created_at) VALUES (?, datetime('now'))";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, requestId);
            int changed = ps.executeUpdate();
            return changed == 1;
        } catch (SQLException ex) {
            throw new RuntimeException("markProcessed failed: " + ex.getMessage(), ex);
        }
    }

    // ---------- Helpers ----------
    private Expense map(ResultSet rs) throws SQLException {
        return new Expense(
                rs.getString("id"),
                rs.getString("category"),
                rs.getString("currency"),
                rs.getDouble("amount"),
                rs.getString("description"),
                rs.getString("date_display")
        );
    }

    private String toIso(String ddMMyyyy) {
        LocalDate d = LocalDate.parse(ddMMyyyy, OUT_FMT);
        return d.format(ISO_FMT);
    }
}

