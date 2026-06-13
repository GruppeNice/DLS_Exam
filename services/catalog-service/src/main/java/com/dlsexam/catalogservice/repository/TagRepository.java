package com.dlsexam.catalogservice.repository;

import com.dlsexam.catalogservice.domain.Tag;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, UUID> {
}
