package expondo.evolution.user.dto;

public record TeamCreateDto(
        String name,
        Integer memberCount,
        String color
) {}