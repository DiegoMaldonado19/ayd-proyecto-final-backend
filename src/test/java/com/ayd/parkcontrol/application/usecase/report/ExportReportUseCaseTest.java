package com.ayd.parkcontrol.application.usecase.report;

import com.ayd.parkcontrol.application.dto.request.report.ExportReportRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ExportReportUseCaseTest {

    @InjectMocks
    private ExportReportUseCase exportReportUseCase;

    @Test
    void execute_shouldReturnEmptyByteArray_whenExportingReport() {
        ExportReportRequest request = ExportReportRequest.builder()
                .reportType("BILLING")
                .exportFormat("PDF")
                .build();

        byte[] result = exportReportUseCase.execute(request);

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }
}
