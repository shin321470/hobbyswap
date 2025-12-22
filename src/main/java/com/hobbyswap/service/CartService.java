package com.hobbyswap.service;

import com.hobbyswap.model.*;
import com.hobbyswap.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    @Autowired private CartRepository cartRepository;
    @Autowired private ItemRepository itemRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private CartItemRepository cartItemRepository;

    public Cart getOrCreateCart(User user) {
        Cart cart = cartRepository.findByUser(user);
        if (cart == null) {
            cart = new Cart();
            cart.setUser(user);
            cartRepository.save(cart);
        }
        return cart;
    }

    @Transactional
    public void addToCart(User user, Long itemId, int quantity) {
        Cart cart = getOrCreateCart(user);
        Item item = itemRepository.findById(itemId).orElse(null);

        if (item != null) {
            Optional<CartItem> existingItem = cart.getItems().stream()
                    .filter(ci -> ci.getItem().getId().equals(itemId))
                    .findFirst();

            if (existingItem.isPresent()) {
                CartItem ci = existingItem.get();
                ci.setQuantity(ci.getQuantity() + quantity);
            } else {
                CartItem newItem = new CartItem();
                newItem.setCart(cart);
                newItem.setItem(item);
                newItem.setQuantity(quantity);

                cart.getItems().add(newItem);
            }
            cartRepository.save(cart);
        }
    }

    @Transactional
    public void removeFromCart(User user, Long cartItemId) {
        Cart cart = getOrCreateCart(user);
        cart.getItems().removeIf(item -> item.getId().equals(cartItemId));
        cartRepository.save(cart);
    }

    @Transactional
    public void clearCart(User user) {
        Cart cart = getOrCreateCart(user);
        if (!cart.getItems().isEmpty()) {
            cartItemRepository.deleteAll(cart.getItems());
            cart.getItems().clear();
        }
        cartRepository.save(cart);
    }

    @Transactional
    public void checkout(User user, String receiverName, String phone, String address) {
        Cart cart = cartRepository.findByUser(user);

        if (cart == null) {
            throw new RuntimeException("Cart not found");
        }

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        Order order = new Order();
        order.setUser(user);
        order.setReceiverName(receiverName);
        order.setPhone(phone);
        order.setAddress(address);
        order.setTotalPrice(cart.getTotalPrice());
        order.setStatus("PAID");

        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cart.getItems()) {
            Item item = cartItem.getItem();

            if (item.getStockQuantity() < cartItem.getQuantity()) {
                throw new RuntimeException("庫存不足: " + item.getTitle());
            }

            item.setStockQuantity(item.getStockQuantity() - cartItem.getQuantity());

            if (item.getStockQuantity() == 0) {
                item.setStatus("SOLD");
            }
            itemRepository.save(item);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setItem(item);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(item.getPrice());
            orderItems.add(orderItem);
        }

        order.setOrderItems(orderItems);

        orderRepository.save(order);

        cartItemRepository.deleteAll(cart.getItems());
        cart.getItems().clear();
        cartRepository.save(cart);
    }
}