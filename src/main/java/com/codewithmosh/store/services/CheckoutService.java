package com.codewithmosh.store.services;

import com.codewithmosh.store.dtos.CheckoutRequest;
import com.codewithmosh.store.dtos.CheckoutResponse;
import com.codewithmosh.store.entities.Order;
import com.codewithmosh.store.exceptions.CartEmptyException;
import com.codewithmosh.store.exceptions.CartNotFoundException;
import com.codewithmosh.store.repositories.CartRepository;
import com.codewithmosh.store.repositories.OrderRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@RequiredArgsConstructor // Only uses constructor injection for final fields
@Service
public class CheckoutService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final AuthService authService;
    private final CartService cartService;

    // Inject the URL for the client dynamically (for environment URL flexibility)
    @Value("${websiteUrl}")
    private String websiteUrl;

    @Transactional
    public CheckoutResponse checkout(CheckoutRequest request) throws StripeException {
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
            // Create the parameters to be used for the Stripe Session
            var builder = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(websiteUrl + "/checkout-success?orderId=" + order.getId())
                    .setCancelUrl(websiteUrl + "/checkout-cancel");

            order.getItems().forEach(item -> {
                // Create a "Stripe Line Item" param for each of the items in the order
                var lineItem = SessionCreateParams.LineItem.builder()
                    .setQuantity(Long.valueOf(item.getQuantity()))
                    .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency("usd")

                            // Must use smallest unit of currency when passing amount to stripe
                                // So we have to convert the price in dollars to the price in cents
                            .setUnitAmountDecimal(item
                                    .getUnitPrice()
                                    .multiply(BigDecimal.valueOf(100))
                            )

                            .setProductData(
                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                    .setName(item.getProduct().getName())
                                    .build()
                            )

                            .build()
                    )
                    .build();

                // Add each line item to our Stripe Session Params
                builder.addLineItem(lineItem);
            });

            // Create the Stripe Session using the Stripe Session Params we built up
            var session = Session.create(builder.build());

            // Get the URL from the Stripe Session the client will use to go to the checkout page
            var stripeCheckoutUrl = session.getUrl();

            cartService.clearCart(cart.getId());

            // Return the OrderId and Stripe Checkout Url in the response
            return new CheckoutResponse(order.getId(), stripeCheckoutUrl);
        }
        catch (StripeException e) {
            // Delete the order, so if the client tries multiple times with the same exception
                // we are not creating multiple orders with no meaning
            orderRepository.delete(order);

            // Re-throw the exception so we can handle it in our Controller layer
                // Since the controller should be responsible for handling http requests and responses NOT the service
            throw e;
        }
    }

}
