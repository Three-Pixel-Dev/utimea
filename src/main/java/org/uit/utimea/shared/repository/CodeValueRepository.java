package org.uit.utimea.shared.repository;

import org.springframework.stereotype.Repository;
import org.uit.utimea.shared.entity.Code;
import org.uit.utimea.shared.entity.CodeValue;

import java.util.List;
import java.util.Optional;

@Repository
public interface CodeValueRepository extends BaseRepository<CodeValue> {
    Optional<CodeValue> findByCodeAndName(Code code, String name);
    List<CodeValue> findByCode(Code code);
}
