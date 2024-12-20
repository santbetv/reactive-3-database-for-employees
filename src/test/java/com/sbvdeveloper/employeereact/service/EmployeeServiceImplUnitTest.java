package com.sbvdeveloper.employeereact.service;

import com.sbvdeveloper.employeereact.domain.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.core.ReactiveDeleteOperation;
import org.springframework.data.r2dbc.core.ReactiveSelectOperation;
import org.springframework.data.relational.core.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplUnitTest {

    private EmployeeService employeeService;

    @Mock
    @Qualifier("primaryR2dbcEntityTemplate")
    private R2dbcEntityTemplate primaryR2dbcEntityTemplate;

    @Mock
    @Qualifier("secondaryR2dbcEntityTemplate")
    private R2dbcEntityTemplate secondaryR2dbcEntityTemplate;

    @BeforeEach
    void setUp() {
        employeeService = new EmployeeServiceImpl(primaryR2dbcEntityTemplate, secondaryR2dbcEntityTemplate);
    }


    @Test
    @DisplayName("Get all employees")
    void getAllEmployees() {

        Employee employee1 = Employee.builder().id(1L).name("Alice").role("Developer").build();
        Employee employee2 = Employee.builder().id(2L).name("Jane Smith").role("Manager").build();

        // Mock del objeto intermedio
        ReactiveSelectOperation.ReactiveSelect<Employee> reactiveSelectMock = mock(ReactiveSelectOperation.ReactiveSelect.class);

        // Configurar mock para `select(...)`
        when(primaryR2dbcEntityTemplate.select(Employee.class)).thenReturn(reactiveSelectMock);

        // Configurar mock para `select(...).all()`
        when(reactiveSelectMock.all()).thenReturn(Flux.just(employee1, employee2));

        // Ejecutar el metodo a probar
        Flux<Employee> result = employeeService.getAllEmployees();

        // Validar el flujo reactivo con StepVerifier
        StepVerifier.create(result)
                .expectNextMatches(e -> e.getRole().equals("Developer"))
                .expectNextMatches(e -> e.getRole().equals("Manager"))
                .verifyComplete();

        // Verificar que el mock fue invocado
        verify(primaryR2dbcEntityTemplate, times(1)).select(Employee.class);
    }

    @Test
    @DisplayName("Get employee by ID")
    void getEmployeeById() {

        Employee employee = Employee.builder().id(1L).name("Alice").role("Developer").build();

        // Mock del objeto intermedio
        ReactiveSelectOperation.ReactiveSelect<Employee> reactiveSelectMock = mock(ReactiveSelectOperation.ReactiveSelect.class);

        // Configurar el mock para `select(...).matching(...)`
        when(primaryR2dbcEntityTemplate.select(Employee.class)).thenReturn(reactiveSelectMock);

        // Configurar el mock para `matching(...).one()` para devolver un Mono del empleado
        when(reactiveSelectMock.matching(any())).thenReturn(reactiveSelectMock);
        when(reactiveSelectMock.one()).thenReturn(Mono.just(employee));

        // Ejecutar el metodo a probar
        Mono<Employee> result = employeeService.getEmployeeById(1L);

        // Validar el Mono usando StepVerifier
        StepVerifier.create(result)
                .expectNextMatches(e -> e.getId().equals(1L) && e.getName().equals("Alice"))
                .verifyComplete();

        // Verificar que el mock fue invocado correctamente
        verify(primaryR2dbcEntityTemplate).select(Employee.class);
        verify(reactiveSelectMock).matching(any());
        verify(reactiveSelectMock).one();
    }

    @Test
    @DisplayName("Save employee when employee doesn't exist")
    void saveEmployeeWhenNotExists() {
        // Crear un nuevo empleado para la prueba
        Employee employee = Employee.builder().id(1L).name("Alice").role("Developer").build();

        // Crear el mock de ReactiveSelect para que coincida con el select
        ReactiveSelectOperation.ReactiveSelect<Employee> reactiveSelectMock = mock(ReactiveSelectOperation.ReactiveSelect.class);

        // Configurar el mock para `select(...).matching(...).one()` para devolver Mono.empty(), ya que el empleado no existe
        when(primaryR2dbcEntityTemplate.select(Employee.class)).thenReturn(reactiveSelectMock);
        when(reactiveSelectMock.matching(any(Query.class))).thenReturn(reactiveSelectMock);
        when(reactiveSelectMock.one()).thenReturn(Mono.empty());  // Empleado no encontrado

        // Mock del insert cuando el empleado no existe
        when(primaryR2dbcEntityTemplate.insert(any(Employee.class))).thenReturn(Mono.just(employee));

        // Ejecutar el metodo a probar
        Mono<Employee> result = employeeService.saveEmployee(employee);

        // Validar el Mono usando StepVerifier
        StepVerifier.create(result)
                .expectNextMatches(e -> e.getId().equals(1L) && e.getName().equals("Alice"))
                .verifyComplete();

        // Verificar que las invocaciones se realizaron correctamente
        verify(primaryR2dbcEntityTemplate).select(Employee.class);
        verify(primaryR2dbcEntityTemplate).insert(any(Employee.class));
    }

    @Test
    @DisplayName("Update employee when employee already exists")
    void saveEmployeeWhenExists() {
        // Crear un empleado existente y uno nuevo para la prueba
        Employee existingEmployee = Employee.builder().id(1L).name("Alice").role("Developer").build();
        Employee updatedEmployee = Employee.builder().id(1L).name("Alice Updated").role("Manager").build();

        // Crear el mock de ReactiveSelect
        ReactiveSelectOperation.ReactiveSelect<Employee> reactiveSelectMock = mock(ReactiveSelectOperation.ReactiveSelect.class);

        // Configurar el mock para `select(...).matching(...).one()` para devolver un empleado existente
        when(primaryR2dbcEntityTemplate.select(Employee.class)).thenReturn(reactiveSelectMock);
        when(reactiveSelectMock.matching(any())).thenReturn(reactiveSelectMock);
        when(reactiveSelectMock.one()).thenReturn(Mono.just(existingEmployee));

        // Configurar el mock para `insert(...)` para actualizar el empleado existente
        when(primaryR2dbcEntityTemplate.insert(any(Employee.class))).thenReturn(Mono.just(updatedEmployee));

        // Ejecutar el método a probar
        Mono<Employee> result = employeeService.saveEmployee(updatedEmployee);

        // Validar el Mono usando StepVerifier
        StepVerifier.create(result)
                .expectNextMatches(e -> e.getId().equals(1L) && e.getName().equals("Alice Updated"))
                .verifyComplete();

        // Verificar que las invocaciones se realizaron correctamente
        verify(primaryR2dbcEntityTemplate).select(Employee.class);
        verify(primaryR2dbcEntityTemplate).insert(any(Employee.class));
    }

    @Test
    @DisplayName("Update employee when employee exists")
    void updateEmployeeWhenExists() {
        // Crear un empleado existente y uno actualizado para la prueba
        Employee existingEmployee = Employee.builder().id(1L).name("Alice").role("Developer").build();
        Employee updatedEmployee = Employee.builder().id(1L).name("Alice Updated").role("Manager").build();

        // Crear el mock de ReactiveSelect
        ReactiveSelectOperation.ReactiveSelect<Employee> reactiveSelectMock = mock(ReactiveSelectOperation.ReactiveSelect.class);

        // Configurar el mock para `select(...).matching(...).one()` para devolver un empleado existente
        when(primaryR2dbcEntityTemplate.select(Employee.class)).thenReturn(reactiveSelectMock);
        when(reactiveSelectMock.matching(any())).thenReturn(reactiveSelectMock);
        when(reactiveSelectMock.one()).thenReturn(Mono.just(existingEmployee));

        // Configurar el mock para `update(...)` para actualizar el empleado
        when(primaryR2dbcEntityTemplate.update(any(Employee.class))).thenReturn(Mono.just(updatedEmployee));

        // Ejecutar el metodo a probar
        Mono<Employee> result = employeeService.updateEmployee(1L, updatedEmployee);

        // Validar el Mono usando StepVerifier
        StepVerifier.create(result)
                .expectNextMatches(e -> e.getId().equals(1L) && e.getName().equals("Alice Updated"))
                .verifyComplete();

        // Verificar que las invocaciones se realizaron correctamente
        verify(primaryR2dbcEntityTemplate).select(Employee.class);
        verify(primaryR2dbcEntityTemplate).update(any(Employee.class));
    }

    @Test
    @DisplayName("Throw exception when employee does not exist for update")
    void updateEmployeeWhenNotExists() {
        // Crear un empleado que no existe para la prueba
        Employee updatedEmployee = Employee.builder().id(1L).name("Alice Updated").role("Manager").build();

        // Crear el mock de ReactiveSelect
        ReactiveSelectOperation.ReactiveSelect<Employee> reactiveSelectMock = mock(ReactiveSelectOperation.ReactiveSelect.class);

        // Configurar el mock para `select(...).matching(...).one()` para devolver Mono.empty(), ya que el empleado no existe
        when(primaryR2dbcEntityTemplate.select(Employee.class)).thenReturn(reactiveSelectMock);
        when(reactiveSelectMock.matching(any())).thenReturn(reactiveSelectMock);
        when(reactiveSelectMock.one()).thenReturn(Mono.empty());

        // Ejecutar el metodo a probar y esperar la excepción
        Mono<Employee> result = employeeService.updateEmployee(1L, updatedEmployee);

        // Validar que el Mono emite una excepción
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof Exception && throwable.getMessage().equals("El empleado no existe para actualizar"))
                .verify();

        // Verificar que la selección del empleado fue intentada
        verify(primaryR2dbcEntityTemplate).select(Employee.class);
    }

    @Test
    @DisplayName("Delete employee when employee exists")
    void deleteEmployeeWhenExists() {
        // Crear un empleado para la prueba
        Employee existingEmployee = Employee.builder().id(1L).name("Alice").role("Developer").build();

        // Crear el mock de ReactiveSelect
        ReactiveSelectOperation.ReactiveSelect<Employee> reactiveSelectMock = mock(ReactiveSelectOperation.ReactiveSelect.class);

        // Configurar el mock para `select(...).matching(...).one()` para devolver un empleado existente
        when(primaryR2dbcEntityTemplate.select(Employee.class)).thenReturn(reactiveSelectMock);
        when(reactiveSelectMock.matching(any())).thenReturn(reactiveSelectMock);
        when(reactiveSelectMock.one()).thenReturn(Mono.just(existingEmployee));

        // Crear el mock para la operación de eliminación (ReactiveDeleteOperation.ReactiveDelete)
        ReactiveDeleteOperation.ReactiveDelete deleteMock = mock(ReactiveDeleteOperation.ReactiveDelete.class);
        ReactiveDeleteOperation.TerminatingDelete terminatingDeleteMock = mock(ReactiveDeleteOperation.TerminatingDelete.class);

        // Configurar el mock para `delete(...).matching(...).all()` para eliminar el empleado
        when(primaryR2dbcEntityTemplate.delete(Employee.class)).thenReturn(deleteMock);
        when(deleteMock.matching(any())).thenReturn(terminatingDeleteMock);
        when(terminatingDeleteMock.all()).thenReturn(Mono.just(1L)); // Simulando que se eliminó un empleado

        // Ejecutar el metodo a probar
        Mono<Employee> result = employeeService.deleteEmployee(1L);

        // Validar el Mono usando StepVerifier
        StepVerifier.create(result)
                .expectNextMatches(e -> e.getId().equals(1L) && e.getName().equals("Alice"))
                .verifyComplete();

        // Verificar que las invocaciones se realizaron correctamente
        verify(primaryR2dbcEntityTemplate).select(Employee.class);
        verify(primaryR2dbcEntityTemplate).delete(Employee.class);
        verify(deleteMock).matching(any());
        verify(terminatingDeleteMock).all();
    }

    @Test
    @DisplayName("Throw exception when employee does not exist for deletion")
    void deleteEmployeeWhenNotExists() {
        // Crear el mock de ReactiveSelect
        ReactiveSelectOperation.ReactiveSelect<Employee> reactiveSelectMock = mock(ReactiveSelectOperation.ReactiveSelect.class);

        // Configurar el mock para `select(...).matching(...).one()` para devolver Mono.empty(), ya que el empleado no existe
        when(primaryR2dbcEntityTemplate.select(Employee.class)).thenReturn(reactiveSelectMock);
        when(reactiveSelectMock.matching(any())).thenReturn(reactiveSelectMock);
        when(reactiveSelectMock.one()).thenReturn(Mono.empty());

        // Ejecutar el metodo a probar y esperar la excepción
        Mono<Employee> result = employeeService.deleteEmployee(1L);

        // Validar que el Mono emite una excepción
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof Exception && throwable.getMessage().equals("El empleado no existe para eliminar"))
                .verify();

        // Verificar que la selección del empleado fue intentada
        verify(primaryR2dbcEntityTemplate).select(Employee.class);
    }


}