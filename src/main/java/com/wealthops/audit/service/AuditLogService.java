package com.wealthops.audit.service;

import com.wealthops.audit.entity.AuditAction;
import com.wealthops.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditLogService {

    void log(String performedBy, String performedByRole, AuditAction action,
             String entityType, Long entityId, String oldValue, String newValue);

    Page<AuditLog> getAllLogs(Pageable pageable);
}