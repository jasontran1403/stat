package com.stat.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfirmationRepository extends JpaRepository<Confirmation, String>{
	Optional<Confirmation> findByEmail(String email);
}
