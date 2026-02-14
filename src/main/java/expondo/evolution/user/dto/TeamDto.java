package expondo.evolution.user.dto;

public record TeamDto(
        Long id,
        String name,
        Integer memberCount,
        String color
) {}