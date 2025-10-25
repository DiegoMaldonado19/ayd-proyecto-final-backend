package com.ayd.parkcontrol.application.usecase.rate;

import com.ayd.parkcontrol.application.dto.response.rate.RateBranchResponse;
import com.ayd.parkcontrol.application.mapper.RateDtoMapper;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaBranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListRateBranchesUseCase {

    private final JpaBranchRepository branchRepository;
    private final RateDtoMapper mapper;

    @Transactional(readOnly = true)
    public List<RateBranchResponse> execute() {
        return branchRepository.findAll()
                .stream()
                .map(mapper::toDomain)
                .map(mapper::toRateBranchResponse)
                .collect(Collectors.toList());
    }
}
