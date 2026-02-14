package expondo.evolution.planning.dto;

import java.time.LocalDate;

public record TimeboxDto(
        Long id,
        Long cycleId,
        Integer number,
        LocalDate startDate,
        LocalDate endDate,
        boolean closed
) {}
