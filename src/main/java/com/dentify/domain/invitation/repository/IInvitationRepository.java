package com.dentify.domain.invitation.repository;

import com.dentify.domain.invitation.model.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {
}
