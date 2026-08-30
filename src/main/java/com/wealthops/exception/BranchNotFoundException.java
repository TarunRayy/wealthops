package com.wealthops.exception;

public class BranchNotFoundException extends RuntimeException {

    public BranchNotFoundException(Long branchId) {
        super("No branch found with id " + branchId);
    }
}