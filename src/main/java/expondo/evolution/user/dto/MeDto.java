package expondo.evolution.user.dto;

import expondo.evolution.user.AppRole;

import java.util.Set;

public record MeDto(
        String email,
        String displayName,
        Set<AppRole> roles
) {}