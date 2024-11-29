package com.sbvdeveloper.employeereact.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author santiago betancur
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    private Long id;
    private String name;
    private String role;

    @Override
    public String toString() {
        return "Employee{" + "id=" + id + ", name=" + name + ", role=" + role + '}';
    }
}
