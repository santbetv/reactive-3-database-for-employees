package com.sbvdeveloper.employeereact.config;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.r2dbc.ConnectionFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;

@Configuration
public class SecondaryPostgresConfig extends AbstractR2dbcConfiguration {

    @Value("${spring.r2dbc.secondary.url}")
    private String r2dbcUrl;

    @Value("${spring.r2dbc.secondary.username}")
    private String username;

    @Value("${spring.r2dbc.secondary.password}")
    private String password;

    @Override
    @Bean("secondaryConnectionFactory")
    public ConnectionFactory connectionFactory() {
        return ConnectionFactoryBuilder
                .withUrl(r2dbcUrl)
                .username(username)
                .password(password)
                .build();
    }
}
