package com.hobbyswap.controller;

import com.hobbyswap.model.Item;
import com.hobbyswap.model.User;
import com.hobbyswap.service.ItemService;
import com.hobbyswap.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
public class ItemController {

    @Autowired private ItemService itemService;
    @Autowired private UserService userService;

    // 首頁：顯示所有上架中的商品
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("items", itemService.findAllOnSale());
        return "index";
    }

    // 商品詳情頁：顯示單一商品資訊
    @GetMapping("/items/{id}")
    public String itemDetail(@PathVariable Long id, Model model) {
        model.addAttribute("item", itemService.findById(id));
        return "item-detail";
    }

    // 刊登商品頁：顯示填寫表單
    @GetMapping("/items/new")
    public String createItemPage(Model model) {
        model.addAttribute("item", new Item());
        return "item-form";
    }

    // 處理刊登動作：接收表單資料並儲存
    @PostMapping("/items")
    public String createItem(@ModelAttribute Item item, Principal principal) {
        User seller = userService.findByEmail(principal.getName());
        itemService.createItem(item, seller);
        return "redirect:/";
    }

    // 處理購買動作：將商品狀態設為已售出
    @PostMapping("/items/{id}/buy")
    public String buyItem(@PathVariable Long id) {
        itemService.buyItem(id);
        return "redirect:/";
    }
}