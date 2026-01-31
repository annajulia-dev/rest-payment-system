package com.paymentservice.api;

import com.paymentservice.api.dtos.AuthorizationResponse;
import com.paymentservice.api.dtos.TransferDTO;
import com.paymentservice.api.enums.TransactionMessage;
import com.paymentservice.api.exception.InsufficientFundsException;
import com.paymentservice.api.exception.ServiceUnavailableException;
import com.paymentservice.api.exception.UnauthorizedTransactionException;
import com.paymentservice.api.exception.UserNotFoundException;
import com.paymentservice.api.model.Transaction;
import com.paymentservice.api.model.User;
import com.paymentservice.api.enums.UserType;
import com.paymentservice.api.proxy.TransferAuthorizationProxy;
import com.paymentservice.api.repository.TransactionRepository;
import com.paymentservice.api.repository.UserRepository;
import com.paymentservice.api.services.NotificationService;
import com.paymentservice.api.services.TransactionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class TransferServiceUnitTest {

    public static TransferDTO createValidTransfer(){
        return new TransferDTO(new BigDecimal("100"), 1L, 2L);
    }
    public static User createUser(UserType type, BigDecimal balance, Long id){
       User user = new User();

        user.setId(id);
        user.setUserType(type);
        user.setBalance(balance);

        user.setFirstName("Test");
        user.setLastName("User");
        user.setDocument("12345678900");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        return user;
    }

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransferAuthorizationProxy transferAuthorizationProxy;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    public void moneyTransferHappyFlow(){
        TransferDTO transferDTO = createValidTransfer();
        AuthorizationResponse authorizationResponse = new AuthorizationResponse("success", new AuthorizationResponse.AuthorizationData(true));
        User sender = createUser(UserType.COMMON, new BigDecimal("100"), 1L);
        User receiver = createUser(UserType.MERCHANT, new BigDecimal("0"), 2L);

        given(transferAuthorizationProxy.authorizeTransfer())
                .willReturn(authorizationResponse);

        given( userRepository.findById( transferDTO.sender() ) )
                .willReturn(Optional.of(sender));

        given( userRepository.findById( transferDTO.receiver() ) )
                .willReturn(Optional.of(receiver));

        transactionService.transferMoney(transferDTO);

        assertEquals(0, sender.getBalance().compareTo(BigDecimal.ZERO), "Sender balance should be 0");
        assertEquals(0, receiver.getBalance().compareTo(new BigDecimal("100")),"Receiver balance should be 100");

        // CHECK THE TRANSACTION RETURNED OBJECT
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        then(transactionRepository).should().save(transactionCaptor.capture());
        Transaction transaction = transactionCaptor.getValue();

        assertEquals(new BigDecimal("100"), transaction.getAmount());
        assertEquals(sender.getId(), transaction.getSender().getId());
        assertEquals(receiver.getId(), transaction.getReceiver().getId());

        then(userRepository).should(times(2)).save(any(User.class));
        then(notificationService).should().sendNotification();
    }

    @Test
    public void moneyTransferToSameAccountException(){
        TransferDTO transferDTO = new TransferDTO(new BigDecimal("100"), 1L, 1L);

        UnauthorizedTransactionException exception = assertThrows(
                UnauthorizedTransactionException.class,
                () -> transactionService.transferMoney(transferDTO));

        assertEquals(TransactionMessage.SAME_ACCOUNT.getMessage(), exception.getMessage());

        then(userRepository).shouldHaveNoInteractions();
        then(transactionRepository).shouldHaveNoInteractions();
        then(transferAuthorizationProxy).shouldHaveNoInteractions();
        then(notificationService).shouldHaveNoInteractions();
    }

    @Test
    public void userNotFoundException(){
        TransferDTO transferDTO = createValidTransfer();

        given(userRepository.findById( transferDTO.sender() ))
                .willReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> transactionService.transferMoney(transferDTO));

        String expectedMessage = TransactionMessage.USER_NOT_FOUND.getMessage() + transferDTO.sender();
        assertEquals(expectedMessage, exception.getMessage());

        then(transferAuthorizationProxy).shouldHaveNoInteractions();
        then(transactionRepository).shouldHaveNoInteractions();
        then(notificationService).shouldHaveNoInteractions();

        then(userRepository).should(times(1)).findById(any());
    }

    @Test
    public void merchantTransferMoneyException(){
        TransferDTO transferDTO = createValidTransfer();
        User sender = createUser(UserType.MERCHANT, new BigDecimal("100"), 1L);

        given(userRepository.findById( transferDTO.sender() ))
                .willReturn(Optional.of(sender));

        UnauthorizedTransactionException exception = assertThrows(UnauthorizedTransactionException.class,
                () -> transactionService.transferMoney(transferDTO));

        assertEquals(TransactionMessage.MERCHANT_TRANSFER.getMessage(), exception.getMessage());

        then(userRepository).should(times(1)).findById(transferDTO.sender());
        then(transferAuthorizationProxy).shouldHaveNoInteractions();
        then(transactionRepository).shouldHaveNoInteractions();
        then(notificationService).shouldHaveNoInteractions();
    }

    @Test
    public void transferServiceUnavailableException(){
        TransferDTO transferDTO = createValidTransfer();
        User sender = createUser(UserType.COMMON, new BigDecimal("100"), 1L);
        User receiver = createUser(UserType.MERCHANT, new BigDecimal("0"), 2L);

        given(userRepository.findById( transferDTO.sender() ))
                .willReturn(Optional.of(sender));

        given(userRepository.findById( transferDTO.receiver() ))
                .willReturn(Optional.of(receiver));

        given(transferAuthorizationProxy.authorizeTransfer())
                .willThrow(new RuntimeException("504 Gateway Timeout"));

        ServiceUnavailableException exception = assertThrows(ServiceUnavailableException.class,
                () -> transactionService.transferMoney(transferDTO));

        assertEquals(TransactionMessage.AUTHORIZATION_SERVICE_UNAVAILABLE.getMessage(), exception.getMessage());

        then(transactionRepository).shouldHaveNoInteractions();
        then(notificationService).shouldHaveNoInteractions();
        then(userRepository).should(times(2)).findById(any());
    }

    @Test
    public void transferServiceNotAuthorizedException(){
        TransferDTO transferDTO = createValidTransfer();
        User sender = createUser(UserType.COMMON, new BigDecimal("100"), 1L);
        User receiver = createUser(UserType.MERCHANT, new BigDecimal("0"), 2L);
        AuthorizationResponse authorizationResponseFail = new AuthorizationResponse("fail", new AuthorizationResponse.AuthorizationData(false));

        given(userRepository.findById( transferDTO.sender() ))
                .willReturn(Optional.of(sender));

        given(userRepository.findById( transferDTO.receiver() ))
                .willReturn(Optional.of(receiver));

        given(transferAuthorizationProxy.authorizeTransfer())
                .willReturn(authorizationResponseFail);

        UnauthorizedTransactionException exception = assertThrows(UnauthorizedTransactionException.class,
                () -> transactionService.transferMoney(transferDTO));

        assertEquals(TransactionMessage.UNAUTHORIZED.getMessage(), exception.getMessage());

        then(userRepository).should(times(2)).findById(any());
        then(transferAuthorizationProxy).should().authorizeTransfer();
        then(transactionRepository).shouldHaveNoInteractions();
        then(notificationService).shouldHaveNoInteractions();
    }

    @Test
    public void insufficientFundsException(){
        TransferDTO transferDTO = createValidTransfer();
        User sender = createUser(UserType.COMMON, new BigDecimal("50"), 1L);

        given(userRepository.findById( transferDTO.sender() ))
                .willReturn(Optional.of(sender));

        InsufficientFundsException exception = assertThrows(InsufficientFundsException.class,
                () -> transactionService.transferMoney(transferDTO));

        assertEquals(TransactionMessage.INSUFFICIENT_FUNDS.getMessage(), exception.getMessage());

        then(userRepository).should(times(1)).findById( transferDTO.sender() );
        then(transactionRepository).shouldHaveNoInteractions();
        then(transferAuthorizationProxy).shouldHaveNoInteractions();
        then(notificationService).shouldHaveNoInteractions();
    }
}
