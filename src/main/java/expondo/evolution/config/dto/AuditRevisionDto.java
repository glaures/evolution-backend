package expondo.evolution.config.dto;

import java.time.Instant;
import java.util.List;

public record AuditRevisionDto(
        Long revisionId,
        Instant timestamp,
        String userEmail,
        String userName,
        List<AuditEntryDto> changes
) {}