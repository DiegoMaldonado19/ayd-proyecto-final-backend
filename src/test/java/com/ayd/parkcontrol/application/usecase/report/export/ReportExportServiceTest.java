package com.ayd.parkcontrol.application.usecase.report.export;

import com.ayd.parkcontrol.application.dto.response.report.BillingReportResponse;
import com.ayd.parkcontrol.application.dto.response.report.OccupancyReportResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReportExportServiceTest {

    private ReportExportService reportExportService;

    @BeforeEach
    void setUp() {
        reportExportService = new ReportExportService();
    }

    @Test
    void exportOccupancyReportToPdf_shouldReturnNonEmptyByteArray() {
        // Arrange
        OccupancyReportResponse response = new OccupancyReportResponse();
        response.setBranchId(1L);
        response.setBranchName("Sucursal Centro");
        response.setVehicleType("2R");
        response.setTotalCapacity(100);
        response.setCurrentOccupancy(75);
        response.setPeakOccupancy(95);
        response.setOccupancyPercentage(75.0);

        List<OccupancyReportResponse> data = Arrays.asList(response);

        // Act
        byte[] result = reportExportService.exportOccupancyReportToPdf(data);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
        // Verificar que comienza con el header PDF
        assertTrue(result.length > 4);
        assertEquals('%', result[0]);
        assertEquals('P', result[1]);
        assertEquals('D', result[2]);
        assertEquals('F', result[3]);
    }

    @Test
    void exportOccupancyReportToCsv_shouldReturnValidCsv() {
        // Arrange
        OccupancyReportResponse response = new OccupancyReportResponse();
        response.setBranchId(1L);
        response.setBranchName("Sucursal Centro");
        response.setVehicleType("2R");
        response.setTotalCapacity(100);
        response.setCurrentOccupancy(75);
        response.setPeakOccupancy(95);
        response.setOccupancyPercentage(75.0);

        List<OccupancyReportResponse> data = Arrays.asList(response);

        // Act
        byte[] result = reportExportService.exportOccupancyReportToCsv(data);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
        String content = new String(result);
        assertTrue(content.contains("branch_id,branch_name,vehicle_type"));
        assertTrue(content.contains("1,\"Sucursal Centro\",\"2R\""));
    }

    @Test
    void exportOccupancyReportToImage_shouldReturnNonEmptyByteArray() {
        // Arrange
        OccupancyReportResponse response = new OccupancyReportResponse();
        response.setBranchId(1L);
        response.setBranchName("Sucursal Centro");
        response.setVehicleType("2R");
        response.setTotalCapacity(100);
        response.setCurrentOccupancy(75);
        response.setPeakOccupancy(95);
        response.setOccupancyPercentage(75.0);

        List<OccupancyReportResponse> data = Arrays.asList(response);

        // Act
        byte[] result = reportExportService.exportOccupancyReportToImage(data);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
        // Verificar que comienza con el header PNG
        assertTrue(result.length > 8);
        assertEquals((byte) 0x89, result[0]);
        assertEquals('P', result[1]);
        assertEquals('N', result[2]);
        assertEquals('G', result[3]);
    }

    @Test
    void exportBillingReportToPdf_shouldReturnNonEmptyByteArray() {
        // Arrange
        BillingReportResponse response = new BillingReportResponse();
        response.setBranchId(1L);
        response.setBranchName("Sucursal Centro");
        response.setTotalRevenue(new BigDecimal("15000.50"));
        response.setTotalTickets(150L);
        response.setAverageTicketValue(new BigDecimal("100.00"));

        List<BillingReportResponse> data = Arrays.asList(response);

        // Act
        byte[] result = reportExportService.exportBillingReportToPdf(data);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
        // Verificar que comienza con el header PDF
        assertTrue(result.length > 4);
        assertEquals('%', result[0]);
        assertEquals('P', result[1]);
        assertEquals('D', result[2]);
        assertEquals('F', result[3]);
    }

    @Test
    void exportBillingReportToCsv_shouldReturnValidCsv() {
        // Arrange
        BillingReportResponse response = new BillingReportResponse();
        response.setBranchId(1L);
        response.setBranchName("Sucursal Centro");
        response.setTotalRevenue(new BigDecimal("15000.50"));
        response.setTotalTickets(150L);
        response.setAverageTicketValue(new BigDecimal("100.00"));

        List<BillingReportResponse> data = Arrays.asList(response);

        // Act
        byte[] result = reportExportService.exportBillingReportToCsv(data);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
        String content = new String(result);
        assertTrue(content.contains("branch_id,branch_name,total_revenue"));
        assertTrue(content.contains("1,\"Sucursal Centro\""));
        assertTrue(content.contains("15000.50"));
    }

    @Test
    void exportBillingReportToImage_shouldReturnNonEmptyByteArray() {
        // Arrange
        BillingReportResponse response = new BillingReportResponse();
        response.setBranchId(1L);
        response.setBranchName("Sucursal Centro");
        response.setTotalRevenue(new BigDecimal("15000.50"));
        response.setTotalTickets(150L);
        response.setAverageTicketValue(new BigDecimal("100.00"));

        List<BillingReportResponse> data = Arrays.asList(response);

        // Act
        byte[] result = reportExportService.exportBillingReportToImage(data);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
        // Verificar que comienza con el header PNG
        assertTrue(result.length > 8);
        assertEquals((byte) 0x89, result[0]);
        assertEquals('P', result[1]);
        assertEquals('N', result[2]);
        assertEquals('G', result[3]);
    }

    @Test
    void exportOccupancyReportToCsv_withMultipleRecords_shouldIncludeAllRecords() {
        // Arrange
        OccupancyReportResponse response1 = new OccupancyReportResponse();
        response1.setBranchId(1L);
        response1.setBranchName("Sucursal 1");
        response1.setVehicleType("2R");
        response1.setTotalCapacity(100);
        response1.setCurrentOccupancy(75);
        response1.setPeakOccupancy(95);
        response1.setOccupancyPercentage(75.0);

        OccupancyReportResponse response2 = new OccupancyReportResponse();
        response2.setBranchId(2L);
        response2.setBranchName("Sucursal 2");
        response2.setVehicleType("4R");
        response2.setTotalCapacity(50);
        response2.setCurrentOccupancy(30);
        response2.setPeakOccupancy(45);
        response2.setOccupancyPercentage(60.0);

        List<OccupancyReportResponse> data = Arrays.asList(response1, response2);

        // Act
        byte[] result = reportExportService.exportOccupancyReportToCsv(data);

        // Assert
        assertNotNull(result);
        String content = new String(result);
        assertTrue(content.contains("Sucursal 1"));
        assertTrue(content.contains("Sucursal 2"));
        assertTrue(content.contains("2R"));
        assertTrue(content.contains("4R"));
    }
}
