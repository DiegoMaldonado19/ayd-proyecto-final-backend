package com.ayd.parkcontrol.application.usecase.report;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GenerateCashClosingReportUseCase {

    @Transactional(readOnly = true)
    public List<Object> execute() {
        return new ArrayList<>();
    }
}
