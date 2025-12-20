package com.hobbyswap.controller;

import com.hobbyswap.model.*;
import com.hobbyswap.repository.*;
import com.hobbyswap.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/forum")
public class ForumController {

    @Autowired private ForumRepository forumRepository;
    @Autowired private ItemRepository itemRepository; // 需要用來找使用者的商品
    @Autowired private UserService userService;
    @Autowired private ForumCommentRepository commentRepository;

    // 論壇首頁
    @GetMapping
    public String list(@RequestParam(required = false) ForumCategory category, Model model) {
        List<ForumPost> posts;
        if (category != null) {
            posts = forumRepository.findByCategoryOrderByCreatedAtDesc(category);
            model.addAttribute("currentCategory", category);
        } else {
            posts = forumRepository.findAllByOrderByCreatedAtDesc();
        }

        model.addAttribute("posts", posts);
        model.addAttribute("categories", ForumCategory.values()); // 傳入所有分類給前端做 Tabs
        return "forum/list";
    }

    // 發表新文章頁面
    @GetMapping("/new")
    public String newPostForm(Model model, Principal principal) {
        model.addAttribute("post", new ForumPost());
        model.addAttribute("categories", ForumCategory.values());

        // 撈出「當前使用者」正在販售的商品 (讓他在發文時可以選擇)
        User user = userService.findByEmail(principal.getName());
        List<Item> myItems = itemRepository.findBySellerAndStatus(user, "ON_SALE");
        // 註：如果您的 ItemRepository 沒這個方法，可以用 findBySeller 代替，或在 Repo 加一下

        model.addAttribute("myItems", myItems);
        return "forum/form";
    }

    @GetMapping("/{id}")
    public String viewPost(@PathVariable Long id, Model model) {
        ForumPost post = forumRepository.findById(id).orElse(null);
        if (post == null) return "redirect:/forum";

        // 撈出這篇文章的所有評論
        List<ForumComment> comments = commentRepository.findByPostOrderByCreatedAtAsc(post);

        model.addAttribute("post", post);
        model.addAttribute("comments", comments);
        return "forum/detail"; // 我們等一下要建立這個頁面
    }

    // 提交評論
    @PostMapping("/{id}/comment")
    public String addComment(@PathVariable Long id,
                             @RequestParam String content,
                             Principal principal) {

        ForumPost post = forumRepository.findById(id).orElse(null);
        if (post != null && principal != null) {
            User user = userService.findByEmail(principal.getName());

            ForumComment comment = new ForumComment();
            comment.setPost(post);
            comment.setAuthor(user);
            comment.setContent(content);

            commentRepository.save(comment);
        }
        return "redirect:/forum/" + id; // 留言後重新整理該頁面
    }

    // 處理提交
    @PostMapping("/new")
    public String createPost(@ModelAttribute ForumPost post,
                             @RequestParam(required = false) Long sharedItemId,
                             Principal principal) {

        User user = userService.findByEmail(principal.getName());
        post.setAuthor(user);

        // 如果有選擇分享商品
        if (sharedItemId != null) {
            itemRepository.findById(sharedItemId).ifPresent(post::setSharedItem);
        }

        forumRepository.save(post);
        return "redirect:/forum";
    }

    // 刪除文章功能
    @PostMapping("/{id}/delete")
    public String deletePost(@PathVariable Long id, Principal principal) {
        ForumPost post = forumRepository.findById(id).orElse(null);

        if (post != null) {
            // 權限檢查：只有「發文者本人」或「管理員」可以刪除
            // principal.getName() 抓到的是登入者的 email
            boolean isAuthor = post.getAuthor().getEmail().equals(principal.getName());

            // 這裡假設我們簡單判斷：如果是本人就可以刪
            // (如果您希望管理員也能刪，可以多判斷 User role)
            if (isAuthor) {
                forumRepository.delete(post); // 因為設了 Cascade，評論也會一起消失
            }
        }
        return "redirect:/forum";
    }

    @PostMapping("/comments/{id}/delete")
    public String deleteComment(@PathVariable Long id, Principal principal) {
        // 1. 找出評論
        ForumComment comment = commentRepository.findById(id).orElse(null);

        if (comment != null) {
            // 2. 權限檢查：只有「留言者本人」可以刪除
            if (comment.getAuthor().getEmail().equals(principal.getName())) {
                Long postId = comment.getPost().getId(); // 記住這篇文的 ID，等一下要跳轉回去
                commentRepository.delete(comment);
                return "redirect:/forum/" + postId; // 刪除後回到該文章頁面
            }
        }
        return "redirect:/forum";
    }
}