package org.uit.utimea.shared.repository;

import org.springframework.stereotype.Repository;
import org.uit.utimea.shared.entity.Code;

import java.util.Optional;

@Repository
public interface CodeRepository extends BaseRepository<Code> {
    Optional<Code> findByConstantValue(String constantValue);
}
