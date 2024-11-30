package com.sbvdeveloper.employeereact.repository.postgres.primary;

import com.sbvdeveloper.employeereact.domain.Employee;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PrimaryEmployeeRepository extends ReactiveCrudRepository<Employee, Long> {
    Flux<Employee> findByRole(String role);

    @Transactional
    @Modifying
    Mono<Void> deleteByRole(String role);

    @Transactional
    @Modifying
    @Query("DELETE FROM employees WHERE id = :id")
    Mono<Void> deleteBId(Long id);
}
