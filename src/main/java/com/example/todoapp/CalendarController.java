package com.example.todoapp;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Controller
public class CalendarController {

    @GetMapping("/calendar")
    public String calendar(@RequestParam(required = false) Integer year,
                           @RequestParam(required = false) Integer month,
                           Model model) {
        YearMonth currentMonth = YearMonth.now();
        YearMonth displayedMonth = YearMonth.of(
                year == null ? currentMonth.getYear() : year,
                month == null ? currentMonth.getMonthValue() : month);

        LocalDate firstDate = displayedMonth.atDay(1);
        LocalDate lastDate = displayedMonth.atEndOfMonth();
        List<List<LocalDate>> calendarWeeks = createCalendarWeeks(firstDate, lastDate);

        model.addAttribute("displayedMonth", displayedMonth);
        model.addAttribute("firstDate", firstDate);
        model.addAttribute("lastDate", lastDate);
        model.addAttribute("calendarWeeks", calendarWeeks);
        model.addAttribute("previousMonth", displayedMonth.minusMonths(1));
        model.addAttribute("nextMonth", displayedMonth.plusMonths(1));
        return "calendar";
    }

    private List<List<LocalDate>> createCalendarWeeks(LocalDate firstDate, LocalDate lastDate) {
        List<LocalDate> calendarDays = new ArrayList<>();
        int emptyDaysBefore = firstDate.getDayOfWeek().getValue() % 7;

        for (int i = 0; i < emptyDaysBefore; i++) {
            calendarDays.add(null);
        }
        for (LocalDate date = firstDate; !date.isAfter(lastDate); date = date.plusDays(1)) {
            calendarDays.add(date);
        }
        while (calendarDays.size() % 7 != 0) {
            calendarDays.add(null);
        }
        List<List<LocalDate>> calendarWeeks = new ArrayList<>();
        for (int i = 0; i < calendarDays.size(); i += 7) {
            calendarWeeks.add(calendarDays.subList(i, i + 7));
        }
        return calendarWeeks;
    }
}
