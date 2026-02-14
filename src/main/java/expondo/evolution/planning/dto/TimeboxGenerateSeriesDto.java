package expondo.evolution.planning.dto;

import java.time.LocalDate;

public record TimeboxGenerateSeriesDto(
        LocalDate startDate,
        Integer intervalDays
) {}
