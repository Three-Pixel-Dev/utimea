package org.uit.utimea.shared.repository;

import org.springframework.stereotype.Repository;
import org.uit.utimea.shared.entity.MajorSection;

import java.util.Optional;

@Repository
public interface MajorSectionRepository extends BaseRepository<MajorSection> {
    Optional<MajorSection> findByName(String name);
}
