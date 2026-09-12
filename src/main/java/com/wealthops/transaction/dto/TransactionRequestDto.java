package com.wealthops.transaction.dto;

import com.wealthops.transaction.entity.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class TransactionRequestDto {

    @NotNull
    private Long clientId;

    // Optional: pass if this transaction is against an existing holding (SELL/SWITCH,
    // or BUY into an already-held fund)
    private Long holdingId;

    @NotNull
    private TransactionType type;

    @NotBlank
    private String fundName;

    // Required only if holdingId is null (brand-new fund)
    private String folioNumber;

    @NotNull
    @DecimalMin(value = "1.0", message = "Amount must be greater than 0")
    private BigDecimal amount;

    private BigDecimal units;

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Long getHoldingId() {
        return holdingId;
    }

    public void setHoldingId(Long holdingId) {
        this.holdingId = holdingId;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public String getFundName() {
        return fundName;
    }

    public void setFundName(String fundName) {
        this.fundName = fundName;
    }

    public String getFolioNumber() {
        return folioNumber;
    }

    public void setFolioNumber(String folioNumber) {
        this.folioNumber = folioNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getUnits() {
        return units;
    }

    public void setUnits(BigDecimal units) {
        this.units = units;
    }
}