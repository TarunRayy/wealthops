package com.wealthops.portfolio.service;

import com.wealthops.portfolio.dto.HoldingRequest;
import com.wealthops.portfolio.dto.HoldingResponse;
import com.wealthops.portfolio.dto.PortfolioResponse;

public interface PortfolioService {
    PortfolioResponse getPortfolioByClientId(Long clientId);
    PortfolioResponse getPortfolioById(Long portfolioId);
    HoldingResponse addHolding(Long portfolioId, HoldingRequest request);
    HoldingResponse updateHolding(Long portfolioId, Long holdingId, HoldingRequest request);
    void deleteHolding(Long portfolioId, Long holdingId);
}