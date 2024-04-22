package com.stat.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PrevRepository extends JpaRepository<Prev, Long> {
  Optional<Prev> getPrevByUser(User user);
}
