package com.paymentservice.api.services;

import com.paymentservice.api.dtos.AuthorizationResponse;
import com.paymentservice.api.dtos.TransferDTO;
import com.paymentservice.api.exception.AuthorizationServiceUnavailableException;
import com.paymentservice.api.exception.UnauthorizedTransactionException;
import com.paymentservice.api.exception.UserNotFoundException;
import com.paymentservice.api.model.Transaction;
import com.paymentservice.api.model.User;
import com.paymentservice.api.proxy.TransferAuthorizationProxy;
import com.paymentservice.api.repository.TransactionRepository;
import com.paymentservice.api.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TransactionService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final TransferAuthorizationProxy transferAuthorizationProxy;

    public TransactionService(UserRepository userRepository, TransactionRepository transactionRepository, TransferAuthorizationProxy transferAuthorizationProxy){
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.transferAuthorizationProxy = transferAuthorizationProxy;
    }

    @Transactional
    public Transaction transferMoney(TransferDTO data){
        if(data.sender().equals( data.receiver())){
            throw new UnauthorizedTransactionException("Você não pode transferir para si mesmo.");
        }
        try{
            AuthorizationResponse authorization = transferAuthorizationProxy.authorizeTransfer();
        } catch (Exception e) {
            throw new AuthorizationServiceUnavailableException("Serviço de autorização indisponível.");
        }

        User sender = userRepository.findById( data.sender() )
                .orElseThrow(() -> new UserNotFoundException("Pagador não encontrado."));

        sender.validateTransactability( data.value() );

        User receiver = userRepository.findById( data.receiver() )
                .orElseThrow(() -> new UserNotFoundException("Beneficiário não encontrado."));

        sender.setBalance( sender.getBalance().subtract( data.value() ) );
        receiver.setBalance( receiver.getBalance().add( data.value() ) );

        Transaction transaction = new Transaction(
                data.value(),
                sender,
                receiver,
                LocalDateTime.now());

        transactionRepository.save(transaction);
        userRepository.save(sender);
        userRepository.save(receiver);

        return transaction;
    }

}
