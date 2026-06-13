package com.dlsexam.catalogservice.repository;

import com.dlsexam.catalogservice.domain.Genre;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GenreRepository extends JpaRepository<Genre, UUID> {
}
