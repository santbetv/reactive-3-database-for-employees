package com.sbvdeveloper.employeereact.service;

import com.sbvdeveloper.employeereact.config.ConfigDataEmployee;
import com.sbvdeveloper.employeereact.domain.Employee;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final ConfigDataEmployee configDataEmployee;

    public EmployeeServiceImpl(ConfigDataEmployee configDataEmployee) {
        this.configDataEmployee = configDataEmployee;
    }

    @Override
    public Flux<Employee> getAllEmployees() {
        return Flux.fromIterable(configDataEmployee.crearBDMemoria());
    }
    
    @Override
    public Mono<Employee> getEmployeeById(Long id) {
        return this.getAllEmployees()
                .filter(employee -> employee.getId().equals(id))
                .next();
    }

    @Override
    public Mono<Employee> saveEmployee(Employee employee) {
        return this.getEmployeeById(employee.getId())
                .switchIfEmpty(Mono.just(employee).map(p -> {
                    configDataEmployee.crearBDMemoria().add(employee);
                    return p;
                }))
                .then(Mono.just(employee));

    }

    @Override
    public Mono<Employee> updateEmployee(Long id, Employee employee) {
        return this.getEmployeeById(id)
                .map(p -> {
                    p.setName(employee.getName());
                    p.setRole(employee.getRole());
                    return p;
                }).switchIfEmpty(Mono.error(new Exception("El empleado NO existe")));
    }

    @Override
    public Mono<Employee> deleteEmployee(Long id) {
        return this.getEmployeeById(id)
                .map(employee -> {
                    configDataEmployee.crearBDMemoria().removeIf(e -> e.getId().equals(id));
                    return employee;
                })
                .switchIfEmpty(Mono.error(new Exception("El empleado NO existe")));
    }
}
