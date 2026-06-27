package com.dentify.integration.cloudinary;

import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class CloudinaryService implements ICloudinaryService {

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    private final Cloudinary cloudinary;

    /**
     * Upload PDF to Cloudinary
     * @param filename File name (e.g., "comprobante_123.pdf")
     * @param fileBytes PDF content in bytes
     * @return Public URL of the file
     */
    @Override
    public String uploadPDF(String filename, byte[] fileBytes) {
        try {
            //Remove .pdf extension to use as public_id
            String publicId = filename.replace(".pdf", "");

            Map<String, Object> options = new HashMap<>();
            options.put("public_id", "receipts/" + publicId);
            options.put("resource_type", "raw"); // important for PDFs
            options.put("folder", "odontologia/comprobantes");
            options.put("overwrite", false);    //do not overwrite if it already exists

            // Upload
            Map uploadResult = cloudinary.uploader().upload(fileBytes, options);

            String publicUrl = (String) uploadResult.get("secure_url");

            log.info("PDF uploaded: {} -> {}", filename, publicUrl);

            return publicUrl;

        } catch (Exception e) {
            log.error("Error uploading PDF to Cloudinary: {}", filename, e);
            throw new RuntimeException("Error uploading file to cloud storage", e);
        }
    }

    /**
     * Delete PDF from Cloudinary
     * @param filename Name of the file to delete (e.g., "comprobante_123.pdf")
     */
    @Override
    public void delete(String filename) {

        try {

            //Build the correct public_id
            String publicId = "receipts/" + filename.replace(".pdf", "");

            Map<String, Object> options = new HashMap<>();
            options.put("resource_type", "raw"); // important for PDFs
            options.put("invalidate", true); // Invalidates CDN cache

            //Delete
            Map result = cloudinary.uploader().destroy(publicId, options);

            String status = (String) result.get("result");

            if ("ok".equals(status)) {
                log.info("PDF deleted: {}", filename);
            } else {
                log.warn("Delete status: {} for file: {}", status, filename);
            }

        } catch (Exception e) {
            log.error("Error deleting PDF from Cloudinary: {}", filename, e);
            // I don't throw an exception to avoid breaking the flow.
            // If the file doesn't exist, Cloudinary returns "not found" but doesn't fail.
        }
    }

    /**
     * Builds a Cloudinary download URL by inserting the fl_attachment
     * flag into the stored URL, forcing browser download instead of render.
     *
     * Stored:   .../raw/upload/v123/odontologia/comprobantes/receipts/file
     * Download: .../raw/upload/fl_attachment/v123/odontologia/comprobantes/receipts/file
     *
     * @param storedUrl url_pdf persisted in Receipt entity
     * @return ready-to-use download URL
     */
    @Override
    public String buildDownloadUrl(String storedUrl) {

        if ( storedUrl == null || storedUrl.isBlank() ) {
            throw new IllegalArgumentException("Cannot build download URL: stored URL is empty");
        }

        String UPLOAD_SEGMENT = "/upload/";

        int insertionIndex = storedUrl.indexOf(UPLOAD_SEGMENT);

        if (insertionIndex == -1) {
            throw new IllegalArgumentException("Cannot build download URL: unexpected Cloudinary URL format -> " + storedUrl);
        }

        int afterUpload = insertionIndex + UPLOAD_SEGMENT.length();

        return storedUrl.substring(0, afterUpload) + "fl_attachment/" + storedUrl.substring(afterUpload);
    }
}
