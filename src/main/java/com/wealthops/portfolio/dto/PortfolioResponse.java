package com.wealthops.portfolio.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PortfolioResponse {

    private Long id;
    private Long clientId;
    private String clientName;
    private BigDecimal totalInvestedAmount;
    private BigDecimal totalCurrentValue;
    private LocalDateTime createdAt;
    private List<HoldingResponse> holdings;

    public PortfolioResponse(Long id, Long clientId, String clientName, BigDecimal totalInvestedAmount,
                             BigDecimal totalCurrentValue, LocalDateTime createdAt, List<HoldingResponse> holdings) {
        this.id = id;
        this.clientId = clientId;
        this.clientName = clientName;
        this.totalInvestedAmount = totalInvestedAmount;
        this.totalCurrentValue = totalCurrentValue;
        this.createdAt = createdAt;
        this.holdings = holdings;
    }

    public Long getId() {
        return id;
    }

    public Long getClientId() {
        return clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public BigDecimal getTotalInvestedAmount() {
        return totalInvestedAmount;
    }

    public BigDecimal getTotalCurrentValue() {
        return totalCurrentValue;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<HoldingResponse> getHoldings() {
        return holdings;
    }
}