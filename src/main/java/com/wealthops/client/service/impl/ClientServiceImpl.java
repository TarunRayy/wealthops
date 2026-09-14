package com.wealthops.client.service.impl;

import com.wealthops.branch.entity.Branch;
import com.wealthops.security.CurrentUserService;
import com.wealthops.branch.repository.BranchRepository;
import com.wealthops.client.dto.ClientRequest;
import com.wealthops.client.dto.ClientResponse;
import com.wealthops.portfolio.entity.Portfolio;
import com.wealthops.portfolio.repository.PortfolioRepository;
import com.wealthops.client.entity.Client;
import com.wealthops.client.entity.ClientStatus;
import com.wealthops.client.repository.ClientRepository;
import com.wealthops.client.service.ClientService;
import com.wealthops.exception.ResourceNotFoundException;
import com.wealthops.registration.entity.Role;
import com.wealthops.registration.entity.User;
import org.springframework.transaction.annotation.Transactional;
import com.wealthops.registration.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClientServiceImpl implements ClientService {


    private final ClientRepository clientRepository;
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;
    private final CurrentUserService currentUserService;

    public ClientServiceImpl(ClientRepository clientRepository,
                             BranchRepository branchRepository,
                             UserRepository userRepository,
                             PortfolioRepository portfolioRepository,
                             CurrentUserService currentUserService) {
        this.clientRepository = clientRepository;
        this.branchRepository = branchRepository;
        this.userRepository = userRepository;
        this.portfolioRepository = portfolioRepository;
        this.currentUserService = currentUserService;
    }

    @Override
    @Transactional
    public ClientResponse createClient(ClientRequest request) {
        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + request.getBranchId()));

        User rm = userRepository.findById(request.getAssignedRmId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getAssignedRmId()));

        if (rm.getRole() != Role.RELATIONSHIP_MANAGER) {
            throw new IllegalArgumentException("Assigned user must have role RELATIONSHIP_MANAGER");
        }

        Client client = new Client();
        client.setFullName(request.getFullName());
        client.setEmail(request.getEmail());
        client.setPhone(request.getPhone());
        client.setPanNumber(request.getPanNumber());
        client.setDateOfBirth(request.getDateOfBirth());
        client.setAddress(request.getAddress());
        client.setBranch(branch);
        client.setAssignedRm(rm);
        client.setStatus(ClientStatus.ACTIVE);

        Client saved = clientRepository.save(client);

        Portfolio portfolio = new Portfolio();
        portfolio.setClient(saved);
        portfolioRepository.save(portfolio);

        return toResponse(saved);
    }


    @Override
    @Transactional(readOnly = true)
    public ClientResponse getClientById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));
        assertInScope(client);
        return toResponse(client);
    }

    @Override
    @Transactional(readOnly = true)
    public ClientResponse getClientByEmail(String email) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with email: " + email));
        return toResponse(client);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientResponse> getAllClients() {
        Role role = currentUserService.getCurrentUserRole();
        List<Client> clients;

        if (currentUserService.isAdminOrCompliance()) {
            clients = clientRepository.findAll();
        } else if (role == Role.BRANCH_MANAGER) {
            clients = clientRepository.findByBranchId(currentUserService.getCurrentUserBranchId());
        } else if (role == Role.RELATIONSHIP_MANAGER) {
            clients = clientRepository.findByAssignedRmId(currentUserService.getCurrentUserId());
        } else {
            clients = List.of();
        }

        return clients.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientResponse> getClientsByBranch(Long branchId) {
        Role role = currentUserService.getCurrentUserRole();
        if (role == Role.BRANCH_MANAGER
                && !branchId.equals(currentUserService.getCurrentUserBranchId())) {
            throw new ResourceNotFoundException("Branch not found with id: " + branchId);
        }
        return clientRepository.findByBranchId(branchId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientResponse> getClientsByRm(Long rmId) {
        Role role = currentUserService.getCurrentUserRole();
        if (role == Role.RELATIONSHIP_MANAGER
                && !rmId.equals(currentUserService.getCurrentUserId())) {
            throw new ResourceNotFoundException("RM not found with id: " + rmId);
        }
        return clientRepository.findByAssignedRmId(rmId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ClientResponse updateClient(Long id, ClientRequest request) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));
        assertInScope(client);

        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + request.getBranchId()));

        User rm = userRepository.findById(request.getAssignedRmId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getAssignedRmId()));

        if (rm.getRole() != Role.RELATIONSHIP_MANAGER) {
            throw new IllegalArgumentException("Assigned user must have role RELATIONSHIP_MANAGER");
        }

        client.setFullName(request.getFullName());
        client.setEmail(request.getEmail());
        client.setPhone(request.getPhone());
        client.setPanNumber(request.getPanNumber());
        client.setDateOfBirth(request.getDateOfBirth());
        client.setAddress(request.getAddress());
        client.setBranch(branch);
        client.setAssignedRm(rm);

        Client updated = clientRepository.save(client);
        return toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));
        assertInScope(client);
        clientRepository.delete(client);
    }

    private ClientResponse toResponse(Client client) {
        return new ClientResponse(
                client.getId(),
                client.getFullName(),
                client.getEmail(),
                client.getPhone(),
                client.getPanNumber(),
                client.getDateOfBirth(),
                client.getAddress(),
                client.getStatus(),
                client.getBranch().getId(),
                client.getBranch().getName(),
                client.getAssignedRm().getId(),
                client.getAssignedRm().getFullName(),
                client.getCreatedAt()
        );
    }
    private void assertInScope(Client client) {
        Role role = currentUserService.getCurrentUserRole();

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
        throw new ResourceNotFoundException("Client not found with id: " + client.getId());
    }
}
