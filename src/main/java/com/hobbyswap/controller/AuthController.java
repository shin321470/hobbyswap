package com.hobbyswap.controller;

import com.hobbyswap.model.User;
import com.hobbyswap.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    @Autowired private UserService userService;

    // 顯示登入頁面
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    // 顯示註冊頁面
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    // 處理註冊表單提交
    @PostMapping("/register")
    public String registerProcess(@Valid @ModelAttribute("user") User user, BindingResult result) {
        // 驗證未通過 (例如 Email 格式錯、密碼太短) -> 回到註冊頁顯示錯誤
        if (result.hasErrors()) {
            return "register";
        }
        userService.register(user);
        return "redirect:/login"; // 註冊成功後跳轉至登入頁
    }
}