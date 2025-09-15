package com.hivefi.db;

import com.hivefi.models.Transaction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    public TransactionDAO() {
        ensureSchema();
    }

    private void ensureSchema() {
        final String create = ""
                + "CREATE TABLE IF NOT EXISTS transactions ("
                + "  id TEXT PRIMARY KEY,"
                + "  action TEXT NOT NULL,"
                + "  expense_id TEXT NOT NULL,"
                + "  category TEXT,"
                + "  currency TEXT,"
                + "  amount REAL,"
                + "  date_display TEXT,"
                + "  description TEXT,"
                + "  timestamp TEXT NOT NULL,"
                + "  prev_hash TEXT,"
                + "  tx_hash TEXT NOT NULL"
                + ");";
        final String idxTs = "CREATE INDEX IF NOT EXISTS idx_tx_timestamp ON transactions(timestamp);";
        final String idxExp = "CREATE INDEX IF NOT EXISTS idx_tx_expense ON transactions(expense_id);";
        try (Connection c = DatabaseManager.getConnection();
             Statement st = c.createStatement()) {
            st.execute(create);
            st.execute(idxTs);
            st.execute(idxExp);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to ensure transactions schema: " + e.getMessage(), e);
        }
    }

    public void append(Transaction t) {
        final String sql = "INSERT INTO transactions (id, action, expense_id, category, currency, amount, "
                + "date_display, description, timestamp, prev_hash, tx_hash) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, t.getId());
            ps.setString(2, t.getAction().name());
            ps.setString(3, t.getExpenseId());
            ps.setString(4, t.getCategory());
            ps.setString(5, t.getCurrency());
            ps.setDouble(6, t.getAmount());
            ps.setString(7, t.getDate());
            ps.setString(8, t.getDescription());
            ps.setString(9, t.getTimestamp());
            ps.setString(10, t.getPrevHash());
            ps.setString(11, t.getTxHash());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Append transaction failed: " + e.getMessage(), e);
        }
    }

    public List<Transaction> findAll() {
        final String sql = "SELECT id, action, expense_id, category, currency, amount, "
                + "date_display, description, timestamp, prev_hash, tx_hash "
                + "FROM transactions ORDER BY timestamp ASC";
        List<Transaction> out = new ArrayList<>();
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(map(rs));
            }
            return out;
        } catch (SQLException e) {
            throw new RuntimeException("Read transactions failed: " + e.getMessage(), e);
        }
    }

    /** Returns the last transaction's hash, or empty string if none. */
    public String lastHash() {
        final String sql = "SELECT tx_hash FROM transactions ORDER BY timestamp DESC LIMIT 1";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getString(1);
            return "";
        } catch (SQLException e) {
            throw new RuntimeException("Fetch last hash failed: " + e.getMessage(), e);
        }
    }

    private static Transaction map(ResultSet rs) throws SQLException {
        return new Transaction(
                rs.getString("id"),
                Transaction.Action.valueOf(rs.getString("action")),
                rs.getString("expense_id"),
                rs.getString("category"),
                rs.getString("currency"),
                rs.getDouble("amount"),
                rs.getString("date_display"),
                rs.getString("description"),
                rs.getString("timestamp"),
                rs.getString("prev_hash"),
                rs.getString("tx_hash")
        );
    }
}
