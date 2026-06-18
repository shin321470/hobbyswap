package com.hobbyswap.config;

import com.hobbyswap.model.Item;
import com.hobbyswap.model.ItemStatus;
import com.hobbyswap.model.Role;
import com.hobbyswap.model.User;
import com.hobbyswap.repository.ItemRepository;
import com.hobbyswap.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

/**
 * 啟動時建立預設資料 (管理員、示範使用者、示範商品)。
 *
 * <p>原本 admin 帳號在兩個地方各被建立一次，這裡整併為單一進入點 {@link #run},
 * 並把建立邏輯拆成清楚、可重用的私有方法。所有建立都先檢查是否已存在，可重複安全執行。</p>
 */
@Configuration
public class DataSeeder implements CommandLineRunner {

    @Autowired private UserRepository userRepository;
    @Autowired private ItemRepository itemRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedAdmin();
        seedDemoUsersAndItems();
    }

    /** 建立管理員帳號 (若尚未存在)。 */
    private void seedAdmin() {
        if (userRepository.findByEmail("admin@hobbyswap.com").isEmpty()) {
            User admin = new User();
            admin.setName("Super Admin");
            admin.setEmail("admin@hobbyswap.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ROLE_ADMIN);
            admin.setEnabled(true);
            userRepository.save(admin);
            System.out.println("✅ 管理員帳號已建立: admin@hobbyswap.com / admin123");
        }
    }

    /** 建立示範用的一般使用者與商品 (商品為空時才塞)。 */
    private void seedDemoUsersAndItems() {
        User alice = findOrCreateUser("alice@test.com", "Alice", "password");
        User bob = findOrCreateUser("bob@test.com", "Bob", "password");

        if (itemRepository.count() == 0) {
            itemRepository.save(buildItem("二手 PS5 (九成新)", 12000.0,
                    "買來沒時間玩，便宜賣。附原廠手把。", alice));
            itemRepository.save(buildItem("哈利波特全套小說", 1500.0,
                    "書況良好，無任何劃記，適合收藏。", bob));
            System.out.println("✅ 已塞入示範商品資料");
        }
    }

    /** 依 Email 找使用者，找不到就建立一個新的一般使用者。 */
    private User findOrCreateUser(String email, String name, String rawPassword) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User user = new User();
            user.setEmail(email);
            user.setName(name);
            user.setPassword(passwordEncoder.encode(rawPassword));
            return userRepository.save(user);
        });
    }

    /** 組裝一個上架中的商品。 */
    private Item buildItem(String title, double price, String description, User seller) {
        Item item = new Item();
        item.setTitle(title);
        item.setPrice(price);
        item.setDescription(description);
        item.setStatus(ItemStatus.ON_SALE);
        item.setSeller(seller);
        item.setUploadDate(LocalDateTime.now());
        return item;
    }
}
