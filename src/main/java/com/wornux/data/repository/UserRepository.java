package com.wornux.data.repository;

import com.wornux.data.base.AbstractRepository;
import com.wornux.data.entity.User;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends AbstractRepository<User, Long> {
  Optional<User> findByUsername(String username);

  Optional<User> findByEmail(String email);
}
