package test;

import com.hivefi.model.Expense;
import com.hivefi.service.*;
import java.time.Instant;

public class ExpenseServiceTests {
  public static void run() {
    var api = new ApiRateService();
    var conv = new Converter(api);
    var svc  = new ExpenseService(conv);
    String trip = "t1";
    svc.add(new Expense("1", trip, "Hotel", 200, "EUR", Instant.now()));
    svc.add(new Expense("2", trip, "Food",   50, "USD", Instant.now()));
    double totalUsd = svc.totalTripInHome(trip, "USD");
    assert totalUsd > 0 && Math.abs(totalUsd - 267.0) < 20.0 : "Total looks wrong: " + totalUsd;
    var cats = svc.categoryTotalsInHome(trip, "USD");
    assert cats.containsKey("Hotel") && cats.containsKey("Food") : "Missing categories";
  }
}
