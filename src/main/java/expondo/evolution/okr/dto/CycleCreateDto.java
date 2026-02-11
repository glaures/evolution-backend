package expondo.evolution.okr.dto;

import java.time.LocalDate;

public record CycleCreateDto(
        String name,
        LocalDate startDate,
        LocalDate endDate
) {}