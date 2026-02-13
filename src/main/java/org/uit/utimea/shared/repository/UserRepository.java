package org.uit.utimea.shared.repository;

import org.springframework.stereotype.Repository;
import org.uit.utimea.shared.entity.User;

import java.util.Optional;

@Repository
public interface UserRepository extends BaseRepository<User> {
    Optional<User> findByEmail(String email);
}
