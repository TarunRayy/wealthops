package com.wealthops.portfolio.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class HoldingResponse {

    private Long id;
    private String fundName;
    private String folioNumber;
    private BigDecimal units;
    private BigDecimal investedAmount;
    private BigDecimal currentValue;
    private LocalDate purchaseDate;

    public HoldingResponse(Long id, String fundName, String folioNumber, BigDecimal units,
                           BigDecimal investedAmount, BigDecimal currentValue, LocalDate purchaseDate) {
        this.id = id;
        this.fundName = fundName;
        this.folioNumber = folioNumber;
        this.units = units;
        this.investedAmount = investedAmount;
        this.currentValue = currentValue;
        this.purchaseDate = purchaseDate;
    }

    public Long getId() {
        return id;
    }

    public String getFundName() {
        return fundName;
    }

    public String getFolioNumber() {
        return folioNumber;
    }

    public BigDecimal getUnits() {
        return units;
    }

    public BigDecimal getInvestedAmount() {
        return investedAmount;
    }

    public BigDecimal getCurrentValue() {
        return currentValue;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }
}