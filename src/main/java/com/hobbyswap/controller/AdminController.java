package com.hobbyswap.controller;

import com.hobbyswap.model.Item;
import com.hobbyswap.model.ItemStatus;
import com.hobbyswap.model.Report;
import com.hobbyswap.model.User;
import com.hobbyswap.repository.ReportRepository;
import com.hobbyswap.service.ItemService;
import com.hobbyswap.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin") // 所有網址都以 /admin 開頭
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    @Autowired private UserService userService;
    @Autowired private ItemService itemService;
    @Autowired private ReportRepository reportRepository;

    // 1. 後台儀表板 (顯示所有使用者與商品)
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("users", userService.findAllUsers());
        model.addAttribute("items", itemService.findAllOnSale());

        // 撈出待處理檢舉
        List<Report> pendingReports = reportRepository.findByStatusOrderByCreatedAtDesc("PENDING");
        model.addAttribute("reports", pendingReports);
        return "admin/dashboard";
    }

    // 2. 封鎖/解鎖使用者
    @PostMapping("/users/{id}/toggle-ban")
    public String toggleBanUser(@PathVariable Long id) {
        User user = userService.findById(id);

        if (user != null) {
            user.setEnabled(!user.isEnabled());   // 切換封鎖/解鎖
            userService.save(user);
            log.debug("已切換使用者 {} 的啟用狀態為 {}", id, user.isEnabled());
        } else {
            log.warn("找不到要封鎖/解鎖的使用者，ID: {}", id);
        }

        return "redirect:/admin/dashboard";
    }

    // 3. 強制下架商品
    @PostMapping("/items/{id}/delete")
    public String deleteItem(@PathVariable Long id) {
        Item item = itemService.findById(id);
        if (item != null) {
            // 不真的刪除，而是把狀態改成 BANNED (強制下架)
            item.setStatus(ItemStatus.BANNED);
            itemService.save(item);
        }
        return "redirect:/admin/dashboard";
    }

    // 管理員登入頁面
    @GetMapping("/login")
    public String adminLoginPage() {
        return "admin/login"; // 對應 templates/admin/login.html
    }

    // 處理檢舉
    @PostMapping("/reports/{id}/handle")
    public String handleReport(@PathVariable Long id, @RequestParam String action) {
        Report report = reportRepository.findById(id).orElse(null);

        if (report != null) {
            if ("BAN".equals(action)) {
                // 如果決定封鎖：
                // 1. 把商品狀態改成 BANNED
                Item item = report.getItem();
                item.setStatus(ItemStatus.BANNED);
                itemService.save(item);

                // 2. 檢舉結案 (已處理)
                report.setStatus("RESOLVED");
            } else if ("DISMISS".equals(action)) {
                // 如果覺得沒問題，直接駁回檢舉
                report.setStatus("DISMISSED");
            }
            reportRepository.save(report);
        }
        return "redirect:/admin/dashboard";
    }

}