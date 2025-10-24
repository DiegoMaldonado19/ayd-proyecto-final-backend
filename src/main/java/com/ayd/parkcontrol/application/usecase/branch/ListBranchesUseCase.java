package com.ayd.parkcontrol.application.usecase.branch;

import com.ayd.parkcontrol.application.dto.response.branch.BranchResponse;
import com.ayd.parkcontrol.application.dto.response.common.PageResponse;
import com.ayd.parkcontrol.application.mapper.BranchDtoMapper;
import com.ayd.parkcontrol.domain.model.branch.Branch;
import com.ayd.parkcontrol.domain.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ListBranchesUseCase {

    private final BranchRepository branchRepository;
    private final BranchDtoMapper branchDtoMapper;

    @Transactional(readOnly = true)
    public PageResponse<BranchResponse> execute(Integer page, Integer size, String sortBy, String sortDirection,
            Boolean isActive) {
        log.debug("Listing branches - page: {}, size: {}, sortBy: {}, sortDirection: {}, isActive: {}",
                page, size, sortBy, sortDirection, isActive);

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<Branch> branchPage;
        if (isActive != null) {
            branchPage = branchRepository.findByIsActive(isActive, pageable);
        } else {
            branchPage = branchRepository.findAll(pageable);
        }

        List<BranchResponse> content = branchPage.getContent().stream()
                .map(branchDtoMapper::toResponse)
                .collect(Collectors.toList());

        log.debug("Found {} branches", branchPage.getTotalElements());

        return PageResponse.<BranchResponse>builder()
                .content(content)
                .page_number(branchPage.getNumber())
                .page_size(branchPage.getSize())
                .total_elements(branchPage.getTotalElements())
                .total_pages(branchPage.getTotalPages())
                .is_first(branchPage.isFirst())
                .is_last(branchPage.isLast())
                .has_previous(branchPage.hasPrevious())
                .has_next(branchPage.hasNext())
                .build();
    }
}
