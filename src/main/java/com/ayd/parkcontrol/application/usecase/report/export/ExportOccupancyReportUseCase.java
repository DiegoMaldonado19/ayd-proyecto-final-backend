package com.ayd.parkcontrol.application.usecase.report.export;

import com.ayd.parkcontrol.application.dto.response.report.OccupancyReportResponse;
import com.ayd.parkcontrol.application.usecase.report.GenerateOccupancyReportUseCase;
import com.ayd.parkcontrol.domain.exception.BusinessRuleException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Use case para exportar reportes de ocupación en diferentes formatos
 */
@Service
@RequiredArgsConstructor
public class ExportOccupancyReportUseCase {

    private final GenerateOccupancyReportUseCase generateOccupancyReportUseCase;
    private final ReportExportService exportService;

    public byte[] exportToPdf() {
        List<OccupancyReportResponse> data = generateOccupancyReportUseCase.execute();
        return exportService.exportOccupancyReportToPdf(data);
    }

    public byte[] exportToCsv() {
        List<OccupancyReportResponse> data = generateOccupancyReportUseCase.execute();
        return exportService.exportOccupancyReportToCsv(data);
    }

    public byte[] exportToImage() {
        List<OccupancyReportResponse> data = generateOccupancyReportUseCase.execute();
        return exportService.exportOccupancyReportToImage(data);
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
