package com.wealthops.client.controller;

import com.wealthops.client.dto.ClientRequest;
import com.wealthops.client.dto.ClientResponse;
import com.wealthops.client.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/me")
    public ResponseEntity<ClientResponse> getMyProfile(Authentication authentication) {
        return ResponseEntity.ok(clientService.getClientByEmail(authentication.getName()));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','BRANCH_MANAGER')")
    @PostMapping
    public ResponseEntity<ClientResponse> createClient(@Valid @RequestBody ClientRequest request) {
        return new ResponseEntity<>(clientService.createClient(request), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','BRANCH_MANAGER','RELATIONSHIP_MANAGER','CLIENT')")
    @GetMapping("/{id}")
    public ResponseEntity<ClientResponse> getClient(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.getClientById(id));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','BRANCH_MANAGER')")
    @GetMapping
    public ResponseEntity<List<ClientResponse>> getAllClients() {
        return ResponseEntity.ok(clientService.getAllClients());
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','BRANCH_MANAGER')")
    @GetMapping("/branch/{branchId}")
    public ResponseEntity<List<ClientResponse>> getClientsByBranch(@PathVariable Long branchId) {
        return ResponseEntity.ok(clientService.getClientsByBranch(branchId));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','BRANCH_MANAGER','RELATIONSHIP_MANAGER')")
    @GetMapping("/rm/{rmId}")
    public ResponseEntity<List<ClientResponse>> getClientsByRm(@PathVariable Long rmId) {
        return ResponseEntity.ok(clientService.getClientsByRm(rmId));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN','BRANCH_MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<ClientResponse> updateClient(@PathVariable Long id, @Valid @RequestBody ClientRequest request) {
        return ResponseEntity.ok(clientService.updateClient(id, request));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        clientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }
}