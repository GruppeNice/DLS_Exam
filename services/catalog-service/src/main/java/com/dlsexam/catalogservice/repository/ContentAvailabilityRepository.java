package com.dlsexam.catalogservice.repository;

import com.dlsexam.catalogservice.domain.ContentAvailability;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentAvailabilityRepository extends JpaRepository<ContentAvailability, UUID> {

    List<ContentAvailability> findByContentId(UUID contentId);
}
