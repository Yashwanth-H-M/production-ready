package com.nurserycrm.identity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IdentityUserRepository extends JpaRepository<IdentityUser, UUID> {
}
