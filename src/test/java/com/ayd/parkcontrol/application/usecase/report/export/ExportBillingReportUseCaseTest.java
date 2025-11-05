package com.ayd.parkcontrol.application.usecase.report.export;

import com.ayd.parkcontrol.application.dto.response.report.BillingReportResponse;
import com.ayd.parkcontrol.application.usecase.report.GenerateBillingReportUseCase;
import com.ayd.parkcontrol.domain.exception.BusinessRuleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExportBillingReportUseCaseTest {

    @Mock
    private GenerateBillingReportUseCase generateBillingReportUseCase;

    @Mock
    private ReportExportService exportService;

    @InjectMocks
    private ExportBillingReportUseCase exportBillingReportUseCase;

    private List<BillingReportResponse> mockReportData;

    @BeforeEach
    void setUp() {
        BillingReportResponse response = new BillingReportResponse();
        response.setBranchId(1L);
        response.setBranchName("Sucursal Centro");
        response.setTotalRevenue(new BigDecimal("15000.50"));
        response.setTotalTickets(150L);
        response.setAverageTicketValue(new BigDecimal("100.00"));

        mockReportData = Arrays.asList(response);
    }

    @Test
    void exportToPdf_shouldCallGenerateAndExportService() {
        // Arrange
        byte[] expectedPdfBytes = "PDF Content".getBytes();
        when(generateBillingReportUseCase.execute()).thenReturn(mockReportData);
        when(exportService.exportBillingReportToPdf(mockReportData)).thenReturn(expectedPdfBytes);

        // Act
        byte[] result = exportBillingReportUseCase.exportToPdf();

        // Assert
        assertNotNull(result);
        assertArrayEquals(expectedPdfBytes, result);
        verify(generateBillingReportUseCase, times(1)).execute();
        verify(exportService, times(1)).exportBillingReportToPdf(mockReportData);
    }

    @Test
    void exportToCsv_shouldCallGenerateAndExportService() {
        // Arrange
        byte[] expectedCsvBytes = "CSV Content".getBytes();
        when(generateBillingReportUseCase.execute()).thenReturn(mockReportData);
        when(exportService.exportBillingReportToCsv(mockReportData)).thenReturn(expectedCsvBytes);

        // Act
        byte[] result = exportBillingReportUseCase.exportToCsv();

        // Assert
        assertNotNull(result);
        assertArrayEquals(expectedCsvBytes, result);
        verify(generateBillingReportUseCase, times(1)).execute();
        verify(exportService, times(1)).exportBillingReportToCsv(mockReportData);
    }

    @Test
    void exportToImage_shouldCallGenerateAndExportService() {
        // Arrange
        byte[] expectedImageBytes = "PNG Content".getBytes();
        when(generateBillingReportUseCase.execute()).thenReturn(mockReportData);
        when(exportService.exportBillingReportToImage(mockReportData)).thenReturn(expectedImageBytes);

        // Act
        byte[] result = exportBillingReportUseCase.exportToImage();

        // Assert
        assertNotNull(result);
        assertArrayEquals(expectedImageBytes, result);
        verify(generateBillingReportUseCase, times(1)).execute();
        verify(exportService, times(1)).exportBillingReportToImage(mockReportData);
    }

    @Test
    void export_withPdfFormat_shouldCallExportToPdf() {
        // Arrange
        byte[] expectedPdfBytes = "PDF Content".getBytes();
        when(generateBillingReportUseCase.execute()).thenReturn(mockReportData);
        when(exportService.exportBillingReportToPdf(mockReportData)).thenReturn(expectedPdfBytes);

        // Act
        byte[] result = exportBillingReportUseCase.export("PDF");

        // Assert
        assertNotNull(result);
        assertArrayEquals(expectedPdfBytes, result);
    }

    @Test
    void export_withCsvFormat_shouldCallExportToCsv() {
        // Arrange
        byte[] expectedCsvBytes = "CSV Content".getBytes();
        when(generateBillingReportUseCase.execute()).thenReturn(mockReportData);
        when(exportService.exportBillingReportToCsv(mockReportData)).thenReturn(expectedCsvBytes);

        // Act
        byte[] result = exportBillingReportUseCase.export("CSV");

        // Assert
        assertNotNull(result);
        assertArrayEquals(expectedCsvBytes, result);
    }

    @Test
    void export_withPngFormat_shouldCallExportToImage() {
        // Arrange
        byte[] expectedImageBytes = "PNG Content".getBytes();
        when(generateBillingReportUseCase.execute()).thenReturn(mockReportData);
        when(exportService.exportBillingReportToImage(mockReportData)).thenReturn(expectedImageBytes);

        // Act
        byte[] result = exportBillingReportUseCase.export("PNG");

        // Assert
        assertNotNull(result);
        assertArrayEquals(expectedImageBytes, result);
    }

    @Test
    void export_withInvalidFormat_shouldThrowBusinessRuleException() {
        // Act & Assert
        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> exportBillingReportUseCase.export("XML"));

        assertEquals("Formato de exportación no soportado: XML", exception.getMessage());
        verify(generateBillingReportUseCase, never()).execute();
    }
}
