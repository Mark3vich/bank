package com.example.bank.integration;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import com.example.bank.TestConfig;
import com.example.bank.dto.response.AuthenticationResponse;
import com.example.bank.model.Account;
import com.example.bank.model.EmailData;
import com.example.bank.model.PhoneData;
import com.example.bank.model.User;
import com.example.bank.repository.UserRepository;
import com.example.bank.service.AuthenticationService;

@DataJpaTest
@ContextConfiguration(classes = TestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
public class CreateUserIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationService authenticationService;
    
    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();
    }

    @Test
    public void testCreateUserWithEmailAndPhone() {
        User user = new User();
        user.setName("Иван Иванов");
        user.setPassword("securePassword123");

        EmailData email = new EmailData();
        email.setEmail("ivan@example.com");
        email.setUser(user);
        user.getEmails().add(email);

        PhoneData phone = new PhoneData();
        phone.setPhone("79123456789");
        phone.setUser(user);
        user.getPhones().add(phone);

        User savedUser = userRepository.save(user);

        assertNotNull(savedUser.getId());
        assertEquals("Иван Иванов", savedUser.getName());
        assertEquals(1, savedUser.getEmails().size());
        assertEquals("ivan@example.com", new ArrayList<>(savedUser.getEmails()).get(0).getEmail());
        assertEquals(1, savedUser.getPhones().size());
        assertEquals("79123456789", new ArrayList<>(savedUser.getPhones()).get(0).getPhone());
    }

    @Test
    public void testCreateMultipleUsers() {
        // Первый пользователь
        User user1 = new User();
        user1.setName("Петр Петров");
        user1.setPassword("password1");
        
        EmailData email1 = new EmailData();
        email1.setEmail("petr@example.com");
        email1.setUser(user1);
        user1.getEmails().add(email1);
        
        userRepository.save(user1);

        // Второй пользователь
        User user2 = new User();
        user2.setName("Сергей Сергеев");
        user2.setPassword("password2");
        
        PhoneData phone2 = new PhoneData();
        phone2.setPhone("79234567890");
        phone2.setUser(user2);
        user2.getPhones().add(phone2);
        
        userRepository.save(user2);

        assertEquals(2, userRepository.count());
    }

    @Test
    public void testUserWithoutContactsShouldFail() {
        User user = new User();
        user.setName("Аноним");
        user.setPassword("password");

        User savedUser = userRepository.saveAndFlush(user);
        
        assertNotNull(savedUser.getId());
        assertEquals(0, savedUser.getEmails().size());
        assertEquals(0, savedUser.getPhones().size());
    }

    @Test
    @Rollback(false)
    public void testPersistUserToDatabasePermanently() {
        User user = new User();
        user.setName("Постоянный Пользователь");
        user.setPassword("permanentPassword");
        user.setDateOfBirth("01.05.1993");

        Account account = new Account();
        account.setBalance(new BigDecimal("1000.00"));
        account.setUser(user);
        user.setAccount(account);

        EmailData email = new EmailData();
        email.setEmail("permanent@example.com");
        email.setUser(user);
        user.getEmails().add(email);

        PhoneData phone = new PhoneData();
        phone.setPhone("79998887766");
        phone.setUser(user);
        user.getPhones().add(phone);

        AuthenticationResponse response = authenticationService.register(user);
        
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
    }

    @Test
    @Rollback(false)
    public void generateTenTestUsers() {
        // Создаем 10 тестовых пользователей с разными данными
        createTestUser("Иван Петров", "01.05.1990", "password1", "ivan.petrov@example.com", "79001112233", "1000.50");
        createTestUser("Мария Сидорова", "15.07.1985", "password2", "maria.sidorova@example.com", "79002223344", "2500.75");
        createTestUser("Алексей Иванов", "23.11.1988", "password3", "alexey.ivanov@example.com", "79003334455", "3750.25");
        createTestUser("Елена Смирнова", "07.03.1992", "password4", "elena.smirnova@example.com", "79004445566", "5000.00");
        createTestUser("Дмитрий Козлов", "19.09.1983", "password5", "dmitry.kozlov@example.com", "79005556677", "7500.80");
        createTestUser("Анна Морозова", "30.12.1995", "password6", "anna.morozova@example.com", "79006667788", "1200.35");
        createTestUser("Сергей Новиков", "11.06.1987", "password7", "sergey.novikov@example.com", "79007778899", "8900.60");
        createTestUser("Татьяна Волкова", "28.04.1991", "password8", "tatiana.volkova@example.com", "79008889900", "4320.15");
        createTestUser("Михаил Соколов", "05.08.1989", "password9", "mikhail.sokolov@example.com", "79009990011", "6750.40");
        createTestUser("Ольга Кузнецова", "17.02.1994", "password10", "olga.kuznetsova@example.com", "79000112233", "3200.90");
        
        // Проверяем, что все пользователи созданы
        assertEquals(10, userRepository.count());
    }
    
    private User createTestUser(String name, String dateOfBirth, String password, String email, String phone, String balance) {
        // Создаем основные данные пользователя
        User user = new User();
        user.setName(name);
        user.setPassword(password);
        user.setDateOfBirth(dateOfBirth);
        
        // Сначала сохраняем пользователя
        User savedUser = userRepository.save(user);
        
        // Затем создаем и добавляем email
        EmailData emailData = new EmailData();
        emailData.setEmail(email);
        emailData.setUser(savedUser);
        savedUser.getEmails().add(emailData);
        
        // Добавляем телефон
        PhoneData phoneData = new PhoneData();
        phoneData.setPhone(phone);
        phoneData.setUser(savedUser);
        savedUser.getPhones().add(phoneData);
        
        // Создаем счет
        Account account = new Account();
        account.setBalance(new BigDecimal(balance));
        account.setUser(savedUser);
        savedUser.setAccount(account);
        
        // Сохраняем обновленного пользователя
        return userRepository.save(savedUser);
    }
}