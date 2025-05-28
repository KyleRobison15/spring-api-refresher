package com.codewithmosh.store.services;

import com.codewithmosh.store.dtos.CheckoutRequest;
import com.codewithmosh.store.dtos.CheckoutResponse;
import com.codewithmosh.store.entities.Order;
import com.codewithmosh.store.exceptions.CartEmptyException;
import com.codewithmosh.store.exceptions.CartNotFoundException;
import com.codewithmosh.store.exceptions.PaymentException;
import com.codewithmosh.store.repositories.CartRepository;
import com.codewithmosh.store.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor // Only uses constructor injection for final fields
@Service
public class CheckoutService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final AuthService authService;
    private final CartService cartService;
    private final PaymentGateway paymentGateway;

    @Transactional
    public CheckoutResponse checkout(CheckoutRequest request) {
        var cart = cartRepository.getCartWithItems(request.getCartId()).orElse(null);

        if (cart == null) {
            throw new CartNotFoundException();
        }

        if(cart.isEmpty()) {
            throw new CartEmptyException();
        }

        var order = Order.fromCart(cart, authService.getCurrentUser());

        orderRepository.save(order);

        try {

            // Use the PaymentGateway abstraction to create a checkout session
            var session = paymentGateway.createCheckoutSession(order);

            // Get the URL from the Checkout Session the client will use to go to the checkout page
            var stripeCheckoutUrl = session.getCheckoutUrl();

            cartService.clearCart(cart.getId());

            // Return the OrderId and Checkout Url in the response
            return new CheckoutResponse(order.getId(), stripeCheckoutUrl);
        }
        catch (PaymentException e) {
            // Delete the order, so if the client tries multiple times with the same exception
                // we are not creating multiple orders with no meaning
            orderRepository.delete(order);

            // Re-throw the exception so we can handle it in our Controller layer
                // Since the controller should be responsible for handling http requests and responses NOT the service
            throw e;
        }
    }

    public void handleWebhookEvent(WebhookRequest request) {
        paymentGateway
                .parseWebhookRequest(request)
                .ifPresent(paymentResult -> {
                    var order = orderRepository.findById(paymentResult.getOrderId()).orElseThrow();
                    order.setStatus(paymentResult.getPaymentStatus());
                    orderRepository.save(order);
                });

    }

}
