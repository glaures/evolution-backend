package expondo.evolution.user.dto;

import expondo.evolution.user.AppRole;

import java.util.Set;

public record UserRoleUpdateDto(
        Set<AppRole> roles,
        boolean enabled
) {}
