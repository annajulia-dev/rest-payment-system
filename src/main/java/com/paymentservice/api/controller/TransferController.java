package com.paymentservice.api.controller;


import com.paymentservice.api.dtos.TransactionResponseDTO;
import com.paymentservice.api.dtos.TransferDTO;
import com.paymentservice.api.model.Transaction;
import com.paymentservice.api.services.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transactions")
public class TransferController {

    private final TransactionService transactionService;

    public TransferController(TransactionService transactionService){
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<TransactionResponseDTO> transferMoney(
            @Valid @RequestBody TransferDTO data){

        Transaction transaction = transactionService.transferMoney(data);

        TransactionResponseDTO response = new TransactionResponseDTO(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getSender().getId(),
                transaction.getSender().getFirstName(),
                transaction.getReceiver().getId(),
                transaction.getReceiver().getFirstName(),
                transaction.getTimeStamp());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }
}
