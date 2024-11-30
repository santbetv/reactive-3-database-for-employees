package com.sbvdeveloper.employeereact.config;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;

public class MongoConfig extends AbstractReactiveMongoConfiguration {

    // Valores que serán inyectados desde application.properties o application.yml
    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Value("${spring.data.mongodb.database}")
    private String databaseName;

    @Override
    @Bean
    public MongoClient reactiveMongoClient() {
        // Crear cliente utilizando la URI configurada
        return MongoClients.create(mongoUri);
    }

    @Override
    protected String getDatabaseName() {
        // Retornar el nombre de la base de datos configurada
        return databaseName;
    }

}
