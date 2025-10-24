package com.ayd.parkcontrol.domain.repository;

import com.ayd.parkcontrol.domain.model.branch.Branch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface BranchRepository {
    Branch save(Branch branch);

    Optional<Branch> findById(Long id);

    Optional<Branch> findByName(String name);

    Page<Branch> findAll(Pageable pageable);

    Page<Branch> findByIsActive(Boolean isActive, Pageable pageable);

    void deleteById(Long id);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}
