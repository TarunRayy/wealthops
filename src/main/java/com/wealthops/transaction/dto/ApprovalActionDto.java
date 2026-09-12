package com.wealthops.transaction.dto;

public class ApprovalActionDto {

    private String remarks; // required for reject, optional for approve

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}