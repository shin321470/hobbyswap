package com.hobbyswap.controller;

import com.hobbyswap.model.*;
import com.hobbyswap.repository.ItemRepository;
import com.hobbyswap.repository.OrderRepository;
import com.hobbyswap.service.CartService;
import com.hobbyswap.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class CheckoutController {

    @Autowired private CartService cartService;
    @Autowired private UserService userService;
    @Autowired private ItemRepository itemRepository;
    @Autowired private OrderRepository orderRepository;

    @GetMapping("/checkout")
    public String checkoutPage(Model model, Principal principal) {
        User user = userService.findByEmail(principal.getName());
        Cart cart = cartService.getOrCreateCart(user);

        if (cart == null || cart.getItems().isEmpty()) {
            return "redirect:/cart";
        }

        model.addAttribute("cart", cart);
        model.addAttribute("user", user);
        return "checkout";
    }

    @PostMapping("/checkout/confirm")
    public String confirmOrder(@RequestParam String receiverName,
                               @RequestParam String phone,
                               @RequestParam String address,
                               Principal principal) {

        User user = userService.findByEmail(principal.getName());
        cartService.checkout(user, receiverName, phone, address);
        return "redirect:/mypage";
    }

    @PostMapping("/checkout/direct")
    public String directCheckoutPage(@RequestParam Long itemId,
                                     @RequestParam int quantity,
                                     Model model,
                                     Principal principal) {
        User user = userService.findByEmail(principal.getName());

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        Cart virtualCart = new Cart();
        virtualCart.setUser(user);

        CartItem virtualItem = new CartItem();
        virtualItem.setItem(item);
        virtualItem.setQuantity(quantity);

        List<CartItem> items = new ArrayList<>();
        items.add(virtualItem);
        virtualCart.setItems(items);

        model.addAttribute("cart", virtualCart);
        model.addAttribute("user", user);

        model.addAttribute("isDirectBuy", true);
        model.addAttribute("directItemId", itemId);
        model.addAttribute("directQuantity", quantity);

        return "checkout";
    }

    @PostMapping("/checkout/direct/confirm")
    @Transactional
    public String confirmDirectBuy(@RequestParam Long itemId,
                                   @RequestParam int quantity,
                                   @RequestParam String receiverName,
                                   @RequestParam String phone,
                                   @RequestParam String address,
                                   Principal principal) {
        User user = userService.findByEmail(principal.getName());
        Item item = itemRepository.findById(itemId).orElseThrow();

        if (item.getStockQuantity() < quantity) {
            throw new RuntimeException("庫存不足");
        }
        item.setStockQuantity(item.getStockQuantity() - quantity);
        if (item.getStockQuantity() == 0) {
            item.setStatus("SOLD");
        }
        itemRepository.save(item);

        Order order = new Order();
        order.setUser(user);
        order.setReceiverName(receiverName);
        order.setPhone(phone);
        order.setAddress(address);
        order.setTotalPrice(item.getPrice() * quantity);
        order.setStatus("PAID");

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setItem(item);
        orderItem.setQuantity(quantity);
        orderItem.setPrice(item.getPrice());

        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(orderItem);
        order.setOrderItems(orderItems);

        orderRepository.save(order);

        return "redirect:/mypage";
    }
}