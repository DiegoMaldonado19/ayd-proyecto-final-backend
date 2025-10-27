package com.ayd.parkcontrol.application.usecase.report;

import com.ayd.parkcontrol.application.dto.request.report.ExportReportRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExportReportUseCase {

    public byte[] execute(ExportReportRequest request) {
        return new byte[0];
    }
}
