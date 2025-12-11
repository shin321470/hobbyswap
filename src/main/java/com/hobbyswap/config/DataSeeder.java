package com.hobbyswap.config;

import com.hobbyswap.model.Item;
import com.hobbyswap.model.User;
import com.hobbyswap.repository.ItemRepository;
import com.hobbyswap.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner demoData(UserRepository userRepo, ItemRepository itemRepo, PasswordEncoder encoder) {
        return args -> {
            if (userRepo.count() == 0 || itemRepo.count() == 0) {

                // 先嘗試找 Alice，找不到才建立
                User alice = userRepo.findByEmail("alice@test.com").orElse(null);
                if (alice == null) {
                    alice = new User();
                    alice.setEmail("alice@test.com");
                    alice.setName("Alice");
                    alice.setPassword(encoder.encode("password"));
                    userRepo.save(alice);
                }

                // 先嘗試找 Bob，找不到才建立
                User bob = userRepo.findByEmail("bob@test.com").orElse(null);
                if (bob == null) {
                    bob = new User();
                    bob.setEmail("bob@test.com");
                    bob.setName("Bob");
                    bob.setPassword(encoder.encode("password"));
                    userRepo.save(bob);
                }

                // 檢查商品是否為空，如果是空的就塞入商品
                if (itemRepo.count() == 0) {
                    Item item1 = new Item();
                    item1.setTitle("二手 PS5 (九成新)");
                    item1.setPrice(12000.0);
                    item1.setDescription("買來沒時間玩，便宜賣。附原廠手把。");
                    item1.setStatus("ON_SALE");
                    item1.setSeller(alice);
                    item1.setUploadDate(LocalDateTime.now());
                    itemRepo.save(item1);

                    Item item2 = new Item();
                    item2.setTitle("哈利波特全套小說");
                    item2.setPrice(1500.0);
                    item2.setDescription("書況良好，無任何劃記，適合收藏。");
                    item2.setStatus("ON_SALE");
                    item2.setSeller(bob);
                    item2.setUploadDate(LocalDateTime.now());
                    itemRepo.save(item2);

                    System.out.println("成功塞入假商品資料！");
                }
            }
        };
    }
}