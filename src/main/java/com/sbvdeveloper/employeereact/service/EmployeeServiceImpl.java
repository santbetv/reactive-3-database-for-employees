package com.sbvdeveloper.employeereact.service;

import com.sbvdeveloper.employeereact.domain.Employee;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class EmployeeServiceImpl implements EmployeeService {


    @Qualifier("primaryR2dbcEntityTemplate")
    private final R2dbcEntityTemplate primaryR2dbcEntityTemplate;

    @Qualifier("secondaryR2dbcEntityTemplate")
    private final R2dbcEntityTemplate secondaryR2dbcEntityTemplate;

    public EmployeeServiceImpl(R2dbcEntityTemplate primaryR2dbcEntityTemplate, R2dbcEntityTemplate secondaryR2dbcEntityTemplate) {
        this.primaryR2dbcEntityTemplate = primaryR2dbcEntityTemplate;
        this.secondaryR2dbcEntityTemplate = secondaryR2dbcEntityTemplate;
    }

    //La funcion de flatMap es transformar el objeto que se recibe en el parametro en otro objeto
    // y devolverlo como un Mono

    @Override
    @Transactional(readOnly = true)
    public Flux<Employee> getAllEmployees() {
        return this.primaryR2dbcEntityTemplate.select(Employee.class)
                .all()
                .doOnError(e -> System.out.println("Error al obtener empleados: " + e.getMessage()));
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<Employee> getAllEmployeesSecundary() {
        return this.secondaryR2dbcEntityTemplate.select(Employee.class)
                .all()
                .doOnError(e -> System.out.println("Error al obtener empleados secundarios: " + e.getMessage()));
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<Employee> getEmployeeById(Long id) {
        return this.primaryR2dbcEntityTemplate.select(Employee.class)
                .matching(Query.query(Criteria.where("id").is(id)))
                .one()
                .doOnError(e -> System.out.println("Error al obtener empleado por ID: " + e.getMessage()));
    }

    @Override
    @Transactional
    public Mono<Employee> saveEmployee(Employee employee) {
        // Verificar si el empleado ya existe en la base de datos
        return this.primaryR2dbcEntityTemplate.select(Employee.class)
                .matching(Query.query(Criteria.where("id").is(employee.getId())))
                .one()
                .flatMap(existingEmployee -> {
                    // Si el empleado existe, se actualiza
                    existingEmployee.setName(employee.getName());
                    existingEmployee.setRole(employee.getRole());
                    return this.primaryR2dbcEntityTemplate.insert(existingEmployee); // Actualizar
                })
                .switchIfEmpty(Mono.defer(() -> {
                    // Si el empleado no existe, se crea uno nuevo
                    employee.setIsNew(true);
                    return this.primaryR2dbcEntityTemplate.insert(employee); // Crear
                }));
    }

    @Override
    @Transactional
    public Mono<Employee> updateEmployee(Long id, Employee employee) {
        return this.primaryR2dbcEntityTemplate.select(Employee.class)
                .matching(Query.query(Criteria.where("id").is(id)))
                .one()
                .flatMap(existingEmployee -> {
                    // Si el empleado existe, actualizamos sus datos
                    existingEmployee.setName(employee.getName());
                    existingEmployee.setRole(employee.getRole());
                    return this.primaryR2dbcEntityTemplate.update(existingEmployee); // Actualizar
                })
                .switchIfEmpty(Mono.error(new Exception("El empleado no existe para actualizar")));
    }

    @Override
    @Transactional
    public Mono<Employee> deleteEmployee(Long id) {
        return this.primaryR2dbcEntityTemplate.select(Employee.class)
                .matching(Query.query(Criteria.where("id").is(id)))
                .one()
                .flatMap(employee ->
                        // Si el empleado existe, proceder con la eliminación
                        this.primaryR2dbcEntityTemplate.delete(Employee.class)
                                .matching(Query.query(Criteria.where("id").is(id)))
                                .all()
                                .thenReturn(employee) // Devolver el empleado eliminado
                )
                .switchIfEmpty(Mono.error(new Exception("El empleado no existe para eliminar")));
    }

    //Ejemplo de consulta personalizada avanzada Spring Data R2DBC con R2dbcEntityTemplate
//    public Flux<EmployeeWithDepartment> findEmployeesWithDepartments() {
//        String query = """
//                    SELECT e.id AS employee_id, e.name AS employee_name, d.name AS department_name
//                    FROM employees e
//                    JOIN departments d ON e.department_id = d.id
//                """;
//
//        return primaryR2dbcEntityTemplate
//                .getDatabaseClient()
//                .sql(query)
//                .map((row, metadata) -> new EmployeeWithDepartment(
//                        row.get("employee_id", Long.class),
//                        row.get("employee_name", String.class),
//                        row.get("department_name", String.class)
//                ))
//                .all();
//    }


}
