package com.ayd.parkcontrol.application.usecase.report.export;

import com.ayd.parkcontrol.application.dto.response.report.BillingReportResponse;
import com.ayd.parkcontrol.application.dto.response.report.OccupancyReportResponse;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Servicio para exportar reportes a diferentes formatos (PDF, CSV, PNG)
 * Implementación completa usando iText para PDF y JFreeChart para gráficos
 */
@Service
public class ReportExportService {

    // ==================== Reportes de Ocupación ====================

    public byte[] exportOccupancyReportToPdf(List<OccupancyReportResponse> data) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Título
            Paragraph title = new Paragraph("REPORTE DE OCUPACIÓN")
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(title);

            // Fecha de generación
            String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            Paragraph date = new Paragraph("Generado: " + fecha)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(date);

            // Tabla
            float[] columnWidths = { 2, 2, 1.5f, 1.5f, 1.5f, 1.5f };
            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));

            // Encabezados
            String[] headers = { "Sucursal", "Tipo Vehículo", "Capacidad", "Ocupación", "Pico", "% Ocupación" };
            for (String header : headers) {
                Cell cell = new Cell().add(new Paragraph(header).setBold())
                        .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                        .setTextAlignment(TextAlignment.CENTER);
                table.addCell(cell);
            }

            // Datos
            for (OccupancyReportResponse item : data) {
                table.addCell(new Cell().add(new Paragraph(item.getBranchName())));
                table.addCell(new Cell().add(new Paragraph(item.getVehicleType())));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(item.getTotalCapacity())))
                        .setTextAlignment(TextAlignment.CENTER));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(item.getCurrentOccupancy())))
                        .setTextAlignment(TextAlignment.CENTER));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(item.getPeakOccupancy())))
                        .setTextAlignment(TextAlignment.CENTER));
                table.addCell(new Cell().add(new Paragraph(String.format("%.2f%%", item.getOccupancyPercentage())))
                        .setTextAlignment(TextAlignment.CENTER));
            }

            document.add(table);
            document.close();

            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF de ocupación", e);
        }
    }

    public byte[] exportOccupancyReportToCsv(List<OccupancyReportResponse> data) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(baos, true, StandardCharsets.UTF_8);

        // CSV Header
        writer.println(
                "branch_id,branch_name,vehicle_type,total_capacity,current_occupancy,peak_occupancy,occupancy_percentage");

        // CSV Data
        for (OccupancyReportResponse item : data) {
            writer.printf("%d,\"%s\",\"%s\",%d,%d,%d,%.2f%n",
                    item.getBranchId(),
                    item.getBranchName(),
                    item.getVehicleType(),
                    item.getTotalCapacity(),
                    item.getCurrentOccupancy(),
                    item.getPeakOccupancy(),
                    item.getOccupancyPercentage());
        }

        writer.flush();
        return baos.toByteArray();
    }

    public byte[] exportOccupancyReportToImage(List<OccupancyReportResponse> data) {
        try {
            // Crear dataset para el gráfico
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();

            for (OccupancyReportResponse item : data) {
                String label = item.getBranchName() + " (" + item.getVehicleType() + ")";
                dataset.addValue(item.getCurrentOccupancy(), "Ocupación Actual", label);
                dataset.addValue(item.getTotalCapacity(), "Capacidad Total", label);
            }

            // Crear gráfico de barras
            JFreeChart chart = ChartFactory.createBarChart(
                    "Reporte de Ocupación",
                    "Sucursal",
                    "Cantidad",
                    dataset,
                    PlotOrientation.VERTICAL,
                    true,
                    true,
                    false);

            // Personalizar colores
            CategoryPlot plot = chart.getCategoryPlot();
            plot.setBackgroundPaint(java.awt.Color.WHITE);
            plot.setRangeGridlinePaint(java.awt.Color.GRAY);

            // Convertir a bytes (PNG)
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ChartUtils.writeChartAsPNG(baos, chart, 800, 600);
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generando imagen de ocupación", e);
        }
    }

    // ==================== Reportes de Facturación ====================

    public byte[] exportBillingReportToPdf(List<BillingReportResponse> data) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Título
            Paragraph title = new Paragraph("REPORTE DE FACTURACIÓN")
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(title);

            // Fecha de generación
            String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            Paragraph date = new Paragraph("Generado: " + fecha)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(date);

            // Tabla
            float[] columnWidths = { 3, 2, 2, 2 };
            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));

            // Encabezados
            String[] headers = { "Sucursal", "Ingreso Total (Q)", "Total Tickets", "Promedio/Ticket (Q)" };
            for (String header : headers) {
                Cell cell = new Cell().add(new Paragraph(header).setBold())
                        .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                        .setTextAlignment(TextAlignment.CENTER);
                table.addCell(cell);
            }

            // Datos
            for (BillingReportResponse item : data) {
                table.addCell(new Cell().add(new Paragraph(item.getBranchName())));
                table.addCell(new Cell().add(new Paragraph(String.format("%.2f", item.getTotalRevenue())))
                        .setTextAlignment(TextAlignment.RIGHT));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(item.getTotalTickets())))
                        .setTextAlignment(TextAlignment.CENTER));
                table.addCell(new Cell().add(new Paragraph(String.format("%.2f", item.getAverageTicketValue())))
                        .setTextAlignment(TextAlignment.RIGHT));
            }

            document.add(table);
            document.close();

            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF de facturación", e);
        }
    }

    public byte[] exportBillingReportToCsv(List<BillingReportResponse> data) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(baos, true, StandardCharsets.UTF_8);

        // CSV Header
        writer.println("branch_id,branch_name,total_revenue,total_tickets,average_ticket_value");

        // CSV Data
        for (BillingReportResponse item : data) {
            writer.printf("%d,\"%s\",%.2f,%d,%.2f%n",
                    item.getBranchId(),
                    item.getBranchName(),
                    item.getTotalRevenue(),
                    item.getTotalTickets(),
                    item.getAverageTicketValue());
        }

        writer.flush();
        return baos.toByteArray();
    }

    public byte[] exportBillingReportToImage(List<BillingReportResponse> data) {
        try {
            // Crear dataset para el gráfico
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();

            for (BillingReportResponse item : data) {
                dataset.addValue(item.getTotalRevenue().doubleValue(), "Ingresos (Q)", item.getBranchName());
            }

            // Crear gráfico de barras
            JFreeChart chart = ChartFactory.createBarChart(
                    "Reporte de Facturación",
                    "Sucursal",
                    "Ingresos (Q)",
                    dataset,
                    PlotOrientation.VERTICAL,
                    true,
                    true,
                    false);

            // Personalizar colores
            CategoryPlot plot = chart.getCategoryPlot();
            plot.setBackgroundPaint(java.awt.Color.WHITE);
            plot.setRangeGridlinePaint(java.awt.Color.GRAY);
            plot.getRenderer().setSeriesPaint(0, new java.awt.Color(34, 139, 34)); // Verde

            // Convertir a bytes (PNG)
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ChartUtils.writeChartAsPNG(baos, chart, 800, 600);
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generando imagen de facturación", e);
        }
    }
}
