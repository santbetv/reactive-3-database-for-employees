package com.sbvdeveloper.employeereact.controller.mongo;

import com.sbvdeveloper.employeereact.domain.mongo.EmployeeMongo;
import com.sbvdeveloper.employeereact.service.mongo.EmployeeSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/mongo")
@RequiredArgsConstructor
public class EmployeeMongoController {

    private final EmployeeSyncService employeeSyncService;

    @GetMapping("/employees")
    public Flux<EmployeeMongo> getAllEmployees() {
        return employeeSyncService.getAllEmployees();
    }


    @PostMapping("/employees")
    public ResponseEntity<Mono<EmployeeMongo>> saveEmployee(@RequestBody EmployeeMongo employee) {
        return new ResponseEntity<>(employeeSyncService.saveEmployee(employee), HttpStatus.CREATED);
    }

    @GetMapping("/employees/{id}")
    public ResponseEntity<Mono<EmployeeMongo>> getEmployeeById(@PathVariable("id") String id) {
        return new ResponseEntity<>(employeeSyncService.getEmployeeById(id), HttpStatus.OK);
    }


    @DeleteMapping("/employees/{id}")
    public Mono<ResponseEntity<EmployeeMongo>> deleteEmployee(@PathVariable("id") String id) {
        return employeeSyncService.deleteEmployee(id)
                .map(employee -> new ResponseEntity<>(employee, HttpStatus.OK))
                .switchIfEmpty(Mono.just(new ResponseEntity<>(HttpStatus.NOT_FOUND)));
    }

    //    //Responder de manera reactiva si se actualizo o no el empleado con ResponseEntity<
    @PutMapping("/employees/{id}")
    public Mono<ResponseEntity<EmployeeMongo>> updateEmployee(@PathVariable("id") String id, @RequestBody EmployeeMongo employee) {
        return employeeSyncService.updateEmployee(id, employee)
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .switchIfEmpty(Mono.just(new ResponseEntity<>(HttpStatus.NOT_FOUND)));
    }

}
