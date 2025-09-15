package com.hivefi.services;

import com.hivefi.db.ExpenseDAO;
import com.hivefi.db.TransactionDAO;
import com.hivefi.models.Expense;
import com.hivefi.models.Transaction;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LedgerService {
    private final ExpenseDAO dao;
    private final TransactionDAO txDao;

    public LedgerService(ExpenseDAO dao) {
        this.dao = dao;
        this.txDao = new TransactionDAO();
    }

    public Expense recordExpense(String category,
                                 String currency,
                                 double amount,
                                 String description,
                                 String dateDisplay,
                                 String requestId) {
        if (requestId != null && !requestId.isBlank()) {
            boolean firstTime = dao.markProcessed(requestId);
            if (!firstTime) {
                throw new IllegalStateException("Duplicate request: " + requestId);
            }
        }
        Expense e = new Expense(category, currency, amount, description, dateDisplay);
        dao.insert(e);

        String prev = txDao.lastHash();
        Transaction t = new Transaction(Transaction.Action.CREATE, e, prev);
        txDao.append(t);

        return e;
    }

    public List<Expense> listAll() {
        return dao.findAll();
    }

    public List<Expense> listByCategory(String category) {
        return dao.findByCategory(category);
    }

    public List<Expense> listByDateRange(LocalDate fromInclusive, LocalDate toInclusive) {
        return dao.findByDateRange(fromInclusive, toInclusive);
    }

    public Map<String, Map<String, Double>> categoryBreakdownByCurrency() {
        List<Expense> all = dao.findAll();
        Map<String, Map<String, Double>> out = new LinkedHashMap<>();
        for (Expense e : all) {
            out.computeIfAbsent(e.getCategory(), k -> new LinkedHashMap<>())
               .merge(e.getCurrency(), e.getAmount(), Double::sum);
        }
        return out;
    }

    public List<Transaction> transactions() {
        return txDao.findAll();
    }

    public int count() {
        return dao.findAll().size();
    }
}