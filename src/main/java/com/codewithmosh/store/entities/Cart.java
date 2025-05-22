package com.codewithmosh.store.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "carts")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "date_created", insertable = false, updatable = false)
    private LocalDate dateCreated;

    // To persist a Cart Item when saving a cart, we use the MERGE cascade type
        // This is because we are UPDATING a cart when we add a cart item
        // The MERGE attribute tells JPA to persist the update for any child entities
        // Cart Item is a child of Cart
    @OneToMany(mappedBy = "cart", cascade = {CascadeType.MERGE}, fetch = FetchType.EAGER)
    private Set<CartItem> items = new HashSet<>();

    public BigDecimal getTotalPrice() {
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (CartItem item : items) {
            totalPrice = totalPrice.add(item.getTotalPrice());
        }
        return totalPrice;
    }

}