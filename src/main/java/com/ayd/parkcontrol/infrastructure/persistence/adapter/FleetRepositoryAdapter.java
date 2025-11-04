package com.ayd.parkcontrol.infrastructure.persistence.adapter;

import com.ayd.parkcontrol.domain.model.fleet.FleetCompany;
import com.ayd.parkcontrol.domain.repository.FleetRepository;
import com.ayd.parkcontrol.infrastructure.persistence.entity.FleetCompanyEntity;
import com.ayd.parkcontrol.infrastructure.persistence.mapper.FleetCompanyEntityMapper;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaFleetCompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FleetRepositoryAdapter implements FleetRepository {

    private final JpaFleetCompanyRepository jpaFleetCompanyRepository;
    private final FleetCompanyEntityMapper fleetCompanyEntityMapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Optional<FleetCompany> findByAdminUserId(Long userId) {
        return jpaFleetCompanyRepository.findByAdminUserId(userId)
                .map(fleetCompanyEntityMapper::toDomain);
    }

    @Override
    public Optional<BigDecimal> getTotalConsumptionByFleetId(Long fleetId) {
        String sql = """
                SELECT COALESCE(SUM(p.amount_paid), 0) as total_consumption
                FROM payments p
                INNER JOIN subscriptions s ON p.subscription_id = s.id
                INNER JOIN fleet_vehicles fv ON s.vehicle_id = fv.id
                WHERE fv.company_id = ? AND p.status = 'COMPLETED'
                """;

        BigDecimal total = jdbcTemplate.queryForObject(sql, BigDecimal.class, fleetId);
        return Optional.ofNullable(total != null ? total : BigDecimal.ZERO);
    }

    @Override
    public Optional<BigDecimal> getCurrentMonthConsumptionByFleetId(Long fleetId) {
        String sql = """
                SELECT COALESCE(SUM(p.amount_paid), 0) as monthly_consumption
                FROM payments p
                INNER JOIN subscriptions s ON p.subscription_id = s.id
                INNER JOIN fleet_vehicles fv ON s.vehicle_id = fv.id
                WHERE fv.company_id = ?
                AND p.status = 'COMPLETED'
                AND YEAR(p.payment_date) = YEAR(CURDATE())
                AND MONTH(p.payment_date) = MONTH(CURDATE())
                """;

        BigDecimal monthly = jdbcTemplate.queryForObject(sql, BigDecimal.class, fleetId);
        return Optional.ofNullable(monthly != null ? monthly : BigDecimal.ZERO);
    }

    @Override
    public Integer countActiveVehiclesByFleetId(Long fleetId) {
        String sql = """
                SELECT COUNT(*)
                FROM fleet_vehicles
                WHERE company_id = ? AND is_active = true
                """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, fleetId);
        return count != null ? count : 0;
    }

    @Override
    public Integer countTotalVehiclesByFleetId(Long fleetId) {
        String sql = """
                SELECT COUNT(*)
                FROM fleet_vehicles
                WHERE company_id = ?
                """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, fleetId);
        return count != null ? count : 0;
    }

    @Override
    public FleetCompany save(FleetCompany fleetCompany) {
        FleetCompanyEntity entity = fleetCompanyEntityMapper.toEntity(fleetCompany);
        FleetCompanyEntity savedEntity = jpaFleetCompanyRepository.save(entity);
        return fleetCompanyEntityMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<FleetCompany> findById(Long id) {
        return jpaFleetCompanyRepository.findById(id)
                .map(fleetCompanyEntityMapper::toDomain);
    }
}
