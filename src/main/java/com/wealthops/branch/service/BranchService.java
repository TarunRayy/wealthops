package com.wealthops.branch.service;

import com.wealthops.branch.dto.BranchRequest;
import com.wealthops.branch.dto.BranchResponse;

import java.util.List;

public interface BranchService {
    BranchResponse createBranch(BranchRequest request);
    BranchResponse getBranchById(Long id);
    List<BranchResponse> getAllBranches();
    BranchResponse updateBranch(Long id, BranchRequest request);
    void deleteBranch(Long id);
}