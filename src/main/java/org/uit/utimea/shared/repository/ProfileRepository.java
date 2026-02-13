package org.uit.utimea.shared.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uit.utimea.shared.entity.Profile;

import java.util.Optional;

@Repository
public interface ProfileRepository extends BaseRepository<Profile> {
    // Count students in a major section
    @Query("SELECT COUNT(p) FROM Profile p WHERE p.majorSection.id = :majorSectionId")
    long countByMajorSectionId(@Param("majorSectionId") Long majorSectionId);
    
    // Fetch Profile with User relationship
    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT p FROM Profile p WHERE p.id = :id")
    Optional<Profile> findByIdWithUser(@Param("id") Long id);
}
