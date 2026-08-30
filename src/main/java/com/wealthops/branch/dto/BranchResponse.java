package com.wealthops.branch.dto;

public class BranchResponse {

    private Long id;
    private String name;
    private String branchCode;

    public BranchResponse(Long id, String name, String branchCode) {
        this.id = id;
        this.name = name;
        this.branchCode = branchCode;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getBranchCode() {
        return branchCode;
    }
}