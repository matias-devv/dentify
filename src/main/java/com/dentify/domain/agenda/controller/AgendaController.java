package com.dentify.domain.agenda.controller;

import com.dentify.domain.agenda.dto.request.CreateAgendaRequest;
import com.dentify.domain.agenda.dto.response.CreateAgendaResponse;
import com.dentify.domain.agenda.service.IAgendaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping ("/api/agendas")
@RequiredArgsConstructor
public class AgendaController {

    private final IAgendaService agendaService;

    @PostMapping
    public ResponseEntity<CreateAgendaResponse> createAgenda(@AuthenticationPrincipal UserDetails userDetails,
                                                       @RequestBody @Valid CreateAgendaRequest request) {

        CreateAgendaResponse response = agendaService.save(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<List<CreateAgendaResponse>> getAgendasByDentist(@AuthenticationPrincipal UserDetails userDetails,
                                                                          @RequestParam ( required = false) Long id) {

        List<CreateAgendaResponse> response = agendaService.findAgendasByDentist( id, userDetails.getUsername() );
        return ResponseEntity.ok(response);
    }
//
//    @GetMapping("/user/{id_user_app}")
//    public ResponseEntity<List> findAgendasByUser(@PathVariable Long id_user_app) {
//        List<AgendaResponseDTO> listDto = agendaService.findAgendasByUser(id_user_app);
//        return ResponseEntity.ok(listDto);
//    }
//
//    @GetMapping("/{id_agenda}")
//    public ResponseEntity<AgendaResponseDTO> findAgendaById(@PathVariable Long id_agenda){
//        Optional<AgendaResponseDTO> agendaDto = agendaService.findAgendaById(id_agenda);
//        return agendaDto.map(ResponseEntity::ok).orElse( ResponseEntity.notFound().build() );
//    }
//
//    @PatchMapping("/patch")
//    public ResponseEntity<String> patchStatusAgenda( @RequestBody AgendaRequestDTO agendaRequestDTO){
//        return ResponseEntity.ok( agendaService.patchStatusAgenda(agendaRequestDTO) );
//    }
//
//    @PutMapping("/edit")
//    public ResponseEntity<String> editAgenda( @RequestBody AgendaRequestDTO agendaRequestDTO){
//        return ResponseEntity.ok( agendaService.editAgenda(agendaRequestDTO) );
//    }
//
//    @GetMapping("/calendar/day")
//    public ResponseEntity<FullDailyResponseDTO> getAllSlotsInDay(@RequestBody DayRequestDTO request){
//        return ResponseEntity.ok( agendaService.getAllSlotsInDay( request) );
//    }
//
//    @GetMapping("/calendar/week")
//    public ResponseEntity<WeekSummaryResponseDTO> getAvailableSlotsInWeek(@RequestBody WeekDateRangeRequestDTO request){
//        return ResponseEntity.ok( agendaService.getAvailableSlotsInWeek( request) );
//    }
//
//    @GetMapping("/calendar/month")
//    public ResponseEntity<MonthSummaryResponseDTO> getSummaryOfTheMonth(@RequestBody MonthDateRangeRequestDTO request){
//        return ResponseEntity.ok( agendaService.getSummaryOfTheMonth( request) );
//    }

}
