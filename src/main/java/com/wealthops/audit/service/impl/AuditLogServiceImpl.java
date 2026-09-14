package com.wealthops.audit.service.impl;

import com.wealthops.audit.entity.AuditAction;
import com.wealthops.audit.entity.AuditLog;
import com.wealthops.audit.repository.AuditLogRepository;
import com.wealthops.audit.service.AuditLogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public void log(String performedBy, String performedByRole, AuditAction action,
                    String entityType, Long entityId, String oldValue, String newValue) {
        AuditLog log = new AuditLog();
        log.setPerformedBy(performedBy);
        log.setPerformedByRole(performedByRole);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        auditLogRepository.save(log);
    }

    @Override
    public Page<AuditLog> getAllLogs(Pageable pageable) {
        return auditLogRepository.findAllByOrderByTimestampDesc(pageable);
    }
}