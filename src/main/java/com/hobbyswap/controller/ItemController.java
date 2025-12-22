package com.hobbyswap.controller;

import com.hobbyswap.model.Item;
import com.hobbyswap.model.Report;
import com.hobbyswap.model.User;
import com.hobbyswap.repository.ReportRepository;
import com.hobbyswap.service.CartService;
import com.hobbyswap.service.ItemService;
import com.hobbyswap.service.UserService;
import com.hobbyswap.model.Review;
import com.hobbyswap.repository.ReviewRepository;
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
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class ItemController {

    @Autowired private ItemService itemService;
    @Autowired private UserService userService;
    @Autowired private CartService cartService;
    @Autowired private com.hobbyswap.repository.UserRepository userRepository;
    @Autowired private ReviewRepository reviewRepository;
    @Autowired private ReportRepository reportRepository;

    @GetMapping("/")
    public String index(@RequestParam(required = false) String keyword, Model model) {
        List<Item> items;

        if (keyword != null && !keyword.isEmpty()) {
            items = itemService.searchItems(keyword);
        } else {
            items = itemService.findAllOnSale();
        }

        model.addAttribute("items", items);
        model.addAttribute("keyword", keyword);
        return "index";
    }

    @GetMapping("/items/{id}")
    public String itemDetail(@PathVariable Long id, Model model) {
        Item item = itemService.findById(id);
        model.addAttribute("item", item);

        List<Review> reviews = reviewRepository.findByItemId(id);
        model.addAttribute("reviews", reviews);

        return "item-detail";
    }

    @PostMapping("/items/{id}/review")
    public String addReview(@PathVariable Long id,
                            @RequestParam("content") String content,
                            @RequestParam("rating") int rating,
                            Principal principal) {

        Item item = itemService.findById(id);
        User user = userService.findByEmail(principal.getName());

        Review review = new Review();
        review.setContent(content);
        review.setRating(rating);
        review.setUser(user);
        review.setItem(item);
        review.setCreatedAt(LocalDateTime.now());

        reviewRepository.save(review);

        return "redirect:/items/" + id;
    }

    @GetMapping("/items/new")
    public String createItemPage(Model model) {
        model.addAttribute("item", new Item());
        return "item-form";
    }

    @PostMapping("/items")
    public String createItem(@ModelAttribute Item item,
                             @RequestParam("imageFile") MultipartFile file,
                             Principal principal) throws IOException {

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

    @PostMapping("/items/{id}/buy")
    public String buyNow(@PathVariable Long id,
                         @RequestParam int quantity,
                         Principal principal) {

        User user = userService.findByEmail(principal.getName());

        cartService.addToCart(user, id, quantity);

        return "redirect:/checkout";
    }

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

    @PostMapping("/items/{id}/favorite")
    @ResponseBody
    public org.springframework.http.ResponseEntity<String> toggleFavorite(@PathVariable Long id, Principal principal) {
        if (principal == null) {
            return org.springframework.http.ResponseEntity.status(403).body("Not logged in");
        }

        itemService.toggleFavorite(id, principal.getName());
        return org.springframework.http.ResponseEntity.ok("Success");
    }

    @PostMapping("/items/reviews/{id}/delete")
    public String deleteReview(@PathVariable Long id, Principal principal) {
        // 1. 找出評論
        Review review = reviewRepository.findById(id).orElse(null);

        if (review != null) {
            if (review.getUser().getEmail().equals(principal.getName())) {
                Long itemId = review.getItem().getId();
                reviewRepository.delete(review);
                return "redirect:/items/" + itemId;
            }
        }
        return "redirect:/";
    }

    @PostMapping("/items/{id}/report")
    public String reportItem(@PathVariable Long id,
                             @RequestParam String reason,
                             Principal principal) {
        Item item = itemService.findById(id);
        if (item != null && principal != null) {
            User reporter = userService.findByEmail(principal.getName());

            Report report = new Report();
            report.setItem(item);
            report.setReporter(reporter);
            report.setReason(reason);
            reportRepository.save(report);
        }
        return "redirect:/items/" + id + "?reported=true";
    }

    @GetMapping("/login-check")
    public String loginCheck(@RequestParam String target) {
        return "redirect:" + target;
    }
}