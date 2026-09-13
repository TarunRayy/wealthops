package com.wealthops.transaction.controller;

import com.wealthops.transaction.dto.ApprovalActionDto;
import com.wealthops.transaction.dto.TransactionRequestDto;
import com.wealthops.transaction.dto.TransactionResponseDto;
import com.wealthops.transaction.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENT', 'RELATIONSHIP_MANAGER')")
    public ResponseEntity<TransactionResponseDto> create(
            @Valid @RequestBody TransactionRequestDto dto,
            Authentication authentication) {
        TransactionResponseDto response = transactionService.createTransaction(dto, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('BRANCH_MANAGER', 'COMPLIANCE_OFFICER', 'SUPER_ADMIN')")
    public ResponseEntity<TransactionResponseDto> approve(
            @PathVariable Long id,
            @RequestBody(required = false) ApprovalActionDto dto,
            Authentication authentication) {
        TransactionResponseDto response = transactionService.approveTransaction(
                id, dto != null ? dto : new ApprovalActionDto(), authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('BRANCH_MANAGER', 'COMPLIANCE_OFFICER', 'SUPER_ADMIN')")
    public ResponseEntity<TransactionResponseDto> reject(
            @PathVariable Long id,
            @RequestBody ApprovalActionDto dto,
            Authentication authentication) {
        TransactionResponseDto response = transactionService.rejectTransaction(id, dto, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TransactionResponseDto> getById(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(transactionService.getById(id, authentication.getName()));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<List<TransactionResponseDto>> getMyTransactions(Authentication authentication) {
        return ResponseEntity.ok(transactionService.getMyTransactions(authentication.getName()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER', 'BRANCH_MANAGER', 'COMPLIANCE_OFFICER', 'SUPER_ADMIN')")
    public ResponseEntity<List<TransactionResponseDto>> getAll(Authentication authentication) {
        return ResponseEntity.ok(transactionService.getAll(authentication.getName()));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER', 'BRANCH_MANAGER', 'COMPLIANCE_OFFICER', 'SUPER_ADMIN')")
    public ResponseEntity<List<TransactionResponseDto>> getPending(Authentication authentication) {
        return ResponseEntity.ok(transactionService.getPending(authentication.getName()));
    }
}