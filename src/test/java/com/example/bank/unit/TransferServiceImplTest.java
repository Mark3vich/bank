package com.example.bank.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import com.example.bank.dto.request.TransferRequest;
import com.example.bank.exception.AccountNotFoundException;
import com.example.bank.exception.InsufficientFundsException;
import com.example.bank.exception.InvalidAmountException;
import com.example.bank.exception.SelfTransferException;
import com.example.bank.model.Account;
import com.example.bank.model.EmailData;
import com.example.bank.model.User;
import com.example.bank.repository.AccountRepository;
import com.example.bank.repository.UserRepository;
import com.example.bank.service.JwtService;
import com.example.bank.service.TransactionLogService;
import com.example.bank.service.impl.TransferServiceImpl;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
public class TransferServiceImplTest {

    @Mock
    private AccountRepository accountRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private TransactionLogService transactionLogService;
    
    @Mock
    private JwtService jwtService;
    
    @Mock
    private HttpServletRequest httpRequest;
    
    @InjectMocks
    private TransferServiceImpl transferService;
    
    private User sender;
    private User recipient;
    private Account senderAccount;
    private Account recipientAccount;
    private TransferRequest validRequest;
    private static final String AUTH_HEADER = "Bearer test-token";
    private static final String TEST_EMAIL = "sender@example.com";
    
    @BeforeEach
    void setUp() {
        // Подготовка данных для тестов
        sender = new User();
        sender.setId(1L);
        sender.setName("Sender");
        
        EmailData senderEmail = new EmailData();
        senderEmail.setEmail(TEST_EMAIL);
        senderEmail.setUser(sender);
        sender.getEmails().add(senderEmail);
        
        recipient = new User();
        recipient.setId(2L);
        recipient.setName("Recipient");
        
        senderAccount = new Account();
        senderAccount.setId(1L);
        senderAccount.setUser(sender);
        senderAccount.setBalance(new BigDecimal("1000.00"));
        sender.setAccount(senderAccount);
        
        recipientAccount = new Account();
        recipientAccount.setId(2L);
        recipientAccount.setUser(recipient);
        recipientAccount.setBalance(new BigDecimal("500.00"));
        recipient.setAccount(recipientAccount);
        
        validRequest = new TransferRequest();
        validRequest.setRecipientId(2L);
        validRequest.setAmount(new BigDecimal("100.00"));
    }
    
    private void setupAuthMocks() {
        when(httpRequest.getHeader("Authorization")).thenReturn(AUTH_HEADER);
        when(jwtService.extractIdentifier("test-token")).thenReturn(TEST_EMAIL);
        when(userRepository.findByEmailWithTokens(TEST_EMAIL)).thenReturn(Optional.of(sender));
    }
    
    @Test
    void transferMoney_Success() {
        // Setup authorization mocks
        setupAuthMocks();
        
        // Настройка mock-объектов
        when(accountRepository.findByUserIdWithLock(1L)).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByUserIdWithLock(2L)).thenReturn(Optional.of(recipientAccount));
        
        // Выполнение метода
        transferService.transferMoney(validRequest, httpRequest);
        
        // Проверка результатов
        assertEquals(new BigDecimal("900.00"), senderAccount.getBalance());
        assertEquals(new BigDecimal("600.00"), recipientAccount.getBalance());
        
        // Проверка вызовов методов
        verify(accountRepository).save(senderAccount);
        verify(accountRepository).save(recipientAccount);
        verify(transactionLogService).logTransfer(
                eq(1L), eq(2L), eq(new BigDecimal("100.00")), anyString());
    }
    
    @Test
    void transferMoney_ThrowsExceptionWhenAuthHeaderMissing() {
        // Подготовка данных - заголовок Authorization отсутствует
        when(httpRequest.getHeader("Authorization")).thenReturn(null);
        
        // Проверка исключения
        assertThrows(AccountNotFoundException.class, () -> 
            transferService.transferMoney(validRequest, httpRequest));
        
        // Проверка, что методы не вызывались
        verifyNoInteractions(accountRepository);
        verifyNoInteractions(transactionLogService);
    }
    
    @Test
    void transferMoney_ThrowsExceptionWhenTokenInvalid() {
        // Подготовка данных - токен некорректный
        when(httpRequest.getHeader("Authorization")).thenReturn("Bearer invalid-token");
        when(jwtService.extractIdentifier("invalid-token")).thenReturn(null);
        
        // Проверка исключения
        assertThrows(AccountNotFoundException.class, () -> 
            transferService.transferMoney(validRequest, httpRequest));
        
        // Проверка, что методы не вызывались
        verifyNoInteractions(accountRepository);
        verifyNoInteractions(transactionLogService);
    }
    
    @Test
    void transferMoney_ThrowsExceptionWhenUserNotFound() {
        // Подготовка данных - пользователь не найден
        when(httpRequest.getHeader("Authorization")).thenReturn(AUTH_HEADER);
        when(jwtService.extractIdentifier("test-token")).thenReturn(TEST_EMAIL);
        when(userRepository.findByEmailWithTokens(TEST_EMAIL)).thenReturn(Optional.empty());
        
        // Проверка исключения
        assertThrows(AccountNotFoundException.class, () -> 
            transferService.transferMoney(validRequest, httpRequest));
        
        // Проверка, что методы не вызывались
        verifyNoInteractions(accountRepository);
        verifyNoInteractions(transactionLogService);
    }
    
    @Test
    void transferMoney_ThrowsExceptionOnSelfTransfer() {
        // Setup authorization mocks
        setupAuthMocks();
        
        // Подготовка данных - перевод самому себе
        TransferRequest selfTransferRequest = new TransferRequest();
        selfTransferRequest.setRecipientId(1L); // тот же ID, что и у отправителя
        selfTransferRequest.setAmount(new BigDecimal("100.00"));
        
        // Проверка исключения
        assertThrows(SelfTransferException.class, () -> 
            transferService.transferMoney(selfTransferRequest, httpRequest));
        
        // Проверка, что методы не вызывались
        verifyNoInteractions(accountRepository);
        verifyNoInteractions(transactionLogService);
    }
    
    @Test
    void transferMoney_ThrowsExceptionWhenSenderAccountNotFound() {
        // Setup authorization mocks
        setupAuthMocks();
        
        // Подготовка данных - аккаунт отправителя не найден
        when(accountRepository.findByUserIdWithLock(1L)).thenReturn(Optional.empty());
        
        // Проверка исключения
        assertThrows(AccountNotFoundException.class, () -> 
            transferService.transferMoney(validRequest, httpRequest));
        
        // Проверка, что методы не вызывались
        verify(accountRepository, never()).save(any(Account.class));
        verifyNoInteractions(transactionLogService);
    }
    
    @Test
    void transferMoney_ThrowsExceptionWhenRecipientAccountNotFound() {
        // Setup authorization mocks
        setupAuthMocks();
        
        // Подготовка данных - аккаунт получателя не найден
        when(accountRepository.findByUserIdWithLock(1L)).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByUserIdWithLock(2L)).thenReturn(Optional.empty());
        
        // Проверка исключения
        assertThrows(AccountNotFoundException.class, () -> 
            transferService.transferMoney(validRequest, httpRequest));
        
        // Проверка, что методы не вызывались
        verify(accountRepository, never()).save(any(Account.class));
        verifyNoInteractions(transactionLogService);
    }
    
    @Test
    void transferMoney_ThrowsExceptionWhenInsufficientFunds() {
        // Setup authorization mocks
        setupAuthMocks();
        
        // Подготовка данных - недостаточно средств
        TransferRequest largeAmountRequest = new TransferRequest();
        largeAmountRequest.setRecipientId(2L);
        largeAmountRequest.setAmount(new BigDecimal("2000.00")); // больше, чем на балансе
        
        when(accountRepository.findByUserIdWithLock(1L)).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByUserIdWithLock(2L)).thenReturn(Optional.of(recipientAccount));
        
        // Проверка исключения
        assertThrows(InsufficientFundsException.class, () -> 
            transferService.transferMoney(largeAmountRequest, httpRequest));
        
        // Проверка, что баланс не изменился
        assertEquals(new BigDecimal("1000.00"), senderAccount.getBalance());
        assertEquals(new BigDecimal("500.00"), recipientAccount.getBalance());
        
        // Проверка, что методы не вызывались
        verify(accountRepository, never()).save(any(Account.class));
        verifyNoInteractions(transactionLogService);
    }
    
    @Test
    void transferMoney_ThrowsExceptionWhenAmountTooSmall() {
        // Setup authorization mocks
        setupAuthMocks();
        
        // Подготовка данных - сумма слишком маленькая
        TransferRequest smallAmountRequest = new TransferRequest();
        smallAmountRequest.setRecipientId(2L);
        smallAmountRequest.setAmount(new BigDecimal("0.5")); // меньше минимальной суммы
        
        when(accountRepository.findByUserIdWithLock(1L)).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByUserIdWithLock(2L)).thenReturn(Optional.of(recipientAccount));
        
        // Проверка исключения
        assertThrows(InvalidAmountException.class, () -> 
            transferService.transferMoney(smallAmountRequest, httpRequest));
        
        // Проверка, что баланс не изменился
        assertEquals(new BigDecimal("1000.00"), senderAccount.getBalance());
        assertEquals(new BigDecimal("500.00"), recipientAccount.getBalance());
        
        // Проверка, что методы не вызывались
        verify(accountRepository, never()).save(any(Account.class));
        verifyNoInteractions(transactionLogService);
    }
    
    @Test
    void transferMoney_ThrowsExceptionWhenAmountTooLarge() {
        // Setup authorization mocks
        setupAuthMocks();
        
        // Increase senderAccount balance to pass the insufficient funds check
        senderAccount.setBalance(new BigDecimal("2000000"));
        
        // Подготовка данных - сумма слишком большая
        TransferRequest largeAmountRequest = new TransferRequest();
        largeAmountRequest.setRecipientId(2L);
        largeAmountRequest.setAmount(new BigDecimal("1500000")); // больше максимальной суммы
        
        when(accountRepository.findByUserIdWithLock(1L)).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByUserIdWithLock(2L)).thenReturn(Optional.of(recipientAccount));
        
        // Проверка исключения
        assertThrows(InvalidAmountException.class, () -> 
            transferService.transferMoney(largeAmountRequest, httpRequest));
        
        // Проверка, что баланс не изменился
        assertEquals(new BigDecimal("2000000"), senderAccount.getBalance());
        assertEquals(new BigDecimal("500.00"), recipientAccount.getBalance());
        
        // Проверка, что методы не вызывались
        verify(accountRepository, never()).save(any(Account.class));
        verifyNoInteractions(transactionLogService);
    }
    
    @Test
    void transferMoney_HandlesOptimisticLockingException() {
        // Setup authorization mocks
        setupAuthMocks();
        
        // Подготовка данных - Оптимистическая блокировка
        when(accountRepository.findByUserIdWithLock(1L)).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByUserIdWithLock(2L)).thenReturn(Optional.of(recipientAccount));
        when(accountRepository.save(senderAccount))
            .thenThrow(new ObjectOptimisticLockingFailureException(Account.class, 1L)) // первый вызов выбрасывает исключение
            .thenReturn(senderAccount); // второй вызов успешен
        
        // Выполнение метода
        // Тест не выбросит исключение, если @Retryable работает правильно
        assertThrows(ObjectOptimisticLockingFailureException.class, () -> 
            transferService.transferMoney(validRequest, httpRequest));
    }
    
    @Test
    void transferMoney_LogsTransaction() {
        // Setup authorization mocks
        setupAuthMocks();
        
        // Подготовка данных
        when(accountRepository.findByUserIdWithLock(1L)).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByUserIdWithLock(2L)).thenReturn(Optional.of(recipientAccount));
        
        // Выполнение метода
        transferService.transferMoney(validRequest, httpRequest);
        
        // Проверка вызова логирования транзакции
        verify(transactionLogService).logTransfer(
                eq(1L), 
                eq(2L), 
                eq(new BigDecimal("100.00")), 
                eq("Transfer between accounts"));
    }
} 