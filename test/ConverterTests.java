package test;

import com.hivefi.service.*;
public class ConverterTests {
  public static void run() {
    var api = new ApiRateService();               // stubbed rates for now
    var conv = new Converter(api);
    double v = conv.convert(100, "USD", "EUR");   // stub: ~0.920
    assert !Double.isNaN(v) && v > 0.0 : "USD→EUR failed";
    double back = conv.convert(v, "EUR", "USD");
    assert Math.abs(back - 100.0) < 0.1 : "Round-trip off: " + back;
  }
}
