package com.wealthops.registration.repository;

import com.wealthops.registration.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRoleIn(Collection<com.wealthops.registration.entity.Role> roles);

    List<User> findByBranchIdAndRoleIn(Long branchId, Collection<com.wealthops.registration.entity.Role> roles);

    List<User> findByRole(com.wealthops.registration.entity.Role role);
}