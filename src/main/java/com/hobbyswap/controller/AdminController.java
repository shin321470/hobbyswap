package com.hobbyswap.controller;

import com.hobbyswap.model.Item;
import com.hobbyswap.model.User;
import com.hobbyswap.service.ItemService;
import com.hobbyswap.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin") // 所有網址都以 /admin 開頭
public class AdminController {

    @Autowired private UserService userService;
    @Autowired private ItemService itemService;

    // 1. 後台儀表板 (顯示所有使用者與商品)
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("users", userService.findAllUsers());
        model.addAttribute("items", itemService.findAllOnSale()); // 或是您可以寫一個 findAll() 包含已售出的
        return "admin/dashboard";
    }

    // 2. 封鎖/解鎖使用者
    @PostMapping("/users/{id}/toggle-ban")
    public String toggleBanUser(@PathVariable Long id) {
        System.out.println("🔥 [DEBUG] 收到封鎖/解鎖請求，目標 ID: " + id);

        User user = userService.findById(id);

        if (user != null) {
            System.out.println("🔄 [DEBUG] 修改前狀態 (Enabled): " + user.isEnabled());

            // 执行切换
            user.setEnabled(!user.isEnabled());

            // 保存
            userService.save(user);

            System.out.println("✅ [DEBUG] 修改後狀態 (Enabled): " + user.isEnabled() + " (已執行 save)");
        } else {
            System.out.println("❌ [DEBUG] 找不到使用者，ID: " + id);
        }

        return "redirect:/admin/dashboard";
    }

    // 3. 強制下架商品
    @PostMapping("/items/{id}/delete")
    public String deleteItem(@PathVariable Long id) {
        Item item = itemService.findById(id);
        if (item != null) {
            // 我們不真的刪除，而是把狀態改成 BANNED 或 SOLD，這裡示範改成 "BANNED"
            // 請確認您的 Item 狀態欄位是 String 還是 Enum
            item.setStatus("BANNED");
            itemService.save(item);
        }
        return "redirect:/admin/dashboard";
    }

    // 管理員登入頁面
    @GetMapping("/login")
    public String adminLoginPage() {
        return "admin/login"; // 對應 templates/admin/login.html
    }
}