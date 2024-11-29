package com.sbvdeveloper.employeereact.controller;

import com.sbvdeveloper.employeereact.domain.Employee;
import com.sbvdeveloper.employeereact.service.EmployeeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class EmployeeController {


    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping("/employees")
    public ResponseEntity<Flux<Employee>> getAllEmployees(){
        return new ResponseEntity<>(employeeService.getAllEmployees(), HttpStatus.OK);
    }

    @PostMapping("/employees")
    public ResponseEntity<Mono<Employee>> saveEmployee(@RequestBody Employee employee){
        return new ResponseEntity<>(employeeService.saveEmployee(employee), HttpStatus.CREATED);
    }

    @GetMapping("/employees/{id}")
    public ResponseEntity<Mono<Employee>> getEmployeeById(@RequestParam("id") Long id){
        return new ResponseEntity<>(employeeService.getEmployeeById(id), HttpStatus.OK);
    }

    @DeleteMapping("/employees")
    public Mono<ResponseEntity<Employee>> deleteEmployee(@RequestParam("id") Long id){
        return employeeService.deleteEmployee(id)
                .map(employee -> new ResponseEntity<>(employee, HttpStatus.OK))
                .switchIfEmpty(Mono.just(new ResponseEntity<>(HttpStatus.NOT_FOUND)));
    }

    //Responder de manera reactiva si se actualizo o no el empleado con ResponseEntity<
    @PutMapping("/employees/{id}")
    public Mono<ResponseEntity<Employee>> updateEmployee(@RequestParam("id") Long id, @RequestBody Employee employee){
        return employeeService.updateEmployee(id, employee)
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .switchIfEmpty(Mono.just(new ResponseEntity<>(HttpStatus.NOT_FOUND)));
    }
}
