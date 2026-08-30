package com.wealthops.registration.entity;

/**
 * Platform roles (SRS 1.5). SUPER_ADMIN is deliberately not creatable
 * via any registration API — seed it directly in the DB.
 */
public enum Role {
    SUPER_ADMIN,
    BRANCH_MANAGER,
    RELATIONSHIP_MANAGER,
    COMPLIANCE_OFFICER,
    CLIENT
}