package com.wealthops.portfolio.service.impl;

import com.wealthops.exception.ResourceNotFoundException;
import com.wealthops.portfolio.dto.HoldingRequest;
import com.wealthops.portfolio.dto.HoldingResponse;
import com.wealthops.portfolio.dto.PortfolioResponse;
import com.wealthops.portfolio.entity.Holding;
import com.wealthops.client.entity.Client;
import com.wealthops.registration.entity.Role;
import com.wealthops.security.CurrentUserService;
import com.wealthops.portfolio.entity.Portfolio;
import com.wealthops.portfolio.repository.HoldingRepository;
import com.wealthops.portfolio.repository.PortfolioRepository;
import com.wealthops.portfolio.service.PortfolioService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PortfolioServiceImpl implements PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final HoldingRepository holdingRepository;
    private final CurrentUserService currentUserService;

    public PortfolioServiceImpl(PortfolioRepository portfolioRepository, HoldingRepository holdingRepository, CurrentUserService currentUserService) {
        this.portfolioRepository = portfolioRepository;
        this.holdingRepository = holdingRepository;
        this.currentUserService = currentUserService;
    }

    @Override
    @Transactional(readOnly = true)
    public PortfolioResponse getPortfolioByClientId(Long clientId) {
        Portfolio portfolio = portfolioRepository.findByClientId(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio not found for client id: " + clientId));
        assertInScope(portfolio);
        return toResponse(portfolio);
    }

    @Override
    @Transactional(readOnly = true)
    public PortfolioResponse getPortfolioById(Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio not found with id: " + portfolioId));
        assertInScope(portfolio);
        return toResponse(portfolio);
    }

    @Override
    @Transactional
    public HoldingResponse addHolding(Long portfolioId, HoldingRequest request) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio not found with id: " + portfolioId));
        assertInScope(portfolio);

        Holding holding = new Holding();
        holding.setPortfolio(portfolio);
        holding.setFundName(request.getFundName());
        holding.setFolioNumber(request.getFolioNumber());
        holding.setUnits(request.getUnits());
        holding.setInvestedAmount(request.getInvestedAmount());
        holding.setCurrentValue(request.getCurrentValue());
        holding.setPurchaseDate(request.getPurchaseDate());

        Holding saved = holdingRepository.save(holding);
        return toHoldingResponse(saved);
    }

    @Override
    @Transactional
    public HoldingResponse updateHolding(Long portfolioId, Long holdingId, HoldingRequest request) {
        Holding holding = holdingRepository.findById(holdingId)
                .orElseThrow(() -> new ResourceNotFoundException("Holding not found with id: " + holdingId));

        if (!holding.getPortfolio().getId().equals(portfolioId)) {
            throw new ResourceNotFoundException("Holding " + holdingId + " does not belong to portfolio " + portfolioId);
        }
        assertInScope(holding.getPortfolio());

        holding.setFundName(request.getFundName());
        holding.setFolioNumber(request.getFolioNumber());
        holding.setUnits(request.getUnits());
        holding.setInvestedAmount(request.getInvestedAmount());
        holding.setCurrentValue(request.getCurrentValue());
        holding.setPurchaseDate(request.getPurchaseDate());

        Holding updated = holdingRepository.save(holding);
        return toHoldingResponse(updated);
    }

    @Override
    @Transactional
    public void deleteHolding(Long portfolioId, Long holdingId) {
        Holding holding = holdingRepository.findById(holdingId)
                .orElseThrow(() -> new ResourceNotFoundException("Holding not found with id: " + holdingId));

        if (!holding.getPortfolio().getId().equals(portfolioId)) {
            throw new ResourceNotFoundException("Holding " + holdingId + " does not belong to portfolio " + portfolioId);
        }

        assertInScope(holding.getPortfolio());
        holdingRepository.delete(holding);
    }

    private PortfolioResponse toResponse(Portfolio portfolio) {
        List<Holding> holdings = holdingRepository.findByPortfolioId(portfolio.getId());

        BigDecimal totalInvested = holdings.stream()
                .map(Holding::getInvestedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCurrent = holdings.stream()
                .map(Holding::getCurrentValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<HoldingResponse> holdingResponses = holdings.stream()
                .map(this::toHoldingResponse)
                .collect(Collectors.toList());

        return new PortfolioResponse(
                portfolio.getId(),
                portfolio.getClient().getId(),
                portfolio.getClient().getFullName(),
                totalInvested,
                totalCurrent,
                portfolio.getCreatedAt(),
                holdingResponses
        );
    }

    private HoldingResponse toHoldingResponse(Holding holding) {
        return new HoldingResponse(
                holding.getId(),
                holding.getFundName(),
                holding.getFolioNumber(),
                holding.getUnits(),
                holding.getInvestedAmount(),
                holding.getCurrentValue(),
                holding.getPurchaseDate()
        );
    }
    private void assertInScope(Portfolio portfolio) {
        Role role = currentUserService.getCurrentUserRole();
        Client client = portfolio.getClient();

        if (currentUserService.isAdminOrCompliance()) {
            return;
        }
        if (role == Role.BRANCH_MANAGER
                && client.getBranch().getId().equals(currentUserService.getCurrentUserBranchId())) {
            return;
        }
        if (role == Role.RELATIONSHIP_MANAGER
                && client.getAssignedRm().getId().equals(currentUserService.getCurrentUserId())) {
            return;
        }
        // CLIENT role uses a separate /portfolio/me-style endpoint, not this one
        throw new ResourceNotFoundException("Portfolio not found with id: " + portfolio.getId());
    }
}