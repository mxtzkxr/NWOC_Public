package ru.nwoc.T_Invest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.ttech.piapi.core.connector.ConnectorConfiguration;
import ru.ttech.piapi.core.connector.ServiceStubFactory;
import ru.ttech.piapi.springboot.configuration.ConnectorProperties;
import ru.ttech.piapi.springboot.configuration.InvestAutoConfiguration;

@Configuration
public class TInvestConfig {
    public ConnectorProperties connectorProperties() {
        return new ConnectorProperties();
    }

    @Bean
    public ConnectorConfiguration connectorConfiguration(ConnectorProperties connectorProperties) {
        return new InvestAutoConfiguration(connectorProperties).connectorConfiguration();
    }

    @Bean
    public ServiceStubFactory serviceStubFactory(ConnectorConfiguration configuration) {
        return ServiceStubFactory.create(configuration);
    }
}