package com.wealthops.client.dto;

import com.wealthops.client.entity.ClientStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ClientResponse {

    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String panNumber;
    private LocalDate dateOfBirth;
    private String address;
    private ClientStatus status;
    private Long branchId;
    private String branchName;
    private Long assignedRmId;
    private String assignedRmName;
    private LocalDateTime createdAt;

    public ClientResponse(Long id, String fullName, String email, String phone, String panNumber,
                          LocalDate dateOfBirth, String address, ClientStatus status,
                          Long branchId, String branchName, Long assignedRmId, String assignedRmName,
                          LocalDateTime createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.panNumber = panNumber;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.status = status;
        this.branchId = branchId;
        this.branchName = branchName;
        this.assignedRmId = assignedRmId;
        this.assignedRmName = assignedRmName;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getPanNumber() {
        return panNumber;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getAddress() {
        return address;
    }

    public ClientStatus getStatus() {
        return status;
    }

    public Long getBranchId() {
        return branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public Long getAssignedRmId() {
        return assignedRmId;
    }

    public String getAssignedRmName() {
        return assignedRmName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}