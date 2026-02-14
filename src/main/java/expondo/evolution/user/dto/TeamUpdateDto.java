package expondo.evolution.user.dto;

public record TeamUpdateDto(
        String name,
        Integer memberCount,
        String color
) {}