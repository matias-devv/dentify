package com.dentify.domain.complementaryexam.model;

import com.dentify.domain.medicalhistory.model.MedicalHistory;
import com.dentify.domain.userProfile.model.UserProfile;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Stores metadata for supplementary files (X-rays, lab results, images) attached
 * to a clinical history. The actual binary is stored in Cloudinary (or S3);
 * only the resulting URL and file metadata are persisted here.
 * <p>
 * Upload flow (to be implemented in service + Cloudinary SDK):
 * <ol>
 *   <li>Frontend POST multipart/form-data to backend.</li>
 *   <li>Backend uploads binary to Cloudinary.</li>
 *   <li>Backend persists this entity with the returned URL.</li>
 *   <li>Backend returns the created entity to frontend.</li>
 * </ol>
 * </p>
 * Does not extend TenantEntity; tenant scope is reached via {@code MedicalHistory}.
 */
@Entity
@Table(name = "complementary_exam")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplementaryExam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Public URL returned by Cloudinary (or S3 signed/public URL). */
    @Column(name = "file_url", nullable = false)
    private String file_url;

    /** Original filename as uploaded by the user (e.g. "rx_panoramica.png"). */
    @Column(name = "filename", nullable = false)
    private String filename;

    /** MIME type (e.g. "image/png", "application/pdf"). Nullable — inferred from upload. */
    @Column(name = "file_type")
    private String fileType;

    @CreationTimestamp
    @Column(name = "upload_date", nullable = false, updatable = false)
    private LocalDateTime uploadDate;

    // ── Relationships ──────────────────────────────────────────────────────────

    /** Parent clinical history. Deletion cascades from MedicalHistory. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_history_id", nullable = false)
    private MedicalHistory medicalHistory;

    /** UserProfile who uploaded the file. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploadBy", nullable = false)
    private UserProfile uploadBy;
}
