package ru.nwoc.T_Invest.service;

import lombok.extern.slf4j.Slf4j;
import ru.ttech.piapi.core.connector.ServiceStubFactory;

@Slf4j
public abstract class BaseTInvestService {
    protected final ServiceStubFactory stubFactory;

    protected BaseTInvestService(ServiceStubFactory stubFactory) {
        this.stubFactory = stubFactory;
    }

    protected void handleError(Exception e) {
        log.error("Tinkoff API error: {}", e.getMessage(), e);
    }
}