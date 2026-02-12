package org.uit.utimea.shared.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uit.utimea.shared.entity.Profile;

@Repository
public interface ProfileRepository extends BaseRepository<Profile> {
    // Count students in a major section
    @Query("SELECT COUNT(p) FROM Profile p WHERE p.majorSection.id = :majorSectionId")
    long countByMajorSectionId(@Param("majorSectionId") Long majorSectionId);
}
