package com.sbvdeveloper.employeereact.config;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;

@Configuration
public class R2dbcConfig {

    @Qualifier("primaryConnectionFactory") // Asegúrate de usar el 'primaryConnectionFactory' adecuado
    private final ConnectionFactory primaryConnectionFactory;

    @Qualifier("secondaryConnectionFactory") // Asegúrate de usar el 'secondaryConnectionFactory' adecuado
    private final ConnectionFactory secondaryConnectionFactory;

    public R2dbcConfig(ConnectionFactory primaryConnectionFactory, ConnectionFactory secondaryConnectionFactory) {
        this.primaryConnectionFactory = primaryConnectionFactory;
        this.secondaryConnectionFactory = secondaryConnectionFactory;
    }

    // Para la base de datos primaria
    @Bean("primaryR2dbcEntityTemplate")
    public R2dbcEntityTemplate primaryR2dbcEntityTemplate() {
        return new R2dbcEntityTemplate(primaryConnectionFactory);
    }

    // Para la base de datos secundaria
    @Bean("secondaryR2dbcEntityTemplate")
    public R2dbcEntityTemplate secondaryR2dbcEntityTemplate() {
        return new R2dbcEntityTemplate(secondaryConnectionFactory);
    }
}