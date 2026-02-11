package expondo.evolution.okr.dto;

import java.time.LocalDate;

public record CycleUpdateDto(
        String name,
        LocalDate startDate,
        LocalDate endDate
) {}