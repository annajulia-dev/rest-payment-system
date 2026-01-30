package com.paymentservice.api;

import com.paymentservice.api.dtos.AuthorizationResponse;
import com.paymentservice.api.dtos.TransferDTO;
import com.paymentservice.api.model.Transaction;
import com.paymentservice.api.model.User;
import com.paymentservice.api.model.UserType;
import com.paymentservice.api.proxy.NotificationProxy;
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
}
