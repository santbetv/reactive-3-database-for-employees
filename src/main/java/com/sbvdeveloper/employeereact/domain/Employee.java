package com.sbvdeveloper.employeereact.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

/**
 * @author santiago betancur
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("employees")
public class Employee implements Persistable<Long> {

    @Id
    private Long id;
    private String name;
    private String role;
    @Transient
    private boolean isNew;


    @Override
    public String toString() {
        return "Employee{" + "id=" + id + ", name=" + name + ", role=" + role + '}';
    }


    @Override
    public boolean isNew() {
        return isNew;
    }

    public void setIsNew(boolean isNew) {
        this.isNew = isNew;
    }
}
