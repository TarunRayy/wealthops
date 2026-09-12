package com.wealthops.transaction.service.impl;

import com.wealthops.client.entity.Client;
import com.wealthops.client.repository.ClientRepository;
import com.wealthops.exception.ResourceNotFoundException;
import com.wealthops.portfolio.entity.Holding;
import com.wealthops.portfolio.entity.Portfolio;
import com.wealthops.portfolio.repository.HoldingRepository;
import com.wealthops.portfolio.repository.PortfolioRepository;
import com.wealthops.registration.entity.Role;
import com.wealthops.registration.entity.User;
import com.wealthops.registration.repository.UserRepository;
import com.wealthops.transaction.dto.ApprovalActionDto;
import com.wealthops.transaction.dto.TransactionRequestDto;
import com.wealthops.transaction.dto.TransactionResponseDto;
import com.wealthops.transaction.entity.Transaction;
import com.wealthops.transaction.entity.TransactionStatus;
import com.wealthops.transaction.repository.TransactionRepository;
import com.wealthops.transaction.service.TransactionService;
import com.wealthops.transaction.util.ApprovalThreshold;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final ClientRepository clientRepository;
    private final PortfolioRepository portfolioRepository;
    private final HoldingRepository holdingRepository;
    private final UserRepository userRepository;

    public TransactionServiceImpl(TransactionRepository transactionRepository,
                                  ClientRepository clientRepository,
                                  PortfolioRepository portfolioRepository,
                                  HoldingRepository holdingRepository,
                                  UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.clientRepository = clientRepository;
        this.portfolioRepository = portfolioRepository;
        this.holdingRepository = holdingRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public TransactionResponseDto createTransaction(TransactionRequestDto dto, String requesterEmail) {
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Requester not found"));

        Client client = clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

        validateRequesterOwnsClient(requester, client);

        Portfolio portfolio = portfolioRepository.findByClientId(client.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio not found for this client"));

        Holding holding = null;
        if (dto.getHoldingId() != null) {
            holding = holdingRepository.findById(dto.getHoldingId())
                    .orElseThrow(() -> new ResourceNotFoundException("Holding not found"));

            if (!holding.getPortfolio().getId().equals(portfolio.getId())) {
                throw new AccessDeniedException("This holding does not belong to the specified client");
            }
        } else if (dto.getFolioNumber() == null || dto.getFolioNumber().isBlank()) {
            throw new IllegalArgumentException("folioNumber is required when investing in a new fund");
        }

        Transaction txn = new Transaction();
        txn.setClient(client);
        txn.setPortfolio(portfolio);
        txn.setHolding(holding);
        txn.setType(dto.getType());
        txn.setFundName(dto.getFundName());
        txn.setFolioNumber(dto.getFolioNumber());
        txn.setAmount(dto.getAmount());
        txn.setUnits(dto.getUnits());
        txn.setStatus(TransactionStatus.PENDING);
        txn.setRequestedBy(requester);

        return toDto(transactionRepository.save(txn));
    }

    private void validateRequesterOwnsClient(User requester, Client client) {
        Role role = requester.getRole();

        if (role == Role.CLIENT) {
            if (!client.getEmail().equalsIgnoreCase(requester.getEmail())) {
                throw new AccessDeniedException("You can only create transactions for your own account");
            }
        } else if (role == Role.RELATIONSHIP_MANAGER) {
            if (client.getAssignedRm() == null || !client.getAssignedRm().getId().equals(requester.getId())) {
                throw new AccessDeniedException("You can only create transactions for your assigned clients");
            }
        } else {
            throw new AccessDeniedException("Your role is not permitted to create transactions");
        }
    }

    @Override
    @Transactional
    public TransactionResponseDto approveTransaction(Long id, ApprovalActionDto dto, String approverEmail) {
        Transaction txn = getPendingOrThrow(id);
        User approver = userRepository.findByEmail(approverEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Approver not found"));

        validateApproverAuthority(txn, approver);

        applyToPortfolio(txn);

        txn.setStatus(TransactionStatus.APPROVED);
        txn.setApprovedBy(approver);
        txn.setRemarks(dto.getRemarks());

        return toDto(transactionRepository.save(txn));
    }

    @Override
    @Transactional
    public TransactionResponseDto rejectTransaction(Long id, ApprovalActionDto dto, String approverEmail) {
        Transaction txn = getPendingOrThrow(id);
        User approver = userRepository.findByEmail(approverEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Approver not found"));

        validateApproverAuthority(txn, approver);

        txn.setStatus(TransactionStatus.REJECTED);
        txn.setApprovedBy(approver);
        txn.setRemarks(dto.getRemarks());

        return toDto(transactionRepository.save(txn));
    }

    private Transaction getPendingOrThrow(Long id) {
        Transaction txn = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        if (txn.getStatus() != TransactionStatus.PENDING) {
            throw new IllegalStateException("Transaction already " + txn.getStatus());
        }
        return txn;
    }

    private void validateApproverAuthority(Transaction txn, User approver) {
        Role role = approver.getRole();

        if (role == Role.SUPER_ADMIN) {
            return;
        }

        boolean isHighValue = txn.getAmount().compareTo(ApprovalThreshold.HIGH_VALUE_LIMIT) >= 0;

        if (isHighValue) {
            if (role != Role.COMPLIANCE_OFFICER) {
                throw new AccessDeniedException(
                        "Transactions of ₹" + ApprovalThreshold.HIGH_VALUE_LIMIT + " or more require Compliance Officer approval");
            }
        } else {
            if (role != Role.BRANCH_MANAGER) {
                throw new AccessDeniedException("This transaction requires Branch Manager approval");
            }
            if (!approver.getBranch().getId().equals(txn.getClient().getBranch().getId())) {
                throw new AccessDeniedException("You can only approve transactions for your own branch");
            }
        }
    }

    private void applyToPortfolio(Transaction txn) {
        Portfolio portfolio = txn.getPortfolio();
        Holding holding = txn.getHolding();

        if (holding == null) {
            holding = new Holding();
            holding.setPortfolio(portfolio);
            holding.setFundName(txn.getFundName());
            holding.setFolioNumber(txn.getFolioNumber());
            holding.setUnits(BigDecimal.ZERO);
            holding.setInvestedAmount(BigDecimal.ZERO);
            holding.setCurrentValue(BigDecimal.ZERO);
            holding.setPurchaseDate(LocalDate.now());
        }

        BigDecimal txnUnits = txn.getUnits() != null ? txn.getUnits() : BigDecimal.ZERO;

        switch (txn.getType()) {
            case BUY -> {
                holding.setUnits(holding.getUnits().add(txnUnits));
                holding.setInvestedAmount(holding.getInvestedAmount().add(txn.getAmount()));
                holding.setCurrentValue(holding.getCurrentValue().add(txn.getAmount()));
            }
            case SELL, SWITCH -> {
                // SWITCH simplified for now: only handles the "sell out of current fund" side.
                // Buy-side into the new fund is not yet automated — can be added as a follow-up
                // enhancement once NAV handling exists.
                holding.setUnits(holding.getUnits().subtract(txnUnits));
                holding.setInvestedAmount(holding.getInvestedAmount().subtract(txn.getAmount()));
                holding.setCurrentValue(holding.getCurrentValue().subtract(txn.getAmount()));
            }
        }

        holdingRepository.save(holding);
        txn.setHolding(holding);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponseDto getById(Long id, String requesterEmail) {
        Transaction txn = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Requester not found"));

        validateViewAccess(txn, requester);

        return toDto(txn);
    }

    private void validateViewAccess(Transaction txn, User requester) {
        Role role = requester.getRole();

        switch (role) {
            case SUPER_ADMIN, COMPLIANCE_OFFICER -> {
                // full access
            }
            case BRANCH_MANAGER -> {
                if (!requester.getBranch().getId().equals(txn.getClient().getBranch().getId())) {
                    throw new AccessDeniedException("Not authorized to view this transaction");
                }
            }
            case RELATIONSHIP_MANAGER -> {
                if (txn.getClient().getAssignedRm() == null
                        || !txn.getClient().getAssignedRm().getId().equals(requester.getId())) {
                    throw new AccessDeniedException("Not authorized to view this transaction");
                }
            }
            case CLIENT -> {
                if (!txn.getClient().getEmail().equalsIgnoreCase(requester.getEmail())) {
                    throw new AccessDeniedException("Not authorized to view this transaction");
                }
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponseDto> getMyTransactions(String clientEmail) {
        Client client = clientRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Client record not found for this account"));

        return transactionRepository.findByClientId(client.getId())
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponseDto> getAll(String requesterEmail) {
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Requester not found"));

        List<Transaction> transactions = switch (requester.getRole()) {
            case SUPER_ADMIN, COMPLIANCE_OFFICER -> transactionRepository.findAll();
            case BRANCH_MANAGER -> transactionRepository.findByClient_Branch_Id(requester.getBranch().getId());
            case RELATIONSHIP_MANAGER -> transactionRepository.findByClient_AssignedRm_Id(requester.getId());
            default -> throw new AccessDeniedException("Not authorized to view all transactions");
        };

        return transactions.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponseDto> getPending(String requesterEmail) {
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Requester not found"));

        List<Transaction> transactions = switch (requester.getRole()) {
            case SUPER_ADMIN, COMPLIANCE_OFFICER -> transactionRepository.findByStatus(TransactionStatus.PENDING);
            case BRANCH_MANAGER -> transactionRepository.findByClient_Branch_IdAndStatus(
                    requester.getBranch().getId(), TransactionStatus.PENDING);
            case RELATIONSHIP_MANAGER -> transactionRepository.findByClient_AssignedRm_IdAndStatus(
                    requester.getId(), TransactionStatus.PENDING);
            default -> throw new AccessDeniedException("Not authorized to view pending transactions");
        };

        return transactions.stream().map(this::toDto).collect(Collectors.toList());
    }

    private TransactionResponseDto toDto(Transaction txn) {
        return new TransactionResponseDto(
                txn.getId(),
                txn.getClient().getId(),
                txn.getClient().getFullName(),
                txn.getPortfolio().getId(),
                txn.getHolding() != null ? txn.getHolding().getId() : null,
                txn.getType(),
                txn.getFundName(),
                txn.getFolioNumber(),
                txn.getAmount(),
                txn.getUnits(),
                txn.getStatus(),
                txn.getRequestedBy().getFullName(),
                txn.getApprovedBy() != null ? txn.getApprovedBy().getFullName() : null,
                txn.getRemarks(),
                txn.getCreatedAt(),
                txn.getUpdatedAt()
        );
    }
}