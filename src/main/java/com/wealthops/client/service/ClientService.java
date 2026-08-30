package com.wealthops.client.service;

import com.wealthops.client.dto.ClientRequest;
import com.wealthops.client.dto.ClientResponse;

import java.util.List;

public interface ClientService {
    ClientResponse createClient(ClientRequest request);
    ClientResponse getClientById(Long id);
    List<ClientResponse> getAllClients();
    List<ClientResponse> getClientsByBranch(Long branchId);
    List<ClientResponse> getClientsByRm(Long rmId);
    ClientResponse updateClient(Long id, ClientRequest request);
    void deleteClient(Long id);
}