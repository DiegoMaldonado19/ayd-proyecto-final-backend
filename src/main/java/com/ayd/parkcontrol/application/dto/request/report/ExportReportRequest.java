package com.ayd.parkcontrol.application.dto.request.report;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportReportRequest {

    @JsonProperty("report_type")
    @NotNull(message = "Report type is required")
    private String reportType;

    @JsonProperty("export_format")
    @NotNull(message = "Export format is required")
    private String exportFormat;

    @JsonProperty("branch_id")
    private Long branchId;

    @JsonProperty("start_date")
    private String startDate;

    @JsonProperty("end_date")
    private String endDate;
}
