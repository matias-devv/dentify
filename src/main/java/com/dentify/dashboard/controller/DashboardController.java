package com.dentify.dashboard.controller;

import com.dentify.dashboard.dto.DashboardAlert;
import com.dentify.dashboard.dto.DashboardSummary;
import com.dentify.dashboard.service.IDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final IDashboardService dashboardService;

    @PreAuthorize("hasRole('DENTIST')")
    @GetMapping("/summary")
    public ResponseEntity<DashboardSummary> getDashboardSummary() {

        return ResponseEntity.status(HttpStatus.OK).body( dashboardService.getDashboardSummary() );
    }

    @PreAuthorize("hasRole('DENTIST')")
    @GetMapping("/alerts")
    public ResponseEntity<List> getAlertsTodayForDentist(@AuthenticationPrincipal String username) {

        return ResponseEntity.status(HttpStatus.OK).body( dashboardService.getAlertsToday(username) );
    }

}
