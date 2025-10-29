package com.example.clothesshop.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.util.ArrayList;

@Entity
@Getter
@Setter
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();
}
