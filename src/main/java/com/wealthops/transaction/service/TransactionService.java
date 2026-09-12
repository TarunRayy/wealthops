package com.wealthops.transaction.service;

import com.wealthops.transaction.dto.ApprovalActionDto;
import com.wealthops.transaction.dto.TransactionRequestDto;
import com.wealthops.transaction.dto.TransactionResponseDto;

import java.util.List;

public interface TransactionService {

    TransactionResponseDto createTransaction(TransactionRequestDto dto, String requesterEmail);

    TransactionResponseDto approveTransaction(Long id, ApprovalActionDto dto, String approverEmail);

    TransactionResponseDto rejectTransaction(Long id, ApprovalActionDto dto, String approverEmail);

    TransactionResponseDto getById(Long id, String requesterEmail);

    List<TransactionResponseDto> getMyTransactions(String clientEmail);

    List<TransactionResponseDto> getAll(String requesterEmail);

    List<TransactionResponseDto> getPending(String requesterEmail);
}