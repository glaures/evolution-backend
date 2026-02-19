package expondo.evolution.user;

import expondo.evolution.user.dto.MeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MeController {

    private final UserRepository userRepository;

    @GetMapping
    public MeDto me(@AuthenticationPrincipal Jwt jwt) {
        String oid = jwt.getClaimAsString("oid");
        String email = jwt.getClaimAsString("preferred_username");
        String displayName = jwt.getClaimAsString("name");

        Set<AppRole> roles = userRepository.findByMicrosoftOid(oid)
                .map(AppUser::getRoles)
                .orElse(Set.of());

        return new MeDto(email, displayName, roles);
    }
}