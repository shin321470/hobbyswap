package com.hobbyswap.controller;

import com.hobbyswap.model.Item;
import com.hobbyswap.model.User;
import com.hobbyswap.service.ItemService;
import com.hobbyswap.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Controller
public class ItemController {

    @Autowired private ItemService itemService;
    @Autowired private UserService userService;
    @Autowired private com.hobbyswap.repository.UserRepository userRepository;

    // 首頁 (包含搜尋邏輯)
    @GetMapping("/")
    public String index(@RequestParam(required = false) String keyword, Model model) {
        List<Item> items;

        if (keyword != null && !keyword.isEmpty()) {
            items = itemService.searchItems(keyword);
        } else {
            items = itemService.findAllOnSale();
        }

        model.addAttribute("items", items);
        model.addAttribute("keyword", keyword); // 把關鍵字傳回去，讓搜尋框保留文字
        return "index";
    }

    // 商品詳情
    @GetMapping("/items/{id}")
    public String itemDetail(@PathVariable Long id, Model model, Principal principal) {
        Item item = itemService.findById(id);
        model.addAttribute("item", item);

        boolean isFavorite = false;
        if (principal != null) {
            User user = userRepository.findByEmail(principal.getName()).orElse(null);
            if (user != null) {
                isFavorite = user.getFavoriteItems().contains(item);
            }
        }
        model.addAttribute("isFavorite", isFavorite);

        return "item-detail";
    }

    // 刊登頁面
    @GetMapping("/items/new")
    public String createItemPage(Model model) {
        model.addAttribute("item", new Item());
        return "item-form";
    }

    // 處理刊登 (上傳圖片)
    @PostMapping("/items")
    public String createItem(@ModelAttribute Item item,
                             @RequestParam("imageFile") MultipartFile file,
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

    // 購買
    @PostMapping("/items/{id}/buy")
    public String buyItem(@PathVariable Long id,
                          @RequestParam(defaultValue = "1") int quantity) {

        itemService.buyItem(id, quantity);

        return "redirect:/";
    }

    // 刪除
    @PostMapping("/items/delete/{id}")
    public String deleteItem(@PathVariable Long id, Principal principal) {
        Item item = itemService.findById(id);

        System.out.println("=== 嘗試刪除商品 ===");
        System.out.println("商品 ID: " + id);
        System.out.println("登入者 (Principal): " + principal.getName());
        System.out.println("賣家 (Seller Email): " + item.getSeller().getEmail());

        if (item != null && item.getSeller().getEmail().equals(principal.getName())) {
            itemService.deleteItem(id);
            System.out.println(">>> 刪除成功！");
        } else {
            System.out.println(">>> 刪除失敗：權限不符或商品不存在");
        }

        return "redirect:/mypage";
    }

    // ItemController.java

    @PostMapping("/items/{id}/favorite")
    @ResponseBody
    public org.springframework.http.ResponseEntity<String> toggleFavorite(@PathVariable Long id, Principal principal) {
        if (principal == null) {
            return org.springframework.http.ResponseEntity.status(403).body("Not logged in");
        }

        itemService.toggleFavorite(id, principal.getName());
        return org.springframework.http.ResponseEntity.ok("Success");
    }
}