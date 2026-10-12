package com.hobbyswap.service;

import com.hobbyswap.controller.CheckoutController;
import com.hobbyswap.model.Cart;
import com.hobbyswap.model.Item;
import com.hobbyswap.model.ItemStatus;
import com.hobbyswap.model.Order;
import com.hobbyswap.model.User;
import com.hobbyswap.repository.ItemRepository;
import com.hobbyswap.repository.OrderRepository;
import com.hobbyswap.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CommerceIntegrationTest {
    @Autowired CartService carts;
    @Autowired ItemService items;
    @Autowired CheckoutController directCheckout;
    @Autowired UserRepository users;
    @Autowired ItemRepository itemRepository;
    @Autowired OrderRepository orders;
    @Autowired EntityManager entityManager;

    private User buyer() {
        User user = new User();
        user.setEmail("buyer-" + UUID.randomUUID() + "@example.com");
        user.setName("Buyer");
        user.setPassword("password");
        return users.save(user);
    }

    private Item item(String title, int stock) {
        Item item = new Item();
        item.setTitle(title);
        item.setPrice(20.0);
        item.setStockQuantity(stock);
        item.setStatus(ItemStatus.ON_SALE);
        return itemRepository.save(item);
    }

    @Test
    void addingTheSameItemMergesCartQuantity() {
        User buyer = buyer();
        Item item = item("Board game", 5);

        carts.addToCart(buyer, item.getId(), 1);
        carts.addToCart(buyer, item.getId(), 2);

        Cart cart = carts.getOrCreateCart(buyer);
        assertEquals(1, cart.getItems().size());
        assertEquals(3, cart.getItems().getFirst().getQuantity());
        assertEquals(60.0, cart.getTotalPrice());
    }

    @Test
    void checkoutCreatesOrderDecrementsStockAndEmptiesCart() {
        User buyer = buyer();
        Item item = item("Camera", 3);
        carts.addToCart(buyer, item.getId(), 2);

        carts.checkout(buyer, "Buyer", "0912345678", "Taipei");
        entityManager.flush();
        entityManager.clear();

        List<Order> buyerOrders = orders.findByUserOrderByCreatedAtDesc(buyer);
        assertEquals(1, buyerOrders.size());
        assertEquals("PAID", buyerOrders.getFirst().getStatus());
        assertEquals(40.0, buyerOrders.getFirst().getTotalPrice());
        assertEquals(2, buyerOrders.getFirst().getOrderItems().getFirst().getQuantity());
        assertEquals(1, itemRepository.findById(item.getId()).orElseThrow().getStockQuantity());
        assertTrue(carts.getOrCreateCart(buyer).getItems().isEmpty());
    }

    @Test
    void checkoutRejectsInsufficientStockWithoutCreatingOrder() {
        User buyer = buyer();
        Item item = item("Lens", 1);
        carts.addToCart(buyer, item.getId(), 2);

        assertThrows(RuntimeException.class,
                () -> carts.checkout(buyer, "Buyer", "0912345678", "Taipei"));
        assertEquals(1, itemRepository.findById(item.getId()).orElseThrow().getStockQuantity());
        assertTrue(orders.findByUserOrderByCreatedAtDesc(buyer).isEmpty());
    }

    @Test
    void directCheckoutRejectsInsufficientStockWithoutCreatingOrder() {
        User buyer = buyer();
        Item item = item("Rare game", 1);

        assertThrows(RuntimeException.class,
                () -> directCheckout.confirmDirectBuy(item.getId(), 2,
                        "Buyer", "0912345678", "Taipei", buyer::getEmail));
        assertEquals(1, itemRepository.findById(item.getId()).orElseThrow().getStockQuantity());
        assertTrue(orders.findByUserOrderByCreatedAtDesc(buyer).isEmpty());
    }

    @Test
    void searchOnlyReturnsOnSaleItems() {
        Item available = item("Needlecraft kit", 2);
        Item sold = item("Needlecraft book", 0);
        sold.setStatus(ItemStatus.SOLD);
        itemRepository.save(sold);

        List<Item> result = items.searchItems("needlecraft");

        assertEquals(List.of(available.getId()), result.stream().map(Item::getId).toList());
    }

    @Test
    void buyingLastItemMarksItSold() {
        Item item = item("Last copy", 1);

        items.buyItem(item.getId(), 1);

        assertEquals(0, item.getStockQuantity());
        assertEquals(ItemStatus.SOLD, item.getStatus());
    }

    @Test
    void cartRejectsZeroQuantity() {
        User buyer = buyer();
        Item item = item("Puzzle", 5);

        assertThrows(IllegalArgumentException.class,
                () -> carts.addToCart(buyer, item.getId(), 0));
    }
}
