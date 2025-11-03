package com.ayd.parkcontrol.domain.service;

import com.ayd.parkcontrol.domain.exception.LostTicketTimeframeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IncidentValidatorTest {

    private IncidentValidator incidentValidator;

    @BeforeEach
    void setUp() {
        incidentValidator = new IncidentValidator();
    }

    @Test
    void validateLostTicketTimeframe_WithJustEnteredVehicle_ShouldPass() {
        // Arrange
        LocalDateTime entryTime = LocalDateTime.now().minusMinutes(30);

        // Act & Assert - should not throw exception
        incidentValidator.validateLostTicketTimeframe(entryTime);
    }

    @Test
    void validateLostTicketTimeframe_WithEntryAt23Hours_ShouldPass() {
        // Arrange
        LocalDateTime entryTime = LocalDateTime.now().minusHours(23);

        // Act & Assert - should not throw exception
        incidentValidator.validateLostTicketTimeframe(entryTime);
    }

    @Test
    void validateLostTicketTimeframe_WithEntryExactly24Hours_ShouldPass() {
        // Arrange
        LocalDateTime entryTime = LocalDateTime.now().minusHours(24);

        // Act & Assert - should not throw exception
        incidentValidator.validateLostTicketTimeframe(entryTime);
    }

    @Test
    void validateLostTicketTimeframe_WithEntryAt25Hours_ShouldThrowException() {
        // Arrange
        LocalDateTime entryTime = LocalDateTime.now().minusHours(25);

        // Act & Assert
        assertThatThrownBy(() -> incidentValidator.validateLostTicketTimeframe(entryTime))
                .isInstanceOf(LostTicketTimeframeException.class);
    }

    @Test
    void validateLostTicketTimeframe_WithEntryAt48Hours_ShouldThrowException() {
        // Arrange
        LocalDateTime entryTime = LocalDateTime.now().minusHours(48);

        // Act & Assert
        assertThatThrownBy(() -> incidentValidator.validateLostTicketTimeframe(entryTime))
                .isInstanceOf(LostTicketTimeframeException.class);
    }

    @Test
    void validateLostTicketTimeframe_WithEntry1WeekAgo_ShouldThrowException() {
        // Arrange
        LocalDateTime entryTime = LocalDateTime.now().minusWeeks(1);

        // Act & Assert
        assertThatThrownBy(() -> incidentValidator.validateLostTicketTimeframe(entryTime))
                .isInstanceOf(LostTicketTimeframeException.class);
    }

    @Test
    void validateLostTicketTimeframe_WithEntryJustNow_ShouldPass() {
        // Arrange
        LocalDateTime entryTime = LocalDateTime.now();

        // Act & Assert - should not throw exception
        incidentValidator.validateLostTicketTimeframe(entryTime);
    }

    @Test
    void validateLostTicketTimeframe_WithEntry1Minute_ShouldPass() {
        // Arrange
        LocalDateTime entryTime = LocalDateTime.now().minusMinutes(1);

        // Act & Assert - should not throw exception
        incidentValidator.validateLostTicketTimeframe(entryTime);
    }

    @Test
    void validateLostTicketTimeframe_WithEntry23Hours59Minutes_ShouldPass() {
        // Arrange
        LocalDateTime entryTime = LocalDateTime.now().minusHours(23).minusMinutes(59);

        // Act & Assert - should not throw exception
        incidentValidator.validateLostTicketTimeframe(entryTime);
    }

    @Test
    void validateLostTicketTimeframe_WithEntry25Hours_ShouldThrowException() {
        // Arrange - 25 hours ensures it's definitely over the limit
        LocalDateTime entryTime = LocalDateTime.now().minusHours(25);

        // Act & Assert
        assertThatThrownBy(() -> incidentValidator.validateLostTicketTimeframe(entryTime))
                .isInstanceOf(LostTicketTimeframeException.class);
    }

    @Test
    void isWithinValidTimeframe_WithJustEnteredVehicle_ShouldReturnTrue() {
        // Arrange
        LocalDateTime entryTime = LocalDateTime.now().minusMinutes(30);

        // Act
        boolean result = incidentValidator.isWithinValidTimeframe(entryTime);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void isWithinValidTimeframe_WithEntry23Hours_ShouldReturnTrue() {
        // Arrange
        LocalDateTime entryTime = LocalDateTime.now().minusHours(23);

        // Act
        boolean result = incidentValidator.isWithinValidTimeframe(entryTime);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void isWithinValidTimeframe_WithEntryExactly24Hours_ShouldReturnTrue() {
        // Arrange
        LocalDateTime entryTime = LocalDateTime.now().minusHours(24);

        // Act
        boolean result = incidentValidator.isWithinValidTimeframe(entryTime);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void isWithinValidTimeframe_WithEntry25Hours_ShouldReturnFalse() {
        // Arrange
        LocalDateTime entryTime = LocalDateTime.now().minusHours(25);

        // Act
        boolean result = incidentValidator.isWithinValidTimeframe(entryTime);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void isWithinValidTimeframe_WithEntry48Hours_ShouldReturnFalse() {
        // Arrange
        LocalDateTime entryTime = LocalDateTime.now().minusHours(48);

        // Act
        boolean result = incidentValidator.isWithinValidTimeframe(entryTime);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void isWithinValidTimeframe_WithEntry1WeekAgo_ShouldReturnFalse() {
        // Arrange
        LocalDateTime entryTime = LocalDateTime.now().minusWeeks(1);

        // Act
        boolean result = incidentValidator.isWithinValidTimeframe(entryTime);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void isWithinValidTimeframe_WithEntryJustNow_ShouldReturnTrue() {
        // Arrange
        LocalDateTime entryTime = LocalDateTime.now();

        // Act
        boolean result = incidentValidator.isWithinValidTimeframe(entryTime);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void isWithinValidTimeframe_WithEntry23Hours59Minutes_ShouldReturnTrue() {
        // Arrange
        LocalDateTime entryTime = LocalDateTime.now().minusHours(23).minusMinutes(59);

        // Act
        boolean result = incidentValidator.isWithinValidTimeframe(entryTime);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void isWithinValidTimeframe_WithBoundaryCase_24HoursExactly_ShouldReturnTrue() {
        // Arrange - Test exact boundary at 24 hours
        LocalDateTime entryTime = LocalDateTime.now().minusHours(24).minusSeconds(0);

        // Act
        boolean result = incidentValidator.isWithinValidTimeframe(entryTime);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void validateLostTicketTimeframe_WithVariousValidTimeframes_ShouldAllPass() {
        // Test multiple valid timeframes
        LocalDateTime[] validEntryTimes = {
            LocalDateTime.now(),
            LocalDateTime.now().minusHours(1),
            LocalDateTime.now().minusHours(12),
            LocalDateTime.now().minusHours(18),
            LocalDateTime.now().minusHours(24)
        };

        for (LocalDateTime entryTime : validEntryTimes) {
            // Should not throw exception for any of these
            incidentValidator.validateLostTicketTimeframe(entryTime);
        }
    }

    @Test
    void validateLostTicketTimeframe_WithVariousInvalidTimeframes_ShouldAllFail() {
        // Test multiple invalid timeframes
        LocalDateTime[] invalidEntryTimes = {
            LocalDateTime.now().minusHours(25),
            LocalDateTime.now().minusHours(30),
            LocalDateTime.now().minusDays(2),
            LocalDateTime.now().minusWeeks(1),
            LocalDateTime.now().minusMonths(1)
        };

        for (LocalDateTime entryTime : invalidEntryTimes) {
            assertThatThrownBy(() -> incidentValidator.validateLostTicketTimeframe(entryTime))
                    .isInstanceOf(LostTicketTimeframeException.class);
        }
    }

    @Test
    void isWithinValidTimeframe_ComparedToValidateMethod_ShouldBeConsistent() {
        // Test that both methods are consistent with each other
        LocalDateTime validEntry = LocalDateTime.now().minusHours(20);
        LocalDateTime invalidEntry = LocalDateTime.now().minusHours(26);

        // Valid entry
        assertThat(incidentValidator.isWithinValidTimeframe(validEntry)).isTrue();
        // Should not throw
        incidentValidator.validateLostTicketTimeframe(validEntry);

        // Invalid entry
        assertThat(incidentValidator.isWithinValidTimeframe(invalidEntry)).isFalse();
        // Should throw
        assertThatThrownBy(() -> incidentValidator.validateLostTicketTimeframe(invalidEntry))
                .isInstanceOf(LostTicketTimeframeException.class);
    }
}
