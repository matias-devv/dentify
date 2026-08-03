package com.dentify.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;
import java.time.Duration;

/**
 * Exposes {@link S3Client} and {@link S3Presigner} beans pointed at Backblaze
 * B2's S3-compatible endpoint.
 * Reference: <a href="https://www.backblaze.com/docs/cloud-storage-use-the-aws-sdk-for-java-v2-with-backblaze-b2-cloud-storage">
 */
@Configuration
public class B2ClientConfig {

    @Value("${B2_APPLICATION_KEY_ID}")
    private String applicationKeyId;

    @Value("${B2_APPLICATION_KEY}")
    private String applicationKey;

    @Value("${B2_ENDPOINT}")
    private String endpoint;

    @Value("${B2_REGION}")
    private String region;

    // NFR sección 8: explicit connect/read timeouts so a stuck B2 call never hangs the request indefinitely.
    private static final Duration CONNECTION_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(30);

    private AwsBasicCredentials credentials() {
        return AwsBasicCredentials.create(applicationKeyId, applicationKey);
    }

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                       .endpointOverride( URI.create(endpoint) )
                       .region( Region.of(region) )
                       .credentialsProvider( StaticCredentialsProvider.create( this.credentials() ) )
                       .httpClientBuilder( ApacheHttpClient.builder()
                                                           .connectionTimeout(CONNECTION_TIMEOUT)
                                                           .socketTimeout(READ_TIMEOUT) )
                       .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                          .endpointOverride( URI.create(endpoint) )
                          .region( Region.of(region) )
                          .credentialsProvider( StaticCredentialsProvider.create(credentials() ) )
                          .build();
    }
}