package expondo.evolution.config.dto;

import java.util.Map;

public record AuditEntryDto(
        String entityType,
        Long entityId,
        String revisionType,
        Map<String, Object> data
) {}