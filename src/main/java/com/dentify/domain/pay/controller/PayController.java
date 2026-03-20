package com.dentify.domain.pay.controller;

import com.dentify.domain.pay.dto.request.ConfirmCashRequest;
import com.dentify.domain.pay.dto.response.PaymentTodayResponse;
import com.dentify.domain.pay.service.IPayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController @RequiredArgsConstructor
@RequestMapping ("/api/payments")
public class PayController {

    private final IPayService payService;

    @PreAuthorize("hasRole('DENTIST')")
    @GetMapping("/today")
    public ResponseEntity<List> getListOfTodayPayments(@AuthenticationPrincipal String username){

        List<PaymentTodayResponse> payments = payService.getListOfTodayPayments(username);

        return ResponseEntity.status(HttpStatus.OK).body(payments);
    }

    @PreAuthorize("hasAnyRole('DENTIST', 'SECRETARY')")
    @PatchMapping("/confirm-cash")
    public ResponseEntity<PaymentTodayResponse> confirmCash(@AuthenticationPrincipal String username,
                                                            @RequestBody ConfirmCashRequest request,){

        PaymentTodayResponse updatedPayment = payService.confirmCash(request, username);

        return ResponseEntity.status(HttpStatus.OK).body(updatedPayment);
    }
}
