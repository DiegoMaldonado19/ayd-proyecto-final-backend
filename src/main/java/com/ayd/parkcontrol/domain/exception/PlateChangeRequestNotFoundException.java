package com.ayd.parkcontrol.domain.exception;

public class PlateChangeRequestNotFoundException extends ResourceNotFoundException {
    public PlateChangeRequestNotFoundException(Long id) {
        super(String.format("Solicitud de cambio de placa no encontrada con ID: %d", id));
    }
}
