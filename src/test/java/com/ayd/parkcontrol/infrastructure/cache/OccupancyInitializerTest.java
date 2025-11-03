package com.ayd.parkcontrol.infrastructure.cache;

import com.ayd.parkcontrol.infrastructure.persistence.entity.BranchEntity;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaBranchRepository;
import com.ayd.parkcontrol.infrastructure.persistence.repository.JpaTicketRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Tests unitarios para OccupancyInitializer.
 */
@ExtendWith(MockitoExtension.class)
class OccupancyInitializerTest {

    @Mock
    private RedisOccupancyService redisOccupancyService;

    @Mock
    private JpaBranchRepository branchRepository;

    @Mock
    private JpaTicketRepository ticketRepository;

    @InjectMocks
    private OccupancyInitializer occupancyInitializer;

    @Test
    void initializeOccupancy_withMultipleBranches_shouldSyncAllBranches() {
        // Arrange
        BranchEntity branch1 = createBranchEntity(1L, "Sucursal Centro");
        BranchEntity branch2 = createBranchEntity(2L, "Sucursal Norte");
        BranchEntity branch3 = createBranchEntity(3L, "Sucursal Sur");

        List<BranchEntity> branches = Arrays.asList(branch1, branch2, branch3);

        when(branchRepository.findAll()).thenReturn(branches);
        when(ticketRepository.countActiveTwoWheelerTickets(1L)).thenReturn(5L);
        when(ticketRepository.countActiveFourWheelerTickets(1L)).thenReturn(10L);
        when(ticketRepository.countActiveTwoWheelerTickets(2L)).thenReturn(3L);
        when(ticketRepository.countActiveFourWheelerTickets(2L)).thenReturn(7L);
        when(ticketRepository.countActiveTwoWheelerTickets(3L)).thenReturn(8L);
        when(ticketRepository.countActiveFourWheelerTickets(3L)).thenReturn(12L);

        // Act
        occupancyInitializer.initializeOccupancy();

        // Assert
        verify(branchRepository).findAll();
        verify(ticketRepository).countActiveTwoWheelerTickets(1L);
        verify(ticketRepository).countActiveFourWheelerTickets(1L);
        verify(ticketRepository).countActiveTwoWheelerTickets(2L);
        verify(ticketRepository).countActiveFourWheelerTickets(2L);
        verify(ticketRepository).countActiveTwoWheelerTickets(3L);
        verify(ticketRepository).countActiveFourWheelerTickets(3L);

        verify(redisOccupancyService).setOccupancy(1L, "2R", 5);
        verify(redisOccupancyService).setOccupancy(1L, "4R", 10);
        verify(redisOccupancyService).setOccupancy(2L, "2R", 3);
        verify(redisOccupancyService).setOccupancy(2L, "4R", 7);
        verify(redisOccupancyService).setOccupancy(3L, "2R", 8);
        verify(redisOccupancyService).setOccupancy(3L, "4R", 12);
    }

    @Test
    void initializeOccupancy_withNoBranches_shouldNotCallRedisService() {
        // Arrange
        when(branchRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        occupancyInitializer.initializeOccupancy();

        // Assert
        verify(branchRepository).findAll();
        verify(ticketRepository, never()).countActiveTwoWheelerTickets(anyLong());
        verify(ticketRepository, never()).countActiveFourWheelerTickets(anyLong());
        verify(redisOccupancyService, never()).setOccupancy(anyLong(), anyString(), anyInt());
    }

    @Test
    void initializeOccupancy_withZeroOccupancy_shouldSetZeroInRedis() {
        // Arrange
        BranchEntity branch = createBranchEntity(1L, "Sucursal Vacía");

        when(branchRepository.findAll()).thenReturn(Collections.singletonList(branch));
        when(ticketRepository.countActiveTwoWheelerTickets(1L)).thenReturn(0L);
        when(ticketRepository.countActiveFourWheelerTickets(1L)).thenReturn(0L);

        // Act
        occupancyInitializer.initializeOccupancy();

        // Assert
        verify(redisOccupancyService).setOccupancy(1L, "2R", 0);
        verify(redisOccupancyService).setOccupancy(1L, "4R", 0);
    }

    @Test
    void initializeOccupancy_withSingleBranch_shouldSyncCorrectly() {
        // Arrange
        BranchEntity branch = createBranchEntity(1L, "Única Sucursal");

        when(branchRepository.findAll()).thenReturn(Collections.singletonList(branch));
        when(ticketRepository.countActiveTwoWheelerTickets(1L)).thenReturn(15L);
        when(ticketRepository.countActiveFourWheelerTickets(1L)).thenReturn(20L);

        // Act
        occupancyInitializer.initializeOccupancy();

        // Assert
        verify(branchRepository).findAll();
        verify(ticketRepository).countActiveTwoWheelerTickets(1L);
        verify(ticketRepository).countActiveFourWheelerTickets(1L);
        verify(redisOccupancyService).setOccupancy(1L, "2R", 15);
        verify(redisOccupancyService).setOccupancy(1L, "4R", 20);
    }

    @Test
    void initializeOccupancy_withRepositoryException_shouldNotThrow() {
        // Arrange
        when(branchRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // Act & Assert - should not throw exception
        occupancyInitializer.initializeOccupancy();

        verify(branchRepository).findAll();
        verify(redisOccupancyService, never()).setOccupancy(anyLong(), anyString(), anyInt());
    }

    @Test
    void initializeOccupancy_withTicketRepositoryException_shouldContinueWithOtherBranches() {
        // Arrange
        BranchEntity branch1 = createBranchEntity(1L, "Sucursal 1");
        BranchEntity branch2 = createBranchEntity(2L, "Sucursal 2");

        when(branchRepository.findAll()).thenReturn(Arrays.asList(branch1, branch2));
        when(ticketRepository.countActiveTwoWheelerTickets(1L))
                .thenThrow(new RuntimeException("Ticket error"));
        when(ticketRepository.countActiveTwoWheelerTickets(2L)).thenReturn(3L);
        when(ticketRepository.countActiveFourWheelerTickets(2L)).thenReturn(7L);

        // Act
        occupancyInitializer.initializeOccupancy();

        // Assert - should continue with branch 2 despite error in branch 1
        verify(ticketRepository).countActiveTwoWheelerTickets(1L);
        verify(ticketRepository).countActiveTwoWheelerTickets(2L);
        verify(ticketRepository).countActiveFourWheelerTickets(2L);
        verify(redisOccupancyService).setOccupancy(2L, "2R", 3);
        verify(redisOccupancyService).setOccupancy(2L, "4R", 7);
    }

    @Test
    void initializeOccupancy_withRedisException_shouldNotThrow() {
        // Arrange
        BranchEntity branch = createBranchEntity(1L, "Sucursal Test");

        when(branchRepository.findAll()).thenReturn(Collections.singletonList(branch));
        when(ticketRepository.countActiveTwoWheelerTickets(1L)).thenReturn(5L);
        when(ticketRepository.countActiveFourWheelerTickets(1L)).thenReturn(10L);
        doThrow(new RuntimeException("Redis error"))
                .when(redisOccupancyService).setOccupancy(anyLong(), anyString(), anyInt());

        // Act & Assert - should not throw exception
        occupancyInitializer.initializeOccupancy();

        verify(redisOccupancyService).setOccupancy(1L, "2R", 5);
    }

    @Test
    void initializeOccupancy_withHighOccupancy_shouldHandleCorrectly() {
        // Arrange
        BranchEntity branch = createBranchEntity(1L, "Sucursal Llena");

        when(branchRepository.findAll()).thenReturn(Collections.singletonList(branch));
        when(ticketRepository.countActiveTwoWheelerTickets(1L)).thenReturn(100L);
        when(ticketRepository.countActiveFourWheelerTickets(1L)).thenReturn(150L);

        // Act
        occupancyInitializer.initializeOccupancy();

        // Assert
        verify(redisOccupancyService).setOccupancy(1L, "2R", 100);
        verify(redisOccupancyService).setOccupancy(1L, "4R", 150);
    }

    @Test
    void initializeOccupancy_withOnlyTwoWheelerOccupancy_shouldSyncBothTypes() {
        // Arrange
        BranchEntity branch = createBranchEntity(1L, "Sucursal Motos");

        when(branchRepository.findAll()).thenReturn(Collections.singletonList(branch));
        when(ticketRepository.countActiveTwoWheelerTickets(1L)).thenReturn(25L);
        when(ticketRepository.countActiveFourWheelerTickets(1L)).thenReturn(0L);

        // Act
        occupancyInitializer.initializeOccupancy();

        // Assert
        verify(redisOccupancyService).setOccupancy(1L, "2R", 25);
        verify(redisOccupancyService).setOccupancy(1L, "4R", 0);
    }

    @Test
    void initializeOccupancy_withOnlyFourWheelerOccupancy_shouldSyncBothTypes() {
        // Arrange
        BranchEntity branch = createBranchEntity(1L, "Sucursal Autos");

        when(branchRepository.findAll()).thenReturn(Collections.singletonList(branch));
        when(ticketRepository.countActiveTwoWheelerTickets(1L)).thenReturn(0L);
        when(ticketRepository.countActiveFourWheelerTickets(1L)).thenReturn(30L);

        // Act
        occupancyInitializer.initializeOccupancy();

        // Assert
        verify(redisOccupancyService).setOccupancy(1L, "2R", 0);
        verify(redisOccupancyService).setOccupancy(1L, "4R", 30);
    }

    private BranchEntity createBranchEntity(Long id, String name) {
        return BranchEntity.builder()
                .id(id)
                .name(name)
                .address("Dirección Test")
                .capacity2r(50)
                .capacity4r(100)
                .isActive(true)
                .build();
    }
}
