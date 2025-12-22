package com.hobbyswap.service;

import com.hobbyswap.model.ForumCategory;
import com.hobbyswap.model.ForumPost;
import com.hobbyswap.repository.ForumRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {

    @Autowired
    private ForumRepository forumRepository;

    public List<ForumPost> getAllPosts() {
        return forumRepository.findAll();
    }

    public List<ForumPost> searchPosts(String keyword) {
        return forumRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(keyword, keyword);
    }

    public List<ForumPost> getPostsByCategory(ForumCategory category) {
        return forumRepository.findByCategory(category);
    }
}
