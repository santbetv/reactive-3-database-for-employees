package com.sbvdeveloper.employeereact.service.mongo;

import com.sbvdeveloper.employeereact.domain.mongo.EmployeeMongo;
import com.sbvdeveloper.employeereact.repository.mongo.EmployeeMongoRepository;
import com.sbvdeveloper.employeereact.repository.postgres.primary.PrimaryEmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class EmployeeSyncService {

    private final PrimaryEmployeeRepository employeeRepository;
    private final EmployeeMongoRepository employeeMongoRepository;

    public EmployeeSyncService(PrimaryEmployeeRepository employeeRepository, EmployeeMongoRepository employeeMongoRepository) {
        this.employeeRepository = employeeRepository;
        this.employeeMongoRepository = employeeMongoRepository;


    }

    @Transactional(readOnly = true)
    public Flux<EmployeeMongo> getAllEmployees() {
        return this.employeeMongoRepository.findAll();
    }


    @Transactional(readOnly = true)
    public Mono<EmployeeMongo> getEmployeeById(String id) {
        return this.employeeMongoRepository.findById(id);
    }

    @Transactional
    public Mono<EmployeeMongo> saveEmployee(EmployeeMongo employee) {
        return this.getEmployeeById(employee.getId())
                .switchIfEmpty(Mono.just(employee).flatMap(p -> this.employeeMongoRepository.save(p)))
                .then(Mono.just(employee));

    }


    @Transactional
    public Mono<EmployeeMongo> updateEmployee(String id, EmployeeMongo employee) {
        return this.getEmployeeById(id)
                .flatMap(p -> {
                    p.setName(employee.getName());
                    p.setRole(employee.getRole());
                    return this.employeeMongoRepository.save(p).thenReturn(p);
                })
                .switchIfEmpty(Mono.error(new Exception("El empleado NO existe")));
    }


    @Transactional
    public Mono<EmployeeMongo> deleteEmployee(String id) {
        return this.getEmployeeById(id)
                .flatMap(employee -> this.employeeMongoRepository.deleteXId(employee.getId())
                        .thenReturn(employee))
                .switchIfEmpty(Mono.error(new Exception("El empleado NO existe")));
    }


    //Responder de manera reactiva si se actualizo o no el empleado en la base de datos de mongo se mantine actualizada
    @Transactional
    public Flux<EmployeeMongo> syncEmployeesToMongo() {
        return employeeMongoRepository.findAll()
                .flatMap(employee -> {
                    EmployeeMongo employeeMongo = new EmployeeMongo();
                    employeeMongo.setName(employee.getName());
                    employeeMongo.setRole(employee.getRole());
                    return employeeMongoRepository.save(employeeMongo);
                });
    }
}
