package com.codewithmosh.store.services;

import com.codewithmosh.store.dtos.OrderDto;
import com.codewithmosh.store.exceptions.ForbiddenOrderException;
import com.codewithmosh.store.exceptions.OrderNotFoundException;
import com.codewithmosh.store.mappers.OrderMapper;
import com.codewithmosh.store.repositories.OrderRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@AllArgsConstructor
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final AuthService authService;
    private final OrderMapper orderMapper;

    public List<OrderDto> getAllOrders() {
        var user = authService.getCurrentUser();
        var orders = orderRepository.getAllByCustomer(user);
        return orders.stream()
                .map(orderMapper::toDto)
                .toList();
    }

    public OrderDto getOrderById(Long id) {
        var user = authService.getCurrentUser();
        var order = orderRepository.findById(id).orElse(null);

        if (order == null) {
            throw new OrderNotFoundException();
        }

        if(!Objects.equals(order.getCustomer().getId(), user.getId())) {
            throw new ForbiddenOrderException();
        }

        return orderMapper.toDto(order);
    }

}
