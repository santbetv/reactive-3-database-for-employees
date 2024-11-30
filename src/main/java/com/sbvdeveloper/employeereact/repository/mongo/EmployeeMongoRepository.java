package com.sbvdeveloper.employeereact.repository.mongo;

import com.sbvdeveloper.employeereact.domain.mongo.EmployeeMongo;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface EmployeeMongoRepository extends ReactiveMongoRepository<EmployeeMongo, String> {

    @Query(value = "{ 'id': ?0 }", delete = true)
    Mono<Void> deleteXId(String id);
}
