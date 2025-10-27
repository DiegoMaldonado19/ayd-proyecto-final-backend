package com.ayd.parkcontrol.application.dto.response.fleet;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FleetConsumptionResponse {

    @JsonProperty("company_id")
    private Long companyId;

    @JsonProperty("company_name")
    private String companyName;

    @JsonProperty("period_start")
    private LocalDateTime periodStart;

    @JsonProperty("period_end")
    private LocalDateTime periodEnd;

    @JsonProperty("total_vehicles")
    private Integer totalVehicles;

    @JsonProperty("total_entries")
    private Long totalEntries;

    @JsonProperty("total_hours_consumed")
    private BigDecimal totalHoursConsumed;

    @JsonProperty("total_amount_charged")
    private BigDecimal totalAmountCharged;

    @JsonProperty("vehicle_consumption")
    private List<VehicleConsumptionDetail> vehicleConsumption;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VehicleConsumptionDetail {

        @JsonProperty("license_plate")
        private String licensePlate;

        @JsonProperty("assigned_employee")
        private String assignedEmployee;

        @JsonProperty("entries_count")
        private Long entriesCount;

        @JsonProperty("hours_consumed")
        private BigDecimal hoursConsumed;

        @JsonProperty("amount_charged")
        private BigDecimal amountCharged;
    }
}
