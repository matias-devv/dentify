package com.dentify.mapper;

import com.dentify.domain.complementaryexam.dto.response.ComplementaryExamResponse;
import com.dentify.domain.complementaryexam.model.ComplementaryExam;
import com.dentify.domain.medicalhistory.model.MedicalHistory;
import com.dentify.domain.userProfile.dto.response.SimpleUserProfileResponse;
import com.dentify.domain.userProfile.model.UserProfile;
import com.dentify.utils.FilenameSanitizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ComplementaryExamMapper {

    private final UserProfileMapper userProfileMapper;

    public ComplementaryExam buildComplementaryExam(MultipartFile file, String objectKey, String contentType,
                                                    MedicalHistory medicalHistory, UserProfile uploadBy) {

        return ComplementaryExam.builder()
                                .objectKey(objectKey)
                                .filename( FilenameSanitizer.sanitize(file.getOriginalFilename() ) )
                                .fileType(contentType)
                                .medicalHistory(medicalHistory)
                                .uploadBy(uploadBy)
                                .uploadDate(LocalDateTime.now())
                                .build();
    }

    public ComplementaryExamResponse buildComplementaryExamResponse(ComplementaryExam exam, String presignedUrl) {

        SimpleUserProfileResponse uploadByResponse = userProfileMapper.buildSimpleUserProfileResponse(exam.getUploadBy());

        return new ComplementaryExamResponse(exam.getId(),
                                             presignedUrl,
                                             exam.getFilename(),
                                             exam.getFileType(),
                                             exam.getUploadDate(),
                                             uploadByResponse );
    }

}
