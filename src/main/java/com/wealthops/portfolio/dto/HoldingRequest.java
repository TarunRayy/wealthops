package com.wealthops.portfolio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public class HoldingRequest {

    @NotBlank(message = "Fund name is required")
    private String fundName;

    @NotBlank(message = "Folio number is required")
    private String folioNumber;

    @NotNull(message = "Units is required")
    @Positive(message = "Units must be positive")
    private BigDecimal units;

    @NotNull(message = "Invested amount is required")
    @Positive(message = "Invested amount must be positive")
    private BigDecimal investedAmount;

    @NotNull(message = "Current value is required")
    @Positive(message = "Current value must be positive")
    private BigDecimal currentValue;

    @NotNull(message = "Purchase date is required")
    private LocalDate purchaseDate;

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

    public BigDecimal getUnits() {
        return units;
    }

    public void setUnits(BigDecimal units) {
        this.units = units;
    }

    public BigDecimal getInvestedAmount() {
        return investedAmount;
    }

    public void setInvestedAmount(BigDecimal investedAmount) {
        this.investedAmount = investedAmount;
    }

    public BigDecimal getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(BigDecimal currentValue) {
        this.currentValue = currentValue;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }
}