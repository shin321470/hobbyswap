package com.hobbyswap.controller;

import com.hobbyswap.model.Item;
import com.hobbyswap.model.User;
import com.hobbyswap.service.ItemService;
import com.hobbyswap.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile; // 處理上傳檔案
import org.springframework.web.bind.annotation.RequestParam; // 接收請求參數

import java.security.Principal;
import java.io.IOException;     // 處理 IO 錯誤
import java.nio.file.Files;     // 檔案操作工具
import java.nio.file.Path;      //路徑物件
import java.nio.file.Paths;     // 路徑工具
import java.util.UUID;          // 產生唯一亂碼

@Controller
public class ItemController {

    @Autowired private ItemService itemService;
    @Autowired private UserService userService;

    // 首頁
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("items", itemService.findAllOnSale());
        return "index";
    }

    // 商品詳情
    @GetMapping("/items/{id}")
    public String itemDetail(@PathVariable Long id, Model model) {
        model.addAttribute("item", itemService.findById(id));
        return "item-detail";
    }

    // 刊登頁面
    @GetMapping("/items/new")
    public String createItemPage(Model model) {
        model.addAttribute("item", new Item());
        return "item-form";
    }

    // ▼▼▼ 請檢查這裡！只保留這一個「新的」方法，舊的請刪除 ▼▼▼
    @PostMapping("/items")
    public String createItem(@ModelAttribute Item item,
                             @RequestParam("imageFile") MultipartFile file, // 接收圖片
                             Principal principal) throws IOException {

        // 1. 處理圖片上傳
        if (!file.isEmpty()) {
            String folder = "uploads/";
            Path path = Paths.get(folder);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Files.copy(file.getInputStream(), path.resolve(fileName));
            item.setImageName(fileName);
        }

        // 2. 存入資料庫
        User seller = userService.findByEmail(principal.getName());
        itemService.createItem(item, seller);

        return "redirect:/";
    }
    // ▲▲▲ 結束 ▲▲▲

    // 購買
    @PostMapping("/items/{id}/buy")
    public String buyItem(@PathVariable Long id) {
        itemService.buyItem(id);
        return "redirect:/";
    }

    @PostMapping("/items/delete/{id}")
    public String deleteItem(@PathVariable Long id, Principal principal) {

        Item item = itemService.findById(id);
        if (item != null && item.getSeller().getEmail().equals(principal.getName())) {
            itemService.deleteItem(id);
        }
        return "redirect:/mypage";
    }
}