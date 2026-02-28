package com.dentify.calendar.dto.response;

import java.util.List;

public record AppUserResponse(Long id,
                              String name,
                              String surname,
                              List<String> specialties) {
}
