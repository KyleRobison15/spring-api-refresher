package com.codewithmosh.store.services;

import com.codewithmosh.store.entities.PaymentStauts;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PaymentResult {
    private Long orderId;
    private PaymentStauts paymentStatus;
}
