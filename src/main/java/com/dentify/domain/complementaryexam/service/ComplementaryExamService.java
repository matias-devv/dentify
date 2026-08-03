package com.dentify.domain.complementaryexam.service;



import com.dentify.domain.complementaryexam.dto.response.ComplementaryExamResponse;
import com.dentify.domain.complementaryexam.model.ComplementaryExam;
import com.dentify.domain.medicalhistory.model.MedicalHistory;
import com.dentify.domain.medicalhistory.service.IMedicalHistoryService;
import com.dentify.domain.userProfile.model.UserProfile;
import com.dentify.domain.userProfile.service.IUserProfileService;
import com.dentify.exception.general.EmptyFileException;
import com.dentify.exception.general.FileTooLargeException;
import com.dentify.exception.general.UnsupportedFileTypeException;
import com.dentify.integration.filestorage.IFileStorageService;
import com.dentify.mapper.ComplementaryExamMapper;
import com.dentify.utils.FilenameSanitizer;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

/**
 * Orchestrates {@code POST /api/exams/{medicalHistoryId}}.
 *
 * <p><b>Assumption :</b> queries {@link MedicalHistory} directly via
 * of through {@code IMedicalHistoryService}, since that method isn't necessarily part of the
 * service's public interface contract — same query, same tenant-scoping guarantee.</p>
 *
 * vot a hacerlo via interfcae IMedicalHistoryService
 **/
@Service
@RequiredArgsConstructor
@Slf4j
public class ComplementaryExamService implements IComplementaryExamService {

    // Mime types allowed for upload (2.6) — Content-Type declared is only the first filter;
    // validateMagicBytes below is the real minimal content check (sección 9.13).
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of("image/jpeg", "image/png", "application/pdf");

    // services
    private final IMedicalHistoryService medicalHistoryService;
    private final IUserProfileService userProfileService;
    private final IFileStorageService fileStorageService;

    // mapper
    private final ComplementaryExamMapper complementaryExamMapper;

    @Value("${B2_MAX_FILE_SIZE_MB}")
    private long maxFileSizeMb;

    @Override
    @Transactional
    public ComplementaryExamResponse uploadExam(Long medicalHistoryId, String username, MultipartFile file) {

        // 1. resolve actor UserProfile + clinicId
        UserProfile uploadBy = userProfileService.findByAuthUsernameWithoutClinic(username);
        Long clinicId = userProfileService.findClinicByAuthUserUsername(username).getId();

        // 2-3. resolve MedicalHistory scoped to this clinic
        MedicalHistory medicalHistory = medicalHistoryService.findMedicalHistoryBaseByIdAndClinicId(medicalHistoryId, clinicId);

        // 4. validate file before touching the storage provider (AC3/AC4)
        this.validateFile(file);

        // 5. sanitize filename
        String sanitizedFilename = FilenameSanitizer.sanitize( file.getOriginalFilename() );

        // 6. build the object key
        Long patientId = medicalHistory.getPatient().getId_patient();
        String key = this.buildObjectKey(clinicId, patientId, medicalHistoryId, sanitizedFilename);

        // 7. upload to B2
        fileStorageService.upload(key, file);

        // 8. build the entity and attach it to the medical history
        ComplementaryExam exam = complementaryExamMapper.buildComplementaryExam(file, key, file.getContentType(), medicalHistory, uploadBy);
        medicalHistory.addExam(exam);

        // 9-10. persist, with best-effort compensation on failure (AC5b)
        this.handlePersistenceOfMedicalHistory(medicalHistory, key);

        // 11. presigned URL + response
        String presignedUrl = fileStorageService.generatePresignedUrl(key);

        return complementaryExamMapper.buildComplementaryExamResponse(exam, presignedUrl);
    }

    private void validateFile(MultipartFile file) {

        this.validateFileExistence(file);

        this.validateFileType(file);

        this.validateFileSize(file);
    }

    private void validateFileExistence(MultipartFile file) {
        if ( file == null || file.isEmpty() ) {
            throw new EmptyFileException("The uploaded file must not be empty");
        }
    }

    private void validateFileType (MultipartFile file){

        String declaredContentType = file.getContentType();

        if ( declaredContentType == null || !ALLOWED_MIME_TYPES.contains(declaredContentType) ) {
            throw new UnsupportedFileTypeException("File type not allowed: " + declaredContentType);
        }

        this.validateMagicBytes(file, declaredContentType);
    }

    /**
     * Real minimal content validation: the declared Content-Type is only a first filter, the file's actual magic bytes must match one of the allowed
     * types, or the upload is rejected as a spoofed Content-Type.
     */
    private void validateMagicBytes(MultipartFile file, String declaredContentType) {

        byte[] header;

        try ( var inputStream = file.getInputStream() ) {

            header = inputStream.readNBytes(8);
        } catch (IOException e) {
            throw new UnsupportedFileTypeException("Could not read file contents to validate its type");
        }

        String detectedContentType = this.detectMimeTypeFromMagicBytes(header);

        if ( detectedContentType == null || !detectedContentType.equals(declaredContentType) ) {
            throw new UnsupportedFileTypeException("Declared file type does not match the file's actual content");
        }
    }

    private String detectMimeTypeFromMagicBytes(byte[] header) {

        if ( header.length >= 4 && ( header[0] & 0xFF ) == 0x89 && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47) {
            return "image/png";
        }
        if ( header.length >= 3 && (header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8 && (header[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if ( header.length >= 4 && header[0] == 0x25 && header[1] == 0x50 && header[2] == 0x44 && header[3] == 0x46) {
            return "application/pdf";
        }
        return null;
    }

    private void validateFileSize(MultipartFile file) {

        long maxSizeBytes = maxFileSizeMb * 1024 * 1024;

        if (file.getSize() > maxSizeBytes) {
            throw new FileTooLargeException("File exceeds the maximum allowed size of " + maxFileSizeMb + "MB");
        }
    }


    private String buildObjectKey(Long clinicId, Long patientId, Long medicalHistoryId, String sanitizedFilename) {
        return "%d/%d/%d/%s-%s".formatted(clinicId, patientId, medicalHistoryId, UUID.randomUUID(), sanitizedFilename);
    }

    private void handlePersistenceOfMedicalHistory(MedicalHistory medicalHistory, String key) {
        try {

            medicalHistoryService.persistMedicalHistory(medicalHistory);

        } catch (RuntimeException persistenceFailure) {

            this.compensateOrphanedUpload(key, persistenceFailure);

            throw persistenceFailure;
        }
    }

    /**
     * Best-effort compensation for AC5b: the object was already uploaded to B2 when
     * persistence failed afterward.
     * <p>
     * <b>Known gap :</b> MT-03 explicitly excludes a delete method from {@code IFileStorageService} for this batch
     * ("se agrega en el batch del DELETE"), so this can only log the orphaned key for now — it does not yet call
     * a real delete. Once the DELETE batch adds {@code IFileStorageService.delete(key)},
     * wire the actual deletion call in here.
     * </p>
     */
    private void compensateOrphanedUpload(String key, RuntimeException cause) {
        log.error("Persistence failed after a successful B2 upload; object is orphaned and needs manual/job cleanup. key={}", key, cause);
    }
}