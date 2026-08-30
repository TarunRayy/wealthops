package com.wealthops.branch.service;

import com.wealthops.branch.dto.BranchRequest;
import com.wealthops.branch.dto.BranchResponse;
import com.wealthops.branch.entity.Branch;
import com.wealthops.branch.repository.BranchRepository;
import com.wealthops.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;

    public BranchServiceImpl(BranchRepository branchRepository) {
        this.branchRepository = branchRepository;
    }

    @Override
    public BranchResponse createBranch(BranchRequest request) {
        Branch branch = new Branch();
        branch.setName(request.getName());
        branch.setBranchCode(request.getBranchCode());
        Branch saved = branchRepository.save(branch);
        return toResponse(saved);
    }

    @Override
    public BranchResponse getBranchById(Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + id));
        return toResponse(branch);
    }

    @Override
    public List<BranchResponse> getAllBranches() {
        return branchRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BranchResponse updateBranch(Long id, BranchRequest request) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + id));
        branch.setName(request.getName());
        branch.setBranchCode(request.getBranchCode());
        Branch updated = branchRepository.save(branch);
        return toResponse(updated);
    }

    @Override
    public void deleteBranch(Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + id));
        branchRepository.delete(branch);
    }

    private BranchResponse toResponse(Branch branch) {
        return new BranchResponse(branch.getId(), branch.getName(), branch.getBranchCode());
    }
}