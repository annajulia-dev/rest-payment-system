package com.paymentservice.api.services;

import com.paymentservice.api.dtos.AuthorizationResponse;
import com.paymentservice.api.dtos.TransferDTO;
import com.paymentservice.api.enums.TransactionMessage;
import com.paymentservice.api.exception.InsufficientFundsException;
import com.paymentservice.api.exception.ServiceUnavailableException;
import com.paymentservice.api.exception.UnauthorizedTransactionException;
import com.paymentservice.api.exception.UserNotFoundException;
import com.paymentservice.api.model.Transaction;
import com.paymentservice.api.model.User;
import com.paymentservice.api.proxy.TransferAuthorizationProxy;
import com.paymentservice.api.repository.TransactionRepository;
import com.paymentservice.api.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class TransactionService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final TransferAuthorizationProxy transferAuthorizationProxy;
    private final NotificationService notificationService;

    public TransactionService(UserRepository userRepository, TransactionRepository transactionRepository, TransferAuthorizationProxy transferAuthorizationProxy, NotificationService notificationService){
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.transferAuthorizationProxy = transferAuthorizationProxy;
        this.notificationService = notificationService;
    }

    @Transactional
    public Transaction transferMoney(TransferDTO data){
        if(data.sender().equals( data.receiver())){
            throw new UnauthorizedTransactionException(TransactionMessage.SAME_ACCOUNT.getMessage());
        }

        User sender = findById(data.sender());
        verifySenderEligibility(sender, data.value());

        User receiver = findById(data.receiver());

        validateTransfer(data);

        Transaction transaction = updateBalances(sender, receiver, data.value());

        saveTransaction(sender, receiver, transaction);

        notificationService.sendNotification();

        return transaction;
    }

    private void validateTransfer(TransferDTO data){
        AuthorizationResponse response;
        try{ // VERIFY AUTHORIZATION PROXY
            response = transferAuthorizationProxy.authorizeTransfer();
        } catch (Exception e) {
            throw new ServiceUnavailableException(TransactionMessage.AUTHORIZATION_SERVICE_UNAVAILABLE.getMessage());
        }
        if(response == null || !response.data().authorization()){
            throw new UnauthorizedTransactionException(TransactionMessage.UNAUTHORIZED.getMessage());
        }
    }

    private User findById(Long id){
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(TransactionMessage.USER_NOT_FOUND.getMessage() + id));
    }

    private Transaction updateBalances(User sender, User receiver, BigDecimal value){
        sender.debit(value);
        receiver.credit(value);
        return new Transaction(
                value,
                sender,
                receiver,
                LocalDateTime.now());
    }

    private void saveTransaction(User sender, User receiver, Transaction transaction){
        transactionRepository.save(transaction);
        userRepository.save(sender);
        userRepository.save(receiver);
    }

    private void verifySenderEligibility(User sender, BigDecimal value){
        sender.validateTransactability();

        if(!sender.hasSufficientFunds(value)){
            throw new InsufficientFundsException(TransactionMessage.INSUFFICIENT_FUNDS.getMessage());
        }
    }

}
