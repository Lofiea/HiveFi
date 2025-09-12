package test;

public class TestRunner {
  public static void main(String[] args) {
    System.out.println("Running HiveFi tests…");
    ConverterTests.run();
    ExpenseServiceTests.run();
    System.out.println("All tests passed");
  }
}
