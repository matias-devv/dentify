package com.dentify.calendar.controller;

import com.dentify.calendar.dto.request.day.DetailedDayRequest;
import com.dentify.calendar.dto.request.month.MonthRequest;
import com.dentify.calendar.dto.request.week.WeekRequest;
import com.dentify.calendar.dto.response.day.DetailedDayResponse;
import com.dentify.calendar.dto.response.month.MonthResponse;
import com.dentify.calendar.dto.response.week.WeekResponse;
import com.dentify.calendar.service.ICalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final ICalendarService calendarService;

    /**
     * get monthly summary
     **/
    @PreAuthorize("hasAnyRole('DENTIST','SECRETARY')")
    @GetMapping("/monthly/slots")
    public ResponseEntity<MonthResponse> GetMonthlySummary(@RequestBody MonthRequest request) {
        MonthResponse response = calendarService.getMonthlySummary(request);
        return ResponseEntity.ok().body(response);
    }

    /**
     * get slots by week
    **/
    @PreAuthorize("hasAnyRole('DENTIST','SECRETARY')")
    @GetMapping("/weekly/slots")
    public ResponseEntity<WeekResponse> getWeeklySlots( @RequestBody WeekRequest request) {
        WeekResponse response = calendarService.getWeeklySlots(request);
        return ResponseEntity.ok().body(response);
    }

    /**
     * get slots by day
     **/
    @PreAuthorize("hasAnyRole('DENTIST','SECRETARY')")
    @GetMapping("/day/slots")
    public ResponseEntity<DetailedDayResponse> getDailySlots(@RequestBody DetailedDayRequest request) {
        DetailedDayResponse response = calendarService.getDailySlots(request);
        return ResponseEntity.ok().body(response);
    }
}
