package com.example.clothesshop.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.*;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Table(name = "users")
public class User extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, columnDefinition = "NVARCHAR(100)")
    private String firstName;

    @Column(nullable = false, columnDefinition = "NVARCHAR(100)")
    private String lastName;

    @Column(nullable = false)
    private String phone;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles;

    private boolean enabled = true;

    @Column(name = "verification_code")
    private String verificationCode;

    @Column(name = "verification_code_expiry")
    private LocalDateTime verificationCodeExpiry;

    @Column(name = "email_verified")
    private boolean emailVerified = false;

    @Column(name = "avatar_url")
    private String avatarUrl;
}
