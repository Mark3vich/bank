package com.example.bank.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import com.example.bank.TestConfig;
import com.example.bank.dto.request.TransferRequest;
import com.example.bank.exception.AccountNotFoundException;
import com.example.bank.exception.InsufficientFundsException;
import com.example.bank.exception.SelfTransferException;
import com.example.bank.model.Account;
import com.example.bank.model.EmailData;
import com.example.bank.model.PhoneData;
import com.example.bank.model.User;
import com.example.bank.repository.AccountRepository;
import com.example.bank.repository.UserRepository;
import com.example.bank.service.AuthenticationService;
import com.example.bank.service.JwtService;
import com.example.bank.service.TransferService;
import com.example.bank.service.impl.TransactionLogServiceImpl;
import com.example.bank.service.impl.TransferServiceImpl;
import com.example.bank.service.impl.UserEventPublisher;

@DataJpaTest
@ContextConfiguration(classes = TestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Import({
    TransactionLogServiceImpl.class, 
    TransferServiceImpl.class, 
    JwtService.class,
    UserEventPublisher.class
})
public class TransferIntegrationTest {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private TransferService transferService;
    
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private AuthenticationService authenticationService;
    
    @Autowired
    private UserEventPublisher userEventPublisher;
    
    private User sender;
    private User recipient;
    private String jwtToken;
    
    // Generate unique identifiers for test data
    private String generateUniquePhone() {
        // Generate a unique phone number using timestamp and random UUID
        return "7900" + UUID.randomUUID().toString().replaceAll("-", "").substring(0, 7);
    }
    
    private String generateUniqueEmail(String prefix) {
        // Generate a unique email using timestamp and random UUID
        return prefix + "." + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
    }
    
    @BeforeEach
    public void setUp() {
        // Delete all existing data before each test
        userRepository.deleteAll();
        
        // Создаем отправителя с уникальными данными
        sender = createUser("John Sender", "01.05.1990", "password1", 
                generateUniqueEmail("sender"), generateUniquePhone(), "1000.00");
        
        // Создаем получателя с уникальными данными
        recipient = createUser("Jane Recipient", "15.07.1985", "password2", 
                generateUniqueEmail("recipient"), generateUniquePhone(), "500.00");
        
        // Генерируем JWT токен для отправителя
        jwtToken = jwtService.generateAccessToken(sender);
    }
    
    @Test
    @Rollback(true) // Enable rollback to clean up after the test
    public void testSuccessfulTransfer() {
        // Подготавливаем запрос на перевод
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setRecipientId(recipient.getId());
        transferRequest.setAmount(new BigDecimal("100.00"));
        
        // Подготавливаем HTTP запрос с токеном
        MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        httpRequest.addHeader("Authorization", "Bearer " + jwtToken);
        
        // Выполняем перевод
        transferService.transferMoney(transferRequest, httpRequest);
        
        // Обновляем данные из БД
        Account updatedSenderAccount = accountRepository.findById(sender.getAccount().getId()).orElseThrow();
        Account updatedRecipientAccount = accountRepository.findById(recipient.getAccount().getId()).orElseThrow();
        
        // Проверяем, что баланс отправителя уменьшился
        assertEquals(new BigDecimal("900.00"), updatedSenderAccount.getBalance());
        
        // Проверяем, что баланс получателя увеличился
        assertEquals(new BigDecimal("600.00"), updatedRecipientAccount.getBalance());
    }
    
    @Test
    @Rollback(true) // Enable rollback to clean up after the test
    public void testTransferWithInsufficientFunds() {
        // Подготавливаем запрос на перевод с суммой больше, чем на балансе
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setRecipientId(recipient.getId());
        transferRequest.setAmount(new BigDecimal("2000.00")); // Больше, чем на балансе
        
        // Подготавливаем HTTP запрос с токеном
        MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        httpRequest.addHeader("Authorization", "Bearer " + jwtToken);
        
        // Выполняем перевод и ожидаем исключение
        assertThrows(InsufficientFundsException.class, () -> {
            transferService.transferMoney(transferRequest, httpRequest);
        });
        
        // Обновляем данные из БД
        Account updatedSenderAccount = accountRepository.findById(sender.getAccount().getId()).orElseThrow();
        Account updatedRecipientAccount = accountRepository.findById(recipient.getAccount().getId()).orElseThrow();
        
        // Проверяем, что балансы не изменились
        assertEquals(new BigDecimal("1000.00"), updatedSenderAccount.getBalance());
        assertEquals(new BigDecimal("500.00"), updatedRecipientAccount.getBalance());
    }
    
    @Test
    @Rollback(true) // Enable rollback to clean up after the test
    public void testSelfTransfer() {
        // Подготавливаем запрос на перевод самому себе
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setRecipientId(sender.getId()); // Тот же ID, что и у отправителя
        transferRequest.setAmount(new BigDecimal("100.00"));
        
        // Подготавливаем HTTP запрос с токеном
        MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        httpRequest.addHeader("Authorization", "Bearer " + jwtToken);
        
        // Выполняем перевод и ожидаем исключение
        assertThrows(SelfTransferException.class, () -> {
            transferService.transferMoney(transferRequest, httpRequest);
        });
        
        // Обновляем данные из БД
        Account updatedSenderAccount = accountRepository.findById(sender.getAccount().getId()).orElseThrow();
        
        // Проверяем, что баланс не изменился
        assertEquals(new BigDecimal("1000.00"), updatedSenderAccount.getBalance());
    }
    
    @Test
    @Rollback(true) // Enable rollback to clean up after the test
    public void testMultipleTransfers() {
        // Подготавливаем HTTP запрос с токеном
        MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        httpRequest.addHeader("Authorization", "Bearer " + jwtToken);
        
        // Выполняем первый перевод
        TransferRequest transferRequest1 = new TransferRequest();
        transferRequest1.setRecipientId(recipient.getId());
        transferRequest1.setAmount(new BigDecimal("50.00"));
        transferService.transferMoney(transferRequest1, httpRequest);
        
        // Выполняем второй перевод
        TransferRequest transferRequest2 = new TransferRequest();
        transferRequest2.setRecipientId(recipient.getId());
        transferRequest2.setAmount(new BigDecimal("75.00"));
        transferService.transferMoney(transferRequest2, httpRequest);
        
        // Обновляем данные из БД
        Account updatedSenderAccount = accountRepository.findById(sender.getAccount().getId()).orElseThrow();
        Account updatedRecipientAccount = accountRepository.findById(recipient.getAccount().getId()).orElseThrow();
        
        // Проверяем, что баланс отправителя уменьшился на общую сумму переводов
        assertEquals(new BigDecimal("875.00"), updatedSenderAccount.getBalance());
        
        // Проверяем, что баланс получателя увеличился на общую сумму переводов
        assertEquals(new BigDecimal("625.00"), updatedRecipientAccount.getBalance());
    }
    
    // Вспомогательный метод для создания пользователя
    private User createUser(String name, String dateOfBirth, String password, String email, String phone, String balance) {
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
        
        // Сохраняем обновленного пользователя и публикуем событие
        User userWithAccount = userRepository.save(savedUser);
        userEventPublisher.publishUserCreated(userWithAccount);
        
        return userWithAccount;
    }
} 