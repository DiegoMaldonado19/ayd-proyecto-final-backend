package com.ayd.parkcontrol.application.usecase.dashboard;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetActiveSubscriptionsCountUseCase {

    @Transactional(readOnly = true)
    public Long execute() {
        return 0L;
    }
}
