package expondo.evolution.planning.dto;

import java.time.LocalDate;

public record TimeboxSnapshotSummaryDto(
        Long timeboxId,
        Integer number,
        LocalDate startDate,
        LocalDate endDate,
        boolean closed
) {}
