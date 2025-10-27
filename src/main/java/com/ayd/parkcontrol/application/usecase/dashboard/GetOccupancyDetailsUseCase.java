package com.ayd.parkcontrol.application.usecase.dashboard;

import com.ayd.parkcontrol.application.dto.response.dashboard.OccupancyDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetOccupancyDetailsUseCase {

    @Transactional(readOnly = true)
    public List<OccupancyDetailResponse> execute() {
        return new ArrayList<>();
    }
}
