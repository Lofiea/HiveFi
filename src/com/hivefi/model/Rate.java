package com.hivefi.model;

import java.util.Map;

public record Rate(String base, long fetchedAtMillis, Map<String, Double> quotes) { 

}
