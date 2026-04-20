package com.dentify.dashboard.dto;

import java.util.List;

public record CancelledTodayResponse(List<CancelledDetailResponse> details,
                                     int count){
}
