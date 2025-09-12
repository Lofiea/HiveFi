package com.hivefi.service;

import com.hivefi.model.Rate;
import java.util.Optional;

public interface RateService {
    Optional<Rate> fetchLatestRates(String baseCurrency);
}
