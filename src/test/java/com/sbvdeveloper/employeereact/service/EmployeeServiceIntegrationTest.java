package com.sbvdeveloper.employeereact.service;

import com.sbvdeveloper.employeereact.domain.mongo.EmployeeMongo;
import com.sbvdeveloper.employeereact.service.mongo.EmployeeSyncService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Collections;
import java.util.List;

@AutoConfigureWebTestClient//Sirve para configurar el cliente web en modo MOCK
@ExtendWith(MockitoExtension.class)
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) //Sirve para probar el servicio en un puerto aleatorio, contra el servicio real
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK) //Sirve para probar el servicio config en un puerto MOCK
class EmployeeServiceIntegrationTest {

    private final WebTestClient webTestClient;
    private final EmployeeSyncService employeeSyncService;

    @Value("${config.base.endpoint}")
    private String urlMongo;

    @Autowired
    public EmployeeServiceIntegrationTest(WebTestClient webTestClient, EmployeeSyncService employeeSyncService) {
        this.webTestClient = webTestClient;
        this.employeeSyncService = employeeSyncService;
    }


    @Test
    void getAllEmployees() {
        webTestClient.get()
                .uri(urlMongo)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(EmployeeMongo.class)
                .consumeWith(response -> {
                    List<EmployeeMongo> employees = response.getResponseBody();
                    Assertions.assertNotNull(employees);
                    Assertions.assertFalse(employees.isEmpty());
                    Assertions.assertEquals("Camilo", employees.get(0).getName());
                });
    }

    @Test
    void getEmployeeById() {
        //Para las pruebas desde ser sincronas, se debe usar el metodo block()
        //Ya que las pruebas son sincronas
        EmployeeMongo employeeMongoMono = employeeSyncService.getEmployeeById("300").block();

        webTestClient.get()
                .uri(urlMongo + "/{id}", Collections.singletonMap("id", employeeMongoMono.getId()))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.name").isNotEmpty()
                .jsonPath("$.name").isEqualTo("Camilo")
                .jsonPath("$.role").isEqualTo("PO")
                .jsonPath("$.id").isEqualTo("300");

    }

    @Test
    void saveEmployee() {
        EmployeeMongo employee = EmployeeMongo.builder()
                .id("100")
                .name("Santiago")
                .role("Developer")
                .build();

        webTestClient.post()
                .uri(urlMongo)
                .accept(MediaType.APPLICATION_JSON)// Son los datos que se esperan
                .bodyValue(employee)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)// Son los datos que se envian para la creación
                .expectBody(EmployeeMongo.class)
                .consumeWith(response -> {
                    EmployeeMongo employeeMongo = response.getResponseBody();
                    Assertions.assertNotNull(employeeMongo);
                    Assertions.assertEquals("Santiago", employeeMongo.getName());
                    Assertions.assertEquals("Developer", employeeMongo.getRole());
                });
    }

    @Test
    void updateEmployee() {
        EmployeeMongo employee = EmployeeMongo.builder()
                .id("100")
                .name("Santiago")
                .role("SM")
                .build();

        webTestClient.put()
                .uri(urlMongo + "/{id}", Collections.singletonMap("id", employee.getId()))
                .accept(MediaType.APPLICATION_JSON)// Son los datos que se esperan
                .bodyValue(employee)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(EmployeeMongo.class)
                .consumeWith(response -> {
                    EmployeeMongo employeeMongo = response.getResponseBody();
                    Assertions.assertNotNull(employeeMongo);
                    Assertions.assertEquals("Santiago", employeeMongo.getName());
                    Assertions.assertEquals("SM", employeeMongo.getRole());
                });
    }

    @Test
    void deleteEmployee() {
        EmployeeMongo employeeMongoMono = employeeSyncService.getEmployeeById("100").block();

        webTestClient.delete()
                .uri(urlMongo + "/{id}", Collections.singletonMap("id", employeeMongoMono.getId()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(EmployeeMongo.class)
                .consumeWith(response -> {
                    EmployeeMongo employeeMongo = response.getResponseBody();
                    Assertions.assertNotNull(employeeMongo);
                    Assertions.assertEquals("Santiago", employeeMongo.getName());
                    Assertions.assertEquals("SM", employeeMongo.getRole());
                });

    }

}