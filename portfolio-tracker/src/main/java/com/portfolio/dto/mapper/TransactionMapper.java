package com.portfolio.dto.mapper;

import com.portfolio.dto.response.TransactionResponse;
import com.portfolio.entity.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public static TransactionResponse toResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .portfolioId(transaction.getPortfolio().getId())
                .symbol(transaction.getStock().getSymbol())
                .companyName(transaction.getStock().getCompanyName())
                .type(transaction.getType())
                .quantity(transaction.getQuantity())
                .price(transaction.getPrice())
                .totalAmount(transaction.getTotalAmount())
                .transactionDate(transaction.getTransactionDate())
                .notes(transaction.getNotes())
                .build();
    }
}
