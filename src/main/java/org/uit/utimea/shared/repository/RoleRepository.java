package org.uit.utimea.shared.repository;

import org.springframework.stereotype.Repository;
import org.uit.utimea.shared.entity.Role;

import java.util.Optional;

@Repository
public interface RoleRepository extends BaseRepository<Role> {
    Optional<Role> findByName(String name);
}
