package com.ayd.parkcontrol.application.usecase.report;

import com.ayd.parkcontrol.application.dto.response.report.BillingReportResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GenerateBillingReportUseCase {

    @Transactional(readOnly = true)
    public List<BillingReportResponse> execute() {
        return new ArrayList<>();
    }
}
