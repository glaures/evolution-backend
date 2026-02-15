package expondo.evolution.user.dto;

import expondo.evolution.user.AppRole;

import java.time.Instant;
import java.util.Set;

public record UserDto(
        Long id,
        String microsoftOid,
        String email,
        String displayName,
        boolean enabled,
        Set<AppRole> roles,
        Instant lastLoginAt
) {}
