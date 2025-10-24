package com.ayd.parkcontrol.application.usecase.branch;

import com.ayd.parkcontrol.application.dto.response.branch.OccupancyResponse;
import com.ayd.parkcontrol.domain.exception.BranchNotFoundException;
import com.ayd.parkcontrol.domain.model.branch.Branch;
import com.ayd.parkcontrol.domain.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetOccupancyUseCase {

    private static final String OCCUPANCY_KEY_PREFIX = "branch:occupancy:";
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final BranchRepository branchRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Transactional(readOnly = true)
    public OccupancyResponse execute(Long branchId) {
        log.debug("Fetching occupancy for branch with ID: {}", branchId);

        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new BranchNotFoundException("Branch with ID " + branchId + " not found"));

        Integer occupied2r = getOccupancyFromRedis(branchId, "2R");
        Integer occupied4r = getOccupancyFromRedis(branchId, "4R");

        return buildOccupancyResponse(branch, occupied2r, occupied4r);
    }

    private Integer getOccupancyFromRedis(Long branchId, String vehicleType) {
        String key = OCCUPANCY_KEY_PREFIX + branchId + ":" + vehicleType;
        Object value = redisTemplate.opsForValue().get(key);

        if (value != null) {
            return Integer.parseInt(value.toString());
        }

        return 0;
    }

    private OccupancyResponse buildOccupancyResponse(Branch branch, Integer occupied2r, Integer occupied4r) {
        int available2r = branch.getCapacity2r() - occupied2r;
        int available4r = branch.getCapacity4r() - occupied4r;

        double occupancyPercentage2r = (occupied2r * 100.0) / branch.getCapacity2r();
        double occupancyPercentage4r = (occupied4r * 100.0) / branch.getCapacity4r();

        int totalCapacity = branch.getCapacity2r() + branch.getCapacity4r();
        int totalOccupied = occupied2r + occupied4r;
        int totalAvailable = available2r + available4r;
        double totalOccupancyPercentage = (totalOccupied * 100.0) / totalCapacity;

        return OccupancyResponse.builder()
                .branchId(branch.getId())
                .branchName(branch.getName())
                .capacity2r(branch.getCapacity2r())
                .occupied2r(occupied2r)
                .available2r(available2r)
                .occupancyPercentage2r(Math.round(occupancyPercentage2r * 100.0) / 100.0)
                .capacity4r(branch.getCapacity4r())
                .occupied4r(occupied4r)
                .available4r(available4r)
                .occupancyPercentage4r(Math.round(occupancyPercentage4r * 100.0) / 100.0)
                .totalCapacity(totalCapacity)
                .totalOccupied(totalOccupied)
                .totalAvailable(totalAvailable)
                .totalOccupancyPercentage(Math.round(totalOccupancyPercentage * 100.0) / 100.0)
                .timestamp(LocalDateTime.now().format(DATETIME_FORMATTER))
                .build();
    }
}
