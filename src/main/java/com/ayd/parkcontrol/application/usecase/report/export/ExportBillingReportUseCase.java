package com.ayd.parkcontrol.application.usecase.report.export;

import com.ayd.parkcontrol.application.dto.response.report.BillingReportResponse;
import com.ayd.parkcontrol.application.usecase.report.GenerateBillingReportUseCase;
import com.ayd.parkcontrol.domain.exception.BusinessRuleException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Use case para exportar reportes de facturación en diferentes formatos
 */
@Service
@RequiredArgsConstructor
public class ExportBillingReportUseCase {

    private final GenerateBillingReportUseCase generateBillingReportUseCase;
    private final ReportExportService exportService;

    public byte[] exportToPdf() {
        List<BillingReportResponse> data = generateBillingReportUseCase.execute();
        return exportService.exportBillingReportToPdf(data);
    }

    public byte[] exportToCsv() {
        List<BillingReportResponse> data = generateBillingReportUseCase.execute();
        return exportService.exportBillingReportToCsv(data);
    }

    public byte[] exportToImage() {
        List<BillingReportResponse> data = generateBillingReportUseCase.execute();
        return exportService.exportBillingReportToImage(data);
    }

    public byte[] export(String format) {
        return switch (format.toUpperCase()) {
            case "PDF" -> exportToPdf();
            case "CSV" -> exportToCsv();
            case "PNG", "IMAGE" -> exportToImage();
            default -> throw new BusinessRuleException("Formato de exportación no soportado: " + format);
        };
    }
}
