package com.dlsexam.catalogservice.repository;

import com.dlsexam.catalogservice.domain.CastMember;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CastMemberRepository extends JpaRepository<CastMember, UUID> {

    List<CastMember> findByContentId(UUID contentId);
}
