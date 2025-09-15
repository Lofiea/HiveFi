package com.hivefi.services;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class FXServiceTest {
  private static MockWebServer server;

  @BeforeAll static void start() throws Exception {
    server = new MockWebServer(); server.start();
  }
  @AfterAll static void stop() throws Exception { server.shutdown(); }

  @Test void parses_frankfurter_shape_and_caches() throws Exception {
    server.enqueue(new MockResponse().setBody("{\"amount\":1,\"base\":\"EUR\",\"date\":\"2025-09-01\",\"rates\":{\"USD\":1.10}}").setResponseCode(200));
    String base = server.url("/latest").toString(); 
    String url  = base + "?from=%s&to=%s";         
    FXService fx = new FXService(url, null, 60);

    double r1 = fx.getRate("EUR","USD");
    assertEquals(1.10, r1, 1e-9);

    server.shutdown();
    double r2 = fx.getRate("EUR","USD");
    assertEquals(1.10, r2, 1e-9);
  }
}