package com.codewithmosh.store.users;

import com.codewithmosh.store.products.Product;
import com.krd.auth.model.BaseUser;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

/**
 * User entity extending BaseUser from krd-spring-starters.
 *
 * Inherited from BaseUser:
 * - id, firstName, lastName, email, password
 * - roles (Set<Role>)
 * - addresses (Set<Address>)
 * - createdAt, updatedAt
 * - addRole(), addAddress() methods
 *
 * Domain-specific additions:
 * - favoriteProducts (wishlist)
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder  // Changed from @Builder to @SuperBuilder for inheritance
@EqualsAndHashCode(callSuper = true)  // Include BaseUser fields in equals/hashCode
@Entity
@Table(name = "users")
public class User extends BaseUser {

    // Domain-specific field: favorite products (wishlist)
    @ManyToMany
    @JoinTable(
        name = "wishlist",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    @Builder.Default
    private Set<Product> favoriteProducts = new HashSet<>();

    public void addFavoriteProduct(Product product) {
        favoriteProducts.add(product);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "id = " + getId() + ", " +
                "firstName = " + getFirstName() + ", " +
                "lastName = " + getLastName() + ", " +
                "email = " + getEmail() + ")";
    }
}
