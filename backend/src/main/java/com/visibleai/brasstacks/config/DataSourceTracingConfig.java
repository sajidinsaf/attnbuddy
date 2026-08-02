package com.visibleai.brasstacks.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.jdbc.datasource.JdbcTelemetry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DataSourceTracingConfig {

    @Bean
    @Primary
    public DataSource tracingDataSource(DataSourceProperties properties, OpenTelemetry openTelemetry) {
        DataSource original = properties.initializeDataSourceBuilder().build();
        return JdbcTelemetry.create(openTelemetry).wrap(original);
    }
}
