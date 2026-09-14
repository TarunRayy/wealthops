package com.wealthops.portfolio.controller;

import com.wealthops.portfolio.dto.HoldingRequest;
import com.wealthops.portfolio.dto.HoldingResponse;
import com.wealthops.portfolio.dto.PortfolioResponse;
import com.wealthops.portfolio.service.PortfolioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/portfolios")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','BRANCH_MANAGER','RELATIONSHIP_MANAGER','CLIENT')")
    @GetMapping("/{portfolioId}")
    public ResponseEntity<PortfolioResponse> getPortfolio(@PathVariable Long portfolioId) {
        return ResponseEntity.ok(portfolioService.getPortfolioById(portfolioId));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','BRANCH_MANAGER','RELATIONSHIP_MANAGER','CLIENT')")
    @GetMapping("/client/{clientId}")
    public ResponseEntity<PortfolioResponse> getPortfolioByClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(portfolioService.getPortfolioByClientId(clientId));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','BRANCH_MANAGER','RELATIONSHIP_MANAGER')")
    @PostMapping("/{portfolioId}/holdings")
    public ResponseEntity<HoldingResponse> addHolding(@PathVariable Long portfolioId, @Valid @RequestBody HoldingRequest request) {
        return new ResponseEntity<>(portfolioService.addHolding(portfolioId, request), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','BRANCH_MANAGER','RELATIONSHIP_MANAGER')")
    @PutMapping("/{portfolioId}/holdings/{holdingId}")
    public ResponseEntity<HoldingResponse> updateHolding(@PathVariable Long portfolioId, @PathVariable Long holdingId,
                                                         @Valid @RequestBody HoldingRequest request) {
        return ResponseEntity.ok(portfolioService.updateHolding(portfolioId, holdingId, request));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','BRANCH_MANAGER')")
    @DeleteMapping("/{portfolioId}/holdings/{holdingId}")
    public ResponseEntity<Void> deleteHolding(@PathVariable Long portfolioId, @PathVariable Long holdingId) {
        portfolioService.deleteHolding(portfolioId, holdingId);
        return ResponseEntity.noContent().build();
    }
}