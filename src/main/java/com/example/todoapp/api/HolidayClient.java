package com.example.todoapp.api;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class HolidayClient {
    private static final String HOLIDAYS_URL = "https://holidays-jp.github.io/api/v1/date.json";

    private final RestClient restClient;

    public HolidayClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(3));
        requestFactory.setReadTimeout(Duration.ofSeconds(5));
        restClient = RestClient.builder().requestFactory(requestFactory).build();
    }

    public Map<String, String> findHolidays(LocalDate from, LocalDate to) {
        Map<String, String> holidays = restClient.get()
                .uri(HOLIDAYS_URL)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        return holidays.entrySet().stream()
                .filter(entry -> from == null || !LocalDate.parse(entry.getKey()).isBefore(from))
                .filter(entry -> to == null || !LocalDate.parse(entry.getKey()).isAfter(to))
                .collect(LinkedHashMap::new,
                        (result, entry) -> result.put(entry.getKey(), entry.getValue()),
                        LinkedHashMap::putAll);
    }

    public HolidayLookupResult findHolidaysForApi(LocalDate from, LocalDate to) {
        try {
            return new HolidayLookupResult(findHolidays(from, to), false);
        } catch (RestClientException exception) {
            return new HolidayLookupResult(Map.of(), true);
        }
    }

    public record HolidayLookupResult(Map<String, String> holidays, boolean unavailable) {
    }
}
