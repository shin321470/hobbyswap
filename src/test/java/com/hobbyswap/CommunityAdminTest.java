package com.hobbyswap;

import com.hobbyswap.model.ForumPost;
import com.hobbyswap.model.Item;
import com.hobbyswap.model.ItemStatus;
import com.hobbyswap.model.Report;
import com.hobbyswap.model.User;
import com.hobbyswap.repository.ForumCommentRepository;
import com.hobbyswap.repository.ForumRepository;
import com.hobbyswap.repository.ItemRepository;
import com.hobbyswap.repository.ReportRepository;
import com.hobbyswap.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CommunityAdminTest {
    @Autowired MockMvc mvc;
    @Autowired ForumRepository posts;
    @Autowired ForumCommentRepository comments;
    @Autowired UserRepository users;
    @Autowired ItemRepository items;
    @Autowired ReportRepository reports;

    @Test
    void signedInUserCanPostAndComment() throws Exception {
        String title = "Game night " + UUID.randomUUID();
        mvc.perform(post("/forum/new").with(csrf()).with(user("alice@test.com"))
                        .param("title", title)
                        .param("content", "Looking for players"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/forum"));

        ForumPost post = posts.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(title, title).getFirst();
        assertEquals("alice@test.com", post.getAuthor().getEmail());

        mvc.perform(post("/forum/{id}/comment", post.getId()).with(csrf()).with(user("bob@test.com"))
                        .param("content", "Count me in"))
                .andExpect(status().is3xxRedirection());

        var savedComments = comments.findByPostAndParentIsNullOrderByCreatedAtAsc(post);
        assertEquals(1, savedComments.size());
        assertEquals("bob@test.com", savedComments.getFirst().getAuthor().getEmail());
    }

    @Test
    void onlyAuthorCanDeleteForumPost() throws Exception {
        User alice = users.findByEmail("alice@test.com").orElseThrow();
        ForumPost post = new ForumPost();
        post.setTitle("Ownership");
        post.setContent("Test post");
        post.setAuthor(alice);
        post = posts.save(post);

        mvc.perform(post("/forum/{id}/delete", post.getId()).with(csrf()).with(user("bob@test.com")))
                .andExpect(status().is3xxRedirection());
        assertTrue(posts.existsById(post.getId()));

        mvc.perform(post("/forum/{id}/delete", post.getId()).with(csrf()).with(user("alice@test.com")))
                .andExpect(status().is3xxRedirection());
        assertFalse(posts.existsById(post.getId()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanBanUserAndResolveItemReport() throws Exception {
        User bob = users.findByEmail("bob@test.com").orElseThrow();
        Item item = items.findAll().getFirst();
        Report report = new Report();
        report.setItem(item);
        report.setReporter(bob);
        report.setReason("Prohibited listing");
        report = reports.save(report);

        mvc.perform(post("/admin/users/{id}/toggle-ban", bob.getId()).with(csrf()))
                .andExpect(status().is3xxRedirection());
        assertFalse(users.findById(bob.getId()).orElseThrow().isEnabled());

        mvc.perform(post("/admin/reports/{id}/handle", report.getId()).with(csrf())
                        .param("action", "BAN"))
                .andExpect(status().is3xxRedirection());
        assertEquals("RESOLVED", reports.findById(report.getId()).orElseThrow().getStatus());
        assertEquals(ItemStatus.BANNED, items.findById(item.getId()).orElseThrow().getStatus());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanDismissReportWithoutBanningItem() throws Exception {
        Item item = items.findAll().getFirst();
        Report report = new Report();
        report.setItem(item);
        report.setReason("Review requested");
        report = reports.save(report);

        mvc.perform(post("/admin/reports/{id}/handle", report.getId()).with(csrf())
                        .param("action", "DISMISS"))
                .andExpect(status().is3xxRedirection());

        assertEquals("DISMISSED", reports.findById(report.getId()).orElseThrow().getStatus());
        assertEquals(ItemStatus.ON_SALE, items.findById(item.getId()).orElseThrow().getStatus());
    }
}
