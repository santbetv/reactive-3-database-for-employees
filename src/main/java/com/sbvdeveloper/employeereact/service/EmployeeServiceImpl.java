package com.sbvdeveloper.employeereact.service;

import com.sbvdeveloper.employeereact.domain.Employee;
import com.sbvdeveloper.employeereact.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    //La funcion de flatMap es transformar el objeto que se recibe en el parametro en otro objeto
    // y devolverlo como un Mono

    public EmployeeServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<Employee> getAllEmployees() {
        return this.employeeRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<Employee> getEmployeeById(Long id) {
        return this.employeeRepository.findById(id);
    }

    @Override
    @Transactional
    public Mono<Employee> saveEmployee(Employee employee) {
        employee.setIsNew(true);
        return this.getEmployeeById(employee.getId())
                .switchIfEmpty(Mono.just(employee).flatMap(p -> this.employeeRepository.save(p)))
                .then(Mono.just(employee));

    }

    @Override
    @Transactional
    public Mono<Employee> updateEmployee(Long id, Employee employee) {
        return this.getEmployeeById(id)
                .flatMap(p -> {
                    p.setName(employee.getName());
                    p.setRole(employee.getRole());
                    return this.employeeRepository.save(p).thenReturn(p);
                })
                .switchIfEmpty(Mono.error(new Exception("El empleado NO existe")));
    }

    @Override
    @Transactional
    public Mono<Employee> deleteEmployee(Long id) {
        return this.getEmployeeById(id)
                .flatMap(employee -> this.employeeRepository
                        .deleteById(employee.getId())
                        .thenReturn(employee))
                .switchIfEmpty(Mono.error(new Exception("El empleado NO existe")));
    }
}
