package com.dentify.domain.complementaryexam.dto.response;

import com.dentify.domain.userProfile.dto.response.SimpleUserProfileResponse;

import java.time.LocalDateTime;

public record ComplementaryExamResponse(Long id,
                                        String object_key,
                                        String filename,
                                        String fileType,
                                        LocalDateTime uploadDate,
                                        SimpleUserProfileResponse uploadBy) {
}
