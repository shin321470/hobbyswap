package com.hobbyswap.controller;

import com.hobbyswap.model.User;
import com.hobbyswap.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class PageController {

    @Autowired
    private UserService userService;

    @GetMapping("/mypage")
    public String myPage(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        User user = userService.findByEmail(principal.getName());

        model.addAttribute("user", user);

        int count = (user.getItems() != null) ? user.getItems().size() : 0;
        model.addAttribute("itemCount", count);

        return "mypage";
    }
}