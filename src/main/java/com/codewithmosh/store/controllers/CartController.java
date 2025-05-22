package com.codewithmosh.store.controllers;

import com.codewithmosh.store.dtos.AddItemToCartRequest;
import com.codewithmosh.store.dtos.CartDto;
import com.codewithmosh.store.dtos.CartItemDto;
import com.codewithmosh.store.dtos.UpdateCartItemRequest;
import com.codewithmosh.store.entities.Cart;
import com.codewithmosh.store.mappers.CartMapper;
import com.codewithmosh.store.repositories.CartRepository;
import com.codewithmosh.store.repositories.ProductRepository;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping("/carts")
public class CartController {

    private final CartRepository cartRepository;
    private final CartMapper cartMapper;
    private final ProductRepository productRepository;

    @PostMapping
    public ResponseEntity<CartDto> createCart(UriComponentsBuilder uriComponentsBuilder) {

        var cart = new Cart();

        cartRepository.save(cart);

        var cartDto = cartMapper.toCartDto(cart);
        var uri = uriComponentsBuilder.path("/carts/{id}").buildAndExpand(cartDto.getId()).toUri();

        return ResponseEntity.created(uri).body(cartDto);
    }

    @PostMapping("/{cartId}/items")
    public ResponseEntity<CartItemDto> addItemToCart(@PathVariable UUID cartId,
                                                     @RequestBody AddItemToCartRequest request,
                                                     UriComponentsBuilder uriBuilder) {

        var cart = cartRepository.getCartWithItems(cartId).orElse(null);
        if (cart == null) {
            return ResponseEntity.notFound().build();
        }

        var product = productRepository.findById(request.getProductId()).orElse(null);
        if (product == null) {
            // If product is not found, the user provided a bad product id -> bad request instead of not found
            // We only return not found when a client is requesting a resource that could not be found
            return ResponseEntity.badRequest().build();
        }

        var cartItem = cart.addItem(product);

        // Here we use the CART repository to save the cartItem along with its associated cart
        // This corresponds to the "Aggregate Root" principle in Domain Driven Design:
            // A cart item can never exist without a Cart. So we should never save a cartItem to the DB directly w/o a cart
            // For this reason, we will never create a CartItemRepo. And we will always save cartItems to the DB through a CartRepo
            // This more accurately models the business logic in our code
        cartRepository.save(cart);

        var cartItemDto = cartMapper.toCartItemDto(cartItem);

        var uri = uriBuilder.path("/carts/{id}/items").buildAndExpand(cart.getId()).toUri();

        return ResponseEntity.created(uri).body(cartItemDto);

    }

    @GetMapping("/{cartId}")
    public ResponseEntity<CartDto> getCart(@PathVariable UUID cartId) {
        var cart = cartRepository.getCartWithItems(cartId).orElse(null);
        if (cart == null) {
            return ResponseEntity.notFound().build();
        }
        var cartDto = cartMapper.toCartDto(cart);
        return ResponseEntity.ok(cartDto);
    }

    @PutMapping("/{cartId}/items/{productId}")
    public ResponseEntity<?> updateItemQuantity(
            @PathVariable UUID cartId,
            @PathVariable Long productId,
            @Valid @RequestBody UpdateCartItemRequest request) {

        var cart = cartRepository.getCartWithItems(cartId).orElse(null);

        if (cart == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Cart not found."));
        }

        // Find the cart item for the given product id
        var cartItem = cart.getItem(productId);

        // If it doesn't exist, return a not found error
        if (cartItem == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Product was not found in the cart."));
        }

        // Update the quantity for this cart item
        cartItem.setQuantity(request.getQuantity());
        cartRepository.save(cart);

        var cartItemDto = cartMapper.toCartItemDto(cartItem);

        return ResponseEntity.ok(cartItemDto);
    }

    @DeleteMapping("/{cartId}/items/{productId}")
    public ResponseEntity<?> removeCartItem(@PathVariable UUID cartId, @PathVariable Long productId) {
        var cart = cartRepository.getCartWithItems(cartId).orElse(null);
        if (cart == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Cart not found."));
        }

        cart.removeItem(productId);
        cartRepository.save(cart);

        return ResponseEntity.noContent().build();

    }


}
