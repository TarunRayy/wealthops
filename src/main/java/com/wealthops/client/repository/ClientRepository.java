package com.wealthops.client.repository;

import com.wealthops.client.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByEmail(String email);
    Optional<Client> findByPanNumber(String panNumber);
    List<Client> findByBranchId(Long branchId);
    List<Client> findByAssignedRmId(Long rmId);
}