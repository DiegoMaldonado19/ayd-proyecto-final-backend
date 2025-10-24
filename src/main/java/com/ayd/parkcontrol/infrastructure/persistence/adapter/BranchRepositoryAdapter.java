package com.ayd.parkcontrol.infrastructure.persistence.adapter;

import com.ayd.parkcontrol.domain.model.branch.Branch;
import com.ayd.parkcontrol.domain.repository.BranchRepository;
import com.ayd.parkcontrol.infrastructure.persistence.entity.BranchEntity;
import com.ayd.parkcontrol.infrastructure.persistence.mapper.BranchMapper;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaBranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BranchRepositoryAdapter implements BranchRepository {

    private final JpaBranchRepository jpaBranchRepository;
    private final BranchMapper branchMapper;

    @Override
    public Branch save(Branch branch) {
        BranchEntity entity = branchMapper.toEntity(branch);
        BranchEntity savedEntity = jpaBranchRepository.save(entity);
        return branchMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Branch> findById(Long id) {
        return jpaBranchRepository.findById(id)
                .map(branchMapper::toDomain);
    }

    @Override
    public Optional<Branch> findByName(String name) {
        return jpaBranchRepository.findByName(name)
                .map(branchMapper::toDomain);
    }

    @Override
    public Page<Branch> findAll(Pageable pageable) {
        return jpaBranchRepository.findAll(pageable)
                .map(branchMapper::toDomain);
    }

    @Override
    public Page<Branch> findByIsActive(Boolean isActive, Pageable pageable) {
        return jpaBranchRepository.findByIsActive(isActive, pageable)
                .map(branchMapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        jpaBranchRepository.deleteById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return jpaBranchRepository.existsByName(name);
    }

    @Override
    public boolean existsByNameAndIdNot(String name, Long id) {
        return jpaBranchRepository.existsByNameAndIdNot(name, id);
    }
}
