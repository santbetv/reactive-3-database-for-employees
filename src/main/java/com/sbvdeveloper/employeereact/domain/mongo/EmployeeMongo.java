package com.sbvdeveloper.employeereact.domain.mongo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "employees") // Define el nombre de la colección
public class EmployeeMongo {

    @Id
    private String id; // MongoDB utiliza cadenas para los IDs
    private String name;
    private String role;
}
