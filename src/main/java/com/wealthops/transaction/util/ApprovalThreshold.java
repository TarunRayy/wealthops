package com.wealthops.transaction.util;

import java.math.BigDecimal;

public class ApprovalThreshold {
    // Amounts >= this require COMPLIANCE_OFFICER approval; below it, BRANCH_MANAGER suffices
    public static final BigDecimal HIGH_VALUE_LIMIT = new BigDecimal("100000");
}