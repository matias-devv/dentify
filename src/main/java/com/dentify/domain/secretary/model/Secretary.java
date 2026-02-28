package com.dentify.domain.secretary;

import com.dentify.domain.dentist.Dentist;
import com.dentify.domain.userProfile.model.UserProfile;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "secretaries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Secretary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 1:1 with UserProfile — Secretary is the owner of the relation
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_profile_id", nullable = false, unique = true)
    private UserProfile userProfile;

    // Inverse side of the relation — Dentist is the owner
    @ManyToMany(mappedBy = "secretaries", fetch = FetchType.LAZY)
    private Set<Dentist> dentists = new HashSet<>();

    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}