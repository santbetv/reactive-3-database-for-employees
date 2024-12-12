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


    //Usa flatMap si tu transformación devuelve un Mono o Flux.
    //Usa map si tu transformación devuelve un objeto simple (sincrónico), por ejemplo, Mono.just("Hello").
    //Usa map cuando solo necesitas transformar datos sin realizar operaciones que devuelvan flujos.
    @Transactional
    public Mono<Employee> saveEmployeeWithAdditionalLogic(Employee employee) {
        //Se usa cuando transformas un valor de un flujo directamente a otro valor, sin crear otro flujo reactivo.
        //Devuelve un flujo con el tipo transformado.
        //No aplana flujos anidados.
        Mono<Employee> employeeMono = Mono.just(employee)
                .map(emp -> {
                    emp.setRole("Updated Role");
                    return emp; // Devuelve el mismo flujo con un tipo transformado
                });

        //Se usa cuando el resultado de la transformación es otro flujo reactivo (es decir, un Mono o Flux).
        //Aplana flujos anidados, permitiendo que el resultado de un flujo interno continúe en el flujo principal.
        //Ideal para operaciones asincrónicas o cuando necesitas realizar múltiples llamadas dentro de un flujo.
        Mono<Employee> employeeMonoTwo = Mono.just(employee)
                .flatMap(emp -> this.primaryR2dbcEntityTemplate.update(emp)); // Devuelve un flujo (Mono/Flux)


        return this.primaryR2dbcEntityTemplate.select(Employee.class)
                .matching(Query.query(Criteria.where("id").is(employee.getId())))
                .one()
                .flatMap(existingEmployee -> {// Operación asincrónica: devuelve un Mono
                    // Si el empleado ya existe
                    existingEmployee.setName(employee.getName());
                    existingEmployee.setRole(employee.getRole());

                    // Actualizamos el empleado
                    return this.primaryR2dbcEntityTemplate.update(existingEmployee)
                            .then(
                                    // Insertar un registro adicional en otra tabla después de la actualización
                                    //this.primaryR2dbcEntityTemplate.insert(new AuditLog("Empleado actualizado", existingEmployee.getId()))
                            )
                            .thenReturn(existingEmployee); // Devolver el empleado actualizado
                })
                .switchIfEmpty(
                        Mono.defer(() -> {
                            // Si el empleado no existe
                            employee.setIsNew(true);

                            // Insertamos el empleado y realizamos otra operación después
                            return this.primaryR2dbcEntityTemplate.insert(employee)
                                    .flatMap(newEmployee -> {
                                        // Realizamos un update adicional
                                        newEmployee.setRole("NEW_ROLE");
                                        return this.primaryR2dbcEntityTemplate.update(newEmployee)
                                                .thenReturn(newEmployee); // Devolver el empleado creado
                                    });
                        })
                );
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
