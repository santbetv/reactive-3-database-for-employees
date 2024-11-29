package com.sbvdeveloper.employeereact.service;

import com.sbvdeveloper.employeereact.domain.Employee;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EmployeeService {
    Flux<Employee> getAllEmployees();
    Mono<Employee> getEmployeeById(Long id);
    Mono<Employee> saveEmployee(Employee employee);
    Mono<Employee> updateEmployee(Long id, Employee employee);
    Mono<Employee> deleteEmployee(Long id);
}
