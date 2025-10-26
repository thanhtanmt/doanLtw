package com.example.clothesshop.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Table(name = "roles")
public class Role {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // ví dụ: ROLE_USER, ROLE_ADMIN

    // getters/setters
}
