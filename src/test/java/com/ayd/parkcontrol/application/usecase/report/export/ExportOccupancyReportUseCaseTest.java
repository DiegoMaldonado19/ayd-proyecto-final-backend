package com.ayd.parkcontrol.application.usecase.report.export;

import com.ayd.parkcontrol.application.dto.response.report.OccupancyReportResponse;
import com.ayd.parkcontrol.application.usecase.report.GenerateOccupancyReportUseCase;
import com.ayd.parkcontrol.domain.exception.BusinessRuleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExportOccupancyReportUseCaseTest {

    @Mock
    private GenerateOccupancyReportUseCase generateOccupancyReportUseCase;

    @Mock
    private ReportExportService exportService;

    @InjectMocks
    private ExportOccupancyReportUseCase exportOccupancyReportUseCase;

    private List<OccupancyReportResponse> mockReportData;

    @BeforeEach
    void setUp() {
        OccupancyReportResponse response = new OccupancyReportResponse();
        response.setBranchId(1L);
        response.setBranchName("Sucursal Centro");
        response.setVehicleType("2R");
        response.setTotalCapacity(100);
        response.setCurrentOccupancy(75);
        response.setPeakOccupancy(95);

        mockReportData = Arrays.asList(response);
    }

    @Test
    void exportToPdf_shouldCallGenerateAndExportService() {
        // Arrange
        byte[] expectedPdfBytes = "PDF Content".getBytes();
        when(generateOccupancyReportUseCase.execute()).thenReturn(mockReportData);
        when(exportService.exportOccupancyReportToPdf(mockReportData)).thenReturn(expectedPdfBytes);

        // Act
        byte[] result = exportOccupancyReportUseCase.exportToPdf();

        // Assert
        assertNotNull(result);
        assertArrayEquals(expectedPdfBytes, result);
        verify(generateOccupancyReportUseCase, times(1)).execute();
        verify(exportService, times(1)).exportOccupancyReportToPdf(mockReportData);
    }

    @Test
    void exportToCsv_shouldCallGenerateAndExportService() {
        // Arrange
        byte[] expectedCsvBytes = "CSV Content".getBytes();
        when(generateOccupancyReportUseCase.execute()).thenReturn(mockReportData);
        when(exportService.exportOccupancyReportToCsv(mockReportData)).thenReturn(expectedCsvBytes);

        // Act
        byte[] result = exportOccupancyReportUseCase.exportToCsv();

        // Assert
        assertNotNull(result);
        assertArrayEquals(expectedCsvBytes, result);
        verify(generateOccupancyReportUseCase, times(1)).execute();
        verify(exportService, times(1)).exportOccupancyReportToCsv(mockReportData);
    }

    @Test
    void exportToImage_shouldCallGenerateAndExportService() {
        // Arrange
        byte[] expectedImageBytes = "PNG Content".getBytes();
        when(generateOccupancyReportUseCase.execute()).thenReturn(mockReportData);
        when(exportService.exportOccupancyReportToImage(mockReportData)).thenReturn(expectedImageBytes);

        // Act
        byte[] result = exportOccupancyReportUseCase.exportToImage();

        // Assert
        assertNotNull(result);
        assertArrayEquals(expectedImageBytes, result);
        verify(generateOccupancyReportUseCase, times(1)).execute();
        verify(exportService, times(1)).exportOccupancyReportToImage(mockReportData);
    }

    @Test
    void export_withPdfFormat_shouldCallExportToPdf() {
        // Arrange
        byte[] expectedPdfBytes = "PDF Content".getBytes();
        when(generateOccupancyReportUseCase.execute()).thenReturn(mockReportData);
        when(exportService.exportOccupancyReportToPdf(mockReportData)).thenReturn(expectedPdfBytes);

        // Act
        byte[] result = exportOccupancyReportUseCase.export("PDF");

        // Assert
        assertNotNull(result);
        assertArrayEquals(expectedPdfBytes, result);
    }

    @Test
    void export_withCsvFormat_shouldCallExportToCsv() {
        // Arrange
        byte[] expectedCsvBytes = "CSV Content".getBytes();
        when(generateOccupancyReportUseCase.execute()).thenReturn(mockReportData);
        when(exportService.exportOccupancyReportToCsv(mockReportData)).thenReturn(expectedCsvBytes);

        // Act
        byte[] result = exportOccupancyReportUseCase.export("CSV");

        // Assert
        assertNotNull(result);
        assertArrayEquals(expectedCsvBytes, result);
    }

    @Test
    void export_withPngFormat_shouldCallExportToImage() {
        // Arrange
        byte[] expectedImageBytes = "PNG Content".getBytes();
        when(generateOccupancyReportUseCase.execute()).thenReturn(mockReportData);
        when(exportService.exportOccupancyReportToImage(mockReportData)).thenReturn(expectedImageBytes);

        // Act
        byte[] result = exportOccupancyReportUseCase.export("PNG");

        // Assert
        assertNotNull(result);
        assertArrayEquals(expectedImageBytes, result);
    }

    @Test
    void export_withImageFormat_shouldCallExportToImage() {
        // Arrange
        byte[] expectedImageBytes = "IMAGE Content".getBytes();
        when(generateOccupancyReportUseCase.execute()).thenReturn(mockReportData);
        when(exportService.exportOccupancyReportToImage(mockReportData)).thenReturn(expectedImageBytes);

        // Act
        byte[] result = exportOccupancyReportUseCase.export("IMAGE");

        // Assert
        assertNotNull(result);
        assertArrayEquals(expectedImageBytes, result);
    }

    @Test
    void export_withInvalidFormat_shouldThrowBusinessRuleException() {
        // Act & Assert
        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> exportOccupancyReportUseCase.export("INVALID_FORMAT"));

        assertEquals("Formato de exportación no soportado: INVALID_FORMAT", exception.getMessage());
        verify(generateOccupancyReportUseCase, never()).execute();
    }

    @Test
    void export_withLowercaseFormat_shouldWork() {
        // Arrange
        byte[] expectedPdfBytes = "PDF Content".getBytes();
        when(generateOccupancyReportUseCase.execute()).thenReturn(mockReportData);
        when(exportService.exportOccupancyReportToPdf(mockReportData)).thenReturn(expectedPdfBytes);

        // Act
        byte[] result = exportOccupancyReportUseCase.export("pdf");

        // Assert
        assertNotNull(result);
        assertArrayEquals(expectedPdfBytes, result);
    }
}
