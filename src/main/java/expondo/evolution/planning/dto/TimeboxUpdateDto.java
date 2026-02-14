package expondo.evolution.planning.dto;

import java.time.LocalDate;

public record TimeboxUpdateDto(
        LocalDate startDate,
        LocalDate endDate
) {}
