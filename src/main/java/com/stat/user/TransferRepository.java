package com.stat.user;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferRepository extends JpaRepository<Transfer, Long>{
	List<Transfer> findAllTransferByReceiver(String email);
}
