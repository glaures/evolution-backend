package expondo.evolution.okr.dto;

import java.time.LocalDate;

public record CycleDto(
        Long id,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        boolean current,
        int objectiveCount,
        int timeboxCount
) {}