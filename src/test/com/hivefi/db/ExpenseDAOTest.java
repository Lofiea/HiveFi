package com.hivefi.services;

import com.hivefi.db.ExpenseDAO;
import com.hivefi.models.Expense;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class LedgerServiceTest {
  @TempDir static Path tmp;
  @BeforeAll static void setDb() {
    System.setProperty("HIVEFI_DB_URL", "jdbc:sqlite:" + tmp.resolve("ledger_test.db"));
  }

  @Test void record_and_idempotency() {
    LedgerService ledger = new LedgerService(new ExpenseDAO());
    var e1 = ledger.recordExpense("Food","USD",10.0,"Pizza","01/09/2025","req-xyz");
    assertNotNull(e1.getId());
    assertEquals(1, ledger.count());
    assertThrows(IllegalStateException.class,
      () -> ledger.recordExpense("Food","USD",10.0,"Pizza","01/09/2025","req-xyz"));
  }
}