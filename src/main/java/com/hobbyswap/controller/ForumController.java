package com.hobbyswap.controller;

import com.hobbyswap.model.*;
import com.hobbyswap.repository.*;
import com.hobbyswap.service.PostService;
import com.hobbyswap.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;
import com.hobbyswap.model.ForumCategory;

@Controller
@RequestMapping("/forum")
public class ForumController {

    @Autowired private ForumRepository forumRepository;
    @Autowired private ItemRepository itemRepository;
    @Autowired private UserService userService;
    @Autowired private ForumCommentRepository commentRepository;
    @Autowired private PostService postService;

    @GetMapping
    public String list(@RequestParam(required = false) String category,
                       @RequestParam(required = false) String keyword,
                       Model model) {

        List<ForumPost> posts;

        if (keyword != null && !keyword.isEmpty()) {
            posts = postService.searchPosts(keyword);
            model.addAttribute("keyword", keyword);
        }
        else if (category != null && !category.isEmpty()) {
            try {
                ForumCategory catEnum = ForumCategory.valueOf(category);
                posts = postService.getPostsByCategory(catEnum);
                model.addAttribute("currentCategory", catEnum);
            } catch (IllegalArgumentException e) {
                posts = postService.getAllPosts();
            }
        }
        else {
            posts = postService.getAllPosts();
        }

        model.addAttribute("posts", posts);
        model.addAttribute("categories", ForumCategory.values());

        return "forum/list";
    }

    @GetMapping("/new")
    public String newPostForm(Model model, Principal principal) {
        model.addAttribute("post", new ForumPost());
        model.addAttribute("categories", ForumCategory.values());

        User user = userService.findByEmail(principal.getName());
        List<Item> myItems = itemRepository.findBySellerAndStatus(user, "ON_SALE");

        model.addAttribute("myItems", myItems);
        return "forum/form";
    }

    @GetMapping("/{id}")
    public String viewPost(@PathVariable Long id, Model model) {
        ForumPost post = forumRepository.findById(id).orElse(null);
        if (post == null) return "redirect:/forum";

        List<ForumComment> comments = commentRepository.findByPostAndParentIsNullOrderByCreatedAtAsc(post);

        model.addAttribute("post", post);
        model.addAttribute("comments", comments);
        return "forum/detail";
    }

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
        return "redirect:/forum/" + id;
    }

    @PostMapping("/new")
    public String createPost(@ModelAttribute ForumPost post,
                             @RequestParam(required = false) Long sharedItemId,
                             Principal principal) {

        User user = userService.findByEmail(principal.getName());
        post.setAuthor(user);

        if (sharedItemId != null) {
            itemRepository.findById(sharedItemId).ifPresent(post::setSharedItem);
        }

        forumRepository.save(post);
        return "redirect:/forum";
    }

    @PostMapping("/{id}/delete")
    public String deletePost(@PathVariable Long id, Principal principal) {
        ForumPost post = forumRepository.findById(id).orElse(null);

        if (post != null) {
            boolean isAuthor = post.getAuthor().getEmail().equals(principal.getName());
            if (isAuthor) {
                forumRepository.delete(post);
            }
        }
        return "redirect:/forum";
    }

    @PostMapping("/comments/{id}/delete")
    public String deleteComment(@PathVariable Long id, Principal principal) {
        ForumComment comment = commentRepository.findById(id).orElse(null);

        if (comment != null) {
            if (comment.getAuthor().getEmail().equals(principal.getName())) {
                Long postId = comment.getPost().getId();
                commentRepository.delete(comment);
                return "redirect:/forum/" + postId;
            }
        }
        return "redirect:/forum";
    }

    @PostMapping("/comments/{id}/reply")
    public String replyToComment(@PathVariable Long id,
                                 @RequestParam String content,
                                 Principal principal) {

        ForumComment parentComment = commentRepository.findById(id).orElse(null);

        if (parentComment != null && principal != null) {
            User user = userService.findByEmail(principal.getName());

            ForumComment reply = new ForumComment();
            reply.setPost(parentComment.getPost());
            reply.setAuthor(user);
            reply.setContent(content);
            reply.setParent(parentComment);

            commentRepository.save(reply);

            return "redirect:/forum/" + parentComment.getPost().getId();
        }
        return "redirect:/forum";
    }
}