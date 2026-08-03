package com.dentify.domain.complementaryexam.controller;

import com.dentify.domain.complementaryexam.dto.response.ComplementaryExamResponse;
import com.dentify.domain.complementaryexam.service.IComplementaryExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
public class ComplementaryExamController {

    private final IComplementaryExamService complementaryExamService;

    @PreAuthorize("hasAnyRole('DENTIST','SECRETARY')")
    @PostMapping(value = "/{medicalHistoryId}", consumes = "multipart/form-data")
    public ResponseEntity<ComplementaryExamResponse> uploadExam(@PathVariable Long medicalHistoryId, @RequestParam("file") MultipartFile file,
                                                                @AuthenticationPrincipal String username) {

        ComplementaryExamResponse response = complementaryExamService.uploadExam(medicalHistoryId, username, file);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
