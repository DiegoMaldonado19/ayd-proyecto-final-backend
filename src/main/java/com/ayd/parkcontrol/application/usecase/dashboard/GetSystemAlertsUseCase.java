package com.ayd.parkcontrol.application.usecase.dashboard;

import com.ayd.parkcontrol.application.dto.response.dashboard.SystemAlertResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetSystemAlertsUseCase {

    @Transactional(readOnly = true)
    public List<SystemAlertResponse> execute() {
        return new ArrayList<>();
    }
}
