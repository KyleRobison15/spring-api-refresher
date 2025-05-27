package com.codewithmosh.store.repositories;

import com.codewithmosh.store.entities.Order;
import com.codewithmosh.store.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByCustomer(User customer);
}