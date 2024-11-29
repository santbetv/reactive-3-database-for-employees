package com.sbvdeveloper.employeereact.config;

import com.sbvdeveloper.employeereact.domain.Employee;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Configuration
public class ConfigDataEmployee {

    @Bean
    public List<Employee> crearBDMemoria() {
        List<Employee> employees = new ArrayList<>();

        String[] names = {"Alice", "Bob", "Charlie", "Diana", "Eve", "Frank", "Grace", "Hank"};
        String[] roles = {"Developer", "Tester", "Manager", "Analyst", "Designer", "Support", "Lead", "Engineer"};

        Random random = new Random();
        for (int i = 0; i < 8; i++) {
            Employee employee = Employee.builder()
                    .id((long) (i + 1))
                    .name(names[i])
                    .role(roles[i])
                    .build();
            employees.add(employee);
        }
        employees.forEach(System.out::println);

        return employees;
    }

}
