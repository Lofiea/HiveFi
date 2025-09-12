package com.hivefi.service.dao;

import com.hivefi.model.Expense;
import com.hivefi.utils.Db;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ExpenseDao {

    public void insert(Expense e) {
        String sql = "INSERT OR REPLACE INTO expenses (id, trip_id, category, amount_native, currency, ts) VALUES (?,?,?,?,?,?)";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, e.id());
            ps.setString(2, e.tripId());
            ps.setString(3, e.category());
            ps.setDouble(4, e.amountNative());
            ps.setString(5, e.currency());
            ps.setLong(6, e.timestamp().toEpochMilli());
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public List<Expense> listByTrip(String tripId) {
        String sql = "SELECT * FROM expenses WHERE trip_id=?";
        List<Expense> out = new ArrayList<>();
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, tripId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                out.add(new Expense(
                    rs.getString("id"),
                    rs.getString("trip_id"),
                    rs.getString("category"),
                    rs.getDouble("amount_native"),
                    rs.getString("currency"),
                    Instant.ofEpochMilli(rs.getLong("ts"))
                ));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return out;
    }
}
