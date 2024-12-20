package com.sbvdeveloper.employeereact.service;

import com.sbvdeveloper.employeereact.config.ConfigDataEmployee;
import com.sbvdeveloper.employeereact.domain.Employee;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

//Probar contra la base de datos real
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)//Solo se implementa por que se requiere order de ejecutar el test
@SpringBootTest
class EmployeeServiceImplTest {

    @Autowired
    private EmployeeService employeeService;


    @Autowired
    private ConfigDataEmployee configDataEmployee;


    @Test
    @Order(1)
    void getAllEmployees() {

        StepVerifier.create(employeeService.getAllEmployees().take(2))
                .expectNextMatches(e -> e.getRole().equals("Developer"))
                .expectNextMatches(e -> e.getRole().equals("Tester"))
                .verifyComplete();
    }

    @Test
    @Order(2)
    void getEmployeeById() {
        StepVerifier.create(employeeService.getEmployeeById(1L))
                .expectNextMatches(e -> e.getName().equals("Alice"))
                .expectComplete();
    }

    @Test
    @Order(3)
    void saveEmployee() {

        Employee employee = Employee.builder().id(100L).name("Santi").role("ADMIN").build();

        StepVerifier.create(employeeService.saveEmployee(employee))
                .expectNextMatches(savedEmployee ->
                        savedEmployee.getId().equals(100L) &&
                                savedEmployee.getName().equals("Santi") &&
                                savedEmployee.getRole().equals("ADMIN")
                )
                .verifyComplete();
    }

    @Test
    @Order(4)
    void updateEmployee() {
        Employee employee = Employee.builder().id(100L).name("SantI").role("ADMIn").build();
        StepVerifier.create(employeeService.updateEmployee(1L, employee))
                .expectNextMatches(update -> update.getId().equals(1L))
                .verifyComplete();

    }

    @Test
    @Order(5)
    void deleteEmployee() {

        StepVerifier.create(employeeService.deleteEmployee(1L))
                .expectNextMatches(e -> e.getName().equals("SantI"))
                .expectComplete()
                .verify();
    }

    @Test
    @Order(6)
    void testRoles() {
        StepVerifier.create(Flux.fromIterable(configDataEmployee.crearBDMemoria()))
                .expectNextCount(8)
                .verifyComplete();
    }

}