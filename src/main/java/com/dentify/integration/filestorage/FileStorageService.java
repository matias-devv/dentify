package com.dentify.integration.filestorage;

import com.dentify.exception.general.FileStorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;

/**
 * Concrete {@link IFileStorageService} implementation against Backblaze B2,
 * via the AWS SDK for Java v2 {@link S3Client}/{@link S3Presigner} beans
 * exposed by {B2ClientConfig}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService implements IFileStorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${B2_BUCKET_NAME}")
    private String bucketName;

    @Value("${B2_PRESIGNED_URL_EXPIRATION_MINUTES}")
    private long presignedUrlExpirationMinutes;

    @Override
    public void upload(String key, MultipartFile file) {

        try ( InputStream inputStream = file.getInputStream() ) {

            PutObjectRequest request = PutObjectRequest.builder()
                                                        .bucket(bucketName)
                                                        .key(key)
                                                        .contentType(file.getContentType())
                                                        .build();

            s3Client.putObject( request, RequestBody.fromInputStream(inputStream, file.getSize() ) );

            // NFR sección 8: log success/failure + key, never credentials or the full presigned URL.
            log.info("File uploaded to B2 successfully. key={}", key);

        } catch (IOException | S3Exception e) {
            log.error("Failed to upload file to B2. key={}", key, e);
            throw new FileStorageException("Failed to upload file to storage provider");
        }
    }

    @Override
    public String generatePresignedUrl(String key) {

        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                                                                .bucket(bucketName)
                                                                .key(key)
                                                                .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                                                                            .signatureDuration( Duration.ofMinutes(presignedUrlExpirationMinutes) )
                                                                            .getObjectRequest( getObjectRequest )
                                                                            .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject( presignRequest );

            log.info("Presigned URL generated successfully. key={}", key);

            return presignedRequest.url().toString();

        } catch (S3Exception e) {
            log.error("Failed to generate presigned URL. key={}", key, e);
            throw new FileStorageException("Failed to generate presigned URL");
        }
    }
}