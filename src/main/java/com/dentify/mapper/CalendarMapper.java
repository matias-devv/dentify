package com.dentify.mapper;

import com.dentify.calendar.dto.response.day.DetailedDayResponse;
import com.dentify.calendar.dto.response.day.DetailedSlotResponse;
import com.dentify.calendar.dto.response.month.DailySummaryResponse;
import com.dentify.calendar.dto.response.month.MonthResponse;
import com.dentify.calendar.dto.response.week.DayResponse;
import com.dentify.calendar.dto.response.week.WeekResponse;
import com.dentify.domain.agenda.model.Agenda;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Component
public class CalendarMapper {

    public MonthResponse buildMonthResponse(Agenda agenda, YearMonth yearMonth, List<DailySummaryResponse> days) {

        String nameProduct = "";

        if ( agenda.getProduct() != null ){
            nameProduct = agenda.getProduct().getNameProduct();
        }

        return new MonthResponse(agenda.getId_agenda(),
                yearMonth.getYear(),
                yearMonth.getMonth().getValue(),
                yearMonth.getMonth().getDisplayName(TextStyle.FULL, new Locale("es")),
                nameProduct,
                agenda.getDuration_minutes(),
                days);
    }

    public WeekResponse buildWeekResponse(Agenda agenda, List<DayResponse> dayResponses) {
        return new WeekResponse(agenda.getId_agenda(),
                agenda.getAgenda_name(),
                agenda.getStart_date(),
                agenda.getFinal_date(),
                dayResponses);
    }

    public DetailedDayResponse buildDetailedDayResponse(Agenda agenda, List<DetailedSlotResponse> slots, LocalDate requestedDate,
                                                         Integer totalSlots, Integer freeSlots, Integer occupiedSlots, String message) {
        Long idProduct = null;
        String nameProduct = "";

        if ( agenda.getProduct() != null ){
            idProduct = agenda.getProduct().getId_product();
            nameProduct = agenda.getProduct().getNameProduct();
        }

        return new DetailedDayResponse(agenda.getId_agenda(),
                idProduct,
                requestedDate,
                requestedDate.getDayOfWeek(),
                agenda.getDuration_minutes(),
                nameProduct,
                totalSlots,
                freeSlots,
                occupiedSlots,
                slots ,
                message );
    }

}
