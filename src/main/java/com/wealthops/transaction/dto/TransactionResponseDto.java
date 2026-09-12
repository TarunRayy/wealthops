package com.wealthops.transaction.dto;

import com.wealthops.transaction.entity.TransactionStatus;
import com.wealthops.transaction.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponseDto {

    private Long id;
    private Long clientId;
    private String clientName;
    private Long portfolioId;
    private Long holdingId;
    private TransactionType type;
    private String fundName;
    private String folioNumber;
    private BigDecimal amount;
    private BigDecimal units;
    private TransactionStatus status;
    private String requestedByName;
    private String approvedByName;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TransactionResponseDto() {
    }

    public TransactionResponseDto(Long id, Long clientId, String clientName, Long portfolioId,
                                  Long holdingId, TransactionType type, String fundName,
                                  String folioNumber, BigDecimal amount, BigDecimal units,
                                  TransactionStatus status, String requestedByName,
                                  String approvedByName, String remarks,
                                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.clientId = clientId;
        this.clientName = clientName;
        this.portfolioId = portfolioId;
        this.holdingId = holdingId;
        this.type = type;
        this.fundName = fundName;
        this.folioNumber = folioNumber;
        this.amount = amount;
        this.units = units;
        this.status = status;
        this.requestedByName = requestedByName;
        this.approvedByName = approvedByName;
        this.remarks = remarks;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters (no setters needed — read-only response)

    public Long getId() {
        return id;
    }

    public Long getClientId() {
        return clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public Long getPortfolioId() {
        return portfolioId;
    }

    public Long getHoldingId() {
        return holdingId;
    }

    public TransactionType getType() {
        return type;
    }

    public String getFundName() {
        return fundName;
    }

    public String getFolioNumber() {
        return folioNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getUnits() {
        return units;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public String getRequestedByName() {
        return requestedByName;
    }

    public String getApprovedByName() {
        return approvedByName;
    }

    public String getRemarks() {
        return remarks;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}