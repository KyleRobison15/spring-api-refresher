package com.codewithmosh.store.services;

import com.codewithmosh.store.entities.Order;
import com.codewithmosh.store.exceptions.PaymentException;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class StripePaymentGateway implements PaymentGateway {

    // Inject the URL for the client dynamically (for environment URL flexibility)
    @Value("${websiteUrl}")
    private String websiteUrl;

    @Override
    public CheckoutSession createCheckoutSession(Order order) {
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
            return new CheckoutSession(session.getUrl());
        } catch (StripeException e) {
            // In a real world application, here you should use a Logging service to log the exception for monitoring purposes
            System.out.println(e.getMessage());
            throw new PaymentException();
        }
    }
}
