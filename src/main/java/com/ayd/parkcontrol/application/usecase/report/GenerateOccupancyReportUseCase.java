package com.ayd.parkcontrol.application.usecase.report;

import com.ayd.parkcontrol.application.dto.response.report.OccupancyReportResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GenerateOccupancyReportUseCase {

    @Transactional(readOnly = true)
    public List<OccupancyReportResponse> execute() {
        return new ArrayList<>();
    }
}
