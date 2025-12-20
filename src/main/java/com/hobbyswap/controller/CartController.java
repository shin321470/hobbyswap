package com.hobbyswap.controller;

import com.hobbyswap.model.Cart;
import com.hobbyswap.model.User;
import com.hobbyswap.service.CartService;
import com.hobbyswap.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired private CartService cartService;
    @Autowired private UserService userService;

    // 1. 查看購物車頁面
    @GetMapping
    public String viewCart(Model model, Principal principal) {
        User user = userService.findByEmail(principal.getName());
        Cart cart = cartService.getOrCreateCart(user);

        model.addAttribute("cart", cart);
        return "cart/view";
    }

    // 2. 加入購物車 (從商品詳情頁來的請求)
    @PostMapping("/add")
    public String addToCart(@RequestParam Long itemId,
                            @RequestParam int quantity,
                            Principal principal) {
        User user = userService.findByEmail(principal.getName());
        cartService.addToCart(user, itemId, quantity);
        return "redirect:/cart"; // 加入成功後跳轉去購物車看
    }

    // 3. 刪除購物車項目
    @PostMapping("/remove/{id}")
    public String removeFromCart(@PathVariable Long id, Principal principal) {
        User user = userService.findByEmail(principal.getName());
        cartService.removeFromCart(user, id);
        return "redirect:/cart";
    }
}