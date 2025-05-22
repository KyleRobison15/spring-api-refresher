package com.codewithmosh.store.services;

import com.codewithmosh.store.dtos.CartDto;
import com.codewithmosh.store.dtos.CartItemDto;
import com.codewithmosh.store.entities.Cart;
import com.codewithmosh.store.exceptions.CartNotFoundException;
import com.codewithmosh.store.exceptions.ProductNotFoundException;
import com.codewithmosh.store.mappers.CartMapper;
import com.codewithmosh.store.repositories.CartRepository;
import com.codewithmosh.store.repositories.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@AllArgsConstructor
@Service
public class CartService {

    private CartRepository cartRepository;
    private ProductRepository productRepository;
    private CartMapper cartMapper;

    public CartDto createCart(){
        var cart = new Cart();
        cartRepository.save(cart);

        return cartMapper.toCartDto(cart);
    }

    public CartItemDto addItemToCart(UUID cartId, Long productId) {
        var cart = cartRepository.getCartWithItems(cartId).orElse(null);

        if (cart == null) {
            throw new CartNotFoundException();
        }

        var product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            // If product is not found, the user provided a bad product id -> bad request instead of not found
            // We only return not found when a client is requesting a resource that could not be found
            throw new ProductNotFoundException();
        }

        var cartItem = cart.addItem(product);

        // Here we use the CART repository to save the cartItem along with its associated cart
        // This corresponds to the "Aggregate Root" principle in Domain Driven Design:
        // A cart item can never exist without a Cart. So we should never save a cartItem to the DB directly w/o a cart
        // For this reason, we will never create a CartItemRepo. And we will always save cartItems to the DB through a CartRepo
        // This more accurately models the business logic in our code
        cartRepository.save(cart);

        return cartMapper.toCartItemDto(cartItem);
    }

    public CartDto getCartById(UUID cartId) {
        var cart = cartRepository.getCartWithItems(cartId).orElse(null);
        if (cart == null) {
            throw new CartNotFoundException();
        }
        return cartMapper.toCartDto(cart);
    }

}
