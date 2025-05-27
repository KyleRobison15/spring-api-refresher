package com.codewithmosh.store.repositories;

import com.codewithmosh.store.entities.Order;
import com.codewithmosh.store.entities.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    // We define a custom query here to avoid the N+1 problem when we get a list of orders for a given customer
        // Without this, Hibernate first fetches each Order, then it executes additional queries for n items and n products in each order
        // This is because by default, the items for an order are Lazy loaded
    // The EntityGraph annotation tells hibernate to Eagarly load the attributes we want for this query only
        // EntityGraph + Custom Query -> solves the N+1 problem
        // Now one query will be sent to the DB to fetch all the orders AND the items and products for those orders
    @EntityGraph(attributePaths = "items.product")
    @Query("SELECT o FROM Order o WHERE o.customer = :customer")
    List<Order> getAllByCustomer(@Param("customer") User customer);
}