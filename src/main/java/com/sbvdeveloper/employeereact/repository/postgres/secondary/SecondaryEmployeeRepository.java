package com.sbvdeveloper.employeereact.repository.postgres.secondary;

import com.sbvdeveloper.employeereact.domain.Employee;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;


public interface SecondaryEmployeeRepository extends ReactiveCrudRepository<Employee, Long> {

}
