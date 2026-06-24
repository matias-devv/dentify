package com.dentify.domain.agenda.service;

import com.dentify.domain.agenda.dto.request.CreateAgendaRequest;
import com.dentify.domain.agenda.dto.response.CreateAgendaResponse;
import com.dentify.domain.agenda.model.Agenda;
import com.dentify.domain.agenda.repository.IAgendaRepository;
import com.dentify.domain.clinic.model.Clinic;
import com.dentify.domain.dentist.model.Dentist;
import com.dentify.domain.dentist.service.IDentistService;
import com.dentify.domain.product.model.Product;
import com.dentify.domain.schedule.model.Schedule;
import com.dentify.domain.schedule.service.IScheduleService;
import com.dentify.domain.userProfile.service.IUserProfileService;
import com.dentify.exception.agenda.*;
import com.dentify.domain.product.service.IProductService;
import com.dentify.exception.general.InvalidRequestDateException;
import com.dentify.exception.schedule.InvalidScheduleTimeException;
import com.dentify.mapper.AgendaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AgendaService implements IAgendaService {

    //repository
    private final IAgendaRepository agendaRepository;

    //services
    private final IDentistService dentistService;
    private final IProductService productService;
    private final IScheduleService scheduleService;
    private final IUserProfileService userProfileService;

    //mappers
    private final AgendaMapper agendaMapper;

    @Override
    public CreateAgendaResponse save(CreateAgendaRequest request, String username) {

        Dentist dentist = dentistService.resolveDentist( request.idDentist(), username);

        this.validationsToAgendaRequest(request);

        //create agenda
        Agenda agenda = agendaMapper.setAttributesNewAgenda(request);

        agenda.setDentist(dentist);

        scheduleService.addSchedulesToAgenda( request.schedules(), request.duration_minutes(), agenda);

        agenda.setClinic( dentist.getClinic() );

        productService.setProductToAgenda( request.idProduct(), agenda);

        agendaRepository.save(agenda);

        return agendaMapper.createAgendaResponse(agenda);
    }

    private void validationsToAgendaRequest(CreateAgendaRequest request) {
        this.validateDates(request);

        this.validateName(request);

        this.validateDuration(request);

        scheduleService.validateNullSchedules(request);
    }

    @Override
    public Agenda findAgendaWithDentistById( Long idAgenda) {
        return agendaRepository.findAgendaWithDentistById( idAgenda ).orElseThrow( () -> new AgendaNotFoundException("Agenda not found"));
    }

    @Override
    public void validateIfAgendaIsActive(Agenda agenda) {

        if( !agenda.getActive()){
            throw new AgendaNotActiveException("Agenda is not active");
        }
    }

    @Override
    public void validateAgendaAvailability(Agenda agenda, LocalDate date, LocalTime startTime) {

        if ( date.isBefore( agenda.getStart_date() ) || date.isAfter( agenda.getFinal_date() ) )
            throw new AgendaDateOutOfRangeException("Date outside agenda range");

        Map<DayOfWeek, List<Schedule>> mapSchedules = agenda.fillMapDays();

        if ( mapSchedules.get( date.getDayOfWeek() ) == null){
            throw new DayOfAgendaNotFoundException("The choosen date needs to be an operative day in the agenda");
        }
        else{
            List<Schedule> scheduleList = mapSchedules.get( date.getDayOfWeek() );

            for ( Schedule s :  scheduleList){

                if ( !startTime.isBefore( s.getStart_time() ) && startTime.isBefore( s.getEnd_time()) ){
                    return;
                }
            }
            throw new InvalidScheduleTimeException("The start time is outside all operative schedules for this day");
        }
    }

    private void validateStartTimeIsNotBeforeCurrentTime ( LocalTime startTime ){

        if ( startTime.isBefore( LocalTime.now() ) ) throw new InvalidScheduleTimeException("The start time cannot be before the current time");
    }

    @Override
    public void verifyIfThisAgendaBelongsToTheDentist(Agenda agenda, Dentist dentist) {

        if ( !agenda.getDentist().getId().equals( dentist.getId() ) ){
            throw new AgendaOwnershipException("The dentist must own this appointment book");
        }
    }

    @Override
    public void validatAgendaToCreateAppointment(Agenda agenda, Dentist dentist, LocalDate date, LocalTime startime) {

        this.validatePastDate(date);

        this.validateIfAgendaIsActive( agenda);

        this.verifyIfThisAgendaBelongsToTheDentist( agenda, dentist);

        this.validateAgendaAvailability( agenda, date, startime);

        this.validateStartTimeIsNotBeforeCurrentTime( startime );
    }

    private void validatePastDate(LocalDate date) {

        LocalDate now = LocalDate.now();

        if(date.isBefore(now)){
            throw new InvalidRequestDateException("The date sent must be today's date or later than today's date");
        }
    }

    private void validateDates(CreateAgendaRequest request) {
        if ( request.startDate().isBefore(LocalDate.now() ) ){
            throw new InvalidAgendaDateException("The date cannot be before the current date");
        }
        if ( request.finalDate().isBefore(request.startDate() ) ){
            throw new InvalidAgendaDateException("The final date cannot be before the start date");
        }
        if ( request.startDate().isEqual(request.finalDate() ) ){
            throw new InvalidAgendaDateException("The agenda cannot be for just one day");
        }
    }

    private void validateDuration(CreateAgendaRequest request) {

        if ( request.duration_minutes() > 120) throw new InvalidAgendaDurationException("The time blocks allocated to each appointment cannot exceed 2 hours.");

        if ( request.duration_minutes() < 5) throw new InvalidAgendaDurationException("The time blocks allocated to each appointment cannot be less than 5 minutes.");
    }

    private void validateName(CreateAgendaRequest request) {
        if( request.agendaName() == null || request.agendaName().isEmpty() ){
            throw new InvalidAgendaNameException("The agenda name cannot be empty");
        }
        if( request.agendaName().length() < 3){
            throw new InvalidAgendaNameException("The agenda name is too short");
        }
        if( request.agendaName().length() > 30 ){
            throw new InvalidAgendaNameException("The agenda name is too long");
        }
    }

    @Override
    public void validateDateRangeInAgenda(Agenda agenda, LocalDate startDate, LocalDate endDate) {

        if ( agenda.getStart_date().isAfter(startDate) || agenda.getFinal_date().isBefore(endDate) ) {
            throw new AgendaDateOutOfRangeException("The requested dates are not in the valid range defined in the calendar.");
        }
    }

    @Override
    public void validateDateWithinAgendaRange(Agenda agenda, LocalDate requestedDate) {

        boolean okDateRangeAgenda = this.isDayWithinAgendaRange( agenda, requestedDate );

        if (!okDateRangeAgenda) {
            throw new AgendaDateOutOfRangeException("The requested date it's not in the valid range defined in the agenda.");
        }
    }

    private boolean isDayWithinAgendaRange(Agenda agenda, LocalDate requestedDate) {
        //include boundary dates using !isAfter / !isBefore
        return !requestedDate.isBefore( agenda.getStart_date() ) && !requestedDate.isAfter( agenda.getFinal_date() );
    }

    @Override
    public Agenda findAgendaWithSchedules(Long idAgenda) { //verificar si realmente funciona agenda ( me paso un bug de querer buscar una existente y no encontrarla
        return agendaRepository.findAgendaWithSchedules(idAgenda).orElseThrow(()-> new AgendaNotFoundException("The agenda requested does not exist"));
    }

    @Override
    public List<CreateAgendaResponse> findAgendasByDentist(String username) {

        Dentist dentist = dentistService.findDentistByAuthUserUsername( username);

        List<Agenda> agendas = agendaRepository.findByDentistIdWithSchedules( dentist.getId() );

        return agendas.stream()
                      .map(agendaMapper::createAgendaResponse)
                      .toList();
    }

    @Override
    public List<CreateAgendaResponse> findAgendasByClinic(String username) {

        Clinic clinic = userProfileService.findClinicByAuthUserUsername(username);

        List<Agenda> agendas = agendaRepository.findAgendasByClinicId( clinic.getId() );

        return agendas.stream()
                      .map( agendaMapper::createAgendaResponse)
                      .toList();
    }

    @Override
    public LocalDateTime resolveFirstDateOfMonthInAgenda(Agenda agenda, YearMonth yearMonth) {

        LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();

        LocalDateTime agendaStart = agenda.getStart_date().atStartOfDay();

        if ( agendaStart.isAfter(startOfMonth) ){
            return agendaStart; //load appointments from this date
        }
        return startOfMonth; //in case of false load appointments from first date of month
    }

    @Override
    public LocalDateTime resolveLastDateOfMonthInAgenda(Agenda agenda, YearMonth yearMonth) {

        LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(LocalTime.MAX);

        LocalDateTime agendaEnd = agenda.getFinal_date().atTime(LocalTime.MAX);

        if ( agendaEnd.isBefore(endOfMonth) ){
            return agendaEnd; //load appointments from this date
        }
        return endOfMonth; //in case of false load appointments from end date of month
    }

    @Override
    public void validateMonthInAgenda(Agenda agenda, YearMonth yearMonth) {

        List<Integer> numberOfMonths = this.getNumberOfMonthsBetween(agenda.getStart_date(), agenda.getFinal_date());

        if ( !numberOfMonths.contains(yearMonth.getMonthValue() ) ){
            throw new AgendaMonthOutOfRangeException("The requested month must be within the valid months of the agenda ");
        }
    }

    private List<Integer> getNumberOfMonthsBetween(LocalDate startDate, LocalDate finalDate) {

        LocalDate date = startDate.withDayOfMonth(1);

        List<Integer> months = new ArrayList<>();

        while ( !date.isAfter(finalDate) ) {

            months.add( date.getMonthValue());
            date = date.plusMonths(1);
        }
        return months;
    }


//
//    @Override
//    public String editAgenda(AgendaRequestDTO agendaRequestDTO) {
//
//        Agenda agenda = agendaRepository.findById(agendaRequestDTO.id_agenda()).orElse(null);
//        if (agenda != null) {
//
//            agenda.setAgenda_name(agendaRequestDTO.agendaName());
//            agenda.setStart_date(agendaRequestDTO.startDate());
//            agenda.setFinal_date(agendaRequestDTO.finalDate());
//            agenda.setActive(agendaRequestDTO.active());
//            agenda.setSchedules(agendaRequestDTO.schedules());
//
//            if (agendaRequestDTO.idProduct() != null) {
//                Product product = productService.validateIfProductExists(agendaRequestDTO.idUserApp());
//
//                if (product != null) {
//                    agenda.setProduct(product);
//                }
//            }
////            List<Schedule> removeList = new ArrayList<>();
////
////            //recorro lista de schedules del agendaRequestDTO
////            for (Schedule schedule : agendaRequestDTO.schedules()) {
////
////                for (Schedule oldSchedule : agenda.getSchedules()) {
////
////                    if (schedule.getId_schedule() != oldSchedule.getId_schedule()) {
//                        removeList.add(oldSchedule);
//                    }
////                if (schedule.getId_schedule() == oldSchedule.getId_schedule()) {
////                    oldSchedule = schedule;
////                }
////                if (schedule.getId_schedule() == null) {
////
////                    //llamar al service schedule, persistir
////                    //enlazar esa nueva entidad a el old schedule
////                    //persistir
////                }
//                }
//            }
//            if (!removeList.isEmpty()) {
//                agenda.getSchedules().removeAll(removeList);
//            }
//
//            agendaRepository.save(agenda);
//            return agenda.getAgenda_name() + "successfully updated";
//        }
//        return "the agenda does not exists";
//    }

}